// Não estende Cliente (não tem CPF/CNPJ) - identificado só pela placa
class ClienteAvulso {
    private String placa;
    private boolean usouUltimosTresDias;

    public ClienteAvulso(String placa){
      this.placa = placa;
      this.usouUltimosTresDias = false;
    }

    
    public double calcularCusto(RegistroEstacionamento r){
      return 0;
    }

    
    public boolean podeEntrar(String placa, Set<String> placasEstacionadas){
      return true;
    }
    // cobrança por hora (até 6h) ou diária
    // verifica ClienteFrequente (desconto 10%)
}

/*
class ClienteAvulso {
  - placa : String
  - usouUltimosTresDias : boolean
  + calcularCusto(r: RegistroEstacionamento) : double
  + podeEntrar(placa: String, placasEstacionadas: Set<String>) : boolean
}
*/