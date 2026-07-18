package br.com.peticiona.prazo;

/**
 * Ramo da Justiça — determina quais feriados forenses se aplicam.
 *
 * <p>⚠️ A curadoria de feriados por tribunal é trabalho contínuo e precisa de dono
 * (doc 12). Feriados estaduais e municipais ainda NÃO estão cobertos: um feriado
 * municipal na comarca suspende expediente e desloca o vencimento. Ver
 * {@link CalendarioForense} para o que já está implementado.
 */
public enum Justica {

    /** Justiça Estadual (comum). */
    ESTADUAL("Justiça Estadual"),

    /** Justiça do Trabalho — o nicho da Fase 1. */
    TRABALHISTA("Justiça do Trabalho"),

    /** Justiça Federal — tem feriados próprios (Lei 5.010/1966, art. 62). */
    FEDERAL("Justiça Federal");

    private final String descricao;

    Justica(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
