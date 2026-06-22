class Veiculo {
    private String placa;
    private Cliente proprietario; // null se avulso

    public Veiculo(String placa, Cliente proprietario){
        this.placa = placa;
        this.proprietario = proprietario;
    }

    public String getPlaca() {
        return placa;
    }

    public Cliente getProprietario() {
        return proprietario;
    }
    
    String placa;
    Cliente proprietario; // null se avulso
    public Cliente getProprietario() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProprietario'");
    }
}