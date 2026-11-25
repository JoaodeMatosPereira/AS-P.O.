// Grupo: [nomes dos integrantes]

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Interface de console da locadora: tabela de veiculos, orcamento, aluguel
 * (com confirmacao e datas), devolucao (concluindo o aluguel com comprovante),
 * alteracao de diaria, lista de alugueis, cadastro/exclusao de veiculos e
 * calendario mensal. As datas usam o relogio do sistema (tempo real).
 * Toda a regra de negocio continua em Veiculo/Carro/Moto/Aluguel/Locadora;
 * aqui so ha leitura de dados e impressao de mensagens.
 */
public class MenuConsole {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final String[] MESES = {
        "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };
    // Indice 0 = segunda-feira (DayOfWeek.getValue() - 1)
    private static final String[] DIAS_SEMANA = {
        "segunda-feira", "terca-feira", "quarta-feira", "quinta-feira",
        "sexta-feira", "sabado", "domingo"
    };

    private final Locadora locadora;
    private final Scanner scanner;

    public MenuConsole(Locadora locadora, Scanner scanner) {
        this.locadora = locadora;
        this.scanner = scanner;
    }

    public void iniciar() {
        try {
            int opcao;
            do {
                exibirMenu();
                opcao = lerInteiro("Escolha uma opcao: ");
                System.out.println();
                switch (opcao) {
                    case 1 -> exibirTabela();
                    case 2 -> consultarOrcamento();
                    case 3 -> alugar();
                    case 4 -> concluirAluguel();
                    case 5 -> alterarDiaria();
                    case 6 -> exibirAlugueis();
                    case 7 -> cadastrarVeiculo();
                    case 8 -> excluirVeiculo();
                    case 9 -> exibirCalendario();
                    case 0 -> System.out.println("Encerrando o menu da locadora. Ate logo!");
                    default -> System.out.println("Opcao invalida. Escolha um numero de 0 a 9.");
                }
            } while (opcao != 0);
        } catch (NoSuchElementException e) {
            // Entrada encerrada (nao ha mais nada para ler no console)
            System.out.println("\nEntrada encerrada. Saindo do menu.");
        }
    }

    private void exibirMenu() {
        System.out.println("\n===== LOCADORA DE VEICULOS =====");
        System.out.println("Hoje: " + descreverData(locadora.hoje()));
        System.out.println("1 - Listar veiculos (tabela)");
        System.out.println("2 - Consultar orcamento");
        System.out.println("3 - Alugar veiculo");
        System.out.println("4 - Devolver veiculo e concluir aluguel");
        System.out.println("5 - Alterar valor da diaria");
        System.out.println("6 - Ver alugueis (em andamento e concluidos)");
        System.out.println("7 - Cadastrar novo veiculo");
        System.out.println("8 - Excluir veiculo");
        System.out.println("9 - Calendario (hoje e prazos dos alugueis)");
        System.out.println("0 - Sair");
    }

    // A coluna "Modelo" se ajusta ao maior nome da frota
    private void exibirTabela() {
        if (locadora.getFrota().isEmpty()) {
            System.out.println("Nenhum veiculo na frota.");
            return;
        }
        int largura = "Modelo".length();
        for (Veiculo veiculo : locadora.getFrota()) {
            largura = Math.max(largura, veiculo.getModelo().length());
        }
        String separador = "+--------+-------+" + "-".repeat(largura + 2) + "+------------+------------+";
        String formatoCabecalho = "| %-6s | %-5s | %-" + largura + "s | %10s | %-10s |%n";
        String formatoLinha = "| %6d | %-5s | %-" + largura + "s | %10.2f | %-10s |%n";

        System.out.println(separador);
        System.out.printf(formatoCabecalho, "Codigo", "Tipo", "Modelo", "Diaria R$", "Situacao");
        System.out.println(separador);
        for (Veiculo veiculo : locadora.getFrota()) {
            System.out.printf(formatoLinha,
                    veiculo.getCodigo(),
                    veiculo.getClass().getSimpleName(), // "Carro" ou "Moto"
                    veiculo.getModelo(),
                    veiculo.getValorDiaria(),
                    veiculo.isDisponivel() ? "Disponivel" : "Alugado");
        }
        System.out.println(separador);
    }

    private void consultarOrcamento() {
        Veiculo veiculo = pedirVeiculo();
        if (veiculo == null) {
            return;
        }
        int dias = lerInteiro("Quantidade de dias: ");
        // Mesma chamada para qualquer veiculo: cada objeto aplica a sua formula
        System.out.printf("Orcamento %s (%d dias): R$ %.2f%n",
                veiculo.getModelo(), dias, veiculo.calcularValor(dias));
        if (dias <= 0) {
            System.out.println("Obs.: dias menores ou iguais a zero resultam em valor 0, sem taxa.");
        }
    }

    // Alugar: periodo (dias ou data) -> resumo com datas e total -> confirmacao -> registro
    private void alugar() {
        Veiculo veiculo = pedirVeiculo();
        if (veiculo == null) {
            return;
        }
        if (!veiculo.isDisponivel()) {
            System.out.println(veiculo.getModelo() + " ja esta alugado.");
            return;
        }

        LocalDate hoje = locadora.hoje();
        System.out.println("Retirada: hoje, " + descreverData(hoje) + ".");
        System.out.println("Informe a quantidade de dias OU a data de devolucao (dd/MM/aaaa).");
        String entrada = lerTexto("Periodo: ");
        int dias = converterEmDias(entrada, hoje);
        if (dias <= 0) {
            System.out.println("Periodo invalido: informe dias maiores que zero ou uma data posterior a hoje.");
            return;
        }
        if (dias > Locadora.PRAZO_MAXIMO_DIAS) {
            System.out.println("O prazo maximo de aluguel e de " + Locadora.PRAZO_MAXIMO_DIAS + " dias.");
            return;
        }

        double total = veiculo.calcularValor(dias);
        double taxa = total - dias * veiculo.getValorDiaria();
        System.out.println("\n------ RESUMO DO ALUGUEL ------");
        linha("Veiculo", veiculo.getModelo() + " (" + veiculo.getClass().getSimpleName() + ")");
        linha("Diaria", moeda(veiculo.getValorDiaria()));
        linha("Retirada", formatarData(hoje));
        linha("Devolucao prevista", formatarData(hoje.plusDays(dias)));
        linha("Dias", String.valueOf(dias));
        linha("Taxa de limpeza", moeda(taxa) + " (uma unica vez)");
        linha("TOTAL", moeda(total));

        String resposta = lerTexto("Confirmar aluguel? (s/n): ");
        if (!resposta.equalsIgnoreCase("s")) {
            System.out.println("Aluguel cancelado. " + veiculo.getModelo() + " continua disponivel.");
            return;
        }

        Aluguel aluguel = locadora.registrarAluguel(veiculo, dias);
        if (aluguel == null) {
            System.out.println("Nao foi possivel registrar o aluguel.");
            return;
        }
        System.out.printf("Aluguel n. %d confirmado: %s de %s ate %s (%d dia(s)). Total: R$ %.2f%n",
                aluguel.getNumero(), veiculo.getModelo(),
                formatarData(aluguel.getDataRetirada()), formatarData(aluguel.getDataPrevista()),
                aluguel.getDias(), aluguel.getValorTotal());
    }

    // Devolver o veiculo hoje conclui o aluguel em andamento e emite o comprovante
    private void concluirAluguel() {
        Veiculo veiculo = pedirVeiculo();
        if (veiculo == null) {
            return;
        }
        Aluguel aluguel = locadora.concluirAluguel(veiculo);
        if (aluguel == null) {
            System.out.println(veiculo.getModelo() + " nao possui aluguel em andamento.");
            return;
        }

        int diasCobrados = aluguel.getDias() + aluguel.getDiasAtraso();
        double taxa = aluguel.getValorTotal() - diasCobrados * aluguel.getValorDiaria();
        System.out.println("------ COMPROVANTE DE ALUGUEL ------");
        linha("Aluguel n.", String.valueOf(aluguel.getNumero()));
        linha("Veiculo", veiculo.getModelo() + " (" + veiculo.getClass().getSimpleName() + ")");
        linha("Diaria", moeda(aluguel.getValorDiaria()));
        linha("Retirada", formatarData(aluguel.getDataRetirada()));
        linha("Devolucao prevista", formatarData(aluguel.getDataPrevista()));
        linha("Devolucao realizada", formatarData(aluguel.getDataDevolucao()));
        linha("Dias contratados", String.valueOf(aluguel.getDias()));
        linha("Dias de atraso", String.valueOf(aluguel.getDiasAtraso()));
        linha("Dias cobrados", String.valueOf(diasCobrados));
        linha("Taxa de limpeza", moeda(taxa) + " (uma unica vez)");
        linha("TOTAL", moeda(aluguel.getValorTotal()));
        linha("Situacao", "Concluido (veiculo devolvido)");
        if (aluguel.getDiasAtraso() > 0) {
            System.out.println("Obs.: os dias de atraso foram cobrados pela diaria combinada.");
        } else if (aluguel.getDataDevolucao().isBefore(aluguel.getDataPrevista())) {
            System.out.println("Obs.: devolucao antecipada; o valor contratado foi mantido.");
        }
        System.out.println("------------------------------------");
    }

    private void alterarDiaria() {
        Veiculo veiculo = pedirVeiculo();
        if (veiculo == null) {
            return;
        }
        double novoValor = lerDecimal("Novo valor da diaria (R$): ");
        if (veiculo.alterarValorDiaria(novoValor)) {
            System.out.printf("Diaria de %s atualizada para R$ %.2f%n",
                    veiculo.getModelo(), veiculo.getValorDiaria());
        } else {
            System.out.printf("Valor invalido: a diaria deve ser maior que zero. Mantida em R$ %.2f%n",
                    veiculo.getValorDiaria());
        }
    }

    // Colunas "Veiculo" e "Situacao" se ajustam ao maior texto.
    // Alugueis em andamento mostram o prazo calculado com a data de hoje.
    private void exibirAlugueis() {
        if (locadora.getAlugueis().isEmpty()) {
            System.out.println("Nenhum aluguel registrado ainda.");
            return;
        }
        LocalDate hoje = locadora.hoje();
        int larguraVeiculo = "Veiculo".length();
        int larguraSituacao = "Situacao".length();
        for (Aluguel aluguel : locadora.getAlugueis()) {
            larguraVeiculo = Math.max(larguraVeiculo, aluguel.getVeiculo().getModelo().length());
            larguraSituacao = Math.max(larguraSituacao, descreverSituacao(aluguel, hoje).length());
        }
        String separador = "+-----+" + "-".repeat(larguraVeiculo + 2)
                + "+------------+------------+------+------------+" + "-".repeat(larguraSituacao + 2) + "+";
        String formatoCabecalho = "| %-3s | %-" + larguraVeiculo + "s | %-10s | %-10s | %4s | %10s | %-"
                + larguraSituacao + "s |%n";
        String formatoLinha = "| %3d | %-" + larguraVeiculo + "s | %-10s | %-10s | %4d | %10.2f | %-"
                + larguraSituacao + "s |%n";

        System.out.println("Hoje: " + descreverData(hoje));
        System.out.println(separador);
        System.out.printf(formatoCabecalho,
                "Num", "Veiculo", "Retirada", "Prevista", "Dias", "Total R$", "Situacao");
        System.out.println(separador);
        for (Aluguel aluguel : locadora.getAlugueis()) {
            System.out.printf(formatoLinha,
                    aluguel.getNumero(),
                    aluguel.getVeiculo().getModelo(),
                    formatarData(aluguel.getDataRetirada()),
                    formatarData(aluguel.getDataPrevista()),
                    aluguel.getDias(),
                    aluguel.getValorTotal(),
                    descreverSituacao(aluguel, hoje));
        }
        System.out.println(separador);
        System.out.printf("Total arrecadado (concluidos): R$ %.2f%n", locadora.getTotalArrecadado());
    }

    // Calendario mensal: marca hoje e os prazos dos alugueis em andamento
    private void exibirCalendario() {
        LocalDate hoje = locadora.hoje();
        String texto = lerTexto("Mes e ano (MM/aaaa) ou Enter para o mes atual: ");
        YearMonth mes;
        if (texto.isEmpty()) {
            mes = YearMonth.from(hoje);
        } else {
            try {
                mes = YearMonth.parse(texto, FORMATO_MES);
            } catch (DateTimeParseException e) {
                System.out.println("Formato invalido. Use MM/aaaa (ex.: 12/2026).");
                return;
            }
        }

        String titulo = MESES[mes.getMonthValue() - 1] + " de " + mes.getYear();
        System.out.println();
        System.out.println(" ".repeat(Math.max(0, (35 - titulo.length()) / 2)) + titulo);
        System.out.println(" Dom  Seg  Ter  Qua  Qui  Sex  Sab ");
        System.out.println("-".repeat(35));

        // Domingo = 0 ... Sabado = 6 (DayOfWeek: segunda = 1 ... domingo = 7)
        int deslocamento = mes.atDay(1).getDayOfWeek().getValue() % 7;
        StringBuilder semana = new StringBuilder("     ".repeat(deslocamento));
        for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
            semana.append(celula(mes.atDay(dia), hoje));
            if ((deslocamento + dia) % 7 == 0) {
                System.out.println(semana);
                semana = new StringBuilder();
            }
        }
        if (semana.length() > 0) {
            System.out.println(semana);
        }
        System.out.println("-".repeat(35));
        System.out.println("[dd] hoje | * dia de aluguel | D devolucao prevista | ! dia de atraso");
        System.out.println("\nHoje: " + descreverData(hoje));

        boolean haEmAndamento = false;
        for (Aluguel aluguel : locadora.getAlugueis()) {
            if (!aluguel.isConcluido()) {
                if (!haEmAndamento) {
                    System.out.println("\nAlugueis em andamento:");
                    haEmAndamento = true;
                }
                System.out.printf("- Aluguel %d, %s: retirada %s, devolucao prevista %s (%s)%n",
                        aluguel.getNumero(), aluguel.getVeiculo().getModelo(),
                        formatarData(aluguel.getDataRetirada()),
                        formatarData(aluguel.getDataPrevista()),
                        descreverPrazo(aluguel, hoje));
            }
        }
        if (!haEmAndamento) {
            System.out.println("\nNenhum aluguel em andamento.");
        }
    }

    // Cadastro: tipo -> codigo -> modelo -> diaria. Qualquer dado invalido cancela o cadastro.
    private void cadastrarVeiculo() {
        System.out.println("Tipo do veiculo: 1 - Carro | 2 - Moto");
        int tipo = lerInteiro("Escolha o tipo: ");
        if (tipo != 1 && tipo != 2) {
            System.out.println("Tipo invalido. Cadastro cancelado.");
            return;
        }

        int codigo = lerInteiro("Codigo do novo veiculo: ");
        if (codigo <= 0) {
            System.out.println("O codigo deve ser maior que zero. Cadastro cancelado.");
            return;
        }
        if (locadora.buscar(codigo) != null) {
            System.out.println("Ja existe um veiculo com o codigo " + codigo + ". Cadastro cancelado.");
            return;
        }

        String modelo = lerTexto("Modelo: ");
        if (modelo.isEmpty()) {
            System.out.println("O modelo nao pode ficar vazio. Cadastro cancelado.");
            return;
        }

        double diaria = lerDecimal("Valor da diaria (R$): ");
        if (diaria <= 0) {
            System.out.println("A diaria deve ser maior que zero. Cadastro cancelado.");
            return;
        }

        Veiculo novo;
        if (tipo == 1) {
            novo = new Carro(codigo, modelo, diaria);
        } else {
            novo = new Moto(codigo, modelo, diaria);
        }

        if (locadora.adicionar(novo)) {
            System.out.println(modelo + " cadastrado com sucesso (disponivel para aluguel).\n");
            exibirTabela();
        } else {
            System.out.println("Nao foi possivel cadastrar o veiculo.");
        }
    }

    // Exclusao: nao permite excluir veiculo alugado e pede confirmacao
    private void excluirVeiculo() {
        Veiculo veiculo = pedirVeiculo();
        if (veiculo == null) {
            return;
        }
        if (!veiculo.isDisponivel()) {
            System.out.println("Nao e possivel excluir " + veiculo.getModelo()
                    + ": ele esta alugado. Conclua o aluguel antes (opcao 4).");
            return;
        }

        String resposta = lerTexto("Excluir " + veiculo.getModelo()
                + " (codigo " + veiculo.getCodigo() + ") da frota? (s/n): ");
        if (!resposta.equalsIgnoreCase("s")) {
            System.out.println("Exclusao cancelada.");
            return;
        }

        if (locadora.remover(veiculo)) {
            System.out.println(veiculo.getModelo() + " excluido da frota.\n");
            exibirTabela();
        } else {
            System.out.println("Nao foi possivel excluir o veiculo.");
        }
    }

    // ---------- Calendario e datas ----------

    // Uma celula do calendario tem 5 caracteres: "[20]*" (hoje) ou " 21 *" (dia comum)
    private String celula(LocalDate dia, LocalDate hoje) {
        boolean ehHoje = dia.equals(hoje);
        return (ehHoje ? "[" : " ") + String.format("%2d", dia.getDayOfMonth())
                + (ehHoje ? "]" : " ") + marcadorDoDia(dia, hoje);
    }

    // Prioridade: atraso (!) > devolucao prevista (D) > dia de aluguel (*)
    private char marcadorDoDia(LocalDate dia, LocalDate hoje) {
        boolean atraso = false;
        boolean devolucao = false;
        boolean uso = false;
        for (Aluguel aluguel : locadora.getAlugueis()) {
            if (aluguel.isConcluido()) {
                continue;
            }
            if (dia.isAfter(aluguel.getDataPrevista()) && !dia.isAfter(hoje)) {
                atraso = true;
            } else if (dia.equals(aluguel.getDataPrevista())) {
                devolucao = true;
            } else if (!dia.isBefore(aluguel.getDataRetirada()) && dia.isBefore(aluguel.getDataPrevista())) {
                uso = true;
            }
        }
        if (atraso) {
            return '!';
        }
        if (devolucao) {
            return 'D';
        }
        if (uso) {
            return '*';
        }
        return ' ';
    }

    // Aceita "5" (dias) ou "27/11/2026" (data de devolucao). Retorna 0 se for invalido.
    private int converterEmDias(String entrada, LocalDate hoje) {
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            // nao e um numero: tenta como data
        }
        try {
            LocalDate devolucao = LocalDate.parse(entrada, FORMATO_DATA);
            long dias = ChronoUnit.DAYS.between(hoje, devolucao);
            return (int) Math.min(dias, Integer.MAX_VALUE);
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    private String formatarData(LocalDate data) {
        return FORMATO_DATA.format(data);
    }

    private String descreverData(LocalDate data) {
        return DIAS_SEMANA[data.getDayOfWeek().getValue() - 1] + ", " + formatarData(data);
    }

    private String descreverPrazo(Aluguel aluguel, LocalDate hoje) {
        long restantes = aluguel.diasRestantes(hoje);
        if (restantes > 0) {
            return "faltam " + restantes + " dia(s)";
        }
        if (restantes == 0) {
            return "vence hoje";
        }
        return "atrasado " + (-restantes) + " dia(s)";
    }

    private String descreverSituacao(Aluguel aluguel, LocalDate hoje) {
        if (aluguel.isConcluido()) {
            if (aluguel.getDiasAtraso() > 0) {
                return "Concluido (atraso " + aluguel.getDiasAtraso() + " dia(s))";
            }
            return "Concluido";
        }
        return "Em andamento: " + descreverPrazo(aluguel, hoje);
    }

    // ---------- Apoio ----------

    private String moeda(double valor) {
        return String.format("R$ %.2f", valor);
    }

    private void linha(String rotulo, String valor) {
        System.out.printf("%-21s %s%n", rotulo + ":", valor);
    }

    // Pede o codigo e busca o veiculo; retorna null (com aviso) se nao existir
    private Veiculo pedirVeiculo() {
        int codigo = lerInteiro("Codigo do veiculo: ");
        Veiculo veiculo = locadora.buscar(codigo);
        if (veiculo == null) {
            System.out.println("Veiculo com codigo " + codigo + " nao encontrado.");
        }
        return veiculo;
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    // Repete a pergunta ate o usuario digitar um inteiro valido
    private int lerInteiro(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero inteiro valido.");
            }
        }
    }

    // Repete a pergunta ate o usuario digitar um numero valido (aceita 120,50 ou 120.50)
    private double lerDecimal(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem).replace(',', '.');
            try {
                double valor = Double.parseDouble(texto);
                if (!Double.isNaN(valor) && !Double.isInfinite(valor)) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // cai na mensagem abaixo
            }
            System.out.println("Digite um numero valido (ex.: 120 ou 120,50).");
        }
    }
}
