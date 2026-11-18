// Funcionalidade extra: um terceiro tipo de robo, usando a mesma heranca ja estudada.
public class RoboAnfibio extends Robo {
    private static final long serialVersionUID = 1L;
    private int capacidadeCarga;

    public RoboAnfibio(String id, String nome, int bateria, int capacidadeCarga) {
        super(id, nome, bateria);
        if (capacidadeCarga <= 0) {
            throw new IllegalArgumentException("A capacidade de carga deve ser positiva.");
        }
        this.capacidadeCarga = capacidadeCarga;
    }

    @Override
    public boolean podeExecutar(Missao missao) {
        return missao != null && missao.getTipo() == TipoMissao.TRANSPORTE_SUPRIMENTOS
                && missao.getArea().getTipoTerreno() != TipoTerreno.IRREGULAR
                && missao.getPesoSuprimentos() <= capacidadeCarga;
    }

    @Override
    public int calcularConsumo(Missao missao) {
        if (!podeExecutar(missao)) {
            throw new IllegalArgumentException("Robo anfibio nao executa esta missao.");
        }
        // No solo urbano ele gasta mais que o terrestre; no alagado ele pode navegar.
        int consumo = 25 + adicionalRisco(missao);
        if (missao.getArea().getTipoTerreno() == TipoTerreno.ALAGADO) {
            consumo = consumo + 10;
        }
        return consumo;
    }

    @Override
    public void executarMissao(Missao missao) {
        validarMissaoEmAndamento(missao);
        missao.concluir("Entrega anfibia simulada de " + missao.getPesoSuprimentos()
                + " kg concluida em " + missao.getArea().getNome() + ".");
    }

    public int getCapacidadeCarga() { return capacidadeCarga; }

    @Override
    public String exibirResumo() {
        return super.exibirResumo() + " | Anfibio | capacidade " + capacidadeCarga + " kg";
    }
}
