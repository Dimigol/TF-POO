import java.util.List;
import java.time.LocalDateTime;

interface Desconto {
    double aplicar(String placa, double valorOriginal,List<RegistroEstacionamento> encerrados, LocalDateTime hoje);
    String getId();
}
