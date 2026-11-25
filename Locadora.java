// Grupo: [nomes dos integrantes]

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Guarda a tabela (frota) de veiculos e o registro dos alugueis.
 * A lista da frota e do tipo Veiculo: aceita Carro e Moto sem saber qual e qual.
 * A data de hoje vem do relogio do sistema (tempo real).
 */
public class Locadora {

    public static final int PRAZO_MAXIMO_DIAS = 365;

    private final List<Veiculo> frota = new ArrayList<>();
    private final List<Aluguel> alugueis = new ArrayList<>();
    private final Clock relogio;

    public Locadora() {
        this(Clock.systemDefaultZone()); // tempo real
    }

    // Permite informar outro relogio (util para testar datas sem esperar dias)
    public Locadora(Clock relogio) {
        this.relogio = relogio;
        // Dados fixos: frota inicial da locadora
        adicionar(new Carro(1, "Onix", 100));
        adicionar(new Moto(2, "Biz", 50));
        adicionar(new Carro(3, "Argo", 120));
        adicionar(new Moto(4, "CG 160", 60));
        adicionar(new Carro(5, "Kwid", 90));
        adicionar(new Moto(6, "PCX", 80));
    }

    // Data de hoje, lida do relogio no momento da chamada
    public LocalDate hoje() {
        return LocalDate.now(relogio);
    }

    // Retorna false se o veiculo for nulo ou se o codigo ja existir
    public boolean adicionar(Veiculo veiculo) {
        if (veiculo == null || buscar(veiculo.getCodigo()) != null) {
            return false;
        }
        frota.add(veiculo);
        return true;
    }

    // Remove o veiculo da frota.
    // Retorna false se ele nao estiver na frota ou se estiver alugado.
    public boolean remover(Veiculo veiculo) {
        if (veiculo == null || !veiculo.isDisponivel()) {
            return false;
        }
        return frota.remove(veiculo);
    }

    // Retorna o veiculo com o codigo informado ou null se nao existir
    public Veiculo buscar(int codigo) {
        for (Veiculo veiculo : frota) {
            if (veiculo.getCodigo() == codigo) {
                return veiculo;
            }
        }
        return null;
    }

    // Aluga o veiculo a partir de hoje e registra o aluguel.
    // Retorna null se o veiculo for nulo, os dias forem <= 0 ou acima do prazo maximo,
    // ou se ele ja estiver alugado.
    public Aluguel registrarAluguel(Veiculo veiculo, int dias) {
        if (veiculo == null || dias <= 0 || dias > PRAZO_MAXIMO_DIAS || !veiculo.alugar()) {
            return null;
        }
        Aluguel aluguel = new Aluguel(alugueis.size() + 1, veiculo, dias, hoje());
        alugueis.add(aluguel);
        return aluguel;
    }

    // Devolve o veiculo hoje e conclui o aluguel em andamento dele.
    // Retorna null se nao houver aluguel em andamento para esse veiculo.
    public Aluguel concluirAluguel(Veiculo veiculo) {
        for (Aluguel aluguel : alugueis) {
            if (aluguel.getVeiculo() == veiculo && !aluguel.isConcluido()) {
                if (veiculo.devolver()) {
                    aluguel.concluir(hoje());
                    return aluguel;
                }
            }
        }
        return null;
    }

    // Soma dos valores dos alugueis ja concluidos
    public double getTotalArrecadado() {
        double total = 0;
        for (Aluguel aluguel : alugueis) {
            if (aluguel.isConcluido()) {
                total += aluguel.getValorTotal();
            }
        }
        return total;
    }

    // Listas somente para leitura: quem usa nao consegue alterar por fora
    public List<Veiculo> getFrota() {
        return Collections.unmodifiableList(frota);
    }

    public List<Aluguel> getAlugueis() {
        return Collections.unmodifiableList(alugueis);
    }
}
