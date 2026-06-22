import java.util.Set;

class Estudante extends Cliente {
    // 1 placa apenas
    private double saldo; // pode ficar negativo

    public Estudante(String id, String nome, Set<String> placas, double saldo){
        super(id, nome, placas);
        this.saldo = saldo;

        if (placas.size() > 1){
            throw new IllegalArgumentException("Estudante pode ter no máximo 1 placa.");
        }
    }

    public double getSaldo(){
        return saldo;
    }

    // metodos abstratos da classe Cliente (genericos)
    @Override
    double calcularCusto(RegistroEstacionamento r) {
        return 0;
    }

    @Override
    boolean podeEntrar(String placa, Set<String> placasEstacionadas) {
        if (estaBloqueado()) {
            return false;
        }

        if (!placas.contains(placa)) {
            return false;
        }

        if (placasEstacionadas.contains(placa)) {
            return false;
        }

        return true;
    }

    public void debitar(double valor){
        saldo -= valor;
    }

    public boolean estaBloqueado(){
        return saldo < 0;
    }
}