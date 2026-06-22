public final class DescontoClienteFrequente implements Desconto {
    public static final String ID = "CLIENTE_FREQUENTE";

    @Override
    public double aplicar(double valorOriginal) {
        return valorOriginal * 0.10;
    }

    @Override
    public String getId() {
        return ID;
    }
}
