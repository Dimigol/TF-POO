import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Estacionamento {
    int capacidadeTotal = 9000;
    Map<String, Cliente> clientesCadastrados; // chave: CPF/CNPJ
    Map<String, Veiculo> veiculosCadastrados; // chave: placa -> dono
    Set<String> placasBloqueadas;
    Map<String, RegistroEstacionamento> registrosAtivos; // placa -> registro aberto
    List<RegistroEstacionamento> historico;

    boolean autorizarEntrada(String placa, LocalDateTime momento);
    double processarSaida(String placa, LocalDateTime momento);
    // métodos de relatório...
}