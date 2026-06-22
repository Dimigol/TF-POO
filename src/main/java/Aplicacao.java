/**
 * Ponto de entrada mantido para compatibilidade com configuracoes antigas.
 * Novas execucoes devem utilizar {@link Main}.
 */
@Deprecated
public final class Aplicacao {
    private Aplicacao() {
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}
