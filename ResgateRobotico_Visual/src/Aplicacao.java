import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Aplicacao {
    private Scanner teclado;
    private CentralResgate central;
    private String arquivoDados;

    public Aplicacao(CentralResgate central, String arquivoDados) {
        this.central = central;
        this.arquivoDados = arquivoDados;
        teclado = new Scanner(System.in);
    }

    public static void main(String[] args) {
        String arquivo = "dados/central.dat";
        if (args.length > 0) {
            arquivo = args[0];
        }
        CentralResgate central;
        if (new File(arquivo).exists()) {
            try {
                central = Persistencia.carregar(arquivo);
                System.out.println("Dados recuperados de " + arquivo);
            } catch (IOException e) {
                System.out.println("Falha ao recuperar os dados: " + e.getMessage());
                System.out.println("O arquivo existente foi preservado. Verifique-o antes de continuar.");
                return;
            } catch (ClassNotFoundException e) {
                System.out.println("Classe do arquivo nao encontrada. Use a mesma versao do projeto.");
                return;
            }
        } else {
            central = new CentralResgate("Central de Resgate Robotico");
            System.out.println("Nova central criada. Opcao 21 carrega exemplos em uma central vazia.");
        }
        new Aplicacao(central, arquivo).executar();
    }

    public void executar() {
        boolean continuar = true;
        while (continuar) {
            exibirMenu();
            try {
                int opcao = lerInteiro("Opcao: ");
                switch (opcao) {
                    case 1: cadastrarRobo(); break;
                    case 2: mostrarRobos(central.listarRobos()); break;
                    case 3: mostrarRobos(central.listarRobosDisponiveis()); break;
                    case 4: cadastrarArea(); break;
                    case 5:
                        if (central.listarAreas().isEmpty()) { System.out.println("Nenhuma area cadastrada."); }
                        central.listarAreas().forEach(a -> System.out.println(a.exibirInformacoes()));
                        break;
                    case 6: registrarMissao(); break;
                    case 7: mostrarMissoes(central.listarMissoes()); break;
                    case 8:
                        String idRobo = lerTexto("Id do robo: ");
                        String idMissao = lerTexto("Id da missao: ");
                        central.atribuirRoboMissao(idRobo, idMissao);
                        System.out.println("Robo reservado.");
                        break;
                    case 9:
                        central.iniciarMissao(lerTexto("Id da missao: "));
                        System.out.println("Missao em andamento. Opcao 10 executa e conclui.");
                        break;
                    case 10:
                        central.executarMissao(lerTexto("Id da missao: "));
                        break;
                    case 11:
                        central.cancelarMissao(lerTexto("Id da missao: "));
                        System.out.println("Missao cancelada.");
                        break;
                    case 12: mostrarMissoes(central.listarMissoesPendentes()); break;
                    case 13: System.out.println(central.atenderProximaPendente()); break;
                    case 14: System.out.println(central.simularAutonomia(lerTexto("Id da missao: "))); break;
                    case 15: central.recarregarRobo(lerTexto("Id do robo: ")); break;
                    case 16: manutencao(); break;
                    case 17:
                        String idArea = lerTexto("Id da area: ");
                        central.atualizarRiscoArea(idArea, lerRisco());
                        System.out.println("Risco atualizado.");
                        break;
                    case 18: comunicacao(); break;
                    case 19:
                        Relatorio relatorio = central.gerarRelatorioGeral();
                        System.out.println(relatorio.gerarResumo());
                        relatorio.salvarEmArquivo("relatorios/relatorio_geral.txt");
                        System.out.println("Relatorio salvo em relatorios/relatorio_geral.txt");
                        break;
                    case 20: salvar(); break;
                    case 21:
                        DadosExemplo.preencher(central);
                        System.out.println("Exemplos cadastrados.");
                        break;
                    case 22:
                        central.removerAtribuicao(lerTexto("Id da missao: "));
                        System.out.println("Atribuicao retirada; missao continua na fila.");
                        break;
                    case 23:
                        mostrarMissoes(central.consultarHistoricoMissoes());
                        central.getHistoricoOperacoes().forEach(h -> System.out.println(h));
                        break;
                    case 0:
                        salvar();
                        continuar = false;
                        System.out.println("Programa encerrado.");
                        break;
                    default: System.out.println("Opcao inexistente. Escolha uma opcao do menu.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Dados invalidos: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Operacao recusada: " + e.getMessage());
            } catch (IOException e) {
                // Se salvar na saida falhar, o loop continua para permitir outra tentativa.
                System.out.println("Erro de arquivo: " + e.getMessage());
            } catch (NoSuchElementException e) {
                System.out.println("Entrada encerrada. Tentando salvar os dados.");
                try {
                    salvar();
                } catch (IOException erro) {
                    System.out.println("Nao foi possivel salvar: " + erro.getMessage());
                }
                continuar = false;
            }
        }
    }

    private void exibirMenu() {
        System.out.println("\nCENTRAL DE RESGATE ROBOTICO");
        System.out.println("1 Cadastrar robo             | 2 Consultar robos");
        System.out.println("3 Robos disponiveis          | 4 Cadastrar area");
        System.out.println("5 Consultar areas            | 6 Registrar missao");
        System.out.println("7 Consultar missoes          | 8 Atribuir robo");
        System.out.println("9 Iniciar missao             | 10 Executar e concluir missao");
        System.out.println("11 Cancelar missao           | 12 Fila de pendencias");
        System.out.println("13 Atender proxima da fila   | 14 Simular autonomia e recomendar");
        System.out.println("15 Recarregar robo           | 16 Manutencao");
        System.out.println("17 Atualizar risco           | 18 Comunicacao e mensagens");
        System.out.println("19 Gerar e salvar relatorio  | 20 Salvar dados");
        System.out.println("21 Criar dados de exemplo    | 22 Retirar atribuicao");
        System.out.println("23 Historico das operacoes   | 0 Salvar e sair");
    }

    private void cadastrarRobo() {
        int tipo = lerFaixa("Tipo 1 Drone, 2 Terrestre, 3 Anfibio: ", 1, 3);
        String id = lerTexto("Id: ");
        String nome = lerTexto("Nome: ");
        int bateria = lerFaixa("Bateria de 0 a 100: ", 0, 100);
        Robo robo;
        if (tipo == 1) {
            robo = new Drone(id, nome, bateria, lerInteiro("Altitude maxima em metros: "));
        } else if (tipo == 2) {
            robo = new RoboTerrestre(id, nome, bateria, lerInteiro("Capacidade de carga em kg: "));
        } else {
            robo = new RoboAnfibio(id, nome, bateria, lerInteiro("Capacidade de carga em kg: "));
        }
        central.cadastrarRobo(robo);
        System.out.println("Robo cadastrado.");
    }

    private void cadastrarArea() {
        String id = lerTexto("Id: ");
        String nome = lerTexto("Nome: ");
        String localizacao = lerTexto("Localizacao: ");
        TipoTerreno terreno = lerTerreno();
        NivelRisco risco = lerRisco();
        String descricao = lerTexto("Descricao: ");
        central.cadastrarArea(new AreaOperacao(id, nome, localizacao, terreno, risco, descricao));
        System.out.println("Area cadastrada.");
    }

    private void registrarMissao() {
        String id = lerTexto("Id: ");
        String descricao = lerTexto("Descricao: ");
        TipoMissao tipo = lerTipoMissao();
        AreaOperacao area = central.buscarArea(lerTexto("Id da area cadastrada: "));
        PrioridadeMissao prioridade = lerPrioridade();
        int carga = 0;
        if (tipo == TipoMissao.TRANSPORTE_SUPRIMENTOS) {
            carga = lerInteiro("Peso dos suprimentos em kg: ");
        }
        central.registrarMissao(new Missao(id, descricao, tipo, area, prioridade, carga));
        System.out.println("Missao registrada na fila.");
    }

    private void manutencao() {
        String id = lerTexto("Id do robo: ");
        int opcao = lerFaixa("1 Iniciar manutencao, 2 Finalizar: ", 1, 2);
        if (opcao == 1) {
            central.iniciarManutencao(id);
        } else {
            central.finalizarManutencao(id);
        }
        System.out.println("Estado de manutencao atualizado.");
    }

    private void comunicacao() {
        String id = lerTexto("Id do robo: ");
        int opcao = lerFaixa("1 Central envia, 2 Robo informa, 3 Historico: ", 1, 3);
        if (opcao == 1) {
            central.enviarInstrucao(id, lerTexto("Mensagem: "));
        } else if (opcao == 2) {
            central.receberInformacao(id, lerTexto("Mensagem: "));
        } else {
            List<String> mensagens = central.buscarRobo(id).getHistoricoMensagens();
            if (mensagens.isEmpty()) { System.out.println("Nenhuma mensagem registrada."); }
            mensagens.forEach(m -> System.out.println(m));
        }
    }

    private void mostrarRobos(List<Robo> robos) {
        if (robos.isEmpty()) { System.out.println("Nenhum robo encontrado."); }
        robos.forEach(r -> System.out.println(r.exibirResumo()));
    }

    private void mostrarMissoes(List<Missao> missoes) {
        if (missoes.isEmpty()) { System.out.println("Nenhuma missao encontrada."); }
        missoes.forEach(m -> System.out.println(m.exibirResumo()));
    }

    private String lerTexto(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = teclado.nextLine().trim();
            if (!texto.isEmpty()) { return texto; }
            System.out.println("Preencha o campo.");
        }
    }

    private int lerInteiro(String mensagem) {
        while (true) {
            try {
                // Ler a linha inteira evita a quebra de linha sobrando depois de nextInt().
                return Integer.parseInt(lerTexto(mensagem));
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero inteiro valido.");
            }
        }
    }

    private int lerFaixa(String mensagem, int minimo, int maximo) {
        while (true) {
            int numero = lerInteiro(mensagem);
            if (numero >= minimo && numero <= maximo) { return numero; }
            System.out.println("Informe um valor entre " + minimo + " e " + maximo + ".");
        }
    }

    private TipoTerreno lerTerreno() {
        int opcao = lerFaixa("Terreno 1 Urbano, 2 Alagado, 3 Irregular: ", 1, 3);
        return TipoTerreno.values()[opcao - 1];
    }

    private NivelRisco lerRisco() {
        int opcao = lerFaixa("Risco 1 Baixo, 2 Medio, 3 Alto: ", 1, 3);
        return NivelRisco.values()[opcao - 1];
    }

    private TipoMissao lerTipoMissao() {
        int opcao = lerFaixa("Missao 1 Reconhecimento, 2 Localizar pessoas, 3 Transportar: ", 1, 3);
        return TipoMissao.values()[opcao - 1];
    }

    private PrioridadeMissao lerPrioridade() {
        int opcao = lerFaixa("Prioridade 1 Baixa, 2 Media, 3 Alta: ", 1, 3);
        return PrioridadeMissao.values()[opcao - 1];
    }

    private void salvar() throws IOException {
        Persistencia.salvar(central, arquivoDados);
        System.out.println("Dados salvos em " + arquivoDados);
    }
}
