package br.com.peticiona.prazo;

/**
 * Como o prazo é contado.
 *
 * <p>Prazos processuais correm em dias úteis (CPC art. 219; CLT art. 775, com a
 * redação da Lei 13.467/2017). Prazos de direito material — prescrição, decadência —
 * correm em dias corridos. Quem decide qual é qual é o advogado; o motor só executa.
 */
public enum TipoContagem {

    /** Só dias úteis: exclui sábados, domingos e feriados forenses. */
    DIAS_UTEIS("dias úteis"),

    /** Todos os dias do calendário. O vencimento ainda é prorrogado se cair em dia não útil. */
    DIAS_CORRIDOS("dias corridos");

    private final String descricao;

    TipoContagem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
