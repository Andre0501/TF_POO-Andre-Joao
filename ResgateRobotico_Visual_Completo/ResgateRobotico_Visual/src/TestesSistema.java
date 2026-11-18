import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

// Interface funcional simples, no mesmo estilo de VerificaPessoa dos exemplos da aula.
interface AcaoTeste {
    void executar() throws Exception;
}

// Testes sem bibliotecas externas. Cada teste cria os seus proprios objetos.
public class TestesSistema {
    private static int aprovados = 0;
    private static int reprovados = 0;

    private static CentralResgate exemplo() {
        CentralResgate central = new CentralResgate("Central de testes");
        DadosExemplo.preencher(central);
        return central;
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) { throw new IllegalStateException("Verificacao falhou: " + mensagem); }
    }

    private static void esperarArgumento(AcaoTeste acao) throws Exception {
        try { acao.executar(); }
        catch (IllegalArgumentException e) { return; }
        throw new IllegalStateException("Era esperado IllegalArgumentException.");
    }

    private static void esperarEstado(AcaoTeste acao) throws Exception {
        try { acao.executar(); }
        catch (IllegalStateException e) { return; }
        throw new IllegalStateException("Era esperado IllegalStateException.");
    }

    private static void esperarArquivo(AcaoTeste acao) throws Exception {
        try { acao.executar(); }
        catch (IOException e) { return; }
        throw new IllegalStateException("Era esperado IOException.");
    }

    private static void testar(String nome, AcaoTeste acao) {
        try {
            acao.executar();
            aprovados++;
            System.out.println("[OK] " + nome);
        } catch (Exception e) {
            reprovados++;
            System.out.println("[FALHOU] " + nome + " | " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new File("evidencias/temporarios").mkdirs();
        testar("01 Cadastros, consultas e identificadores ordenados", () -> {
            CentralResgate c = exemplo();
            verificar(c.listarRobos().size() == 4 && c.listarAreas().size() == 3, "quantidades");
            verificar(c.buscarRobo("D1").getNome().equals("Olho do Ceu"), "busca por chave");
            verificar(c.listarRobos().get(0).getId().equals("A1"), "ordenacao");
        });
        testar("02 Robo duplicado nao substitui o original", () -> {
            CentralResgate c = exemplo();
            Robo original = c.buscarRobo("D1");
            esperarArgumento(() -> c.cadastrarRobo(new Drone("D1", "Outro", 100, 100)));
            verificar(c.buscarRobo("D1") == original && c.listarRobos().size() == 4, "cadastro preservado");
        });
        testar("03 Area duplicada rejeitada", () -> {
            CentralResgate c = exemplo();
            esperarArgumento(() -> c.cadastrarArea(new AreaOperacao("AR1", "Outra", "X",
                    TipoTerreno.URBANO, NivelRisco.BAIXO, "Descricao")));
            verificar(c.listarAreas().size() == 3, "quantidade");
        });
        testar("04 Missao duplicada nao duplica a fila", () -> {
            CentralResgate c = exemplo();
            esperarArgumento(() -> c.registrarMissao(new Missao("M1", "Outra", TipoMissao.RECONHECIMENTO,
                    c.buscarArea("AR1"), PrioridadeMissao.BAIXA, 0)));
            verificar(c.listarMissoesPendentes().size() == 4, "fila");
        });
        testar("05 Identificador tem espacos externos removidos", () -> {
            CentralResgate c = new CentralResgate("Teste");
            c.cadastrarRobo(new Drone(" D1 ", " Drone ", 100, 100));
            verificar(c.buscarRobo("D1").getNome().equals("Drone"), "normalizacao");
        });
        testar("06 Bateria negativa rejeitada", () -> esperarArgumento(() -> new Drone("D", "D", -1, 10)));
        testar("07 Bateria acima de 100 rejeitada", () -> esperarArgumento(() -> new Drone("D", "D", 101, 10)));
        testar("08 Textos vazios, nulos e caracteristicas invalidas", () -> {
            esperarArgumento(() -> new Drone("", "D", 100, 10));
            esperarArgumento(() -> new CentralResgate(null));
            esperarArgumento(() -> new Drone("D", "D", 100, 0));
            esperarArgumento(() -> new RoboTerrestre("T", "T", 100, -2));
            esperarArgumento(() -> exemplo().cadastrarRobo(null));
        });
        testar("09 Missao sem area rejeitada", () -> esperarArgumento(() ->
                new Missao("M", "Teste", TipoMissao.RECONHECIMENTO, null, PrioridadeMissao.BAIXA, 0)));
        testar("10 Area nao cadastrada rejeitada", () -> {
            CentralResgate c = exemplo();
            AreaOperacao area = new AreaOperacao("X", "X", "X", TipoTerreno.URBANO, NivelRisco.BAIXO, "X");
            esperarArgumento(() -> c.registrarMissao(new Missao("X", "X", TipoMissao.RECONHECIMENTO,
                    area, PrioridadeMissao.BAIXA, 0)));
        });
        testar("11 Mesmo id de area em outro objeto nao cria vinculo incorreto", () -> {
            CentralResgate c = exemplo();
            AreaOperacao outra = new AreaOperacao("AR1", "Outra", "Outra", TipoTerreno.ALAGADO, NivelRisco.ALTO, "Outra");
            esperarArgumento(() -> c.registrarMissao(new Missao("X", "X", TipoMissao.RECONHECIMENTO,
                    outra, PrioridadeMissao.BAIXA, 0)));
        });
        testar("12 Transporte exige carga positiva", () -> {
            CentralResgate c = exemplo();
            esperarArgumento(() -> new Missao("X", "X", TipoMissao.TRANSPORTE_SUPRIMENTOS,
                    c.buscarArea("AR1"), PrioridadeMissao.MEDIA, 0));
        });
        testar("13 Missao aerea exige carga zero", () -> {
            CentralResgate c = exemplo();
            esperarArgumento(() -> new Missao("X", "X", TipoMissao.RECONHECIMENTO,
                    c.buscarArea("AR1"), PrioridadeMissao.MEDIA, 5));
        });
        testar("14 Drone nao transporta suprimentos", () -> {
            CentralResgate c = exemplo();
            esperarArgumento(() -> c.atribuirRoboMissao("D1", "M3"));
            verificar(c.buscarMissao("M3").getRoboResponsavel() == null, "sem alteracao parcial");
        });
        testar("15 Terrestre nao executa busca aerea", () -> {
            CentralResgate c = exemplo();
            esperarArgumento(() -> c.atribuirRoboMissao("T1", "M2"));
        });
        testar("16 Terrestre nao entra em terreno alagado", () -> {
            CentralResgate c = exemplo();
            esperarArgumento(() -> c.atribuirRoboMissao("T1", "M4"));
        });
        testar("17 Carga superior a capacidade rejeitada", () -> {
            CentralResgate c = exemplo();
            c.registrarMissao(new Missao("PESO", "Carga pesada", TipoMissao.TRANSPORTE_SUPRIMENTOS,
                    c.buscarArea("AR1"), PrioridadeMissao.ALTA, 60));
            esperarArgumento(() -> c.atribuirRoboMissao("T1", "PESO"));
            esperarArgumento(() -> c.atribuirRoboMissao("A1", "PESO"));
        });
        testar("18 Bateria insuficiente nao reserva o robo", () -> {
            CentralResgate c = exemplo();
            c.cadastrarRobo(new Drone("FRACO", "Fraco", 10, 100));
            esperarEstado(() -> c.atribuirRoboMissao("FRACO", "M1"));
            verificar(c.buscarRobo("FRACO").verificarDisponibilidade(), "continua livre");
            verificar(c.buscarMissao("M1").getRoboResponsavel() == null, "sem vinculo");
        });
        testar("19 Robo reservado nao recebe segunda missao", () -> {
            CentralResgate c = exemplo();
            c.atribuirRoboMissao("D1", "M1");
            esperarEstado(() -> c.atribuirRoboMissao("D1", "M2"));
            verificar(c.buscarRobo("D1").getStatus() == StatusRobo.RESERVADO, "reserva");
        });
        testar("20 Inicio sem robo rejeitado", () -> {
            CentralResgate c = exemplo();
            esperarEstado(() -> c.iniciarMissao("M1"));
            verificar(c.listarMissoesPendentes().size() == 4, "permanece na fila");
        });
        testar("21 Ciclo valido atualiza resultado, bateria, status e contador", () -> {
            CentralResgate c = exemplo();
            c.atribuirRoboMissao("D1", "M1");
            c.executarMissao("M1");
            verificar(c.buscarMissao("M1").getStatus() == StatusMissao.CONCLUIDA, "concluida");
            verificar(c.buscarRobo("D1").consultarBateria() == 75, "100 - (15 + 10)");
            verificar(c.buscarRobo("D1").verificarDisponibilidade(), "liberado");
            verificar(c.buscarRobo("D1").getMissoesRealizadas() == 1, "contador");
            verificar(c.buscarMissao("M1").getResultado().contains("Reconhecimento"), "resultado");
        });
        testar("22 Reexecucao e reinicio de concluida bloqueados", () -> {
            CentralResgate c = exemplo();
            c.atribuirRoboMissao("D1", "M1"); c.executarMissao("M1");
            esperarEstado(() -> c.executarMissao("M1"));
            esperarEstado(() -> c.iniciarMissao("M1"));
            verificar(c.buscarRobo("D1").consultarBateria() == 75, "sem novo desconto");
        });
        testar("23 Cancelamento de pendente sem robo remove da fila", () -> {
            CentralResgate c = exemplo();
            c.cancelarMissao("M1");
            esperarEstado(() -> c.iniciarMissao("M1"));
            esperarEstado(() -> c.cancelarMissao("M1"));
            verificar(c.listarMissoesPendentes().size() == 3, "fila atualizada");
        });
        testar("24 Cancelar reserva libera o robo sem consumo", () -> {
            CentralResgate c = exemplo();
            c.atribuirRoboMissao("D1", "M1"); c.cancelarMissao("M1");
            verificar(c.buscarRobo("D1").consultarBateria() == 100, "sem consumo");
            verificar(c.buscarRobo("D1").verificarDisponibilidade(), "liberado");
        });
        testar("25 Cancelar em andamento consome previsao sem contar conclusao", () -> {
            CentralResgate c = exemplo();
            c.atribuirRoboMissao("D1", "M1"); c.iniciarMissao("M1"); c.cancelarMissao("M1");
            verificar(c.buscarRobo("D1").consultarBateria() == 75, "consumo aplicado");
            verificar(c.buscarRobo("D1").getMissoesRealizadas() == 0, "nao concluida");
        });
        testar("26 Recarga e manutencao bloqueadas durante reserva e execucao", () -> {
            CentralResgate c = exemplo(); c.atribuirRoboMissao("D1", "M1");
            esperarEstado(() -> c.recarregarRobo("D1"));
            esperarEstado(() -> c.iniciarManutencao("D1"));
            c.iniciarMissao("M1");
            esperarEstado(() -> c.recarregarRobo("D1"));
            verificar(c.buscarRobo("D1").consultarBateria() == 100, "desconto so ao finalizar");
        });
        testar("27 Bateria exata chega a zero e recarga restaura disponibilidade", () -> {
            CentralResgate c = exemplo(); c.cadastrarRobo(new Drone("EXATO", "Exato", 25, 100));
            c.atribuirRoboMissao("EXATO", "M1"); c.executarMissao("M1");
            verificar(c.buscarRobo("EXATO").getStatus() == StatusRobo.SEM_BATERIA, "zerou");
            verificar(!c.buscarRobo("EXATO").verificarDisponibilidade(), "indisponivel");
            c.recarregarRobo("EXATO");
            verificar(c.buscarRobo("EXATO").consultarBateria() == 100, "recarga");
        });
        testar("28 Manutencao impede atribuicao e recarga nao a encerra", () -> {
            CentralResgate c = exemplo(); c.iniciarManutencao("D1");
            esperarEstado(() -> c.atribuirRoboMissao("D1", "M1"));
            c.recarregarRobo("D1");
            verificar(c.buscarRobo("D1").getStatus() == StatusRobo.EM_MANUTENCAO, "mantem manutencao");
            c.finalizarManutencao("D1");
            verificar(c.buscarRobo("D1").verificarDisponibilidade(), "liberado");
        });
        testar("29 Aumento do risco antes do inicio revalida bateria", () -> {
            CentralResgate c = exemplo(); c.cadastrarRobo(new Drone("LIMITE", "Limite", 25, 100));
            c.atribuirRoboMissao("LIMITE", "M2"); c.atualizarRiscoArea("AR1", NivelRisco.ALTO);
            esperarEstado(() -> c.iniciarMissao("M2"));
            verificar(c.buscarMissao("M2").getStatus() == StatusMissao.PENDENTE, "pendente");
            c.removerAtribuicao("M2"); c.recarregarRobo("LIMITE");
            c.atribuirRoboMissao("LIMITE", "M2"); c.executarMissao("M2");
            verificar(c.buscarRobo("LIMITE").consultarBateria() == 65, "novo consumo 35");
        });
        testar("30 Consumo fixado no inicio nao muda durante a execucao", () -> {
            CentralResgate c = exemplo(); c.atribuirRoboMissao("D1", "M2"); c.iniciarMissao("M2");
            c.atualizarRiscoArea("AR1", NivelRisco.ALTO); c.executarMissao("M2");
            verificar(c.buscarRobo("D1").consultarBateria() == 75, "previsao inicial 25");
        });
        testar("31 Fila sem robo adequado preserva ordem e pendencias", () -> {
            CentralResgate c = exemplo();
            c.iniciarManutencao("D1"); c.iniciarManutencao("D2");
            verificar(c.atenderProximaPendente().contains("permanece"), "mensagem");
            verificar(c.listarMissoesPendentes().get(0).getId().equals("M1"), "ordem FIFO");
            verificar(c.listarMissoesPendentes().size() == 4, "nenhuma perdida");
        });
        testar("32 Atendimento FIFO pula reservas e atende a proxima sem atribuicao", () -> {
            CentralResgate c = exemplo(); c.atribuirRoboMissao("D1", "M1");
            verificar(c.atenderProximaPendente().contains("M2"), "proxima M2");
            verificar(c.buscarMissao("M2").getRoboResponsavel().getId().equals("D2"), "segundo drone");
            verificar(c.listarMissoesPendentes().size() == 4, "reservadas ficam pendentes ate iniciar");
        });
        testar("33 Recomendacao usa consumo, bateria e id como desempates", () -> {
            CentralResgate c = exemplo();
            verificar(c.recomendarRobos("M3").get(0).getId().equals("T1"), "20 vence 25");
            verificar(c.recomendarRobos("M1").get(0).getId().equals("D1"), "maior bateria");
            c.cadastrarRobo(new Drone("D0", "Outro", 100, 100));
            verificar(c.recomendarRobos("M1").get(0).getId().equals("D0"), "menor id");
        });
        testar("34 Simulacao de autonomia nao altera objetos", () -> {
            CentralResgate c = exemplo();
            verificar(c.simularAutonomia("M3").contains("restante 80%"), "previsao");
            verificar(c.buscarRobo("T1").consultarBateria() == 100, "bateria preservada");
            verificar(c.buscarMissao("M3").getRoboResponsavel() == null, "sem atribuicao");
        });
        testar("35 Recomendacao sem candidatos retorna lista vazia", () -> {
            CentralResgate c = exemplo(); c.iniciarManutencao("A1");
            verificar(c.recomendarRobos("M4").isEmpty(), "sem candidato alagado");
        });
        testar("36 Comunicacao registra os dois sentidos e rejeita texto vazio", () -> {
            CentralResgate c = exemplo(); c.enviarInstrucao("D1", "Reconhecer setor");
            c.receberInformacao("D1", "Setor observado");
            verificar(c.buscarRobo("D1").getHistoricoMensagens().size() == 2, "duas mensagens");
            esperarArgumento(() -> c.enviarInstrucao("D1", " "));
        });
        testar("37 Copias de colecoes protegem os registros originais", () -> {
            CentralResgate c = exemplo(); c.listarRobos().clear(); c.listarMissoesPendentes().clear();
            c.getHistoricoOperacoes().clear();
            verificar(c.listarRobos().size() == 4 && c.listarMissoesPendentes().size() == 4, "originais");
            verificar(!c.getHistoricoOperacoes().isEmpty(), "historico");
        });
        testar("38 Relatorio da central vazia nao falha na media", () -> {
            Relatorio r = new CentralResgate("Vazia").gerarRelatorioGeral();
            verificar(r.getTotalRobos() == 0 && r.getMediaBateria() == 0, "media zero");
        });
        testar("39 Relatorio conta todos os estados sem dupla contagem", () -> {
            CentralResgate c = exemplo(); c.atribuirRoboMissao("D1", "M1"); c.executarMissao("M1");
            c.atribuirRoboMissao("D2", "M2"); c.iniciarMissao("M2"); c.cancelarMissao("M3");
            Relatorio r = c.gerarRelatorioGeral();
            verificar(r.getTotalMissoes() == 4 && r.getMissoesConcluidas() == 1
                    && r.getMissoesEmAndamento() == 1 && r.getMissoesCanceladas() == 1
                    && r.getMissoesPendentes() == 1, "contadores");
            verificar(r.getMediaBateria() == 86.25, "media das quatro baterias");
        });
        testar("40 Serializacao preserva vinculos, fila, mensagens e missao em andamento", () -> {
            CentralResgate c = exemplo(); c.atribuirRoboMissao("D1", "M1"); c.iniciarMissao("M1");
            Persistencia.salvar(c, "evidencias/temporarios/retomada.dat");
            CentralResgate lida = Persistencia.carregar("evidencias/temporarios/retomada.dat");
            verificar(lida.buscarMissao("M1").getArea() == lida.buscarArea("AR2"), "mesma area");
            verificar(lida.buscarMissao("M1").getRoboResponsavel() == lida.buscarRobo("D1"), "mesmo robo");
            verificar(lida.buscarRobo("D1").getMissaoAtual() == lida.buscarMissao("M1"), "vinculo inverso");
            verificar(lida.listarMissoesPendentes().size() == 3, "fila");
            verificar(!lida.buscarRobo("D1").getHistoricoMensagens().isEmpty(), "mensagens");
            lida.executarMissao("M1");
            verificar(lida.buscarRobo("D1").consultarBateria() == 75, "retomada correta");
        });
        testar("41 Arquivo corrompido tratado como erro de arquivo", () -> {
            try (FileOutputStream f = new FileOutputStream("evidencias/temporarios/corrompido.dat")) {
                f.write(new byte[] {1, 2, 3, 4, 5});
            }
            esperarArquivo(() -> Persistencia.carregar("evidencias/temporarios/corrompido.dat"));
        });
        testar("42 Arquivo inexistente gera IOException", () ->
                esperarArquivo(() -> Persistencia.carregar("evidencias/temporarios/inexistente-xyz.dat")));
        testar("43 Arquivo com objeto de outra classe rejeitado", () -> {
            try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream("evidencias/temporarios/outro.dat"))) {
                o.writeObject("Nao sou uma central");
            }
            esperarArquivo(() -> Persistencia.carregar("evidencias/temporarios/outro.dat"));
        });
        testar("44 Falha de gravacao tratada", () -> {
            String pasta = "evidencias/temporarios/uma-pasta"; new File(pasta).mkdirs();
            esperarArquivo(() -> Persistencia.salvar(exemplo(), pasta));
            esperarArquivo(() -> exemplo().gerarRelatorioGeral().salvarEmArquivo(pasta));
        });
        testar("45 Bonus anfibio transporta no alagado", () -> {
            CentralResgate c = exemplo(); c.atribuirRoboMissao("A1", "M4"); c.executarMissao("M4");
            verificar(c.buscarRobo("A1").consultarBateria() == 60, "25 + 10 + 5");
            verificar(c.buscarMissao("M4").getResultado().contains("anfibia"), "resultado especifico");
        });
        testar("46 Formulas de consumo e terreno irregular", () -> {
            CentralResgate c = exemplo();
            Missao m = new Missao("IR", "Transporte irregular", TipoMissao.TRANSPORTE_SUPRIMENTOS,
                    c.buscarArea("AR2"), PrioridadeMissao.ALTA, 50);
            verificar(c.buscarRobo("T1").calcularConsumo(m) == 40, "20 + 10 + 10");
            verificar(!c.buscarRobo("A1").podeExecutar(m), "anfibio nao entra no irregular");
            verificar(c.buscarRobo("D1").calcularConsumo(c.buscarMissao("M2")) == 25, "busca baixo");
        });
        testar("47 Relatorio salvo pode ser lido e mantem o instante de geracao", () -> {
            CentralResgate c = exemplo(); Relatorio r = c.gerarRelatorioGeral();
            c.cancelarMissao("M1");
            verificar(r.getMissoesCanceladas() == 0, "fotografia do instante");
            r.salvarEmArquivo("evidencias/temporarios/relatorio.txt");
            try (Scanner sc = new Scanner(new File("evidencias/temporarios/relatorio.txt"), "UTF-8")) {
                verificar(sc.nextLine().equals("RELATORIO GERAL DE RESGATE"), "arquivo legivel");
            }
        });
        testar("48 Fluxo completo das quatro missoes de demonstracao", () -> {
            CentralResgate c = exemplo();
            c.atribuirRoboMissao("D1", "M1"); c.executarMissao("M1");
            c.atribuirRoboMissao("D1", "M2"); c.executarMissao("M2");
            c.atribuirRoboMissao("T1", "M3"); c.executarMissao("M3");
            c.atribuirRoboMissao("A1", "M4"); c.executarMissao("M4");
            verificar(c.listarMissoesPendentes().isEmpty(), "fila vazia");
            verificar(c.gerarRelatorioGeral().getMissoesConcluidas() == 4, "quatro concluidas");
            verificar(c.gerarRelatorioGeral().getMediaBateria() == 65, "media final");
        });
        testar("49 Consultas de ids inexistentes e parametros nulos", () -> {
            CentralResgate c = exemplo();
            esperarArgumento(() -> c.buscarRobo("X"));
            esperarArgumento(() -> c.buscarArea("X"));
            esperarArgumento(() -> c.buscarMissao("X"));
            esperarArgumento(() -> c.buscarArea("AR1").atualizarNivelRisco(null));
        });
        testar("50 Missao nao aceita segunda atribuicao nem conclusao antes do inicio", () -> {
            CentralResgate c = exemplo(); c.atribuirRoboMissao("D1", "M1");
            esperarEstado(() -> c.atribuirRoboMissao("D2", "M1"));
            esperarEstado(() -> c.buscarMissao("M1").concluir("Resultado indevido"));
            verificar(c.buscarRobo("D2").verificarDisponibilidade(), "segundo drone continua livre");
        });

        System.out.println("\nRESULTADO: " + aprovados + " aprovados; " + reprovados + " reprovados.");
        if (reprovados > 0) { throw new IllegalStateException("Existem testes reprovados."); }
    }
}
