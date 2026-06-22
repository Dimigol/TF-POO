import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Estacionamento {
    public static final int CAPACIDADE_TOTAL = 9000;

    private final Map<String, Cliente> clientesCadastrados = new HashMap<>();
    private final Map<String, Veiculo> veiculosCadastrados = new HashMap<>();
    private final Set<String> placasBloqueadas = new HashSet<>();
    private final Map<String, RegistroEstacionamento> registrosAtivos = new HashMap<>();
    private final List<RegistroEstacionamento> historico = new ArrayList<>();
    private final List<PagamentoEmpresa> pagamentosEmpresariais = new ArrayList<>();
    private final TabelaTarifas tarifas;
    private final Desconto descontoFrequente = new DescontoClienteFrequente();

    public Estacionamento() {
        this(new TabelaTarifas());
    }

    public Estacionamento(TabelaTarifas tarifas) {
        if (tarifas == null) {
            throw new IllegalArgumentException("Tabela de tarifas obrigatoria.");
        }
        this.tarifas = tarifas;
    }

    public void cadastrarCliente(Cliente cliente) {
        if (cliente == null || cliente instanceof ClienteAvulso) {
            throw new IllegalArgumentException("Cliente pre-cadastrado invalido.");
        }
        if (clientesCadastrados.containsKey(cliente.getIdentificador())) {
            throw new IllegalArgumentException("Identificador ja cadastrado.");
        }
        for (String placa : cliente.getPlacas()) {
            garantirPlacaDisponivel(placa);
        }
        clientesCadastrados.put(cliente.getIdentificador(), cliente);
        for (String placa : cliente.getPlacas()) {
            veiculosCadastrados.put(placa, new Veiculo(placa, cliente));
        }
    }

    public boolean cadastrarPlaca(String identificador, String placa) {
        Cliente cliente = clienteObrigatorio(identificador);
        String normalizada = placaObrigatoria(placa);
        garantirPlacaDisponivel(normalizada);
        if (!cliente.adicionarPlaca(normalizada)) {
            return false;
        }
        veiculosCadastrados.put(normalizada, new Veiculo(normalizada, cliente));
        return true;
    }

    public boolean removerPlaca(String identificador, String placa) {
        Cliente cliente = clienteObrigatorio(identificador);
        String normalizada = placaObrigatoria(placa);
        if (registrosAtivos.containsKey(normalizada)) {
            throw new IllegalStateException("Nao e possivel remover um veiculo estacionado.");
        }
        if (!cliente.removerPlaca(normalizada)) {
            return false;
        }
        veiculosCadastrados.remove(normalizada);
        return true;
    }

    public double adicionarCreditoEstudante(String identificador, double valor) {
        Cliente cliente = clienteObrigatorio(identificador);
        if (!(cliente instanceof Estudante)) {
            throw new IllegalArgumentException("O cliente informado nao e estudante.");
        }
        Estudante estudante = (Estudante) cliente;
        estudante.adicionarCredito(valor);
        return estudante.getSaldo();
    }

    public double emitirBoletoEmpresa(String identificador) {
        return empresaObrigatoria(identificador).emitirBoleto();
    }

    public void marcarBoletoEmpresaComoVencido(String identificador) {
        Empresa empresa = empresaObrigatoria(identificador);
        if (empresa.getDebitoAcumulado() <= 0) {
            throw new IllegalStateException("A empresa nao possui debito para vencer.");
        }
        empresa.marcarBoletoVencido();
    }

    public double registrarPagamentoEmpresa(String identificador, double valor) {
        return registrarPagamentoEmpresa(identificador, valor, LocalDateTime.now());
    }

    public double registrarPagamentoEmpresa(
            String identificador, double valor, LocalDateTime momento) {
        Empresa empresa = empresaObrigatoria(identificador);
        PagamentoEmpresa pagamento = new PagamentoEmpresa(identificador, momento, valor);
        empresa.registrarPagamento(valor);
        pagamentosEmpresariais.add(pagamento);
        return empresa.getDebitoAcumulado();
    }

    public boolean autorizarEntrada(String placa, LocalDateTime momento) {
        String normalizada = placaObrigatoria(placa);
        if (momento == null || registrosAtivos.size() >= CAPACIDADE_TOTAL
                || registrosAtivos.containsKey(normalizada)
                || placasBloqueadas.contains(normalizada)) {
            return false;
        }

        Veiculo veiculo = veiculosCadastrados.get(normalizada);
        Cliente modalidade;
        String proprietarioId = null;
        if (veiculo == null) {
            modalidade = new ClienteAvulso(normalizada);
        } else {
            Cliente proprietario = veiculo.getProprietario();
            proprietarioId = proprietario.getIdentificador();
            if (!proprietario.podeEntrar(normalizada, registrosAtivos.keySet())) {
                return false;
            }
            if (proprietario instanceof Professor
                    && ((Professor) proprietario).temVeiculoEstacionado(registrosAtivos.keySet())) {
                modalidade = new ClienteAvulso(normalizada);
            } else {
                modalidade = proprietario;
            }
        }

        RegistroEstacionamento registro = new RegistroEstacionamento(
                normalizada, modalidade.getTipo(), proprietarioId, momento);
        registrosAtivos.put(normalizada, registro);
        return true;
    }

    public double processarSaida(String placa, LocalDateTime momento) {
        return processarSaida(placa, momento, null);
    }

    public double calcularValorSaida(String placa, LocalDateTime momento) {
        String normalizada = placaObrigatoria(placa);
        RegistroEstacionamento original = registrosAtivos.get(normalizada);
        if (original == null) {
            throw new IllegalArgumentException("Veiculo nao encontrado no estacionamento.");
        }

        RegistroEstacionamento simulacao = new RegistroEstacionamento(
                original.getPlaca(), original.getTipoCliente(),
                original.getIdentificadorProprietario(), original.getEntrada());
        simulacao.prepararSaida(momento);

        Cliente cobravel = clienteDaModalidade(simulacao);
        double custo = cobravel.calcularCusto(simulacao, tarifas);
        if (simulacao.getTipoCliente() == TipoCliente.AVULSO
                && usouNosUltimosTresDias(simulacao)) {
            return custo - descontoFrequente.aplicar(custo);
        }
        return custo;
    }

    public double processarSaida(String placa, LocalDateTime momento, Double pagamentoAvulso) {
        String normalizada = placaObrigatoria(placa);
        RegistroEstacionamento registro = registrosAtivos.get(normalizada);
        if (registro == null) {
            throw new IllegalArgumentException("Veiculo nao encontrado no estacionamento.");
        }
        if (registro.getTipoCliente() == TipoCliente.AVULSO && pagamentoAvulso == null) {
            throw new IllegalArgumentException("Informe o pagamento do cliente avulso.");
        }
        registro.prepararSaida(momento);

        Cliente cobravel = clienteDaModalidade(registro);
        double custo = cobravel.calcularCusto(registro, tarifas);
        double desconto = 0.0;
        String idDesconto = "NENHUM";
        if (registro.getTipoCliente() == TipoCliente.AVULSO && usouNosUltimosTresDias(registro)) {
            desconto = descontoFrequente.aplicar(custo);
            idDesconto = descontoFrequente.getId();
        }
        double devido = custo - desconto;
        double pago;

        if (registro.getTipoCliente() == TipoCliente.ESTUDANTE) {
            Estudante estudante = (Estudante) cobravel;
            estudante.debitar(devido);
            pago = devido;
        } else if (registro.getTipoCliente() == TipoCliente.EMPRESA) {
            Empresa empresa = (Empresa) cobravel;
            empresa.adicionarDebito(devido);
            pago = 0.0;
        } else if (registro.getTipoCliente() == TipoCliente.PROFESSOR) {
            pago = 0.0;
        } else {
            pago = Math.max(0.0, pagamentoAvulso);
            if (pago < devido) {
                placasBloqueadas.add(normalizada);
            }
        }

        registro.registrarCobranca(custo, idDesconto, desconto, pago);
        registrosAtivos.remove(normalizada);
        historico.add(registro);
        return devido;
    }

    private Cliente clienteDaModalidade(RegistroEstacionamento registro) {
        if (registro.getTipoCliente() == TipoCliente.AVULSO) {
            return new ClienteAvulso(registro.getPlaca());
        }
        return clienteObrigatorio(registro.getIdentificadorProprietario());
    }

    private boolean usouNosUltimosTresDias(RegistroEstacionamento atual) {
        LocalDateTime limite = atual.getEntrada().minusDays(3);
        for (RegistroEstacionamento anterior : historico) {
            if (anterior.getPlaca().equals(atual.getPlaca())
                    && anterior.getTipoCliente() == TipoCliente.AVULSO
                    && anterior.getSaida() != null
                    && !anterior.getSaida().isBefore(limite)
                    && anterior.getSaida().isBefore(atual.getEntrada())) {
                return true;
            }
        }
        return false;
    }

    public double relatorioArrecadacao(LocalDateTime inicio, LocalDateTime fim,
                                       Set<TipoCliente> categorias) {
        validarPeriodo(inicio, fim);
        Set<TipoCliente> filtro = categorias == null || categorias.isEmpty()
                ? EnumSet.allOf(TipoCliente.class) : EnumSet.copyOf(categorias);
        double total = 0.0;
        for (RegistroEstacionamento registro : historico) {
            if (!registro.getSaida().isBefore(inicio) && !registro.getSaida().isAfter(fim)
                    && filtro.contains(registro.getTipoCliente())) {
                total += registro.getValorPago();
            }
        }
        if (filtro.contains(TipoCliente.EMPRESA)) {
            for (PagamentoEmpresa pagamento : pagamentosEmpresariais) {
                if (!pagamento.getMomento().isBefore(inicio)
                        && !pagamento.getMomento().isAfter(fim)) {
                    total += pagamento.getValor();
                }
            }
        }
        return total;
    }

    public String relatorioSituacaoCliente(String identificador) {
        Cliente cliente = clienteObrigatorio(identificador);
        List<String> estacionados = new ArrayList<>();
        for (String placa : cliente.getPlacas()) {
            if (registrosAtivos.containsKey(placa)) {
                estacionados.add(placa);
            }
        }
        String financeiro = "sem saldo ou debito";
        if (cliente instanceof Estudante) {
            financeiro = "saldo=" + ((Estudante) cliente).getSaldo();
        } else if (cliente instanceof Empresa) {
            Empresa empresa = (Empresa) cliente;
            financeiro = "debito=" + empresa.getDebitoAcumulado()
                    + ", inadimplente=" + empresa.isInadimplente();
        }
        return cliente.getNome() + "; estacionados=" + estacionados + "; " + financeiro;
    }

    public List<RegistroEstacionamento> relatorioRegistrosCliente(
            String identificador, LocalDateTime inicio, LocalDateTime fim) {
        clienteObrigatorio(identificador);
        validarPeriodo(inicio, fim);
        List<RegistroEstacionamento> resultado = new ArrayList<>();
        for (RegistroEstacionamento registro : historico) {
            if (identificador.equals(registro.getIdentificadorProprietario())
                    && dentroDoPeriodo(registro, inicio, fim)) {
                resultado.add(registro);
            }
        }
        return resultado;
    }

    public List<RegistroEstacionamento> relatorioRegistrosAvulso(
            String placa, LocalDateTime inicio, LocalDateTime fim) {
        String normalizada = Cliente.normalizarPlaca(placa);
        validarPeriodo(inicio, fim);
        List<RegistroEstacionamento> resultado = new ArrayList<>();
        for (RegistroEstacionamento registro : historico) {
            if ((normalizada == null || registro.getPlaca().equals(normalizada))
                    && registro.getTipoCliente() == TipoCliente.AVULSO
                    && registro.getIdentificadorProprietario() == null
                    && dentroDoPeriodo(registro, inicio, fim)) {
                resultado.add(registro);
            }
        }
        return resultado;
    }

    public Set<String> relatorioImpedidos() {
        Set<String> impedidos = new HashSet<>(placasBloqueadas);
        for (Cliente cliente : clientesCadastrados.values()) {
            if ((cliente instanceof Estudante && ((Estudante) cliente).estaBloqueado())
                    || (cliente instanceof Empresa && ((Empresa) cliente).isInadimplente())) {
                impedidos.add(cliente.getIdentificador());
            }
        }
        return impedidos;
    }

    public Map<String, Long> relatorioTop10Frequentes(int ano) {
        Map<String, Long> contagem = new HashMap<>();
        for (RegistroEstacionamento registro : historico) {
            if (registro.getEntrada().getYear() == ano) {
                String chave = registro.getIdentificadorProprietario() == null
                        ? registro.getPlaca() : registro.getIdentificadorProprietario();
                contagem.put(chave, contagem.getOrDefault(chave, 0L) + 1);
            }
        }
        List<Map.Entry<String, Long>> ordenados = new ArrayList<>(contagem.entrySet());
        ordenados.sort(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey()));
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(10, ordenados.size()); i++) {
            resultado.put(ordenados.get(i).getKey(), ordenados.get(i).getValue());
        }
        return resultado;
    }

    private boolean dentroDoPeriodo(RegistroEstacionamento registro,
                                    LocalDateTime inicio, LocalDateTime fim) {
        return !registro.getEntrada().isBefore(inicio) && !registro.getEntrada().isAfter(fim);
    }

    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Periodo invalido.");
        }
    }

    private Cliente clienteObrigatorio(String identificador) {
        Cliente cliente = clientesCadastrados.get(identificador);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente nao cadastrado: " + identificador);
        }
        return cliente;
    }

    private Empresa empresaObrigatoria(String identificador) {
        Cliente cliente = clienteObrigatorio(identificador);
        if (!(cliente instanceof Empresa)) {
            throw new IllegalArgumentException("O cliente informado nao e uma empresa.");
        }
        return (Empresa) cliente;
    }

    private String placaObrigatoria(String placa) {
        String normalizada = Cliente.normalizarPlaca(placa);
        if (normalizada == null) {
            throw new IllegalArgumentException("Placa obrigatoria.");
        }
        return normalizada;
    }

    private void garantirPlacaDisponivel(String placa) {
        if (veiculosCadastrados.containsKey(placa)) {
            throw new IllegalArgumentException("Placa ja cadastrada: " + placa);
        }
    }

    Map<String, Cliente> clientesParaPersistencia() { return clientesCadastrados; }
    Set<String> bloqueadosParaPersistencia() { return placasBloqueadas; }
    List<RegistroEstacionamento> historicoParaPersistencia() { return historico; }
    Map<String, RegistroEstacionamento> ativosParaPersistencia() { return registrosAtivos; }
    List<PagamentoEmpresa> pagamentosParaPersistencia() { return pagamentosEmpresariais; }
    void restaurarBloqueio(String placa) { placasBloqueadas.add(placaObrigatoria(placa)); }
    void restaurarRegistro(RegistroEstacionamento registro) {
        if (registro.estaAtivo()) registrosAtivos.put(registro.getPlaca(), registro);
        else historico.add(registro);
    }
    void restaurarPagamentoEmpresa(PagamentoEmpresa pagamento) {
        empresaObrigatoria(pagamento.getIdentificadorEmpresa());
        pagamentosEmpresariais.add(pagamento);
    }

    public Map<String, Cliente> getClientesCadastrados() {
        return Collections.unmodifiableMap(clientesCadastrados);
    }

    public List<RegistroEstacionamento> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    public Set<String> getPlacasBloqueadas() {
        return Collections.unmodifiableSet(placasBloqueadas);
    }
}
