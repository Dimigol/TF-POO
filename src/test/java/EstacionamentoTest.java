import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;

public class EstacionamentoTest {
    private static final LocalDateTime BASE = LocalDateTime.of(2026, 6, 1, 10, 0);

    public static void main(String[] args) throws Exception {
        TabelaTarifas tarifas = new TabelaTarifas(10, 50, 15, 30);
        Estacionamento e = new Estacionamento(tarifas);

        Professor professor = new Professor("111", "Professor",
                set("PRO1A23", "PRO2B34"));
        Estudante estudante = new Estudante("222", "Estudante",
                set("EST1C23"), 10);
        Empresa empresa = new Empresa("333", "Empresa",
                set("EMP1D23", "EMP2E34"), 0, false);
        e.cadastrarCliente(professor);
        e.cadastrarCliente(estudante);
        e.cadastrarCliente(empresa);
        verificar(e.cadastrarPlaca("333", "EMP3F45"), "nova placa da empresa");
        verificar(empresa.getPlacas().contains("EMP3F45"), "placa vinculada a empresa");
        verificar(e.removerPlaca("333", "EMP3F45"), "remocao de placa da empresa");
        verificar(!empresa.getPlacas().contains("EMP3F45"), "placa removida da empresa");

        verificar(e.autorizarEntrada("AVU1A23", BASE), "entrada avulsa");
        igual(20, e.calcularValorSaida("AVU1A23", BASE.plusHours(2)),
                "consulta do valor devido antes da saida");
        igual(20, e.processarSaida("AVU1A23", BASE.plusHours(2), 20.0), "custo avulso");
        verificar(e.autorizarEntrada("AVU1A23", BASE.plusDays(2)), "retorno avulso");
        igual(18, e.processarSaida("AVU1A23", BASE.plusDays(2).plusHours(2), 18.0),
                "desconto frequente");
        igual(2, e.relatorioRegistrosAvulso(null, BASE.minusDays(1), BASE.plusDays(3)).size(),
                "listagem de todos os registros avulsos");
        igual(2, e.relatorioRegistrosAvulso("AVU1A23", BASE.minusDays(1), BASE.plusDays(3)).size(),
                "filtro de registros avulsos por placa");
        verificar(e.getHistorico().get(0).toString().contains("Placa: AVU1A23"),
                "formatacao do registro");

        verificar(e.autorizarEntrada("SEM1P23", BASE), "avulso para validar pagamento");
        esperarErro(() -> e.processarSaida("SEM1P23", BASE.plusHours(1)),
                "pagamento avulso obrigatorio");
        igual(10, e.processarSaida("SEM1P23", BASE.plusHours(1), 10.0),
                "saida avulsa apos informar pagamento");

        verificar(e.autorizarEntrada("NEG1A23", BASE), "entrada antes da recusa");
        e.processarSaida("NEG1A23", BASE.plusHours(1), 0.0);
        verificar(!e.autorizarEntrada("NEG1A23", BASE.plusDays(1)), "bloqueio por recusa");

        verificar(e.autorizarEntrada("PRO1A23", BASE), "primeiro veiculo professor");
        verificar(e.autorizarEntrada("PRO2B34", BASE), "segundo veiculo professor como avulso");
        igual(0, e.processarSaida("PRO1A23", BASE.plusHours(1)), "gratuidade professor");
        igual(10, e.processarSaida("PRO2B34", BASE.plusHours(1), 10.0),
                "segundo veiculo professor");

        verificar(e.autorizarEntrada("EST1C23", BASE), "entrada estudante");
        igual(15, e.processarSaida("EST1C23", BASE.plusHours(1)), "ingresso estudante");
        igual(-5, estudante.getSaldo(), "saldo negativo permitido na saida");
        verificar(!e.autorizarEntrada("EST1C23", BASE.plusDays(1)), "estudante bloqueado");
        igual(15, e.adicionarCreditoEstudante("222", 20), "recarga de estudante");
        verificar(e.autorizarEntrada("EST1C23", BASE.plusDays(1)),
                "estudante liberado depois da recarga");
        igual(15, e.processarSaida("EST1C23", BASE.plusDays(1).plusHours(1)),
                "saida depois da recarga");

        verificar(e.autorizarEntrada("EMP1D23", BASE), "entrada empresa");
        igual(130, e.processarSaida("EMP1D23", BASE.plusDays(1)), "diarias e multa empresa");
        igual(130, empresa.getDebitoAcumulado(), "debito empresarial");
        igual(130, e.emitirBoletoEmpresa("333"), "emissao de boleto");
        e.marcarBoletoEmpresaComoVencido("333");
        verificar(!e.autorizarEntrada("EMP2E34", BASE.plusDays(2)), "empresa inadimplente");
        igual(100, e.registrarPagamentoEmpresa("333", 30, BASE.plusDays(3)),
                "pagamento parcial da empresa");
        verificar(!e.autorizarEntrada("EMP2E34", BASE.plusDays(2)),
                "empresa segue inadimplente com debito");
        igual(0, e.registrarPagamentoEmpresa("333", 100, BASE.plusDays(4)),
                "quitacao da empresa");
        verificar(e.autorizarEntrada("EMP2E34", BASE.plusDays(5)),
                "empresa liberada depois da quitacao");
        igual(50, e.processarSaida("EMP2E34", BASE.plusDays(5).plusHours(1)),
                "nova diaria empresarial");

        igual(130, e.relatorioArrecadacao(BASE, BASE.plusDays(10),
                EnumSet.of(TipoCliente.EMPRESA)), "arrecadacao empresarial");
        igual(30, e.relatorioArrecadacao(BASE.plusDays(3),
                BASE.plusDays(3).plusHours(1), EnumSet.of(TipoCliente.EMPRESA)),
                "arrecadacao empresarial por periodo");
        verificar(e.relatorioSituacaoCliente("333").contains("debito=50.0"),
                "relatorio de situacao empresarial");
        igual(2, e.relatorioRegistrosCliente("111", BASE.minusDays(1),
                BASE.plusDays(1)).size(), "registros do professor");
        verificar(e.relatorioImpedidos().contains("NEG1A23"), "relatorio de impedidos");
        verificar(!e.relatorioTop10Frequentes(BASE.getYear()).isEmpty(),
                "relatorio dos clientes frequentes");

        Path csv = Files.createTempDirectory("estacionamento-csv-");
        CSVManager manager = new CSVManager(csv);
        manager.salvarTudo(e);
        Estacionamento recarregado = manager.carregarTudo(tarifas);
        igual(e.getHistorico().size(), recarregado.getHistorico().size(), "historico recarregado");
        igual(e.getClientesCadastrados().size(), recarregado.getClientesCadastrados().size(),
                "clientes recarregados");
        verificar(recarregado.getPlacasBloqueadas().contains("NEG1A23"), "bloqueio recarregado");
        igual(130, recarregado.relatorioArrecadacao(BASE, BASE.plusDays(10),
                EnumSet.of(TipoCliente.EMPRESA)), "pagamentos empresariais recarregados");

        System.out.println("Todos os testes passaram.");
    }

    private static HashSet<String> set(String... valores) {
        return new HashSet<>(Arrays.asList(valores));
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) throw new AssertionError(descricao);
    }

    private static void esperarErro(Runnable acao, String descricao) {
        try {
            acao.run();
            throw new AssertionError(descricao + ": excecao esperada");
        } catch (IllegalArgumentException esperado) {
            // comportamento esperado
        }
    }

    private static void igual(double esperado, double obtido, String descricao) {
        if (Math.abs(esperado - obtido) > 0.001) {
            throw new AssertionError(descricao + ": esperado=" + esperado + ", obtido=" + obtido);
        }
    }

    private static void igual(int esperado, int obtido, String descricao) {
        if (esperado != obtido) {
            throw new AssertionError(descricao + ": esperado=" + esperado + ", obtido=" + obtido);
        }
    }
}
