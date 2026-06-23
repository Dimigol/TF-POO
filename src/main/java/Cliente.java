import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Cliente {
    private final String identificador;
    private final String nome;
    private final Set<String> placas;
    private final int limitePlacas;

    protected Cliente(String identificador, String nome, Set<String> placas, int limitePlacas) {
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException("Identificador obrigatorio.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatorio.");
        }
        this.identificador = normalizarIdentificador(identificador);
        this.nome = nome.trim();
        this.placas = new HashSet<>();
        this.limitePlacas = limitePlacas;
        if (placas != null) {
            for (String placa : placas) {
                if (!adicionarPlaca(placa)) {
                    throw new IllegalArgumentException("Placa invalida ou limite excedido: " + placa);
                }
            }
        }
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getNome() {
        return nome;
    }

    public Set<String> getPlacas() {
        return Collections.unmodifiableSet(placas);
    }

    public final boolean adicionarPlaca(String placa) {
        String normalizada = normalizarPlaca(placa);
        return normalizada != null && placas.size() < limitePlacas && placas.add(normalizada);
    }

    public final boolean removerPlaca(String placa) {
        String normalizada = normalizarPlaca(placa);
        return normalizada != null && placas.remove(normalizada);
    }

    public abstract TipoCliente getTipo();

    public abstract double calcularCusto(RegistroEstacionamento registro, TabelaTarifas tarifas);

    public boolean podeEntrar(String placa, Set<String> placasEstacionadas) {
        String normalizada = normalizarPlaca(placa);
        return normalizada != null && placas.contains(normalizada)
                && !placasEstacionadas.contains(normalizada);
    }

    public static String normalizarPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            return null;
        }
        return placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public static String normalizarIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return null;
        }
        String apenasDigitos = identificador.replaceAll("\\D", "");
        return apenasDigitos.isBlank() ? identificador.trim() : apenasDigitos;
    }

    public static String formatarIdentificador(String identificador) {
        String normalizado = normalizarIdentificador(identificador);
        if (normalizado == null) {
            return "";
        }
        if (normalizado.matches("\\d{11}")) {
            return normalizado.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
        if (normalizado.matches("\\d{14}")) {
            return normalizado.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }
        return normalizado;
    }
}
