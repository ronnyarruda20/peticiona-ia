package br.com.peticiona.demo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * O que a IA extrai de uma publicação.
 *
 * <p>Este record é o <b>contrato</b> da chamada: o SDK Java deriva o JSON Schema dele e a
 * API garante que a resposta valida contra o schema (structured outputs). Não há parsing
 * de texto livre em lugar nenhum — se um campo entrar aqui, ele volta preenchido ou a
 * chamada falha.
 *
 * <p>⚠️ Repare no que <b>não</b> está aqui: <b>data de vencimento</b>. O LLM devolve o
 * <i>número de dias</i> e o <i>regime de contagem</i>; a data sai do
 * {@link br.com.peticiona.prazo.CalculadoraPrazo}. Essa separação é a regra número um do
 * projeto (doc 06) e é o que torna o prazo auditável.
 *
 * @param tipoAto            o ato processual publicado (ex.: "Sentença", "Despacho de citação")
 * @param prazoEmDias        quantos dias a lei/decisão concede — só o número
 * @param tipoContagem       DIAS_UTEIS ou DIAS_CORRIDOS
 * @param providencia        o que o advogado precisa fazer
 * @param tipoPecaSugerida   CONTESTACAO, PETICAO_SIMPLES ou NENHUMA (fora do escopo da Fase 1)
 * @param urgencia           ALTA, MEDIA ou BAIXA
 * @param confianca          0.0 a 1.0 — abaixo de 0,7 a intimação vai para a fila de revisão
 * @param fundamentacao      o trecho da publicação que sustenta a leitura, para conferência
 */
public record ClassificacaoIntimacao(

        @JsonPropertyDescription("O ato processual publicado. Ex: 'Sentença', 'Despacho de citação', 'Intimação para manifestação sobre documentos'.")
        String tipoAto,

        @JsonPropertyDescription("Número de dias do prazo concedido. Apenas o número inteiro. Se a publicação não abrir prazo, use 0.")
        int prazoEmDias,

        @JsonPropertyDescription("Regime de contagem. Exatamente 'DIAS_UTEIS' ou 'DIAS_CORRIDOS'. Prazos processuais correm em dias úteis (CPC art. 219; CLT art. 775).")
        String tipoContagem,

        @JsonPropertyDescription("O que o advogado precisa fazer, em uma frase objetiva e no infinitivo.")
        String providencia,

        @JsonPropertyDescription("Peça a ser redigida. Exatamente 'CONTESTACAO', 'PETICAO_SIMPLES' ou 'NENHUMA'.")
        String tipoPecaSugerida,

        @JsonPropertyDescription("Urgência. Exatamente 'ALTA', 'MEDIA' ou 'BAIXA'.")
        String urgencia,

        @JsonPropertyDescription("Sua confiança na leitura, de 0.0 a 1.0. Seja honesto: publicação truncada, ambígua ou fora do seu conhecimento merece confiança baixa.")
        double confianca,

        @JsonPropertyDescription("O trecho literal da publicação que sustenta o prazo e a providência, para o advogado conferir.")
        String fundamentacao
) {

    /** Abaixo disso a intimação não entra na agenda sozinha — vai para revisão humana (doc 06). */
    public static final double LIMIAR_CONFIANCA = 0.70;

    public boolean precisaRevisao() {
        return confianca < LIMIAR_CONFIANCA;
    }
}
