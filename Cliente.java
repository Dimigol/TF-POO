import java.util.*;

abstract class Cliente {
    private String identificador; // CPF ou CNPJ
    private String nome;
    private Set<String> placas;
    private TipoCliente tipoCliente; // placas cadastradas

    public Cliente(String identificador, String nome, Set<String> placas, TipoCliente tipoCliente){
        this.identificador = identificador;
        this.nome = nome;
        this.placas = placas;
        this.tipoCliente = tipoCliente;
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

    public TipoCliente getTipoCliente(){
        return tipoCliente;
    }
    
    abstract double calcularCusto(RegistroEstacionamento r);
    abstract boolean podeEntrar(String placa, Set<String> placasEstacionadas);

    public boolean adicionarPlaca(String placa){
        if(placa == null || placa.isBlank()){
            return false;
        }
        if(tipoCliente == TipoCliente.PROFESSOR){
            if(placas.size() >= 2){
                return false;
            }
        }
        if(tipoCliente == TipoCliente.ESTUDANTE){
            if(!placas.isEmpty()){
                return false;
            }
        }
        placas.add(placa);
        return true;
    }

    public boolean removePlaca(String placa){
        return placas.remove(placa);
    }
    
}