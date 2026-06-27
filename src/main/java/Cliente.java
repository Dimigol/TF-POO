import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class Cliente {
    private static final Pattern PLACA_MERCOSUL = Pattern.compile("[A-Z]{3}[0-9][A-Z][0-9]{2}");
    private final String identificador;
    private final String nome;
    private final Set<String> placas;
    private final int limitePlacas;

    protected Cliente(String identificador, String nome, Set<String> placas, int limitePlacas) {
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException("Identificador obrigatorio.");
        }
        this.identificador = normalizarIdentificador(identificador);
        this.nome = validarNome(nome);
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
        String normalizada = placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return PLACA_MERCOSUL.matcher(normalizada).matches() ? normalizada : null;
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

    protected static String validarNome(String nome) {
        if (nome == null) {
            throw new IllegalArgumentException("Nome obrigatorio.");
        }
        String normalizado = nome.trim();
        if (normalizado.length() < 3) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 letras.");
        }
        long letras = normalizado.chars().filter(Character::isLetter).count();
        if (letras < 3) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 letras.");
        }
        return normalizado;
    }

    protected static String validarCpf(String cpf) {
        String normalizado = normalizarIdentificador(cpf);
        if (normalizado == null || !normalizado.matches("\\d{11}") || todosDigitosIguais(normalizado)) {
            throw new IllegalArgumentException("CPF invalido.");
        }
        if (!digitoCpfValido(normalizado, 9) || !digitoCpfValido(normalizado, 10)) {
            throw new IllegalArgumentException("CPF invalido.");
        }
        return normalizado;
    }

    protected static String validarCnpj(String cnpj) {
        String normalizado = normalizarIdentificador(cnpj);
        if (normalizado == null || !normalizado.matches("\\d{14}") || todosDigitosIguais(normalizado)) {
            throw new IllegalArgumentException("CNPJ invalido.");
        }
        if (!digitoCnpjValido(normalizado, 12) || !digitoCnpjValido(normalizado, 13)) {
            throw new IllegalArgumentException("CNPJ invalido.");
        }
        return normalizado;
    }

    private static boolean todosDigitosIguais(String valor) {
        return valor.chars().distinct().count() == 1;
    }

    private static boolean digitoCpfValido(String valor, int base) {
        int soma = 0;
        int pesoInicial = base + 1;
        for (int i = 0; i < base; i++) {
            soma += (valor.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = soma % 11;
        int digitoEsperado = resto < 2 ? 0 : 11 - resto;
        return valor.charAt(base) - '0' == digitoEsperado;
    }

    private static boolean digitoCnpjValido(String valor, int base) {
        int[] pesos = base == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < base; i++) {
            soma += (valor.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        int digitoEsperado = resto < 2 ? 0 : 11 - resto;
        return valor.charAt(base) - '0' == digitoEsperado;
    }
}
