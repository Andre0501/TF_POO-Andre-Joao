import java.io.Serializable;

public class AreaOperacao implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nome;
    private String localizacao;
    private TipoTerreno tipoTerreno;
    private NivelRisco nivelRisco;
    private String descricao;

    public AreaOperacao(String id, String nome, String localizacao,
            TipoTerreno tipoTerreno, NivelRisco nivelRisco, String descricao) {
        if (id == null || id.trim().isEmpty() || nome == null || nome.trim().isEmpty()
                || localizacao == null || localizacao.trim().isEmpty()
                || descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Os textos da area devem ser preenchidos.");
        }
        if (tipoTerreno == null || nivelRisco == null) {
            throw new IllegalArgumentException("Terreno e risco devem ser informados.");
        }
        this.id = id.trim();
        this.nome = nome.trim();
        this.localizacao = localizacao.trim();
        this.tipoTerreno = tipoTerreno;
        this.nivelRisco = nivelRisco;
        this.descricao = descricao.trim();
    }

    public void atualizarNivelRisco(NivelRisco novoNivel) {
        if (novoNivel == null) {
            throw new IllegalArgumentException("O novo risco deve ser informado.");
        }
        nivelRisco = novoNivel;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getLocalizacao() { return localizacao; }
    public TipoTerreno getTipoTerreno() { return tipoTerreno; }
    public NivelRisco getNivelRisco() { return nivelRisco; }
    public String getDescricao() { return descricao; }

    public String exibirInformacoes() {
        return id + " | " + nome + " | " + localizacao + " | " + tipoTerreno
                + " | risco " + nivelRisco + " | " + descricao;
    }

    @Override
    public String toString() {
        return exibirInformacoes();
    }
}
