import java.util.Set;

public class Estudante extends Cliente {
    private double saldo;

    public Estudante(String cpf, String nome, Set<String> placas, double saldo) {
        super(cpf, nome, placas, 1);
        this.saldo = saldo;
    }

    @Override
    public TipoCliente getTipo() {
        return TipoCliente.ESTUDANTE;
    }

    @Override
    public double calcularCusto(RegistroEstacionamento registro, TabelaTarifas tarifas) {
        return (registro.getQuantidadeViradasDeDia() + 1) * tarifas.getValorIngresso();
    }

    @Override
    public boolean podeEntrar(String placa, Set<String> placasEstacionadas) {
        return saldo >= 0 && super.podeEntrar(placa, placasEstacionadas);
    }

    public void adicionarCredito(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Credito deve ser positivo.");
        }
        saldo += valor;
    }

    public void debitar(double valor) {
        saldo -= valor;
    }

    public double getSaldo() {
        return saldo;
    }

    public boolean estaBloqueado() {
        return saldo < 0;
    }
}
