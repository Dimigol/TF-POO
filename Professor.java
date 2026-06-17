class Professor extends Cliente {
    private String cpf;

    public Professor(String identificador, String nome, Set<String> placas){
        super(identificador, nome, placas);
    }



    // máx 2 placas
    // entrada gratuita, mas só 1 carro por vez no estacionamento
    // se já tem carro dentro, segundo vira "avulso"

    /* 

    String identificador; // CPF ou CNPJ
    String nome;
    Set<String> placas; 

- cpf : String
  + calcularCusto(r: RegistroEstacionamento) : double
  + podeEntrar(placa: String, placasEstacionadas: Set<String>) : boolean
  + temVeiculoEstacionado(placasEstacionadas: Set<String>) : boolean
} */
}