import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route("")
public class InterfaceUsuario extends VerticalLayout {
    private final Estacionamento estacionamento;
    private final CSVManager csvManager;
    private final VerticalLayout conteudo = new VerticalLayout();

    public InterfaceUsuario(Estacionamento estacionamento, CSVManager csvManager) {
        this.estacionamento = estacionamento;
        this.csvManager = csvManager;
        setSizeFull();
        setPadding(false);

        H1 titulo = new H1("EstACME");
        titulo.getStyle().set("margin", "var(--lumo-space-m)");
        Button salvar = new Button("Salvar dados agora", event -> executar(this::salvarDados));
        HorizontalLayout cabecalho = new HorizontalLayout(titulo, salvar);
        cabecalho.setWidthFull();
        cabecalho.setAlignItems(FlexComponent.Alignment.CENTER);
        cabecalho.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cabecalho.getStyle().set("padding-right", "var(--lumo-space-m)");
        Tab cadastro = new Tab("Clientes");
        Tab movimentacao = new Tab("Entrada e saida");
        Tab relatorios = new Tab("Relatorios");
        Tabs tabs = new Tabs(cadastro, movimentacao, relatorios);
        tabs.setWidthFull();
        conteudo.setPadding(true);
        conteudo.setSizeFull();
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == cadastro) mostrarCadastro();
            else if (event.getSelectedTab() == movimentacao) mostrarMovimentacao();
            else mostrarRelatorios();
        });
        add(cabecalho, tabs, conteudo);
        expand(conteudo);
        mostrarCadastro();
    }

    private void mostrarCadastro() {
        conteudo.removeAll();
        Select<TipoCliente> tipo = new Select<>();
        tipo.setLabel("Categoria");
        tipo.setItems(TipoCliente.PROFESSOR, TipoCliente.ESTUDANTE, TipoCliente.EMPRESA);
        TextField id = new TextField("CPF ou CNPJ");
        TextField nome = new TextField("Nome");
        TextField placas = new TextField("Placas separadas por virgula");
        NumberField saldo = new NumberField("Saldo inicial do estudante");
        saldo.setValue(0.0);
        Button cadastrar = new Button("Cadastrar", event -> executar(() -> {
            Set<String> conjunto = placas.getValue().isBlank() ? Collections.emptySet()
                    : new HashSet<>(Arrays.asList(placas.getValue().split("\\s*,\\s*")));
            Cliente cliente;
            if (tipo.getValue() == TipoCliente.PROFESSOR) {
                cliente = new Professor(id.getValue(), nome.getValue(), conjunto);
            } else if (tipo.getValue() == TipoCliente.ESTUDANTE) {
                cliente = new Estudante(id.getValue(), nome.getValue(), conjunto, saldo.getValue());
            } else if (tipo.getValue() == TipoCliente.EMPRESA) {
                cliente = new Empresa(id.getValue(), nome.getValue(), conjunto, 0, false);
            } else {
                throw new IllegalArgumentException("Selecione a categoria.");
            }
            estacionamento.cadastrarCliente(cliente);
            Notification.show("Cliente cadastrado.");
        }));
        Grid<Cliente> grid = new Grid<>(Cliente.class, false);
        grid.addColumn(c -> Cliente.formatarIdentificador(c.getIdentificador())).setHeader("CPF/CNPJ");
        grid.addColumn(Cliente::getNome).setHeader("Nome");
        grid.addColumn(Cliente::getTipo).setHeader("Categoria");
        grid.addColumn(c -> String.join(", ", c.getPlacas())).setHeader("Placas");
        grid.addColumn(c -> c instanceof Estudante
                ? String.format("R$ %.2f", ((Estudante) c).getSaldo()) : "-")
                .setHeader("Saldo");
        grid.addColumn(c -> c instanceof Empresa
                ? String.format("R$ %.2f", ((Empresa) c).getDebitoAcumulado()) : "-")
                .setHeader("Debito");
        grid.addColumn(c -> c instanceof Empresa
                ? (((Empresa) c).isInadimplente() ? "Inadimplente" : "Regular") : "-")
                .setHeader("Situacao");
        grid.setItems(estacionamento.getClientesCadastrados().values());
        cadastrar.addClickListener(e -> grid.setItems(estacionamento.getClientesCadastrados().values()));

        TextField estudanteId = new TextField("CPF do estudante");
        NumberField valorCredito = new NumberField("Valor da recarga");
        Button adicionarCredito = new Button("Adicionar saldo", event -> executar(() -> {
            if (valorCredito.getValue() == null) {
                throw new IllegalArgumentException("Informe o valor da recarga.");
            }
            double novoSaldo = estacionamento.adicionarCreditoEstudante(
                    estudanteId.getValue(), valorCredito.getValue());
            grid.getDataProvider().refreshAll();
            Notification.show(String.format("Novo saldo: R$ %.2f", novoSaldo));
        }));

        TextField empresaId = new TextField("CNPJ da empresa");
        NumberField valorPagamento = new NumberField("Valor do pagamento");
        DateTimePicker momentoPagamento = new DateTimePicker("Data do pagamento");
        momentoPagamento.setValue(LocalDateTime.now());
        Button emitirBoleto = new Button("Emitir boleto", event -> executar(() -> {
            double valor = estacionamento.emitirBoletoEmpresa(empresaId.getValue());
            Notification.show(String.format("Boleto emitido: R$ %.2f", valor));
        }));
        Button marcarVencido = new Button("Marcar boleto vencido", event -> executar(() -> {
            estacionamento.marcarBoletoEmpresaComoVencido(empresaId.getValue());
            grid.getDataProvider().refreshAll();
            Notification.show("Empresa marcada como inadimplente.");
        }));
        Button registrarPagamento = new Button("Registrar pagamento", event -> executar(() -> {
            if (valorPagamento.getValue() == null) {
                throw new IllegalArgumentException("Informe o valor do pagamento.");
            }
            double restante = estacionamento.registrarPagamentoEmpresa(
                    empresaId.getValue(), valorPagamento.getValue(), momentoPagamento.getValue());
            grid.getDataProvider().refreshAll();
            Notification.show(String.format("Pagamento registrado. Debito restante: R$ %.2f", restante));
        }));

        TextField clientePlacaId = new TextField("CPF ou CNPJ do cliente");
        TextField placaCliente = new TextField("Placa do veiculo");
        Button adicionarPlaca = new Button("Adicionar placa", event -> executar(() -> {
            if (!estacionamento.cadastrarPlaca(
                    clientePlacaId.getValue(), placaCliente.getValue())) {
                throw new IllegalArgumentException("Limite de placas atingido ou placa invalida.");
            }
            grid.getDataProvider().refreshAll();
            Notification.show("Placa adicionada ao cliente.");
        }));
        Button removerPlaca = new Button("Remover placa", event -> executar(() -> {
            if (!estacionamento.removerPlaca(
                    clientePlacaId.getValue(), placaCliente.getValue())) {
                throw new IllegalArgumentException("A placa nao pertence ao cliente informado.");
            }
            grid.getDataProvider().refreshAll();
            Notification.show("Placa removida do cliente.");
        }));

        conteudo.add(
                linhaResponsiva(tipo, id, nome, placas, saldo, cadastrar),
                grid,
                linhaResponsiva(clientePlacaId, placaCliente, adicionarPlaca, removerPlaca),
                linhaResponsiva(estudanteId, valorCredito, adicionarCredito),
                linhaResponsiva(empresaId, valorPagamento, momentoPagamento,
                        emitirBoleto, marcarVencido, registrarPagamento));
    }

    private void mostrarMovimentacao() {
        conteudo.removeAll();
        TextField placa = new TextField("Placa");
        DateTimePicker momento = new DateTimePicker("Data e hora");
        momento.setValue(LocalDateTime.now());
        NumberField pagamento = new NumberField("Pagamento avulso");
        Span valorDevido = new Span("Valor devido: clique em calcular");
        Button entrada = new Button("Registrar entrada", event -> executar(() -> {
            boolean autorizado = estacionamento.autorizarEntrada(placa.getValue(), momento.getValue());
            Notification.show(autorizado ? "Entrada autorizada." : "Entrada negada.");
        }));
        Button calcular = new Button("Calcular valor devido", event -> executar(() -> {
            double devido = estacionamento.calcularValorSaida(
                    placa.getValue(), momento.getValue());
            valorDevido.setText(String.format("Valor devido: R$ %.2f", devido));
        }));
        Button saida = new Button("Registrar saida", event -> executar(() -> {
            double devido = estacionamento.processarSaida(
                    placa.getValue(), momento.getValue(), pagamento.getValue());
            Notification.show(String.format("Saida registrada. Devido: R$ %.2f", devido));
            valorDevido.setText("Valor devido: clique em calcular");
        }));
        conteudo.add(linhaResponsiva(placa, momento, pagamento, entrada, calcular, saida),
                valorDevido);
    }

    private void mostrarRelatorios() {
        conteudo.removeAll();
        ComboBox<String> relatorio = new ComboBox<>("Relatorio");
        relatorio.setItems("Arrecadacao", "Situacao do cliente", "Registros do cliente",
                "Registros avulsos", "Impedidos", "Top 10 do ano");
        TextField referencia = new TextField("CPF, CNPJ ou placa");
        referencia.setHelperText("Para listar todos os avulsos, deixe a placa em branco.");
        int anoAtual = LocalDateTime.now().getYear();
        DateTimePicker inicioPeriodo = new DateTimePicker("Inicio do periodo");
        inicioPeriodo.setValue(LocalDateTime.of(anoAtual, 1, 1, 0, 0));
        DateTimePicker fimPeriodo = new DateTimePicker("Fim do periodo");
        fimPeriodo.setValue(LocalDateTime.of(anoAtual, 12, 31, 23, 59, 59));
        NumberField ano = new NumberField("Ano");
        ano.setValue((double) anoAtual);
        CheckboxGroup<TipoCliente> categorias = new CheckboxGroup<>("Categorias");
        categorias.setItems(TipoCliente.values());
        Pre resultado = new Pre();
        Button gerar = new Button("Gerar", event -> executar(() -> {
            LocalDateTime inicio = inicioPeriodo.getValue();
            LocalDateTime fim = fimPeriodo.getValue();
            String valor;
            switch (relatorio.getValue()) {
                case "Arrecadacao":
                    valor = String.format("R$ %.2f", estacionamento.relatorioArrecadacao(
                            inicio, fim, categorias.getSelectedItems()));
                    break;
                case "Situacao do cliente":
                    valor = estacionamento.relatorioSituacaoCliente(referencia.getValue());
                    break;
                case "Registros do cliente":
                    valor = formatarRegistros(estacionamento.relatorioRegistrosCliente(
                            referencia.getValue(), inicio, fim));
                    break;
                case "Registros avulsos":
                    valor = formatarRegistros(estacionamento.relatorioRegistrosAvulso(
                            referencia.getValue(), inicio, fim));
                    break;
                case "Impedidos":
                    valor = estacionamento.relatorioImpedidos().toString();
                    break;
                case "Top 10 do ano":
                    valor = estacionamento.relatorioTop10Frequentes(ano.getValue().intValue()).toString();
                    break;
                default:
                    throw new IllegalArgumentException("Selecione um relatorio.");
            }
            resultado.setText(valor);
        }));
        conteudo.add(linhaResponsiva(relatorio, referencia, inicioPeriodo, fimPeriodo, ano, gerar),
                categorias, resultado);
    }

    private HorizontalLayout linhaResponsiva(Component... componentes) {
        HorizontalLayout linha = new HorizontalLayout(componentes);
        linha.setWidthFull();
        linha.setAlignItems(FlexComponent.Alignment.END);
        linha.getStyle().set("flex-wrap", "wrap");
        return linha;
    }

    private String formatarRegistros(List<RegistroEstacionamento> registros) {
        if (registros.isEmpty()) {
            return "Nenhum registro encontrado.";
        }
        return registros.stream()
                .map(RegistroEstacionamento::toString)
                .collect(Collectors.joining("\n"));
    }

    private void salvarDados() {
        try {
            csvManager.salvarTudo(estacionamento);
            Notification.show("Dados salvos na pasta dados.");
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel salvar os dados: " + e.getMessage(), e);
        }
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}
