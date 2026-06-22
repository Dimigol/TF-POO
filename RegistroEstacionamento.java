import java.time.LocalDateTime;

class RegistroEstacionamento {
    String placa;
    String tipoCliente;
    LocalDateTime entrada;
    LocalDateTime saida;
    double custo;
    String idDesconto;
    double valorDesconto;
    double valorDevido;
    double valorPago;
    public RegistroEstacionamento(Cliente cliente, String placa2, LocalDateTime momento) {
        //TODO Auto-generated constructor stub
    }
    public void setValorDevido(double valorDevido2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setValorDevido'");
    } //adicionei por causa do estacionamento linha: 66
    public void setValorPago(double valorFinal) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setValorPago'");
    }
    public void setValorDesconto(double valorDesconto2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setValorDesconto'");
    }
    public Cliente getCliente() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCliente'");
    }
    public void setMomentoSaida(LocalDateTime momento) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMomentoSaida'");
    }
}