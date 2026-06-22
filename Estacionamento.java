import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

public class Estacionamento {
    private final int capacidadeTotal = 9000;
    private Map<String, Cliente> clientesCadastrados; // chave: CPF/CNPJ
    private Map<String, Veiculo> veiculosCadastrados; // chave: placa -> dono
    private Set<String> placasBloqueadas;
    private Map<String, RegistroEstacionamento> registrosAtivos; // placa -> registro aberto
    private List<RegistroEstacionamento> historico;

    public Estacionamento(){
        this.clientesCadastrados = new HashMap<>();
        this.veiculosCadastrados = new HashMap<>();
        this.placasBloqueadas = new HashSet<>();
        this.registrosAtivos = new HashMap<>();
        this.historico = new ArrayList<>();

    }
    public boolean autorizarEntrada(String placa, LocalDateTime momento){
        if(registrosAtivos.size() >= capacidadeTotal){
            System.out.println("Estacionamento lotado");
            return false;
        }

        if(placasBloqueadas.contains(placa)){
            System.out.println(" Entrada negada: Placa bloqueada");
            return false;
        }

        if(registrosAtivos.containsKey(placa)){
            System.out.println("Veículo já se encontra no estacionamento");
            return false;
        }
        Cliente cliente;
        if (veiculosCadastrados.containsKey(placa)) {
            cliente = veiculosCadastrados.get(placa).getProprietario();
        } else {
            cliente = new ClienteAvulso();
        }

        if(!cliente.podeEntrar(placa)){
            System.out.println("Entrada negada pelas regras do cliente.");
            return false;
        }

        RegistroEstacionamento registro = new RegistroEstacionamento(cliente, placa, momento);
        registrosAtivos.put(placa, registro);
        System.out.println("Entrada autorizaada para a placa: " + placa);
        return true;
    }
    public double processarSaida(String placa, LocalDateTime momento){
        if(!registrosAtivos.containsKey(placa)){
            throw IllegalArgumentException("Veículo não encontrado no estacionamento");
        }

        RegistroEstacionamento registro = registrosAtivos.get(placa);
        registro.setMomentoSaida(momento);
        Cliente cliente = registro.getCliente();
        double valorDevido = cliente.calcularCusto(registro);
        double valorDesconto = calcularDesconto(registro, valorDevido);
        double valorFinal = valorDevido - valorDesconto;

        registro.setValorDevido(valorDevido);
        registro.setValorDesconto(valorDesconto);
        registro.setValorPago(valorFinal);

        boolean pagamentoRealizado = cliente.processarPagamento(valorFinal);

        if (!pagamentoRealizado && cliente instanceof ClienteAvulso) {
            // Regra 3.1: Avulso que recusa pagar tem a placa bloqueada, mas sai.
            placasBloqueadas.add(placa);
            System.out.println("Alerta: Pagamento não realizado. Placa " + placa + " bloqueada.");
        }
        registrosAtivos.remove(placa);
        historico.add(registro);

        System.out.println("Saída processada. Valor pago: R$ " + valorFinal);
        return valorFinal;


    }
    private double calcularDesconto(RegistroEstacionamento registro, double valorDevido) {
        // Lógica para verificar o desconto de 10% (ClienteFrequente)
        // Precisará iterar no 'historico' buscando a mesma placa nos últimos 3 dias.
        // TODO: Implementar a varredura de histórico.
        return 0.0; 
    }
    // métodos de relatório...
}