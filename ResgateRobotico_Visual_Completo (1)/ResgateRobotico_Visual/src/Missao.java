import java.io.Serializable;

public class Missao implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String descricao;
    private TipoMissao tipo;
    private AreaOperacao area;
    private PrioridadeMissao prioridade;
    private StatusMissao status;
    private Robo roboResponsavel;
    private String resultado;
    private int pesoSuprimentos;
    private int consumoPrevisto;
    private int consumoAplicado;

    public Missao(String id, String descricao, TipoMissao tipo, AreaOperacao area,
            PrioridadeMissao prioridade, int pesoSuprimentos) {
        if (id == null || id.trim().isEmpty() || descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Id e descricao da missao devem ser preenchidos.");
        }
        if (tipo == null || area == null || prioridade == null) {
            throw new IllegalArgumentException("Tipo, area e prioridade devem ser informados.");
        }
        if ((tipo == TipoMissao.TRANSPORTE_SUPRIMENTOS && pesoSuprimentos <= 0)
                || (tipo != TipoMissao.TRANSPORTE_SUPRIMENTOS && pesoSuprimentos != 0)) {
            throw new IllegalArgumentException("Transporte exige carga positiva; outras missoes exigem carga zero.");
        }
        this.id = id.trim();
        this.descricao = descricao.trim();
        this.tipo = tipo;
        this.area = area;
        this.prioridade = prioridade;
        this.pesoSuprimentos = pesoSuprimentos;
        this.status = StatusMissao.PENDENTE;
        this.resultado = "Ainda nao executada.";
    }

    public void atribuirRobo(Robo robo) {
        if (status != StatusMissao.PENDENTE || roboResponsavel != null) {
            throw new IllegalStateException("A missao deve estar pendente e sem robo atribuido.");
        }
        if (robo == null) {
            throw new IllegalArgumentException("Informe um robo.");
        }
        // A reserva valida disponibilidade, compatibilidade e bateria antes de alterar o vinculo.
        robo.reservar(this);
        roboResponsavel = robo;
        robo.receberMensagem("Reservado para a missao " + id + ".");
    }

    public void removerRobo() {
        if (status != StatusMissao.PENDENTE || roboResponsavel == null) {
            throw new IllegalStateException("So e possivel retirar o robo de uma missao pendente atribuida.");
        }
        roboResponsavel.finalizarOperacao(this, false);
        roboResponsavel.receberMensagem("Reserva da missao " + id + " retirada.");
        roboResponsavel = null;
    }

    public void iniciar() {
        if (status != StatusMissao.PENDENTE || roboResponsavel == null) {
            throw new IllegalStateException("A missao deve estar pendente e ter um robo atribuido.");
        }
        int consumo = roboResponsavel.calcularConsumo(this);
        // Revalida: o risco da area pode ter mudado depois da atribuicao.
        roboResponsavel.iniciarOperacao(this, consumo);
        consumoPrevisto = consumo;
        status = StatusMissao.EM_ANDAMENTO;
    }

    public void concluir(String resultado) {
        if (status != StatusMissao.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente uma missao em andamento pode ser concluida.");
        }
        if (resultado == null || resultado.trim().isEmpty()) {
            throw new IllegalArgumentException("O resultado deve ser informado.");
        }
        roboResponsavel.finalizarOperacao(this, true);
        this.resultado = resultado.trim();
        consumoAplicado = consumoPrevisto;
        status = StatusMissao.CONCLUIDA;
        roboResponsavel.enviarMensagem("Missao " + id + " concluida. " + this.resultado);
    }

    public void cancelar() {
        if (status == StatusMissao.CONCLUIDA || status == StatusMissao.CANCELADA) {
            throw new IllegalStateException("Missao concluida ou cancelada nao pode ser cancelada novamente.");
        }
        boolean iniciada = status == StatusMissao.EM_ANDAMENTO;
        if (roboResponsavel != null) {
            roboResponsavel.finalizarOperacao(this, false);
            roboResponsavel.receberMensagem("Missao " + id + " cancelada.");
        }
        if (iniciada) {
            consumoAplicado = consumoPrevisto;
            resultado = "Cancelada apos o inicio; consumo previsto aplicado.";
        } else {
            resultado = "Cancelada antes do inicio; sem consumo de bateria.";
        }
        status = StatusMissao.CANCELADA;
    }

    public String getId() { return id; }
    public String getDescricao() { return descricao; }
    public TipoMissao getTipo() { return tipo; }
    public AreaOperacao getArea() { return area; }
    public PrioridadeMissao getPrioridade() { return prioridade; }
    public StatusMissao getStatus() { return status; }
    public Robo getRoboResponsavel() { return roboResponsavel; }
    public String getResultado() { return resultado; }
    public int getPesoSuprimentos() { return pesoSuprimentos; }
    public int getConsumoPrevisto() { return consumoPrevisto; }
    public int getConsumoAplicado() { return consumoAplicado; }

    public String exibirResumo() {
        String idRobo = "sem robo";
        if (roboResponsavel != null) {
            idRobo = roboResponsavel.getId();
        }
        return id + " | " + tipo + " | area " + area.getId() + " | " + prioridade + " | " + status
                + " | " + idRobo + " | carga " + pesoSuprimentos + " kg | consumo " + consumoAplicado
                + " pontos | " + descricao + " | " + resultado;
    }

    @Override
    public String toString() { return exibirResumo(); }
}
