public final class Veiculo {
    private final String placa;
    private final Cliente proprietario;

    public Veiculo(String placa, Cliente proprietario) {
        String normalizada = Cliente.normalizarPlaca(placa);
        if (normalizada == null || proprietario == null) {
            throw new IllegalArgumentException("Placa e proprietario sao obrigatorios.");
        }
        this.placa = normalizada;
        this.proprietario = proprietario;
    }

    public String getPlaca() { return placa; }
    public Cliente getProprietario() { return proprietario; }
}
