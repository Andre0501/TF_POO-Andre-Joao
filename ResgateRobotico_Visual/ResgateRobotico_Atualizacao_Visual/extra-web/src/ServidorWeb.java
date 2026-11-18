import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Camada EXTRA de acesso web. As classes do trabalho em src/ permanecem intactas.
 * O navegador envia comandos; a CentralResgate original executa todas as regras.
 */
public class ServidorWeb {
    private CentralResgate central;
    private final Path arquivo;
    private final Object trava = new Object();
    private final String token = UUID.randomUUID().toString();
    private String salvoEm;

    public ServidorWeb(Path arquivo) throws Exception {
        this.arquivo = arquivo.toAbsolutePath().normalize();
        if (Files.exists(this.arquivo)) {
            // Uma falha de leitura é informada; nunca substituímos dados inválidos silenciosamente.
            central = Persistencia.carregar(this.arquivo.toString());
            salvoEm = Files.getLastModifiedTime(this.arquivo).toInstant().toString();
        } else {
            central = new CentralResgate("Central de Resgate Robótico");
            DadosExemplo.preencher(central);
            salvar();
        }
    }

    public static void main(String[] args) throws Exception {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        if (porta < 1 || porta > 65535) throw new IllegalArgumentException("Porta inválida.");
        Path arquivo = Paths.get(args.length > 1 ? args[1] : "extra-web/dados/central-web.dat");
        ServidorWeb app = new ServidorWeb(arquivo);
        HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", porta), 0);
        ExecutorService equipe = Executors.newFixedThreadPool(4);
        servidor.setExecutor(equipe);
        servidor.createContext("/", app::atender);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            servidor.stop(0);
            equipe.shutdown();
        }));
        servidor.start();
        System.out.println("\n  RESGATE / Central de operações");
        System.out.println("  Abra http://localhost:" + porta);
        System.out.println("  No Codespaces, abra a porta " + porta + " na aba PORTS.");
        System.out.println("  Salvamento automático: " + app.arquivo);
        System.out.println("  Ctrl+C encerra o servidor.\n");
    }

    private void atender(HttpExchange x) throws IOException {
        try {
            String caminho = x.getRequestURI().getPath();
            if (!caminho.startsWith("/api/")) {
                servirArquivo(x, caminho);
                return;
            }
            synchronized (trava) {
                if ("GET".equals(x.getRequestMethod()) && caminho.equals("/api/estado")) {
                    responderJson(x, 200, estado());
                } else if ("GET".equals(x.getRequestMethod()) && caminho.equals("/api/candidatos")) {
                    Map<String, String> p = parametros(x.getRequestURI().getRawQuery());
                    responderJson(x, 200, candidatos(texto(p, "id")));
                } else if ("GET".equals(x.getRequestMethod()) && caminho.equals("/api/relatorio")) {
                    Relatorio r = central.gerarRelatorioGeral();
                    responderJson(x, 200, mapa("texto", r.gerarResumo(),
                            "geradoEm", Instant.now().toString()));
                } else if ("GET".equals(x.getRequestMethod()) && caminho.equals("/api/relatorio.txt")) {
                    x.getResponseHeaders().set("Content-Disposition",
                            "attachment; filename=\"relatorio-resgate.txt\"");
                    responder(x, 200, "text/plain; charset=utf-8",
                            central.gerarRelatorioGeral().gerarResumo().getBytes(StandardCharsets.UTF_8));
                } else if ("POST".equals(x.getRequestMethod()) && caminho.equals("/api/acao")) {
                    if (!token.equals(x.getRequestHeaders().getFirst("X-Resgate-Token"))) {
                        responderJson(x, 403, mapa("erro", "Atualize a página e tente novamente."));
                        return;
                    }
                    byte[] corpo = x.getRequestBody().readNBytes(65537);
                    if (corpo.length > 65536) {
                        responderJson(x, 413, mapa("erro", "O formulário é muito grande."));
                        return;
                    }
                    Map<String, String> p = parametros(new String(corpo, StandardCharsets.UTF_8));
                    // Cada comando é uma unidade: se validação ou gravação falhar, restaura a memória.
                    byte[] anterior = copiarEstado();
                    String salvoAnterior = salvoEm;
                    String mensagem;
                    try {
                        mensagem = executar(p);
                        salvar();
                    } catch (Exception erro) {
                        central = recuperarEstado(anterior);
                        salvoEm = salvoAnterior;
                        throw erro;
                    }
                    Map<String, Object> resposta = estado();
                    resposta.put("mensagem", mensagem);
                    responderJson(x, 200, resposta);
                } else {
                    responderJson(x, 404, mapa("erro", "Operação não encontrada."));
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            responderJson(x, 400, mapa("erro", mensagemErro(e)));
        } catch (Exception e) {
            System.err.println("Falha no acesso web: " + e);
            responderJson(x, 500, mapa("erro",
                    "Não foi possível concluir a operação. Confira o terminal e tente novamente."));
        } finally {
            x.close();
        }
    }

    private String executar(Map<String, String> p) {
        String acao = texto(p, "acao");
        switch (acao) {
            case "cadastrarRobo": {
                String tipo = texto(p, "tipo");
                String id = texto(p, "id");
                String nome = texto(p, "nome");
                int bateria = inteiro(p, "bateria");
                int capacidade = inteiro(p, "capacidade");
                Robo robo;
                if (tipo.equals("DRONE")) robo = new Drone(id, nome, bateria, capacidade);
                else if (tipo.equals("TERRESTRE")) robo = new RoboTerrestre(id, nome, bateria, capacidade);
                else if (tipo.equals("ANFIBIO")) robo = new RoboAnfibio(id, nome, bateria, capacidade);
                else throw new IllegalArgumentException("Escolha um tipo de robô.");
                central.cadastrarRobo(robo);
                return "Robô " + id + " adicionado à frota.";
            }
            case "cadastrarArea": {
                central.cadastrarArea(new AreaOperacao(texto(p, "id"), texto(p, "nome"),
                        texto(p, "localizacao"), valor(TipoTerreno.class, p, "terreno"),
                        valor(NivelRisco.class, p, "risco"), texto(p, "descricao")));
                return "Área cadastrada. Ela já pode receber missões.";
            }
            case "registrarMissao": {
                TipoMissao tipo = valor(TipoMissao.class, p, "tipo");
                central.registrarMissao(new Missao(texto(p, "id"), texto(p, "descricao"),
                        tipo, central.buscarArea(texto(p, "area")),
                        valor(PrioridadeMissao.class, p, "prioridade"), inteiro(p, "peso")));
                return "Missão registrada na fila de operações.";
            }
            case "atribuir":
                central.atribuirRoboMissao(texto(p, "robo"), texto(p, "missao"));
                return "Robô reservado. A missão está pronta para iniciar.";
            case "removerAtribuicao":
                central.removerAtribuicao(texto(p, "missao"));
                return "Reserva retirada. O robô foi liberado.";
            case "iniciar":
                central.iniciarMissao(texto(p, "missao"));
                return "Missão iniciada. O robô está em operação.";
            case "executar":
                central.executarMissao(texto(p, "missao"));
                return "Missão concluída. Resultado e bateria atualizados.";
            case "cancelar":
                central.cancelarMissao(texto(p, "missao"));
                return "Missão cancelada.";
            case "recarregar":
                central.recarregarRobo(texto(p, "robo"));
                return "Recarga concluída: bateria em 100%.";
            case "iniciarManutencao":
                central.iniciarManutencao(texto(p, "robo"));
                return "Robô encaminhado para manutenção.";
            case "finalizarManutencao":
                central.finalizarManutencao(texto(p, "robo"));
                return "Manutenção finalizada.";
            case "atualizarRisco":
                central.atualizarRiscoArea(texto(p, "area"), valor(NivelRisco.class, p, "risco"));
                return "Nível de risco atualizado.";
            case "enviarInstrucao":
                central.enviarInstrucao(texto(p, "robo"), texto(p, "mensagem"));
                return "Instrução registrada e enviada ao robô.";
            case "receberInformacao":
                central.receberInformacao(texto(p, "robo"), texto(p, "mensagem"));
                return "Resposta do robô registrada.";
            case "atenderProxima":
                return central.atenderProximaPendente();
            case "salvar":
                return "Estado da central salvo.";
            case "reiniciarDemo":
                if (!"REINICIAR".equals(p.get("confirmacao"))) {
                    throw new IllegalArgumentException("Confirme o reinício da demonstração.");
                }
                central = new CentralResgate("Central de Resgate Robótico");
                DadosExemplo.preencher(central);
                return "Demonstração reiniciada com os dados de exemplo.";
            default:
                throw new IllegalArgumentException("Comando desconhecido.");
        }
    }

    private Map<String, Object> estado() {
        List<Object> robos = new ArrayList<>();
        for (Robo r : central.listarRobos()) robos.add(robo(r));
        List<Object> areas = new ArrayList<>();
        for (AreaOperacao a : central.listarAreas()) {
            areas.add(mapa("id", a.getId(), "nome", a.getNome(), "localizacao", a.getLocalizacao(),
                    "terreno", a.getTipoTerreno(), "risco", a.getNivelRisco(),
                    "descricao", a.getDescricao()));
        }
        List<Object> missoes = new ArrayList<>();
        for (Missao m : central.listarMissoes()) {
            missoes.add(mapa("id", m.getId(), "descricao", m.getDescricao(), "tipo", m.getTipo(),
                    "area", m.getArea().getId(), "prioridade", m.getPrioridade(),
                    "status", m.getStatus(), "robo", m.getRoboResponsavel() == null
                            ? null : m.getRoboResponsavel().getId(),
                    "peso", m.getPesoSuprimentos(), "previsto", m.getConsumoPrevisto(),
                    "estimativa", m.getStatus() == StatusMissao.PENDENTE && m.getRoboResponsavel() != null
                            ? m.getRoboResponsavel().calcularConsumo(m) : null,
                    "aplicado", m.getConsumoAplicado(), "resultado", m.getResultado()));
        }
        List<String> fila = new ArrayList<>();
        for (Missao m : central.listarMissoesPendentes()) fila.add(m.getId());
        List<Object> historico = new ArrayList<>();
        for (String h : central.getHistoricoOperacoes()) historico.add(registro(h));
        Relatorio r = central.gerarRelatorioGeral();
        return mapa("nome", central.getNome(), "robos", robos, "areas", areas,
                "missoes", missoes, "fila", fila, "historico", historico, "salvoEm", salvoEm,
                "token", token, "metricas", mapa("totalRobos", r.getTotalRobos(),
                        "disponiveis", central.listarRobosDisponiveis().size(),
                        "totalMissoes", r.getTotalMissoes(), "pendentes", r.getMissoesPendentes(),
                        "emAndamento", r.getMissoesEmAndamento(), "concluidas", r.getMissoesConcluidas(),
                        "canceladas", r.getMissoesCanceladas(), "mediaBateria", r.getMediaBateria()));
    }

    private Map<String, Object> robo(Robo r) {
        String tipo = r instanceof Drone ? "DRONE" : r instanceof RoboAnfibio ? "ANFIBIO" : "TERRESTRE";
        int capacidade = r instanceof Drone ? ((Drone) r).getAltitudeMaxima()
                : r instanceof RoboAnfibio ? ((RoboAnfibio) r).getCapacidadeCarga()
                : ((RoboTerrestre) r).getCapacidadeCarga();
        List<Object> mensagens = new ArrayList<>();
        for (String h : r.getHistoricoMensagens()) mensagens.add(registro(h));
        return mapa("id", r.getId(), "nome", r.getNome(), "tipo", tipo,
                "bateria", r.consultarBateria(), "status", r.getStatus(),
                "realizadas", r.getMissoesRealizadas(), "capacidade", capacidade,
                "missao", r.getMissaoAtual() == null ? null : r.getMissaoAtual().getId(),
                "mensagens", mensagens);
    }

    private Map<String, Object> candidatos(String id) {
        Missao m = central.buscarMissao(id);
        List<Robo> recomendados = central.recomendarRobos(id);
        List<Robo> ordenados = new ArrayList<>(recomendados);
        for (Robo r : central.listarRobos()) if (!ordenados.contains(r)) ordenados.add(r);
        List<Object> lista = new ArrayList<>();
        for (Robo r : ordenados) {
            boolean compativel = r.podeExecutar(m);
            int consumo = compativel ? r.calcularConsumo(m) : 0;
            boolean disponivel = r.verificarDisponibilidade();
            boolean apto = compativel && disponivel && r.consultarBateria() >= consumo;
            String motivo = apto ? "Disponível para esta missão"
                    : !disponivel ? "Robô indisponível: " + r.getStatus()
                    : !compativel ? "Tipo, terreno ou carga incompatível"
                    : "Bateria insuficiente para o consumo previsto";
            lista.add(mapa("robo", r.getId(), "nome", r.getNome(), "tipo",
                    r instanceof Drone ? "DRONE" : r instanceof RoboAnfibio ? "ANFIBIO" : "TERRESTRE",
                    "bateria", r.consultarBateria(), "consumo", compativel ? consumo : null,
                    "restante", compativel ? r.consultarBateria() - consumo : null,
                    "apto", apto, "motivo", motivo));
        }
        return mapa("missao", id, "candidatos", lista,
                "sugestao", recomendados.isEmpty() ? null : recomendados.get(0).getId(),
                "simulacao", central.simularAutonomia(id));
    }

    private Map<String, Object> registro(String registro) {
        String[] partes = registro.split(" \\| ", 2);
        String data = null;
        if (partes.length == 2) {
            try {
                data = LocalDateTime.parse(partes[0]).atZone(ZoneId.systemDefault()).toInstant().toString();
            } catch (Exception ignorada) { }
        }
        return mapa("data", data, "texto", partes.length == 2 ? partes[1] : registro);
    }

    private void salvar() throws IOException {
        Path pasta = arquivo.getParent();
        Files.createDirectories(pasta);
        Path temporario = Files.createTempFile(pasta, "resgate-", ".tmp");
        try {
            Persistencia.salvar(central, temporario.toString());
            try {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
            salvoEm = Instant.now().toString();
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    private byte[] copiarEstado() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(central); }
        return bytes.toByteArray();
    }

    private CentralResgate recuperarEstado(byte[] bytes) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (CentralResgate) in.readObject();
        }
    }

    private void servirArquivo(HttpExchange x, String caminho) throws IOException {
        if (!"GET".equals(x.getRequestMethod()) && !"HEAD".equals(x.getRequestMethod())) {
            responderJson(x, 405, mapa("erro", "Método não permitido."));
            return;
        }
        String nome;
        String mime;
        switch (caminho) {
            case "/": case "/index.html": nome = "index.html"; mime = "text/html; charset=utf-8"; break;
            case "/app.css": nome = "app.css"; mime = "text/css; charset=utf-8"; break;
            case "/app.js": nome = "app.js"; mime = "text/javascript; charset=utf-8"; break;
            case "/scenario.js": nome = "scenario.js"; mime = "text/javascript; charset=utf-8"; break;
            case "/flood.webp": case "/slope.webp": case "/storm.webp":
            case "/drone.webp": case "/rover.webp": case "/amphibious.webp":
                nome = caminho.substring(1); mime = "image/webp"; break;
            case "/favicon.svg": nome = "favicon.svg"; mime = "image/svg+xml"; break;
            default:
                responderJson(x, 404, mapa("erro", "Página não encontrada.")); return;
        }
        try (InputStream in = ServidorWeb.class.getResourceAsStream("/web/" + nome)) {
            if (in == null) throw new FileNotFoundException("Recurso web ausente: " + nome);
            responder(x, 200, mime, in.readAllBytes());
        }
    }

    private static Map<String, String> parametros(String corpo) {
        Map<String, String> p = new LinkedHashMap<>();
        if (corpo == null || corpo.isEmpty()) return p;
        for (String item : corpo.split("&")) {
            String[] parte = item.split("=", 2);
            p.put(URLDecoder.decode(parte[0], StandardCharsets.UTF_8),
                    parte.length == 2 ? URLDecoder.decode(parte[1], StandardCharsets.UTF_8) : "");
        }
        return p;
    }

    private static String texto(Map<String, String> p, String campo) {
        String valor = p.get(campo);
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Preencha o campo " + campo + ".");
        }
        if (valor.length() > 2000) throw new IllegalArgumentException("O campo " + campo + " é muito longo.");
        return valor.trim();
    }

    private static int inteiro(Map<String, String> p, String campo) {
        try { return Integer.parseInt(texto(p, campo)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Informe um número inteiro em " + campo + "."); }
    }

    private static <E extends Enum<E>> E valor(Class<E> classe, Map<String, String> p, String campo) {
        try { return Enum.valueOf(classe, texto(p, campo)); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Escolha uma opção válida em " + campo + "."); }
    }

    private static String mensagemErro(Exception e) {
        return e.getMessage() == null ? "Não foi possível realizar a operação." : e.getMessage();
    }

    private static Map<String, Object> mapa(Object... pares) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) m.put((String) pares[i], pares[i + 1]);
        return m;
    }

    private static void responderJson(HttpExchange x, int status, Object dados) throws IOException {
        responder(x, status, "application/json; charset=utf-8", json(dados).getBytes(StandardCharsets.UTF_8));
    }

    private static void responder(HttpExchange x, int status, String mime, byte[] dados) throws IOException {
        x.getResponseHeaders().set("Content-Type", mime);
        x.getResponseHeaders().set("Cache-Control",
                mime.equals("image/webp") ? "private, max-age=600" : "no-store");
        x.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        if ("HEAD".equals(x.getRequestMethod())) {
            x.sendResponseHeaders(status, -1);
        } else {
            x.sendResponseHeaders(status, dados.length);
            x.getResponseBody().write(dados);
        }
    }

    // Respostas JSON de saída apenas. Os formulários chegam como URLSearchParams.
    private static String json(Object objeto) {
        if (objeto == null) return "null";
        if (objeto instanceof Number || objeto instanceof Boolean) return objeto.toString();
        if (objeto instanceof Map) {
            StringJoiner j = new StringJoiner(",", "{", "}");
            for (Map.Entry<?, ?> item : ((Map<?, ?>) objeto).entrySet()) {
                j.add(json(item.getKey().toString()) + ":" + json(item.getValue()));
            }
            return j.toString();
        }
        if (objeto instanceof Iterable) {
            StringJoiner j = new StringJoiner(",", "[", "]");
            for (Object item : (Iterable<?>) objeto) j.add(json(item));
            return j.toString();
        }
        StringBuilder s = new StringBuilder("\"");
        for (char c : objeto.toString().toCharArray()) {
            switch (c) {
                case '"': s.append("\\\""); break;
                case '\\': s.append("\\\\"); break;
                case '\n': s.append("\\n"); break;
                case '\r': s.append("\\r"); break;
                case '\t': s.append("\\t"); break;
                default:
                    if (c < 32) s.append(String.format("\\u%04x", (int) c));
                    else s.append(c);
            }
        }
        return s.append('"').toString();
    }
}
