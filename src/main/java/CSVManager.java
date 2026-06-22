import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CSVManager {
    private final Path diretorio;

    public CSVManager(Path diretorio) {
        this.diretorio = diretorio;
    }

    public Estacionamento carregarTudo(TabelaTarifas tarifas) throws IOException {
        Estacionamento estacionamento = new Estacionamento(tarifas);
        carregarClientes(estacionamento);
        carregarVeiculos(estacionamento);
        carregarRegistros(estacionamento);
        carregarBloqueados(estacionamento);
        return estacionamento;
    }

    public void salvarTudo(Estacionamento estacionamento) throws IOException {
        Files.createDirectories(diretorio);
        salvarClientes(estacionamento);
        salvarVeiculos(estacionamento);
        salvarRegistros(estacionamento);
        salvarBloqueados(estacionamento);
    }

    public Thread instalarSalvamentoAutomatico(Estacionamento estacionamento) {
        Thread hook = new Thread(() -> {
            try {
                salvarTudo(estacionamento);
            } catch (IOException e) {
                System.err.println("Falha ao salvar CSV no encerramento: " + e.getMessage());
            }
        }, "salvamento-estacionamento");
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    private void carregarClientes(Estacionamento estacionamento) throws IOException {
        for (List<String> row : ler("clientes.csv")) {
            TipoCliente tipo = TipoCliente.valueOf(row.get(0));
            String id = row.get(1);
            String nome = row.get(2);
            Cliente cliente;
            if (tipo == TipoCliente.PROFESSOR) {
                cliente = new Professor(id, nome, Collections.emptySet());
            } else if (tipo == TipoCliente.ESTUDANTE) {
                cliente = new Estudante(id, nome, Collections.emptySet(), Double.parseDouble(row.get(3)));
            } else if (tipo == TipoCliente.EMPRESA) {
                cliente = new Empresa(id, nome, Collections.emptySet(),
                        Double.parseDouble(row.get(4)), Boolean.parseBoolean(row.get(5)));
            } else {
                throw new IOException("Tipo de cliente invalido no CSV: " + tipo);
            }
            estacionamento.cadastrarCliente(cliente);
        }
    }

    private void carregarVeiculos(Estacionamento estacionamento) throws IOException {
        for (List<String> row : ler("veiculos.csv")) {
            estacionamento.cadastrarPlaca(row.get(1), row.get(0));
        }
    }

    private void carregarRegistros(Estacionamento estacionamento) throws IOException {
        for (List<String> row : ler("registros.csv")) {
            String proprietario = row.get(2).isEmpty() ? null : row.get(2);
            RegistroEstacionamento registro = new RegistroEstacionamento(
                    row.get(0), TipoCliente.valueOf(row.get(1)), proprietario,
                    LocalDateTime.parse(row.get(3)));
            if (!row.get(4).isEmpty()) {
                registro.registrarSaida(LocalDateTime.parse(row.get(4)),
                        Double.parseDouble(row.get(5)), row.get(6),
                        Double.parseDouble(row.get(7)), Double.parseDouble(row.get(9)));
            }
            estacionamento.restaurarRegistro(registro);
        }
    }

    private void carregarBloqueados(Estacionamento estacionamento) throws IOException {
        for (List<String> row : ler("bloqueados.csv")) {
            estacionamento.restaurarBloqueio(row.get(0));
        }
    }

    private void salvarClientes(Estacionamento estacionamento) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("tipo,identificador,nome,saldo,debito,inadimplente");
        for (Cliente cliente : estacionamento.clientesParaPersistencia().values()) {
            String saldo = cliente instanceof Estudante
                    ? Double.toString(((Estudante) cliente).getSaldo()) : "";
            String debito = cliente instanceof Empresa
                    ? Double.toString(((Empresa) cliente).getDebitoAcumulado()) : "";
            String inadimplente = cliente instanceof Empresa
                    ? Boolean.toString(((Empresa) cliente).isInadimplente()) : "";
            linhas.add(linha(cliente.getTipo().name(), cliente.getIdentificador(),
                    cliente.getNome(), saldo, debito, inadimplente));
        }
        escreverAtomico("clientes.csv", linhas);
    }

    private void salvarVeiculos(Estacionamento estacionamento) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("placa,identificador_proprietario");
        for (Cliente cliente : estacionamento.clientesParaPersistencia().values()) {
            for (String placa : cliente.getPlacas()) {
                linhas.add(linha(placa, cliente.getIdentificador()));
            }
        }
        escreverAtomico("veiculos.csv", linhas);
    }

    private void salvarRegistros(Estacionamento estacionamento) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("placa,tipo,proprietario,entrada,saida,custo,desconto_id,desconto_valor,devido,pago");
        List<RegistroEstacionamento> registros = new ArrayList<>(estacionamento.historicoParaPersistencia());
        registros.addAll(estacionamento.ativosParaPersistencia().values());
        for (RegistroEstacionamento r : registros) {
            linhas.add(linha(r.getPlaca(), r.getTipoCliente().name(),
                    nuloComoVazio(r.getIdentificadorProprietario()), r.getEntrada().toString(),
                    r.getSaida() == null ? "" : r.getSaida().toString(),
                    Double.toString(r.getCusto()), r.getIdDesconto(),
                    Double.toString(r.getValorDesconto()), Double.toString(r.getValorDevido()),
                    Double.toString(r.getValorPago())));
        }
        escreverAtomico("registros.csv", linhas);
    }

    private void salvarBloqueados(Estacionamento estacionamento) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("placa");
        for (String placa : estacionamento.bloqueadosParaPersistencia()) {
            linhas.add(linha(placa));
        }
        escreverAtomico("bloqueados.csv", linhas);
    }

    private List<List<String>> ler(String nome) throws IOException {
        Path arquivo = diretorio.resolve(nome);
        if (!Files.exists(arquivo)) {
            return Collections.emptyList();
        }
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        List<List<String>> resultado = new ArrayList<>();
        for (int i = 1; i < linhas.size(); i++) {
            if (!linhas.get(i).isBlank()) resultado.add(separar(linhas.get(i)));
        }
        return resultado;
    }

    private void escreverAtomico(String nome, List<String> linhas) throws IOException {
        Path destino = diretorio.resolve(nome);
        Path temporario = diretorio.resolve(nome + ".tmp");
        Files.write(temporario, linhas, StandardCharsets.UTF_8);
        Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }

    private String linha(String... valores) {
        List<String> campos = new ArrayList<>();
        for (String valor : valores) {
            String v = nuloComoVazio(valor);
            campos.add("\"" + v.replace("\"", "\"\"") + "\"");
        }
        return String.join(",", campos);
    }

    private List<String> separar(String linha) throws IOException {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean aspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (aspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    aspas = !aspas;
                }
            } else if (c == ',' && !aspas) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        if (aspas) throw new IOException("Linha CSV com aspas invalidas.");
        campos.add(atual.toString());
        return campos;
    }

    private String nuloComoVazio(String valor) {
        return valor == null ? "" : valor;
    }
}
