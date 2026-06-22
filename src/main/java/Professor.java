import java.util.Set;

public class Professor extends Cliente {
    public Professor(String cpf, String nome, Set<String> placas) {
        super(cpf, nome, placas, 2);
    }

    @Override
    public TipoCliente getTipo() {
        return TipoCliente.PROFESSOR;
    }

    @Override
    public double calcularCusto(RegistroEstacionamento registro, TabelaTarifas tarifas) {
        return 0.0;
    }

    public boolean temVeiculoEstacionado(Set<String> placasEstacionadas) {
        for (String placa : getPlacas()) {
            if (placasEstacionadas.contains(placa)) {
                return true;
            }
        }
        return false;
    }
}
