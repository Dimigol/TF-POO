abstract class Cliente {
    String identificador; // CPF ou CNPJ
    String nome;
    Set<String> placas; // placas cadastradas

    abstract double calcularCusto(RegistroEstacionamento r);
    abstract boolean podeEntrar(String placa, Set<String> placasEstacionadas);
}