import java.util.Set;

class Empresa extends Cliente {
    // N placas vinculadas ao CNPJ

    private double debitoAcumulado;
    private boolean inadimplente;
    // cobrança por diária + multa se passar da meia-noite

    public Empresa(String identificador, String nome, Set<String> placas, double debitoAcumulado, boolean inadimplente){
        super(identificador, nome, placas);
        this.debitoAcumulado = 0.0;
        this.inadimplente = false;
    }

    //metodos override estao sobreescrevendo os metodos abstratos da classe Cliente 
    @Override
    public double calcularCusto(RegistroEstacionamento r){
        return r.getCusto();
    }

    @Override
    public boolean podeEntrar(String placa, Set<String> placasEstacionadas){
        return true;
    }

    public void adicionarDebito(double valor){
        debitoAcumulado += valor;
    }

    public double emitirBoleto(){
        return this.debitoAcumulado;
    }

    public void registrarPagamento(double valor){
        debitoAcumulado -= valor;

        if(debitoAcumulado <= 0){
            debitoAcumulado = 0;
            inadimplente = false;
        }
    }

    //metodos getters da classe

    public double getDebitoAcumulado() {
        return debitoAcumulado;
    }

    public boolean isInadimplente() {
        return inadimplente;
    }

    
}
/*
Ser inadimplente significa não cumprir com uma obrigação financeira dentro do prazo de vencimento. Isso ocorre quando alguém ou 
alguma empresa deixa de pagar uma conta (boleto, fatura, empréstimo ou imposto) na data acordada, tornando-se um devedor com o 
nome "sujo" no mercado, tornando-se um devedor com o nome "sujo" no mercado.
 */
