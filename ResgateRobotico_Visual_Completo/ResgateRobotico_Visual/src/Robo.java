import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Robo implements Comunicavel, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nome;
    private int nivelBateria;
    private StatusRobo status;
    private int missoesRealizadas;
    private Missao missaoAtual;
    private List<String> historicoMensagens;

    public Robo(String id, String nome, int nivelBateria) {
        if (id == null || id.trim().isEmpty() || nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Id e nome do robo devem ser preenchidos.");
        }
        if (nivelBateria < 0 || nivelBateria > 100) {
            throw new IllegalArgumentException("A bateria deve estar entre 0 e 100.");
        }
        this.id = id.trim();
        this.nome = nome.trim();
        this.nivelBateria = nivelBateria;
        this.status = StatusRobo.DISPONIVEL;
        if (nivelBateria == 0) {
            this.status = StatusRobo.SEM_BATERIA;
        }
        this.missoesRealizadas = 0;
        this.historicoMensagens = new ArrayList<>();
    }

    public boolean verificarDisponibilidade() {
        return status == StatusRobo.DISPONIVEL && missaoAtual == null && nivelBateria > 0;
    }

    public int consultarBateria() { return nivelBateria; }

    public void recarregar() {
        if (missaoAtual != null) {
            throw new IllegalStateException("Retire a atribuicao ou finalize a missao antes de recarregar.");
        }
        nivelBateria = 100;
        // Recarregar nao encerra uma manutencao.
        if (status != StatusRobo.EM_MANUTENCAO) {
            status = StatusRobo.DISPONIVEL;
        }
    }

    public void entrarEmManutencao() {
        if (missaoAtual != null || status == StatusRobo.EM_MANUTENCAO) {
            throw new IllegalStateException("Robo ocupado ou ja em manutencao.");
        }
        status = StatusRobo.EM_MANUTENCAO;
    }

    public void finalizarManutencao() {
        if (status != StatusRobo.EM_MANUTENCAO) {
            throw new IllegalStateException("O robo nao esta em manutencao.");
        }
        status = StatusRobo.DISPONIVEL;
        if (nivelBateria == 0) {
            status = StatusRobo.SEM_BATERIA;
        }
    }

    // Estes metodos sao usados por Missao para manter o vinculo nos dois objetos.
    void reservar(Missao missao) {
        if (!verificarDisponibilidade()) {
            throw new IllegalStateException("O robo nao esta disponivel.");
        }
        if (!podeExecutar(missao)) {
            throw new IllegalArgumentException("Robo incompativel com o tipo, terreno ou carga da missao.");
        }
        if (nivelBateria < calcularConsumo(missao)) {
            throw new IllegalStateException("Bateria insuficiente para a missao.");
        }
        missaoAtual = missao;
        status = StatusRobo.RESERVADO;
    }

    void iniciarOperacao(Missao missao, int consumo) {
        if (missaoAtual != missao || status != StatusRobo.RESERVADO) {
            throw new IllegalStateException("O robo nao esta reservado para esta missao.");
        }
        if (!podeExecutar(missao) || consumo <= 0 || nivelBateria < consumo) {
            throw new IllegalStateException("Compatibilidade ou bateria insuficiente no inicio da missao.");
        }
        status = StatusRobo.EM_MISSAO;
        receberMensagem("Iniciar missao " + missao.getId() + ". Consumo previsto: " + consumo + " pontos.");
    }

    protected void validarMissaoEmAndamento(Missao missao) {
        if (missao == null || missaoAtual != missao || status != StatusRobo.EM_MISSAO
                || missao.getStatus() != StatusMissao.EM_ANDAMENTO) {
            throw new IllegalStateException("O robo so pode executar sua propria missao em andamento.");
        }
    }

    void finalizarOperacao(Missao missao, boolean concluida) {
        if (missaoAtual != missao) {
            throw new IllegalStateException("Esta missao nao pertence ao robo.");
        }
        if (concluida) {
            validarMissaoEmAndamento(missao);
        }
        // A simulacao nao tem etapas parciais: cancelar depois de iniciar usa o consumo previsto.
        if (status == StatusRobo.EM_MISSAO) {
            int consumo = missao.getConsumoPrevisto();
            if (consumo <= 0 || consumo > nivelBateria) {
                throw new IllegalStateException("Consumo de bateria inconsistente.");
            }
            nivelBateria = nivelBateria - consumo;
        }
        if (concluida) {
            missoesRealizadas++;
        }
        missaoAtual = null;
        status = StatusRobo.DISPONIVEL;
        if (nivelBateria == 0) {
            status = StatusRobo.SEM_BATERIA;
        }
    }

    protected int adicionalRisco(Missao missao) {
        if (missao.getArea().getNivelRisco() == NivelRisco.ALTO) {
            return 10;
        }
        if (missao.getArea().getNivelRisco() == NivelRisco.MEDIO) {
            return 5;
        }
        return 0;
    }

    public abstract boolean podeExecutar(Missao missao);
    public abstract int calcularConsumo(Missao missao);
    public abstract void executarMissao(Missao missao);

    @Override
    public void enviarMensagem(String mensagem) {
        registrarMensagem("ROBO " + id + " -> CENTRAL", mensagem);
    }

    @Override
    public void receberMensagem(String mensagem) {
        registrarMensagem("CENTRAL -> ROBO " + id, mensagem);
    }

    private void registrarMensagem(String sentido, String mensagem) {
        if (mensagem == null || mensagem.trim().isEmpty()) {
            throw new IllegalArgumentException("A mensagem deve ser preenchida.");
        }
        String registro = LocalDateTime.now() + " | " + sentido + " | " + mensagem.trim();
        historicoMensagens.add(registro);
        System.out.println(registro);
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public int getNivelBateria() { return nivelBateria; }
    public StatusRobo getStatus() { return status; }
    public int getMissoesRealizadas() { return missoesRealizadas; }
    public Missao getMissaoAtual() { return missaoAtual; }

    public List<String> getHistoricoMensagens() {
        // A copia impede que quem recebeu a lista apague o historico original.
        return new ArrayList<>(historicoMensagens);
    }

    public String exibirResumo() {
        return id + " | " + nome + " | bateria " + nivelBateria + "% | " + status
                + " | concluidas " + missoesRealizadas;
    }

    @Override
    public String toString() { return exibirResumo(); }
}
