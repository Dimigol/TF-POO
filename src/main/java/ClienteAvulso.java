import java.util.Collections;

public class ClienteAvulso extends Cliente {
    public ClienteAvulso(String placa) {
        super(Cliente.normalizarPlaca(placa), "Cliente avulso", Collections.singleton(placa), 1);
    }

    @Override
    public TipoCliente getTipo() {
        return TipoCliente.AVULSO;
    }

    @Override
    public double calcularCusto(RegistroEstacionamento registro, TabelaTarifas tarifas) {
        long viradas = registro.getQuantidadeViradasDeDia();
        if (viradas > 0) {
            return (viradas + 1) * tarifas.getValorDiaria();
        }
        long horas = registro.getHorasCobraveis();
        return horas <= 6 ? horas * tarifas.getValorHora() : tarifas.getValorDiaria();
    }
}
