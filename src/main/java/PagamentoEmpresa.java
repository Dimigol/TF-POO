import java.time.LocalDateTime;

public final class PagamentoEmpresa {
    private final String identificadorEmpresa;
    private final LocalDateTime momento;
    private final double valor;

    public PagamentoEmpresa(String identificadorEmpresa, LocalDateTime momento, double valor) {
        if (identificadorEmpresa == null || identificadorEmpresa.isBlank()) {
            throw new IllegalArgumentException("CNPJ da empresa obrigatorio.");
        }
        if (momento == null || valor <= 0) {
            throw new IllegalArgumentException("Dados do pagamento empresarial invalidos.");
        }
        this.identificadorEmpresa = identificadorEmpresa.trim();
        this.momento = momento;
        this.valor = valor;
    }

    public String getIdentificadorEmpresa() {
        return identificadorEmpresa;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    public double getValor() {
        return valor;
    }
}
