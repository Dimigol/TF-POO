import java.time.LocalDateTime;
import java.time.Duration;

class RegistroEstacionamento {
    private String placa;
    private TipoCliente tipoCliente;
    private LocalDateTime entrada;
    private LocalDateTime saida;
    private double custo;
    private String idDesconto;
    private double valorDesconto;
    private double valorPago;

    public RegistroEstacionamento(String placa, TipoCliente tipoCliente, LocalDateTime entrada, String idDesconto) {
        //Cliente cliente, String placa2, LocalDateTime momento
        this.placa = placa;
        this.tipoCliente = tipoCliente;
        this.entrada = entrada;
        saida = null;
        custo = 0.0;
        this.idDesconto = idDesconto;
        valorDesconto = 0.0;
        valorPago = 0.0;
    }

    public boolean estaAtivo(){
        return (saida == null);
    }

    public Duration calculaDuracao(){
        if(saida == null) return Duration.ZERO;
        return Duration.between(entrada, saida);
    }

    public void atualizaSaida(LocalDateTime saida){
        this.saida = saida;
    }

    public void calculaPreco(){
        if(tipoCliente == TipoCliente.PROFESSOR){
            custo = 0.0;
        }else if(tipoCliente == TipoCliente.ESTUDANTE){
            custo = 67.00;
        }
    }

    
    


    public String getPlaca() {
        return placa;
    }




    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }




    public LocalDateTime getEntrada() {
        return entrada;
    }




    public LocalDateTime getSaida() {
        return saida;
    }




    public double getCusto() {
        return custo;
    }




    public String getIdDesconto() {
        return idDesconto;
    }




    public double getValorDesconto() {
        return valorDesconto;
    }




    public double getValorPago() {
        return valorPago;
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