import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

public class EstacionamentoTest {
    private static final LocalDateTime BASE = LocalDateTime.of(2026, 6, 1, 10, 0);

    @Test
    void fluxoPrincipalDoEstacionamento() throws Exception {
        TabelaTarifas tarifas = new TabelaTarifas(10, 50, 15, 30);
        Estacionamento e = new Estacionamento(tarifas);

        Professor professor = new Professor("52998224725", "Professor",
                set("PRO1A23", "PRO2B34"));
        Estudante estudante = new Estudante("39053344705", "Estudante",
                set("EST1C23"), 10);
        Empresa empresa = new Empresa("76688770000130", "Empresa",
                set("EMP1D23", "EMP2E34"), 0, false);
        esperarErro(() -> new Professor("11111111111", "Professor", set("PRO1A23")));
        esperarErro(() -> new Estudante("39053344705", "Al", set("EST1C23"), 0));
        esperarErro(() -> new Empresa("76688770000130", "Empresa", set("ABC1234"), 0, false));

        e.cadastrarCliente(professor);
        e.cadastrarCliente(estudante);
        e.cadastrarCliente(empresa);
        assertTrue(e.cadastrarPlaca("76688770000130", "EMP3F45"));
        assertTrue(empresa.getPlacas().contains("EMP3F45"));
        assertTrue(e.removerPlaca("76688770000130", "EMP3F45"));
        assertFalse(empresa.getPlacas().contains("EMP3F45"));

        assertTrue(e.autorizarEntrada("AVU1A23", BASE));
        igual(20, e.calcularValorSaida("AVU1A23", BASE.plusHours(2)));
        igual(20, e.processarSaida("AVU1A23", BASE.plusHours(2), 20.0));
        assertTrue(e.autorizarEntrada("AVU1A23", BASE.plusDays(2)));
        igual(18, e.processarSaida("AVU1A23", BASE.plusDays(2).plusHours(2), 18.0));
        assertEquals(2, e.relatorioRegistrosAvulso(null, BASE.minusDays(1), BASE.plusDays(3)).size());
        assertEquals(2, e.relatorioRegistrosAvulso("AVU1A23", BASE.minusDays(1), BASE.plusDays(3)).size());
        assertTrue(e.getHistorico().get(0).toString().contains("Placa: AVU1A23"));

        assertTrue(e.autorizarEntrada("SEM1P23", BASE));
        igual(10, e.processarSaida("SEM1P23", BASE.plusHours(1)));
        igual(10, e.calcularDebitoAvulso("SEM1P23"));
        assertFalse(e.autorizarEntrada("SEM1P23", BASE.plusHours(2)));
        igual(5, e.quitarDebitoAvulso("SEM1P23", 5.0));
        assertFalse(e.autorizarEntrada("SEM1P23", BASE.plusHours(3)));
        igual(0, e.quitarDebitoAvulso("SEM1P23", 5.0));
        assertTrue(e.autorizarEntrada("SEM1P23", BASE.plusHours(4)));
        igual(9, e.processarSaida("SEM1P23", BASE.plusHours(5), 10.0));

        assertTrue(e.autorizarEntrada("NEG1A23", BASE));
        e.processarSaida("NEG1A23", BASE.plusHours(1), 0.0);
        assertFalse(e.autorizarEntrada("NEG1A23", BASE.plusDays(1)));

        assertTrue(e.autorizarEntrada("PRO1A23", BASE));
        assertTrue(e.autorizarEntrada("PRO2B34", BASE));
        igual(0, e.processarSaida("PRO1A23", BASE.plusHours(1)));
        igual(10, e.processarSaida("PRO2B34", BASE.plusHours(1), 10.0));

        assertTrue(e.autorizarEntrada("EST1C23", BASE));
        igual(15, e.processarSaida("EST1C23", BASE.plusHours(1)));
        igual(-5, estudante.getSaldo());
        assertFalse(e.autorizarEntrada("EST1C23", BASE.plusDays(1)));
        igual(15, e.adicionarCreditoEstudante("39053344705", 20));
        assertTrue(e.autorizarEntrada("EST1C23", BASE.plusDays(1)));
        igual(15, e.processarSaida("EST1C23", BASE.plusDays(1).plusHours(1)));

        assertTrue(e.autorizarEntrada("EMP1D23", BASE));
        igual(130, e.processarSaida("EMP1D23", BASE.plusDays(1)));
        igual(130, empresa.getDebitoAcumulado());
        igual(130, e.emitirBoletoEmpresa("76688770000130"));
        e.marcarBoletoEmpresaComoVencido("76688770000130");
        assertFalse(e.autorizarEntrada("EMP2E34", BASE.plusDays(2)));
        igual(100, e.registrarPagamentoEmpresa("76688770000130", 30, BASE.plusDays(3)));
        assertFalse(e.autorizarEntrada("EMP2E34", BASE.plusDays(2)));
        igual(0, e.registrarPagamentoEmpresa("76688770000130", 100, BASE.plusDays(4)));
        assertTrue(e.autorizarEntrada("EMP2E34", BASE.plusDays(5)));
        igual(50, e.processarSaida("EMP2E34", BASE.plusDays(5).plusHours(1)));

        igual(130, e.relatorioArrecadacao(BASE, BASE.plusDays(10), EnumSet.of(TipoCliente.EMPRESA)));
        igual(30, e.relatorioArrecadacao(BASE.plusDays(3),
                BASE.plusDays(3).plusHours(1), EnumSet.of(TipoCliente.EMPRESA)));
        assertTrue(e.relatorioSituacaoCliente("76688770000130").contains("debito=50.0"));
        assertEquals(2, e.relatorioRegistrosCliente("52998224725", BASE.minusDays(1),
                BASE.plusDays(1)).size());
        assertTrue(e.relatorioImpedidos().contains("NEG1A23"));
        assertFalse(e.relatorioTop10Frequentes(BASE.getYear()).isEmpty());

        Path csv = Files.createTempDirectory("estacionamento-csv-");
        CSVManager manager = new CSVManager(csv);
        manager.salvarTudo(e);
        Estacionamento recarregado = manager.carregarTudo(tarifas);
        assertEquals(e.getHistorico().size(), recarregado.getHistorico().size());
        assertEquals(e.getClientesCadastrados().size(), recarregado.getClientesCadastrados().size());
        assertTrue(recarregado.getPlacasBloqueadas().contains("NEG1A23"));
        igual(130, recarregado.relatorioArrecadacao(BASE, BASE.plusDays(10),
                EnumSet.of(TipoCliente.EMPRESA)));
    }

    private static HashSet<String> set(String... valores) {
        return new HashSet<>(Arrays.asList(valores));
    }

    private static void esperarErro(Runnable acao) {
        assertThrows(IllegalArgumentException.class, acao::run);
    }

    private static void igual(double esperado, double obtido) {
        assertEquals(esperado, obtido, 0.001);
    }
}
