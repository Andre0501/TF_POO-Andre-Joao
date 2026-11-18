public class Demonstracao {
    public static void main(String[] args) throws Exception {
        CentralResgate central = new CentralResgate("Central de demonstracao");
        DadosExemplo.preencher(central);
        System.out.println("DEMONSTRACAO | DADOS FICTICIOS");
        System.out.println("\n1. INOVACAO: COMPARACAO DE AUTONOMIA");
        System.out.println(central.simularAutonomia("M3"));
        System.out.println("2. CASO VALIDO: RECONHECIMENTO COM DRONE");
        central.atribuirRoboMissao("D1", "M1");
        central.executarMissao("M1");
        System.out.println(central.buscarRobo("D1").exibirResumo());
        System.out.println("\n3. CASO INVALIDO: TERRESTRE NO ALAGADO");
        try {
            central.atribuirRoboMissao("T1", "M4");
        } catch (IllegalArgumentException e) {
            System.out.println("REJEITADO: " + e.getMessage());
        }
        System.out.println("\n4. CASO INVALIDO: REINICIAR MISSAO CONCLUIDA");
        try {
            central.iniciarMissao("M1");
        } catch (IllegalStateException e) {
            System.out.println("REJEITADO: " + e.getMessage());
        }
        System.out.println("\n5. BONUS: ENTREGA COM ANFIBIO");
        central.atribuirRoboMissao("A1", "M4");
        central.executarMissao("M4");
        System.out.println(central.buscarRobo("A1").exibirResumo());
        central.gerarRelatorioGeral().salvarEmArquivo("relatorios/relatorio_demonstracao.txt");
        System.out.println("\nResumo: 4 robos; 4 missoes; 2 concluidas; 2 pendentes.");
        System.out.println("Relatorio gravado em relatorios/relatorio_demonstracao.txt");
    }
}
