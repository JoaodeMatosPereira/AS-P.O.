// Grupo: [nomes dos integrantes]

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Registro de um aluguel: guarda o veiculo, os dias contratados, as datas
 * (retirada, devolucao prevista e devolucao realizada) e o valor.
 * O valor combinado e calculado na contratacao: se a diaria do veiculo mudar
 * depois, este aluguel continua com a diaria combinada.
 */
public class Aluguel {

    private final int numero;
    private final Veiculo veiculo;
    private final int dias;                  // dias contratados
    private final double valorDiaria;        // diaria combinada
    private final LocalDate dataRetirada;
    private final LocalDate dataPrevista;    // data prevista para devolver
    private final double valorContratado;
    private LocalDate dataDevolucao;         // null enquanto em andamento
    private int diasAtraso;
    private double valorTotal;               // contratado; vira o valor final ao concluir
    private boolean concluido;

    public Aluguel(int numero, Veiculo veiculo, int dias, LocalDate dataRetirada) {
        this.numero = numero;
        this.veiculo = veiculo;
        this.dias = dias;
        this.valorDiaria = veiculo.getValorDiaria();
        this.dataRetirada = dataRetirada;
        this.dataPrevista = dataRetirada.plusDays(dias);
        // Polimorfismo: cada veiculo aplica a sua propria formula
        this.valorContratado = veiculo.calcularValor(dias);
        this.valorTotal = valorContratado;
        this.concluido = false; // todo aluguel comeca em andamento
    }

    public int getNumero() {
        return numero;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public int getDias() {
        return dias;
    }

    public double getValorDiaria() {
        return valorDiaria;
    }

    public LocalDate getDataRetirada() {
        return dataRetirada;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public double getValorContratado() {
        return valorContratado;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public boolean isConcluido() {
        return concluido;
    }

    // Contagem em tempo real: quantos dias se passaram desde a retirada
    public long diasDecorridos(LocalDate hoje) {
        return ChronoUnit.DAYS.between(dataRetirada, hoje);
    }

    // Contagem em tempo real: quantos dias faltam para a devolucao prevista
    // (zero = vence hoje; negativo = atrasado)
    public long diasRestantes(LocalDate hoje) {
        return ChronoUnit.DAYS.between(hoje, dataPrevista);
    }

    // Sem "public": so a Locadora conclui, junto com a devolucao do veiculo.
    // Atraso: cada dia depois da data prevista e cobrado pela diaria combinada.
    // Devolucao antecipada: o valor contratado e mantido.
    void concluir(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
        this.diasAtraso = (int) Math.max(0, ChronoUnit.DAYS.between(dataPrevista, dataDevolucao));
        this.valorTotal = valorContratado + diasAtraso * valorDiaria;
        this.concluido = true;
    }
}
