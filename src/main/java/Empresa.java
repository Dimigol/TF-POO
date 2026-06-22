import java.util.Set;

public class Empresa extends Cliente {
    private double debitoAcumulado;
    private boolean inadimplente;

    public Empresa(String cnpj, String nome, Set<String> placas,
                   double debitoAcumulado, boolean inadimplente) {
        super(cnpj, nome, placas, Integer.MAX_VALUE);
        if (debitoAcumulado < 0) {
            throw new IllegalArgumentException("Debito nao pode ser negativo.");
        }
        this.debitoAcumulado = debitoAcumulado;
        this.inadimplente = inadimplente;
    }

    @Override
    public TipoCliente getTipo() {
        return TipoCliente.EMPRESA;
    }

    @Override
    public double calcularCusto(RegistroEstacionamento registro, TabelaTarifas tarifas) {
        long viradas = registro.getQuantidadeViradasDeDia();
        return (viradas + 1) * tarifas.getValorDiaria() + viradas * tarifas.getMultaAtraso();
    }

    @Override
    public boolean podeEntrar(String placa, Set<String> placasEstacionadas) {
        return !inadimplente && super.podeEntrar(placa, placasEstacionadas);
    }

    public void adicionarDebito(double valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Debito nao pode ser negativo.");
        }
        debitoAcumulado += valor;
    }

    public double emitirBoleto() {
        return debitoAcumulado;
    }

    public void marcarBoletoVencido() {
        if (debitoAcumulado > 0) {
            inadimplente = true;
        }
    }

    public void registrarPagamento(double valor) {
        if (valor <= 0 || valor > debitoAcumulado) {
            throw new IllegalArgumentException("Pagamento invalido.");
        }
        debitoAcumulado -= valor;
        if (debitoAcumulado == 0) {
            inadimplente = false;
        }
    }

    public double getDebitoAcumulado() {
        return debitoAcumulado;
    }

    public boolean isInadimplente() {
        return inadimplente;
    }
}
