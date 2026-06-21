import java.util.Set;

class Professor extends Cliente {
// metodo adicionar placa na classe pai (Cliente) deve ser abstrato? fazer verificacao no construtor ou no metodo?
    public Professor(String identificador, String nome, Set<String> placas){
        super(identificador, nome, placas);

        if (placas.size() > 2){
            throw new IllegalArgumentException("Professor pode ter no máximo 2 placas.");
        }
    }

    @Override
    double calcularCusto(RegistroEstacionamento r) {

        return 0;
    }

    @Override
    boolean podeEntrar(String placa, Set<String> placasEstacionadas) {
        if(!placa.contains(placa)){// verifica se ta cadastrada.
            return false;
        }
        if(placasEstacionadas.contains(placa)){ // a mesma placa nao entra se ja ta estacionada.
            return false;
        }
        if(temVeiculoEstacionado(placasEstacionadas)){ // se ja tem, a proxima vira avulsa. A logica disso deve ser feita na classe Estacionamento.
            return false;
        }
        return true;
    }

    //verifica na collections se tem o veiculo atraves da placa que ele passa por parametro.
    public boolean temVeiculoEstacionado(Set<String> placasEstacionadas){
        for (String p : placasEstacionadas) {
            if(placasEstacionadas.contains(p)){
                return true;
            }
        }
        return false;
    }
}