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
}