# Sistema de Controle de Estacionamento EstACME

Trabalho final de Programacao Orientada a Objetos. O sistema implementa cadastro,
entrada, saida, cobranca, bloqueios, relatorios, persistencia CSV e interface Vaadin.

## Requisitos

- JDK 17 ou superior
- Maven 3.9 ou o Maven integrado da IDE

## Executar a aplicacao

```bash
mvn spring-boot:run
```

A interface fica disponivel em `http://localhost:8080` e oferece:

- cadastro de professores, estudantes, empresas e suas placas;
- registro de entrada e saida, incluindo pagamento avulso;
- os seis relatorios gerenciais exigidos no enunciado.

Os dados ficam no diretorio `dados/`, nos arquivos `clientes.csv`, `veiculos.csv`,
`registros.csv` e `bloqueados.csv`. Eles sao carregados ao iniciar e salvos
automaticamente ao encerrar a aplicacao.

## Tarifas padrao

| Tarifa | Valor |
|---|---:|
| Hora avulsa | R$ 10,00 |
| Diaria | R$ 50,00 |
| Ingresso de estudante | R$ 15,00 |
| Multa empresarial por virada de dia | R$ 30,00 |

Os valores podem ser alterados no construtor de `TabelaTarifas`.

## Validacao

O teste executavel cobre cobranca avulsa, desconto frequente, recusa de pagamento,
professor com dois veiculos, saldo negativo de estudante, inadimplencia empresarial
e persistencia CSV.

```bash
mvn compile
java -ea -cp target/classes br.edu.pucrs.estacionamento.EstacionamentoTest
```

## Arquivos principais

- `Estacionamento.java`: regras de entrada, saida, cobranca e relatorios.
- `Cliente.java` e subclasses: regras polimorficas por categoria.
- `RegistroEstacionamento.java`: dados completos de cada permanencia.
- `CSVManager.java`: carga, salvamento manual e salvamento automatico.
- `InterfaceUsuario.java`: interface web Vaadin.
- `diagrama_classes.puml`: diagrama de classes atualizado.
