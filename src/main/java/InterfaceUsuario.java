import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
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
        Tab estacionados = new Tab("Estacionados");
        Tab relatorios = new Tab("Relatorios");
        Tabs tabs = new Tabs(cadastro, movimentacao, estacionados, relatorios);
        tabs.setWidthFull();
        conteudo.setPadding(true);
        conteudo.setSizeFull();
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == cadastro) mostrarCadastro();
            else if (event.getSelectedTab() == movimentacao) mostrarMovimentacao();
            else if (event.getSelectedTab() == estacionados) mostrarEstacionados();
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

        ComboBox<OpcaoCliente> estudanteId = new ComboBox<>("Estudante");
        estudanteId.setItems(opcoesClientesPorTipo(TipoCliente.ESTUDANTE));
        estudanteId.setItemLabelGenerator(OpcaoCliente::descricao);
        NumberField valorCredito = new NumberField("Valor da recarga");
        Button adicionarCredito = new Button("Adicionar saldo", event -> executar(() -> {
            OpcaoCliente selecionado = estudanteId.getValue();
            if (selecionado == null) {
                throw new IllegalArgumentException("Selecione um estudante.");
            }
            if (valorCredito.getValue() == null) {
                throw new IllegalArgumentException("Informe o valor da recarga.");
            }
            double novoSaldo = estacionamento.adicionarCreditoEstudante(
                    selecionado.identificador(), valorCredito.getValue());
            grid.getDataProvider().refreshAll();
            Notification.show(String.format("Novo saldo: R$ %.2f", novoSaldo));
        }));

        ComboBox<OpcaoCliente> empresaId = new ComboBox<>("Empresa");
        empresaId.setItems(opcoesClientesPorTipo(TipoCliente.EMPRESA));
        empresaId.setItemLabelGenerator(OpcaoCliente::descricao);
        NumberField valorPagamento = new NumberField("Valor do pagamento");
        DateTimePicker momentoPagamento = new DateTimePicker("Data do pagamento");
        momentoPagamento.setValue(LocalDateTime.now());
        Button emitirBoleto = new Button("Emitir boleto", event -> executar(() -> {
            OpcaoCliente selecionado = empresaId.getValue();
            if (selecionado == null) {
                throw new IllegalArgumentException("Selecione uma empresa.");
            }
            double valor = estacionamento.emitirBoletoEmpresa(selecionado.identificador());
            Notification.show(String.format("Boleto emitido: R$ %.2f", valor));
        }));
        Button marcarVencido = new Button("Marcar boleto vencido", event -> executar(() -> {
            OpcaoCliente selecionado = empresaId.getValue();
            if (selecionado == null) {
                throw new IllegalArgumentException("Selecione uma empresa.");
            }
            estacionamento.marcarBoletoEmpresaComoVencido(selecionado.identificador());
            grid.getDataProvider().refreshAll();
            Notification.show("Empresa marcada como inadimplente.");
        }));
        Button registrarPagamento = new Button("Registrar pagamento", event -> executar(() -> {
            OpcaoCliente selecionado = empresaId.getValue();
            if (selecionado == null) {
                throw new IllegalArgumentException("Selecione uma empresa.");
            }
            if (valorPagamento.getValue() == null) {
                throw new IllegalArgumentException("Informe o valor do pagamento.");
            }
            double restante = estacionamento.registrarPagamentoEmpresa(
                    selecionado.identificador(), valorPagamento.getValue(), momentoPagamento.getValue());
            grid.getDataProvider().refreshAll();
            Notification.show(String.format("Pagamento registrado. Debito restante: R$ %.2f", restante));
        }));

        ComboBox<OpcaoCliente> clientePlacaId = new ComboBox<>("Cliente");
        clientePlacaId.setItems(opcoesClientes());
        clientePlacaId.setItemLabelGenerator(OpcaoCliente::descricao);
        cadastrar.addClickListener(e -> {
            clientePlacaId.setItems(opcoesClientes());
            estudanteId.setItems(opcoesClientesPorTipo(TipoCliente.ESTUDANTE));
            empresaId.setItems(opcoesClientesPorTipo(TipoCliente.EMPRESA));
        });
        TextField placaCliente = new TextField("Placa do veiculo");
        Button adicionarPlaca = new Button("Adicionar placa", event -> executar(() -> {
            OpcaoCliente selecionado = clientePlacaId.getValue();
            if (selecionado == null) {
                throw new IllegalArgumentException("Selecione um cliente.");
            }
            if (!estacionamento.cadastrarPlaca(
                    selecionado.identificador(), placaCliente.getValue())) {
                throw new IllegalArgumentException("Limite de placas atingido ou placa invalida.");
            }
            grid.getDataProvider().refreshAll();
            Notification.show("Placa adicionada ao cliente.");
        }));
        Button removerPlaca = new Button("Remover placa", event -> executar(() -> {
            OpcaoCliente selecionado = clientePlacaId.getValue();
            if (selecionado == null) {
                throw new IllegalArgumentException("Selecione um cliente.");
            }
            if (!estacionamento.removerPlaca(
                    selecionado.identificador(), placaCliente.getValue())) {
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
        Span tituloCadastrado = new Span("Cliente cadastrado");
        tituloCadastrado.getStyle().set("font-weight", "600");
        Span tituloAvulso = new Span("Cliente avulso");
        tituloAvulso.getStyle().set("font-weight", "600");

        ComboBox<OpcaoPlaca> placasCadastradas = new ComboBox<>("Placas cadastradas");
        placasCadastradas.setItems(opcoesPlacasCadastradas());
        placasCadastradas.setItemLabelGenerator(OpcaoPlaca::descricao);

        TextField placaAvulso = new TextField("Placa do cliente avulso");
        DateTimePicker momentoCadastrado = new DateTimePicker("Data e hora");
        momentoCadastrado.setValue(LocalDateTime.now());
        DateTimePicker momentoAvulso = new DateTimePicker("Data e hora");
        momentoAvulso.setValue(LocalDateTime.now());
        NumberField pagamentoAvulso = new NumberField("Pagamento avulso");
        NumberField pagamentoDebitoAvulso = new NumberField("Pagamento de debito");

        Button entradaCadastrado = new Button("Registrar entrada", event -> executar(() -> {
            OpcaoPlaca selecionada = placasCadastradas.getValue();
            if (selecionada == null) {
                throw new IllegalArgumentException("Selecione uma placa cadastrada.");
            }
            boolean autorizado = estacionamento.autorizarEntrada(
                    selecionada.placa(), momentoCadastrado.getValue());
            Notification.show(autorizado ? "Entrada autorizada." : "Entrada negada.");
        }));
        Button saidaCadastrado = new Button("Registrar saida", event -> executar(() -> {
            OpcaoPlaca selecionada = placasCadastradas.getValue();
            if (selecionada == null) {
                throw new IllegalArgumentException("Selecione uma placa cadastrada.");
            }
            ConfirmDialog confirmacao = new ConfirmDialog();
            confirmacao.setHeader("Confirmar saida");
            confirmacao.setText("Deseja registrar a saida deste veiculo agora?");
            confirmacao.setCancelable(true);
            confirmacao.setConfirmText("Confirmar");
            confirmacao.setCancelText("Cancelar");
            confirmacao.addConfirmListener(confirm -> executar(() -> {
                double devido = estacionamento.processarSaida(
                        selecionada.placa(), momentoCadastrado.getValue(), null);
                Notification.show(String.format("Saida registrada. Devido: R$ %.2f", devido));
                atualizarTelaMovimentacao();
            }));
            confirmacao.open();
        }));

        Button entradaAvulso = new Button("Registrar entrada", event -> executar(() -> {
            boolean autorizado = estacionamento.autorizarEntrada(placaAvulso.getValue(), momentoAvulso.getValue());
            Notification.show(autorizado ? "Entrada autorizada." : "Entrada negada.");
        }));
        Button calcularValorAvulso = new Button("Calcular valor", event -> executar(() -> {
            double devido = estacionamento.calcularValorSaida(
                    placaAvulso.getValue(), momentoAvulso.getValue());
            Notification.show(String.format("Valor devido: R$ %.2f", devido));
        }));
        Button saidaAvulso = new Button("Registrar saida", event -> executar(() -> {
            Double pagamentoInformado = pagamentoAvulso.getValue();
            if (pagamentoInformado != null && pagamentoInformado < 0) {
                throw new IllegalArgumentException("Informe o pagamento do cliente avulso.");
            }
            double devido = estacionamento.calcularValorSaida(
                    placaAvulso.getValue(), momentoAvulso.getValue());
            ConfirmDialog confirmacao = new ConfirmDialog();
            confirmacao.setHeader("Confirmar saida");
            if (pagamentoInformado == null || pagamentoInformado < devido) {
                confirmacao.setText(String.format(
                        "Este cliente entrara na lista de clientes bloqueados com debito de R$ %.2f e "
                                + "so podera voltar a estacionar apos quitar esse valor. Deseja continuar?",
                        devido - (pagamentoInformado == null ? 0.0 : pagamentoInformado)));
            } else {
                confirmacao.setText("Deseja registrar a saida deste avulso agora?");
            }
            confirmacao.setCancelable(true);
            confirmacao.setConfirmText("Confirmar");
            confirmacao.setCancelText("Cancelar");
            confirmacao.addConfirmListener(confirm -> executar(() -> {
                double valorDevido = estacionamento.processarSaida(
                        placaAvulso.getValue(), momentoAvulso.getValue(), pagamentoInformado);
                if (pagamentoInformado == null || pagamentoInformado < valorDevido) {
                    double restante = valorDevido - (pagamentoInformado == null ? 0.0 : pagamentoInformado);
                    Notification.show(String.format(
                            "Saida registrada. Debito pendente: R$ %.2f. Cliente bloqueado ate quitar.",
                            restante));
                } else {
                    Notification.show(String.format("Saida registrada. Devido: R$ %.2f", valorDevido));
                }
                atualizarTelaMovimentacao();
            }));
            confirmacao.open();
        }));
        Button consultarDebitoAvulso = new Button("Consultar debito", event -> executar(() -> {
            double debito = estacionamento.calcularDebitoAvulso(placaAvulso.getValue());
            Notification.show(String.format("Debito pendente: R$ %.2f", debito));
        }));
        Button quitarDebitoAvulso = new Button("Quitar debito", event -> executar(() -> {
            if (pagamentoDebitoAvulso.getValue() == null) {
                throw new IllegalArgumentException("Informe o valor para quitar o debito.");
            }
            double restante = estacionamento.quitarDebitoAvulso(
                    placaAvulso.getValue(), pagamentoDebitoAvulso.getValue());
            if (restante > 0.0) {
                Notification.show(String.format("Pagamento registrado. Debito restante: R$ %.2f", restante));
            } else {
                Notification.show("Debito quitado. Cliente liberado para estacionar novamente.");
            }
            atualizarTelaMovimentacao();
        }));

        VerticalLayout blocoCadastrado = new VerticalLayout(
                tituloCadastrado,
                linhaResponsiva(placasCadastradas, momentoCadastrado, entradaCadastrado,
                        saidaCadastrado));
        blocoCadastrado.setPadding(false);
        blocoCadastrado.setSpacing(true);

        VerticalLayout blocoAvulso = new VerticalLayout(
                tituloAvulso,
                linhaResponsiva(placaAvulso, momentoAvulso, pagamentoAvulso,
                        entradaAvulso, saidaAvulso, calcularValorAvulso),
                linhaResponsiva(pagamentoDebitoAvulso, consultarDebitoAvulso, quitarDebitoAvulso));
        blocoAvulso.setPadding(false);
        blocoAvulso.setSpacing(true);

        conteudo.add(blocoCadastrado, blocoAvulso);
    }

    private void mostrarEstacionados() {
        conteudo.removeAll();
        Grid<EstacionadoLinha> grid = new Grid<>(EstacionadoLinha.class, false);
        grid.addColumn(EstacionadoLinha::placaFormatada).setHeader("Placa");
        grid.addComponentColumn(linha -> {
            Span tipo = new Span(linha.tipo());
            if (linha.tipoCliente() == TipoCliente.AVULSO) {
                tipo.getStyle().set("color", "var(--lumo-error-text-color)");
                tipo.getStyle().set("font-weight", "600");
            } else {
                tipo.getStyle().set("color", "var(--lumo-primary-text-color)");
            }
            return tipo;
        }).setHeader("Tipo").setAutoWidth(true);
        grid.addColumn(EstacionadoLinha::tempoEstacionado).setHeader("Tempo").setAutoWidth(true);
        grid.addColumn(EstacionadoLinha::valorDevido).setHeader("Devido").setAutoWidth(true);
        grid.addColumn(EstacionadoLinha::nomeCliente).setHeader("Cliente");
        grid.addComponentColumn(linha -> {
            Button calcular = new Button("Calcular");
            calcular.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            calcular.addClickListener(event -> executar(() -> {
                double devido = estacionamento.calcularValorSaida(linha.placa(), LocalDateTime.now());
                Notification.show(String.format("%s: R$ %.2f", linha.placaFormatada(), devido));
            }));

            Button receber = new Button("Receber e sair");
            receber.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            receber.addClickListener(event -> executar(() -> {
                double devido = estacionamento.calcularValorSaida(linha.placa(), LocalDateTime.now());
                double pago = linha.tipoCliente() == TipoCliente.AVULSO ? devido : 0.0;
                estacionamento.processarSaida(linha.placa(), LocalDateTime.now(), pago);
                Notification.show(String.format("Saida registrada. Devido: R$ %.2f", devido));
                atualizarListaEstacionados(grid);
            }));

            HorizontalLayout acoes = new HorizontalLayout(calcular, receber);
            acoes.setSpacing(true);
            return acoes;
        }).setHeader("Acoes");

        Button atualizar = new Button("Recarregar", event -> atualizarListaEstacionados(grid));
        atualizar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout topo = new VerticalLayout(
                new H1("Carros estacionados"),
                atualizar);
        topo.setPadding(false);
        topo.setSpacing(false);

        conteudo.add(topo, grid);
        atualizarListaEstacionados(grid);
    }

    private void mostrarRelatorios() {
        conteudo.removeAll();
        ComboBox<String> relatorio = new ComboBox<>("Relatorio");
        relatorio.setItems("Arrecadacao", "Situacao do cliente", "Registros do cliente",
                "Registros avulsos", "Impedidos", "Top 10 do ano");
        TextField referencia = new TextField("CPF, CNPJ ou placa");
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
        Grid<RankingLinha> rankingGrid = new Grid<>(RankingLinha.class, false);
        rankingGrid.addColumn(RankingLinha::posicao).setHeader("#").setAutoWidth(true).setFlexGrow(0);
        rankingGrid.addColumn(RankingLinha::identificador).setHeader("Identificador");
        rankingGrid.addColumn(RankingLinha::quantidade).setHeader("Frequencia").setAutoWidth(true).setFlexGrow(0);
        rankingGrid.setVisible(false);
        rankingGrid.setHeight("280px");
        Button gerar = new Button("Gerar", event -> executar(() -> {
            LocalDateTime inicio = inicioPeriodo.getValue();
            LocalDateTime fim = fimPeriodo.getValue();
            String valor;
            switch (relatorio.getValue()) {
                case "Arrecadacao":
                    rankingGrid.setVisible(false);
                    valor = String.format("R$ %.2f", estacionamento.relatorioArrecadacao(
                            inicio, fim, categorias.getSelectedItems()));
                    break;
                case "Situacao do cliente":
                    rankingGrid.setVisible(false);
                    valor = estacionamento.relatorioSituacaoCliente(referencia.getValue());
                    break;
                case "Registros do cliente":
                    rankingGrid.setVisible(false);
                    valor = formatarRegistros(estacionamento.relatorioRegistrosCliente(
                            referencia.getValue(), inicio, fim));
                    break;
                case "Registros avulsos":
                    rankingGrid.setVisible(false);
                    valor = formatarRegistros(estacionamento.relatorioRegistrosAvulso(
                            referencia.getValue(), inicio, fim));
                    break;
                case "Impedidos":
                    rankingGrid.setVisible(false);
                    valor = formatarLista(
                            estacionamento.relatorioImpedidos().stream().sorted().collect(Collectors.toList()));
                    break;
                case "Top 10 do ano":
                    rankingGrid.setItems(criarRankingTop10(
                            estacionamento.relatorioTop10Frequentes(ano.getValue().intValue())));
                    rankingGrid.setVisible(true);
                    valor = "";
                    break;
                default:
                    throw new IllegalArgumentException("Selecione um relatorio.");
            }
            resultado.setText(valor);
        }));
        conteudo.add(linhaResponsiva(relatorio, referencia, inicioPeriodo, fimPeriodo, ano, gerar),
                categorias, resultado, rankingGrid);
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

    private String formatarLista(List<String> valores) {
        if (valores.isEmpty()) {
            return "Nenhum item encontrado.";
        }
        return valores.stream()
                .map(Cliente::formatarIdentificador)
                .collect(Collectors.joining("\n"));
    }

    private String formatarTop10(Map<String, Long> ranking) {
        if (ranking.isEmpty()) {
            return "Nenhum registro encontrado.";
        }
        StringBuilder sb = new StringBuilder();
        int posicao = 1;
        for (Map.Entry<String, Long> entrada : ranking.entrySet()) {
            sb.append(posicao++)
                    .append(". ")
                    .append(Cliente.formatarIdentificador(entrada.getKey()))
                    .append(" - ")
                    .append(entrada.getValue())
                    .append('\n');
        }
        return sb.toString().trim();
    }

    private List<RankingLinha> criarRankingTop10(Map<String, Long> ranking) {
        if (ranking.isEmpty()) {
            return List.of();
        }
        List<RankingLinha> linhas = new ArrayList<>();
        int posicao = 1;
        for (Map.Entry<String, Long> entrada : ranking.entrySet()) {
            linhas.add(new RankingLinha(posicao++,
                    Cliente.formatarIdentificador(entrada.getKey()),
                    entrada.getValue()));
        }
        return linhas;
    }

    private void atualizarListaEstacionados(Grid<EstacionadoLinha> grid) {
        List<EstacionadoLinha> linhas = estacionamento.getRegistrosAtivos().values().stream()
                .map(registro -> {
                    Cliente cliente = registro.getIdentificadorProprietario() == null
                            ? null
                            : estacionamento.getClientesCadastrados().get(registro.getIdentificadorProprietario());
                    String nome = cliente == null ? "Avulso" : cliente.getNome();
                    double devido = estacionamento.calcularValorSaida(registro.getPlaca(), LocalDateTime.now());
                    return new EstacionadoLinha(
                            registro.getPlaca(),
                            registro.getTipoCliente(),
                            nome,
                            formatarTempo(registro.getEntrada(), LocalDateTime.now()),
                            String.format("R$ %.2f", devido));
                })
                .sorted((a, b) -> a.placa().compareToIgnoreCase(b.placa()))
                .collect(Collectors.toList());
        grid.setItems(linhas);
    }

    private String formatarTempo(LocalDateTime entrada, LocalDateTime agora) {
        long minutos = java.time.Duration.between(entrada, agora).toMinutes();
        long horas = minutos / 60;
        long restoMinutos = minutos % 60;
        if (horas == 0) {
            return String.format("%dm", restoMinutos);
        }
        return String.format("%dh %02dm", horas, restoMinutos);
    }

    private void atualizarTelaMovimentacao() {
        mostrarMovimentacao();
    }

    private List<OpcaoPlaca> opcoesPlacasCadastradas() {
        return estacionamento.getClientesCadastrados().values().stream()
                .flatMap(cliente -> cliente.getPlacas().stream()
                        .map(placa -> new OpcaoPlaca(placa, cliente.getNome())))
                .sorted((a, b) -> a.descricao().compareToIgnoreCase(b.descricao()))
                .collect(Collectors.toList());
    }

    private List<OpcaoCliente> opcoesClientes() {
        return estacionamento.getClientesCadastrados().values().stream()
                .map(cliente -> new OpcaoCliente(cliente.getIdentificador(), cliente.getNome()))
                .sorted((a, b) -> a.descricao().compareToIgnoreCase(b.descricao()))
                .collect(Collectors.toList());
    }

    private List<OpcaoCliente> opcoesClientesPorTipo(TipoCliente tipo) {
        return estacionamento.getClientesCadastrados().values().stream()
                .filter(cliente -> cliente.getTipo() == tipo)
                .map(cliente -> new OpcaoCliente(cliente.getIdentificador(), cliente.getNome()))
                .sorted((a, b) -> a.descricao().compareToIgnoreCase(b.descricao()))
                .collect(Collectors.toList());
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

    private static final class RankingLinha {
        private final int posicao;
        private final String identificador;
        private final Long quantidade;

        private RankingLinha(int posicao, String identificador, Long quantidade) {
            this.posicao = posicao;
            this.identificador = identificador;
            this.quantidade = quantidade;
        }

        public int posicao() { return posicao; }
        public String identificador() { return identificador; }
        public Long quantidade() { return quantidade; }
    }

    private static final class EstacionadoLinha {
        private final String placa;
        private final TipoCliente tipoCliente;
        private final String nomeCliente;
        private final String tempoEstacionado;
        private final String valorDevido;

        private EstacionadoLinha(String placa, TipoCliente tipoCliente, String nomeCliente,
                                 String tempoEstacionado, String valorDevido) {
            this.placa = placa;
            this.tipoCliente = tipoCliente;
            this.nomeCliente = nomeCliente;
            this.tempoEstacionado = tempoEstacionado;
            this.valorDevido = valorDevido;
        }

        public String placa() { return placa; }
        public String placaFormatada() { return placa; }
        public String tipo() { return tipoCliente.toString(); }
        public String nomeCliente() { return nomeCliente; }
        public String tempoEstacionado() { return tempoEstacionado; }
        public String valorDevido() { return valorDevido; }
        public TipoCliente tipoCliente() { return tipoCliente; }
    }

    private static final class OpcaoPlaca {
        private final String placa;
        private final String nomeCliente;

        private OpcaoPlaca(String placa, String nomeCliente) {
            this.placa = placa;
            this.nomeCliente = nomeCliente;
        }

        private String placa() {
            return placa;
        }

        private String descricao() {
            return placa + " - " + nomeCliente;
        }

        @Override
        public String toString() {
            return descricao();
        }
    }

    private static final class OpcaoCliente {
        private final String identificador;
        private final String nomeCliente;

        private OpcaoCliente(String identificador, String nomeCliente) {
            this.identificador = identificador;
            this.nomeCliente = nomeCliente;
        }

        private String identificador() {
            return identificador;
        }

        private String descricao() {
            return Cliente.formatarIdentificador(identificador) + " - " + nomeCliente;
        }

        @Override
        public String toString() {
            return descricao();
        }
    }
}
