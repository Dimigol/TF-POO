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
gi
     
    abstract double calcularCusto(RegistroEstacionamento r);
    abstract boolean podeEntrar(String placa, Set<String> placasEstacionadas);

    public boolean adicionarPlaca(String placa){
        if(placa == null || placa.isBlank()){
            return false;
        }
        return true;
    }

    public boolean removePlaca(String placa){
        return placas.remove(placa);
    }
    
}