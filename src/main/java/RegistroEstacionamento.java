import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class RegistroEstacionamento {
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final String placa;
    private final TipoCliente tipoCliente;
    private final String identificadorProprietario;
    private final LocalDateTime entrada;
    private LocalDateTime saida;
    private double custo;
    private String idDesconto = "NENHUM";
    private double valorDesconto;
    private double valorDevido;
    private double valorPago;

    public RegistroEstacionamento(String placa, TipoCliente tipoCliente,
                                  String identificadorProprietario, LocalDateTime entrada) {
        String normalizada = Cliente.normalizarPlaca(placa);
        if (normalizada == null || tipoCliente == null || entrada == null) {
            throw new IllegalArgumentException("Dados de entrada invalidos.");
        }
        this.placa = normalizada;
        this.tipoCliente = tipoCliente;
        this.identificadorProprietario = identificadorProprietario;
        this.entrada = entrada;
    }

    public void prepararSaida(LocalDateTime saida) {
        if (!estaAtivo()) {
            throw new IllegalStateException("Saida ja registrada.");
        }
        if (saida == null || saida.isBefore(entrada)) {
            throw new IllegalArgumentException("Momento de saida invalido.");
        }
        this.saida = saida;
    }

    public void registrarCobranca(double custo, String idDesconto,
                                  double valorDesconto, double valorPago) {
        if (estaAtivo() || custo < 0 || valorDesconto < 0
                || valorDesconto > custo || valorPago < 0) {
            throw new IllegalArgumentException("Dados de cobranca invalidos.");
        }
        this.custo = custo;
        this.idDesconto = idDesconto == null ? "NENHUM" : idDesconto;
        this.valorDesconto = valorDesconto;
        this.valorDevido = custo - valorDesconto;
        this.valorPago = valorPago;
    }

    public double registrarPagamentoAdicional(double valor) {
        if (estaAtivo() || valor <= 0) {
            throw new IllegalArgumentException("Pagamento adicional invalido.");
        }
        valorPago += valor;
        return Math.max(0.0, valorDevido - valorPago);
    }

    public void registrarSaida(LocalDateTime saida, double custo, String idDesconto,
                               double valorDesconto, double valorPago) {
        prepararSaida(saida);
        registrarCobranca(custo, idDesconto, valorDesconto, valorPago);
    }

    public boolean estaAtivo() { return saida == null; }

    public Duration calcularDuracao() {
        return Duration.between(entrada, estaAtivo() ? LocalDateTime.now() : saida);
    }

    public long getHorasCobraveis() {
        long minutos = Duration.between(entrada, saidaObrigatoria()).toMinutes();
        return Math.max(1, (minutos + 59) / 60);
    }

    public long getQuantidadeViradasDeDia() {
        return ChronoUnit.DAYS.between(entrada.toLocalDate(), saidaObrigatoria().toLocalDate());
    }

    private LocalDateTime saidaObrigatoria() {
        if (saida == null) {
            throw new IllegalStateException("Registro ainda ativo.");
        }
        return saida;
    }

    public double calcularTroco() { return valorPago - valorDevido; }
    public String getPlaca() { return placa; }
    public TipoCliente getTipoCliente() { return tipoCliente; }
    public String getIdentificadorProprietario() { return identificadorProprietario; }
    public LocalDateTime getEntrada() { return entrada; }
    public LocalDateTime getSaida() { return saida; }
    public double getCusto() { return custo; }
    public String getIdDesconto() { return idDesconto; }
    public double getValorDesconto() { return valorDesconto; }
    public double getValorDevido() { return valorDevido; }
    public double getValorPago() { return valorPago; }

    @Override
    public String toString() {
        String saidaFormatada = saida == null ? "em aberto" : saida.format(FORMATO_DATA);
        return String.format(
                "Placa: %s | Tipo: %s | Entrada: %s | Saida: %s | Devido: R$ %.2f | Pago: R$ %.2f",
                placa, tipoCliente, entrada.format(FORMATO_DATA), saidaFormatada,
                valorDevido, valorPago);
    }
}
