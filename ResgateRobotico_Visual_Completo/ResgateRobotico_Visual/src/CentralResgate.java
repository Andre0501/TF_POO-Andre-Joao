import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

public class CentralResgate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private Map<String, Robo> robos;
    private Map<String, AreaOperacao> areas;
    private Map<String, Missao> missoes;
    private Queue<Missao> missoesPendentes;
    private List<String> historicoOperacoes;

    public CentralResgate(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da central deve ser preenchido.");
        }
        this.nome = nome.trim();
        robos = new HashMap<>();
        areas = new HashMap<>();
        missoes = new HashMap<>();
        missoesPendentes = new LinkedList<>();
        historicoOperacoes = new ArrayList<>();
    }

    public void cadastrarRobo(Robo robo) {
        if (robo == null || robos.containsKey(robo.getId())) {
            throw new IllegalArgumentException("Robo nulo ou identificador ja cadastrado.");
        }
        if (robo.getMissaoAtual() != null || robo.getMissoesRealizadas() != 0) {
            throw new IllegalArgumentException("Cadastre um robo sem operacoes anteriores.");
        }
        robos.put(robo.getId(), robo);
        registrarHistorico("Robo cadastrado: " + robo.getId());
    }

    public void cadastrarArea(AreaOperacao area) {
        if (area == null || areas.containsKey(area.getId())) {
            throw new IllegalArgumentException("Area nula ou identificador ja cadastrado.");
        }
        areas.put(area.getId(), area);
        registrarHistorico("Area cadastrada: " + area.getId());
    }

    public void registrarMissao(Missao missao) {
        if (missao == null || missoes.containsKey(missao.getId())) {
            throw new IllegalArgumentException("Missao nula ou identificador ja cadastrado.");
        }
        // A missao deve apontar para o mesmo objeto que foi cadastrado na central.
        if (areas.get(missao.getArea().getId()) != missao.getArea()) {
            throw new IllegalArgumentException("A area da missao deve estar cadastrada nesta central.");
        }
        if (missao.getStatus() != StatusMissao.PENDENTE || missao.getRoboResponsavel() != null) {
            throw new IllegalArgumentException("Registre uma missao nova, pendente e sem robo.");
        }
        missoes.put(missao.getId(), missao);
        missoesPendentes.offer(missao);
        registrarHistorico("Missao registrada na fila: " + missao.getId());
    }

    public Robo buscarRobo(String id) {
        Robo robo = robos.get(id);
        if (robo == null) {
            throw new IllegalArgumentException("Robo nao encontrado: " + id);
        }
        return robo;
    }

    public AreaOperacao buscarArea(String id) {
        AreaOperacao area = areas.get(id);
        if (area == null) {
            throw new IllegalArgumentException("Area nao encontrada: " + id);
        }
        return area;
    }

    public Missao buscarMissao(String id) {
        Missao missao = missoes.get(id);
        if (missao == null) {
            throw new IllegalArgumentException("Missao nao encontrada: " + id);
        }
        return missao;
    }

    public List<Robo> listarRobos() {
        List<Robo> lista = new ArrayList<>(robos.values());
        lista.sort((r1, r2) -> r1.getId().compareTo(r2.getId()));
        return lista;
    }

    public List<AreaOperacao> listarAreas() {
        List<AreaOperacao> lista = new ArrayList<>(areas.values());
        lista.sort((a1, a2) -> a1.getId().compareTo(a2.getId()));
        return lista;
    }

    public List<Missao> listarMissoes() {
        List<Missao> lista = new ArrayList<>(missoes.values());
        lista.sort((m1, m2) -> m1.getId().compareTo(m2.getId()));
        return lista;
    }

    public List<Robo> listarRobosDisponiveis() {
        return listarRobos().stream()
                .filter(r -> r.verificarDisponibilidade())
                .collect(Collectors.toList());
    }

    public List<Missao> listarMissoesPendentes() {
        return new ArrayList<>(missoesPendentes);
    }

    public List<Missao> consultarHistoricoMissoes() {
        return listarMissoes().stream()
                .filter(m -> m.getStatus() == StatusMissao.CONCLUIDA
                        || m.getStatus() == StatusMissao.CANCELADA)
                .collect(Collectors.toList());
    }

    public void atribuirRoboMissao(String idRobo, String idMissao) {
        Missao missao = buscarMissao(idMissao);
        missao.atribuirRobo(buscarRobo(idRobo));
        registrarHistorico("Robo " + idRobo + " reservado para " + idMissao);
    }

    public void removerAtribuicao(String idMissao) {
        buscarMissao(idMissao).removerRobo();
        registrarHistorico("Atribuicao retirada de " + idMissao);
    }

    public void iniciarMissao(String idMissao) {
        Missao missao = buscarMissao(idMissao);
        missao.iniciar();
        // A fila so perde a missao depois de o inicio ter sido validado.
        missoesPendentes.remove(missao);
        registrarHistorico("Missao iniciada: " + idMissao);
    }

    public void executarMissao(String idMissao) {
        Missao missao = buscarMissao(idMissao);
        if (missao.getStatus() == StatusMissao.PENDENTE) {
            iniciarMissao(idMissao);
        }
        if (missao.getStatus() != StatusMissao.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente uma missao pendente atribuida ou em andamento pode ser executada.");
        }
        // Polimorfismo: a variavel e Robo, mas o metodo depende da subclasse real.
        Robo robo = missao.getRoboResponsavel();
        robo.executarMissao(missao);
        registrarHistorico("Missao concluida: " + idMissao + " | " + missao.getResultado());
    }

    public void cancelarMissao(String idMissao) {
        Missao missao = buscarMissao(idMissao);
        missao.cancelar();
        missoesPendentes.remove(missao);
        registrarHistorico("Missao cancelada: " + idMissao);
    }

    public void recarregarRobo(String idRobo) {
        Robo robo = buscarRobo(idRobo);
        robo.recarregar();
        robo.enviarMensagem("Recarga concluida. Bateria: 100%.");
        registrarHistorico("Robo recarregado: " + idRobo);
    }

    public void iniciarManutencao(String idRobo) {
        buscarRobo(idRobo).entrarEmManutencao();
        registrarHistorico("Inicio de manutencao: " + idRobo);
    }

    public void finalizarManutencao(String idRobo) {
        buscarRobo(idRobo).finalizarManutencao();
        registrarHistorico("Fim de manutencao: " + idRobo);
    }

    public void atualizarRiscoArea(String idArea, NivelRisco novoRisco) {
        buscarArea(idArea).atualizarNivelRisco(novoRisco);
        registrarHistorico("Risco da area " + idArea + " atualizado para " + novoRisco);
    }

    public void enviarInstrucao(String idRobo, String mensagem) {
        buscarRobo(idRobo).receberMensagem(mensagem);
        registrarHistorico("Instrucao enviada ao robo " + idRobo + ": " + mensagem);
    }

    public void receberInformacao(String idRobo, String mensagem) {
        buscarRobo(idRobo).enviarMensagem(mensagem);
        registrarHistorico("Informacao do robo " + idRobo + ": " + mensagem);
    }

    // Inovacao: compara a autonomia das alternativas, sem atribuir o robo automaticamente.
    public List<Robo> recomendarRobos(String idMissao) {
        Missao missao = buscarMissao(idMissao);
        if (missao.getStatus() != StatusMissao.PENDENTE || missao.getRoboResponsavel() != null) {
            throw new IllegalStateException("A recomendacao exige missao pendente e sem robo.");
        }
        List<Robo> candidatos = listarRobosDisponiveis().stream()
                .filter(r -> r.podeExecutar(missao))
                .filter(r -> r.consultarBateria() >= r.calcularConsumo(missao))
                .collect(Collectors.toList());

        candidatos.sort((r1, r2) -> {
            int comparacao = Integer.compare(r1.calcularConsumo(missao), r2.calcularConsumo(missao));
            if (comparacao == 0) {
                comparacao = Integer.compare(r2.consultarBateria(), r1.consultarBateria());
            }
            if (comparacao == 0) {
                comparacao = r1.getId().compareTo(r2.getId());
            }
            return comparacao;
        });
        return candidatos;
    }

    public String simularAutonomia(String idMissao) {
        Missao missao = buscarMissao(idMissao);
        List<Robo> candidatos = recomendarRobos(idMissao);
        String texto = "SIMULACAO DE AUTONOMIA | Missao " + idMissao + "\n";
        if (candidatos.isEmpty()) {
            return texto + "Nenhum robo disponivel, compativel e com bateria suficiente.\n";
        }
        texto = texto + "Ordem: menor consumo; maior bateria; identificador.\n";
        for (Robo robo : candidatos) {
            int consumo = robo.calcularConsumo(missao);
            texto = texto + robo.getId() + " | bateria " + robo.consultarBateria()
                    + "% | consumo " + consumo + " pontos | restante "
                    + (robo.consultarBateria() - consumo) + "%\n";
        }
        return texto + "Sugestao: " + candidatos.get(0).getId() + ". A atribuicao continua sendo uma decisao da central.\n";
    }

    public String atenderProximaPendente() {
        Missao proxima = null;
        // FIFO: sempre escolhe a primeira que ainda nao recebeu um robo.
        for (Missao missao : missoesPendentes) {
            if (missao.getRoboResponsavel() == null) {
                proxima = missao;
                break;
            }
        }
        if (proxima == null) {
            return "Nao ha missao pendente sem atribuicao.";
        }
        List<Robo> candidatos = recomendarRobos(proxima.getId());
        if (candidatos.isEmpty()) {
            return "Missao " + proxima.getId() + " permanece na fila: nenhum robo adequado esta disponivel.";
        }
        Robo robo = candidatos.get(0);
        atribuirRoboMissao(robo.getId(), proxima.getId());
        return "Robo " + robo.getId() + " reservado para a primeira missao sem atribuicao: " + proxima.getId();
    }

    public Relatorio gerarRelatorioGeral() {
        return new Relatorio(this);
    }

    private void registrarHistorico(String mensagem) {
        historicoOperacoes.add(LocalDateTime.now() + " | " + mensagem);
    }

    public String getNome() { return nome; }

    public List<String> getHistoricoOperacoes() {
        return new ArrayList<>(historicoOperacoes);
    }
}
