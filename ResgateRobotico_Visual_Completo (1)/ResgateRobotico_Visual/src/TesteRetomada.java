// Execute gravar e retomar em processos Java separados para comprovar a continuidade.
public class TesteRetomada {
    public static void main(String[] args) throws Exception {
        String caminho = "evidencias/temporarios/entre_execucoes.dat";
        if (args.length != 1) {
            throw new IllegalArgumentException("Informe gravar ou retomar.");
        }
        if (args[0].equals("gravar")) {
            CentralResgate central = new CentralResgate("Teste entre execucoes");
            DadosExemplo.preencher(central);
            central.atribuirRoboMissao("D1", "M1");
            central.iniciarMissao("M1");
            Persistencia.salvar(central, caminho);
            System.out.println("PROCESSO 1: salvou M1 EM_ANDAMENTO, D1 EM_MISSAO, bateria 100%, fila com 3.");
        } else if (args[0].equals("retomar")) {
            CentralResgate central = Persistencia.carregar(caminho);
            if (central.buscarMissao("M1").getStatus() != StatusMissao.EM_ANDAMENTO
                    || central.buscarRobo("D1").getStatus() != StatusRobo.EM_MISSAO
                    || central.listarMissoesPendentes().size() != 3) {
                throw new IllegalStateException("Estados nao foram recuperados.");
            }
            central.executarMissao("M1");
            if (central.buscarRobo("D1").consultarBateria() != 75
                    || central.buscarRobo("D1").getMissoesRealizadas() != 1) {
                throw new IllegalStateException("Retomada incorreta.");
            }
            System.out.println("PROCESSO 2: recuperou, concluiu M1 uma vez e obteve D1 com bateria 75%. [OK]");
        } else {
            throw new IllegalArgumentException("Informe gravar ou retomar.");
        }
    }
}
