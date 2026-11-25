// Grupo: [nomes dos integrantes]

/**
 * Classe abstrata: representa o que Carro e Moto tem em comum.
 * Guarda os dados (encapsulados) e as regras de aluguel/devolucao.
 * Como implementa Tarifavel e e abstrata, deixa calcularValor
 * para as subclasses definirem.
 */
public abstract class Veiculo implements Tarifavel {

    // Encapsulamento: atributos privados
    private final int codigo;
    private final String modelo;
    private double valorDiaria;
    private boolean disponivel;

    public Veiculo(int codigo, String modelo, double valorDiaria) {
        this.codigo = codigo;
        this.modelo = modelo;
        this.valorDiaria = valorDiaria;
        this.disponivel = true; // todo veiculo comeca disponivel
    }

    // Apenas getters (sem setters para codigo, modelo e disponibilidade)
    public int getCodigo() {
        return codigo;
    }

    public String getModelo() {
        return modelo;
    }

    public double getValorDiaria() {
        return valorDiaria;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    // Aluga se estiver disponivel; caso contrario retorna false
    public boolean alugar() {
        if (!disponivel) {
            return false;
        }
        disponivel = false;
        return true;
    }

    // Devolve se estiver alugado; caso contrario retorna false
    public boolean devolver() {
        if (disponivel) {
            return false;
        }
        disponivel = true;
        return true;
    }

    // Atualiza apenas se o novo valor for maior que zero
    public boolean alterarValorDiaria(double novoValor) {
        if (novoValor > 0) {
            this.valorDiaria = novoValor;
            return true;
        }
        return false;
    }

    // Cada subclasse define a sua formula (polimorfismo)
    @Override
    public abstract double calcularValor(int dias);
}
