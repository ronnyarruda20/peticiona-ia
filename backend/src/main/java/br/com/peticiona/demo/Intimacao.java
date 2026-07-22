package br.com.peticiona.demo;

import java.time.LocalDate;

/**
 * Uma publicação capturada — o gatilho de todo o fluxo.
 *
 * <p>O texto é o que chega do DJEN: juridiquês corrido, sem estrutura. Ler isso e extrair
 * "que ato é, quantos dias, o que fazer" é exatamente o trabalho que a IA assume.
 *
 * <p>Mutável de propósito: a demo guarda o resultado da classificação e o rascunho no
 * próprio objeto, para o dashboard refletir o que já foi processado na sessão.
 */
public class Intimacao {

    private final String id;
    private final String processoId;
    private final LocalDate dataPublicacao;
    private final String orgao;
    private final String texto;

    /** Preenchido pela IA sob demanda. Nulo = ainda não classificada. */
    private ClassificacaoIntimacao classificacao;

    /** Data-limite calculada pelo motor determinístico, nunca pelo LLM (doc 06). */
    private LocalDate dataVencimento;

    /** Último rascunho gerado. Nulo = nada rascunhado ainda. */
    private String rascunho;

    /** Verdadeiro entre o disparo do fluxo de IA e a chegada do callback. */
    private boolean processando;

    /** O que deu errado no fluxo, para a tela dizer algo melhor que "erro". */
    private String erroIa;

    public Intimacao(String id, String processoId, LocalDate dataPublicacao, String orgao, String texto) {
        this.id = id;
        this.processoId = processoId;
        this.dataPublicacao = dataPublicacao;
        this.orgao = orgao;
        this.texto = texto;
    }

    public String getId() { return id; }
    public String getProcessoId() { return processoId; }
    public LocalDate getDataPublicacao() { return dataPublicacao; }
    public String getOrgao() { return orgao; }
    public String getTexto() { return texto; }

    public ClassificacaoIntimacao getClassificacao() { return classificacao; }
    public void setClassificacao(ClassificacaoIntimacao classificacao) { this.classificacao = classificacao; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public String getRascunho() { return rascunho; }
    public void setRascunho(String rascunho) { this.rascunho = rascunho; }

    public boolean isProcessando() { return processando; }
    public String getErroIa() { return erroIa; }

    /** Entrou na fila do fluxo de IA. Limpa o erro anterior: é uma tentativa nova. */
    public void marcarProcessando() {
        this.processando = true;
        this.erroIa = null;
    }

    /** O fluxo terminou, com resultado ou com erro. Nos dois casos a espera acabou. */
    public void marcarFalha(String motivo) {
        this.processando = false;
        this.erroIa = motivo;
    }

    public void marcarConcluido() {
        this.processando = false;
        this.erroIa = null;
    }

    /**
     * O estado que o dashboard mostra na coluna "situação".
     *
     * <p>PROCESSANDO vem primeiro: enquanto o fluxo roda, é isso que o advogado precisa ver,
     * mesmo que já exista uma classificação de uma rodada anterior.
     */
    public String getSituacao() {
        if (processando) return "PROCESSANDO";
        if (rascunho != null) return "RASCUNHO_PRONTO";
        if (classificacao != null) return "PRAZO_NA_AGENDA";
        return "NAO_LIDA";
    }
}
