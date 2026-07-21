package br.com.peticiona.demo;

/**
 * Um processo do escritório.
 *
 * <p>Na Fase 1 real isto vem do DataJud/CNJ pela OAB do advogado (doc 04, §1). No MVP de
 * apresentação os processos são semeados em memória — o que o cliente precisa ver é o
 * fluxo do aha moment, não o conector.
 */
public record Processo(
        String id,
        String numero,
        String cliente,
        String parteContraria,
        String vara,
        String area,
        String fase,
        String resumo
) {}
