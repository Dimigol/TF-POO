# Sistema de Controle de Estacionamento

Trabalho final da disciplina de Programação Orientada a Objetos — Prof. Bernardo Copstein.

## Contexto

Sistema de controle de um estacionamento subterrâneo de 9.000 vagas, compartilhado
por um shopping center, um edifício corporativo e uma universidade, administrado
pela empresa EstACME. Atende clientes avulsos e clientes pré-cadastrados
(professores, estudantes e empresas), cada um com regras específicas de
cobrança e autorização de entrada.

## Estrutura do projeto

```
src/
  model/
    Cliente.java                  # classe abstrata base
    ClienteAvulso.java
    Professor.java
    Estudante.java
    Empresa.java
    Veiculo.java
    RegistroEstacionamento.java
    Desconto.java                 # interface (polimorfismo de descontos)
    DescontoClienteFrequente.java
    TabelaTarifas.java             # valores de cobrança (singleton)
  service/
    Estacionamento.java            # núcleo do sistema: entrada, saída, cobrança, relatórios
  persistence/
    CSVManager.java                # leitura/escrita dos arquivos CSV
diagrama_classes.puml              # diagrama de classes (PlantUML)
```

## Regras de negócio (resumo)

- **Avulsos**: cobrança por hora (até 6h) ou diária; nova diária a cada virada
  de meia-noite; recusa de pagamento bloqueia futuras entradas.
- **Professores**: até 2 placas por CPF, entrada gratuita, apenas 1 veículo
  estacionado por vez (o segundo é cobrado como avulso).
- **Estudantes**: 1 placa por CPF, pré-pago por ingresso/dia, saldo pode ficar
  negativo (bloqueia novas entradas).
- **Empresas**: N placas por CNPJ, cobrança por diária + multa se ultrapassar
  meia-noite, débitos acumulados via boleto, inadimplência bloqueia todos os
  veículos da empresa.
- **Desconto Cliente Frequente**: 10% para avulsos que usaram o estacionamento
  nos últimos 3 dias.

## Persistência (CSV)

| Arquivo          | Conteúdo                                              |
|-------------------|-------------------------------------------------------|
| `clientes.csv`    | clientes pré-cadastrados (id, tipo, nome, saldo/débito)|
| `veiculos.csv`    | placas vinculadas a clientes pré-cadastrados           |
| `registros.csv`   | histórico de entradas/saídas e cobranças               |
| `bloqueados.csv`  | placas avulsas bloqueadas por recusa de pagamento      |

Todos os dados são carregados para memória na inicialização e salvos
automaticamente no encerramento do sistema.

## Tecnologias

- **Java** (modelagem e regras de negócio)
- **Vaadin** (interface com o usuário, em classe separada)

## Como compilar

```bash
javac -d out $(find src -name "*.java")
```

## Status do projeto

- [x] Fase 1 — Modelagem das classes de domínio
- [ ] Fase 1 — Controle de entrada/saída e cobrança (em andamento)
- [ ] Fase 2 — Persistência em CSV
- [ ] Fase 2 — Interface com o usuário (Vaadin)
- [ ] Fase 2 — Relatórios gerenciais

## Equipe

- Dimitri Jadovski, Rodrigo Bachovas, Frederico dos Santos, Gustavo da Rosa
