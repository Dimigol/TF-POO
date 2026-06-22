public final class TabelaTarifas {
    private final double valorHora;
    private final double valorDiaria;
    private final double valorIngresso;
    private final double multaAtraso;

    public TabelaTarifas() {
        this(10.0, 50.0, 15.0, 30.0);
    }

    public TabelaTarifas(double valorHora, double valorDiaria,
                         double valorIngresso, double multaAtraso) {
        if (valorHora < 0 || valorDiaria < 0 || valorIngresso < 0 || multaAtraso < 0) {
            throw new IllegalArgumentException("Tarifas nao podem ser negativas.");
        }
        this.valorHora = valorHora;
        this.valorDiaria = valorDiaria;
        this.valorIngresso = valorIngresso;
        this.multaAtraso = multaAtraso;
    }

    public double getValorHora() { return valorHora; }
    public double getValorDiaria() { return valorDiaria; }
    public double getValorIngresso() { return valorIngresso; }
    public double getMultaAtraso() { return multaAtraso; }
}
