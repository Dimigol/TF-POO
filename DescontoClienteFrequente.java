import java.time.LocalDateTime;
import java.util.List;

class DescontoClienteFrequente implements Desconto {

    @Override
    public double aplicar(String placa, double valorOriginal, List<RegistroEstacionamento> historico, LocalDateTime hoje) {
        LocalDateTime TresDiasAtras = hoje.minusDays(3);
        boolean tem = historico.stream()
            .anyMatch(r->r.getPlaca().equals(placa) && r.getSaida().isAfter(TresDiasAtras));
        if (!tem) return 0.0;
        else{
            return valorOriginal * 0.1;
        }
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }
    // 10% se usou nos últimos 3 dias
}