import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

public class Relatorio {
    private LocalDateTime dataGeracao;
    private String nomeCentral;
    private int totalRobos;
    private int totalMissoes;
    private long missoesConcluidas;
    private long missoesPendentes;
    private long missoesEmAndamento;
    private long missoesCanceladas;
    private long aguardandoAtribuicao;
    private int robosDisponiveis;
    private int totalMissoesRealizadas;
    private double mediaBateria;
    private String detalhes;

    public Relatorio(CentralResgate central) {
        if (central == null) {
            throw new IllegalArgumentException("Informe a central para gerar o relatorio.");
        }
        dataGeracao = LocalDateTime.now();
        nomeCentral = central.getNome();
        List<Robo> robos = central.listarRobos();
        List<Missao> missoes = central.listarMissoes();
        totalRobos = robos.size();
        totalMissoes = missoes.size();
        missoesConcluidas = missoes.stream().filter(m -> m.getStatus() == StatusMissao.CONCLUIDA).count();
        missoesPendentes = missoes.stream().filter(m -> m.getStatus() == StatusMissao.PENDENTE).count();
        missoesEmAndamento = missoes.stream().filter(m -> m.getStatus() == StatusMissao.EM_ANDAMENTO).count();
        missoesCanceladas = missoes.stream().filter(m -> m.getStatus() == StatusMissao.CANCELADA).count();
        aguardandoAtribuicao = missoes.stream()
                .filter(m -> m.getStatus() == StatusMissao.PENDENTE && m.getRoboResponsavel() == null)
                .count();
        robosDisponiveis = central.listarRobosDisponiveis().size();
        totalMissoesRealizadas = robos.stream().mapToInt(r -> r.getMissoesRealizadas()).sum();
        mediaBateria = 0;
        if (!robos.isEmpty()) {
            mediaBateria = robos.stream().mapToInt(r -> r.consultarBateria()).average().getAsDouble();
        }
        // O texto e montado agora: o relatorio representa o instante de geracao.
        detalhes = "\nROBOS\n";
        for (Robo robo : robos) {
            detalhes = detalhes + robo.exibirResumo() + "\n";
        }
        detalhes = detalhes + "\nAREAS\n";
        for (AreaOperacao area : central.listarAreas()) {
            detalhes = detalhes + area.exibirInformacoes() + "\n";
        }
        detalhes = detalhes + "\nMISSOES E RESULTADOS\n";
        for (Missao missao : missoes) {
            detalhes = detalhes + missao.exibirResumo() + "\n";
        }
        detalhes = detalhes + "\nFILA PENDENTE EM ORDEM DE REGISTRO\n";
        for (Missao missao : central.listarMissoesPendentes()) {
            detalhes = detalhes + missao.getId() + "\n";
        }
        detalhes = detalhes + "\nHISTORICO DA CENTRAL\n";
        for (String registro : central.getHistoricoOperacoes()) {
            detalhes = detalhes + registro + "\n";
        }
        detalhes = detalhes + "\nHISTORICO DE COMUNICACAO\n";
        for (Robo robo : robos) {
            for (String mensagem : robo.getHistoricoMensagens()) {
                detalhes = detalhes + mensagem + "\n";
            }
        }
    }

    public String gerarResumo() {
        return "RELATORIO GERAL DE RESGATE\nCentral: " + nomeCentral + "\nGerado em: " + dataGeracao
                + "\nRobos: " + totalRobos + "\nDisponiveis: " + robosDisponiveis
                + "\nMissoes: " + totalMissoes + "\nConcluidas: " + missoesConcluidas
                + "\nPendentes: " + missoesPendentes + "\nSem atribuicao: " + aguardandoAtribuicao
                + "\nEm andamento: " + missoesEmAndamento + "\nCanceladas: " + missoesCanceladas
                + "\nSoma das missoes realizadas pelos robos: " + totalMissoesRealizadas
                + "\nBateria media: " + mediaBateria + "%\n"
                + detalhes;
    }

    public void salvarEmArquivo(String nomeArquivo) throws IOException {
        File arquivo = new File(nomeArquivo);
        File pasta = arquivo.getParentFile();
        if (pasta != null && !pasta.exists() && !pasta.mkdirs()) {
            throw new IOException("Nao foi possivel criar a pasta do relatorio.");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(nomeArquivo), Charset.forName("UTF-8"))) {
            writer.write(gerarResumo());
        }
    }

    public LocalDateTime getDataGeracao() { return dataGeracao; }
    public int getTotalRobos() { return totalRobos; }
    public int getTotalMissoes() { return totalMissoes; }
    public long getMissoesConcluidas() { return missoesConcluidas; }
    public long getMissoesPendentes() { return missoesPendentes; }
    public long getMissoesEmAndamento() { return missoesEmAndamento; }
    public long getMissoesCanceladas() { return missoesCanceladas; }
    public double getMediaBateria() { return mediaBateria; }
}
