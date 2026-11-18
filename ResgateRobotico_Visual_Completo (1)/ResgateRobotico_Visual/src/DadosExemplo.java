public class DadosExemplo {
    public static void preencher(CentralResgate central) {
        if (!central.listarRobos().isEmpty() || !central.listarAreas().isEmpty()
                || !central.listarMissoes().isEmpty()) {
            throw new IllegalStateException("Os exemplos so podem ser criados em uma central vazia.");
        }
        central.cadastrarRobo(new Drone("D1", "Olho do Ceu", 100, 120));
        central.cadastrarRobo(new Drone("D2", "Sentinela", 70, 100));
        central.cadastrarRobo(new RoboTerrestre("T1", "Apoio Solo", 100, 50));
        central.cadastrarRobo(new RoboAnfibio("A1", "Navegador", 100, 30));
        central.cadastrarArea(new AreaOperacao("AR1", "Centro simulado", "Setor 1",
                TipoTerreno.URBANO, NivelRisco.BAIXO, "Area ficticia urbana."));
        central.cadastrarArea(new AreaOperacao("AR2", "Encosta simulada", "Setor 2",
                TipoTerreno.IRREGULAR, NivelRisco.ALTO, "Area ficticia com obstaculos."));
        central.cadastrarArea(new AreaOperacao("AR3", "Margem simulada", "Setor 3",
                TipoTerreno.ALAGADO, NivelRisco.MEDIO, "Area ficticia alagada."));
        central.registrarMissao(new Missao("M1", "Reconhecer a encosta", TipoMissao.RECONHECIMENTO,
                central.buscarArea("AR2"), PrioridadeMissao.ALTA, 0));
        central.registrarMissao(new Missao("M2", "Buscar pessoas no centro", TipoMissao.LOCALIZACAO_PESSOAS,
                central.buscarArea("AR1"), PrioridadeMissao.ALTA, 0));
        central.registrarMissao(new Missao("M3", "Entregar alimentos no centro", TipoMissao.TRANSPORTE_SUPRIMENTOS,
                central.buscarArea("AR1"), PrioridadeMissao.MEDIA, 20));
        central.registrarMissao(new Missao("M4", "Entregar agua na margem", TipoMissao.TRANSPORTE_SUPRIMENTOS,
                central.buscarArea("AR3"), PrioridadeMissao.ALTA, 25));
    }

    public static void main(String[] args) throws Exception {
        CentralResgate central = new CentralResgate("Central de Resgate Robotico");
        preencher(central);
        String destino = "dados/central.dat";
        if (args.length > 0) {
            destino = args[0];
        }
        Persistencia.salvar(central, destino);
        System.out.println("Exemplos gravados em " + destino);
        System.out.println(central.gerarRelatorioGeral().gerarResumo());
    }
}
