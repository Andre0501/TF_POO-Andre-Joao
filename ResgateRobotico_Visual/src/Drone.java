public class Drone extends Robo {
    private static final long serialVersionUID = 1L;
    private int altitudeMaxima;

    public Drone(String id, String nome, int bateria, int altitudeMaxima) {
        super(id, nome, bateria);
        if (altitudeMaxima <= 0) {
            throw new IllegalArgumentException("A altitude maxima deve ser positiva.");
        }
        this.altitudeMaxima = altitudeMaxima;
    }

    @Override
    public boolean podeExecutar(Missao missao) {
        return missao != null && (missao.getTipo() == TipoMissao.RECONHECIMENTO
                || missao.getTipo() == TipoMissao.LOCALIZACAO_PESSOAS);
    }

    @Override
    public int calcularConsumo(Missao missao) {
        if (!podeExecutar(missao)) {
            throw new IllegalArgumentException("Drone nao executa esta missao.");
        }
        int consumo = 15;
        if (missao.getTipo() == TipoMissao.LOCALIZACAO_PESSOAS) {
            consumo = 25;
        }
        return consumo + adicionalRisco(missao);
    }

    @Override
    public void executarMissao(Missao missao) {
        validarMissaoEmAndamento(missao);
        String resultado = "Reconhecimento aereo simulado concluido em " + missao.getArea().getNome()
                + ". Risco observado: " + missao.getArea().getNivelRisco() + ".";
        if (missao.getTipo() == TipoMissao.LOCALIZACAO_PESSOAS) {
            resultado = "Busca aerea simulada concluida em " + missao.getArea().getNome()
                    + ". Um sinal de pessoa foi localizado na simulacao.";
        }
        missao.concluir(resultado);
    }

    public int getAltitudeMaxima() { return altitudeMaxima; }

    @Override
    public String exibirResumo() {
        return super.exibirResumo() + " | Drone | altitude maxima " + altitudeMaxima + " m";
    }
}
