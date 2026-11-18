public class RoboTerrestre extends Robo {
    private static final long serialVersionUID = 1L;
    private int capacidadeCarga;

    public RoboTerrestre(String id, String nome, int bateria, int capacidadeCarga) {
        super(id, nome, bateria);
        if (capacidadeCarga <= 0) {
            throw new IllegalArgumentException("A capacidade de carga deve ser positiva.");
        }
        this.capacidadeCarga = capacidadeCarga;
    }

    @Override
    public boolean podeExecutar(Missao missao) {
        return missao != null && missao.getTipo() == TipoMissao.TRANSPORTE_SUPRIMENTOS
                && missao.getArea().getTipoTerreno() != TipoTerreno.ALAGADO
                && missao.getPesoSuprimentos() <= capacidadeCarga;
    }

    @Override
    public int calcularConsumo(Missao missao) {
        if (!podeExecutar(missao)) {
            throw new IllegalArgumentException("Robo terrestre nao executa esta missao.");
        }
        int consumo = 20 + adicionalRisco(missao);
        if (missao.getArea().getTipoTerreno() == TipoTerreno.IRREGULAR) {
            consumo = consumo + 10;
        }
        return consumo;
    }

    @Override
    public void executarMissao(Missao missao) {
        validarMissaoEmAndamento(missao);
        missao.concluir("Entrega terrestre simulada de " + missao.getPesoSuprimentos()
                + " kg concluida em " + missao.getArea().getNome() + ".");
    }

    public int getCapacidadeCarga() { return capacidadeCarga; }

    @Override
    public String exibirResumo() {
        return super.exibirResumo() + " | Terrestre | capacidade " + capacidadeCarga + " kg";
    }
}
