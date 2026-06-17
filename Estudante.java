import java.util.Set;

class Estudante extends Cliente {
    // 1 placa apenas
    private double saldo; // pode ficar negativo

    public Estudante(String id, String nome, Set<String> placas, double saldo){
        super(id, nome, placas);
        this.saldo = saldo;
    }

    public double getSaldo(){
        return saldo;
    }

    @Override
    double calcularCusto(RegistroEstacionamento r) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    boolean podeEntrar(String placa, Set<String> placasEstacionadas) {
        // TODO Auto-generated method stub
        return false;
    }
    // valor fixo por ingresso/dia, novo ingresso se sair após meia-noite
    // bloqueado se saldo < 0
}


/*abstract class Cliente {
  - id : String
  - nome : String
  - placas : Set<String>
  + {abstract} calcularCusto(r: RegistroEstacionamento) : double
  + {abstract} podeEntrar(placa: String, placasEstacionadas: Set<String>) : boolean
  + adicionarPlaca(placa: String) : boolean
  + removerPlaca(placa: String) : boolean
}
  
class Estudante {
  - cpf : String
  - saldo : double
  + calcularCusto(r: RegistroEstacionamento) : double
  + podeEntrar(placa: String, placasEstacionadas: Set<String>) : boolean
  + debitar(valor: double) : void
  + estaBloqueado() : boolean
}*/