// Grupo: [nomes dos integrantes]

/**
 * Contrato: quem implementa Tarifavel se compromete a calcular um valor
 * para uma quantidade de dias. A interface so declara o metodo;
 * cada classe define a sua propria formula.
 */
public interface Tarifavel {
    double calcularValor(int dias);
}
