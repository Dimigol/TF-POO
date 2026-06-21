// Não estende Cliente (não tem CPF/CNPJ) - identificado só pela placa
class ClienteAvulso {
    private String placa;

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