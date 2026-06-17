import java.util.*;

abstract class Cliente {
    String identificador; // CPF ou CNPJ
    String nome;
    Set<String> placas; // placas cadastradas

    public Cliente(String identificador, String nome, Set<String> placas){
        this.identificador = identificador;
        this.nome = nome;
        this.placas = placas;
    }

    
    public String getIdentificador() {
        return identificador;
    }


    public String getNome() {
        return nome;
    }


    public Set<String> getPlacas() {
        return placas;
    }

     
    abstract double calcularCusto(RegistroEstacionamento r);
    abstract boolean podeEntrar(String placa, Set<String> placasEstacionadas);

    public boolean adicionarPlaca(String placa){
        if(placa == null || placa.isBlank())
    }

    /*
    - id : String
  - nome : String
  - placas : Set<String>
  + {abstract} calcularCusto(r: RegistroEstacionamento) : double
  + {abstract} podeEntrar(placa: String, placasEstacionadas: Set<String>) : boolean
  + adicionarPlaca(placa: String) : boolean
  + removerPlaca(placa: String) : boolean
    */
}