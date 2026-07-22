package br.com.peticiona.demo;

import br.com.peticiona.auth.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Uma publicação capturada — o gatilho de todo o fluxo.
 *
 * <p>O texto é o que chega do DJEN: juridiquês corrido, sem estrutura. Ler isso e extrair
 * "que ato é, quantos dias, o que fazer" é exatamente o trabalho que a IA assume.
 *
 * <p>A classificação fica <b>achatada em colunas</b>, não guardada como JSON: o
 * {@code prazoEmDias} e o {@code tipoContagem} alimentam a
 * {@link br.com.peticiona.prazo.CalculadoraPrazo} e precisam ser tipados e consultáveis.
 * {@link #getClassificacao()} remonta o record que o resto do sistema já conhece.
 */
@Entity
@Table(name = "intimacoes")
public class Intimacao {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @Column(name = "data_publicacao", nullable = false)
    private LocalDate dataPublicacao;

    private String orgao;

    @Column(nullable = false, columnDefinition = "text")
    private String texto;

    // ── A leitura da IA, campo a campo ────────────────────────────
    @Column(name = "tipo_ato")
    private String tipoAto;

    @Column(name = "prazo_em_dias")
    private Integer prazoEmDias;

    @Column(name = "tipo_contagem")
    private String tipoContagem;

    @Column(columnDefinition = "text")
    private String providencia;

    @Column(name = "tipo_peca_sugerida")
    private String tipoPecaSugerida;

    private String urgencia;

    private BigDecimal confianca;

    @Column(columnDefinition = "text")
    private String fundamentacao;

    /** Data-limite calculada pelo motor determinístico, nunca pelo LLM (doc 06). */
    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    /** Último rascunho gerado. Nulo = nada rascunhado ainda. */
    @Column(columnDefinition = "text")
    private String rascunho;

    /** Verdadeiro entre o disparo do fluxo de IA e a chegada do callback. */
    @Column(nullable = false)
    private boolean processando;

    /** O que deu errado no fluxo, para a tela dizer algo melhor que "erro". */
    @Column(name = "erro_ia", columnDefinition = "text")
    private String erroIa;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    protected Intimacao() {
        // exigido pelo JPA
    }

    public Intimacao(Usuario usuario, Processo processo, LocalDate dataPublicacao,
                     String orgao, String texto) {
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.processo = processo;
        this.dataPublicacao = dataPublicacao;
        this.orgao = orgao;
        this.texto = texto;
        this.criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Processo getProcesso() { return processo; }
    public LocalDate getDataPublicacao() { return dataPublicacao; }
    public String getOrgao() { return orgao; }
    public String getTexto() { return texto; }

    /** Remonta o record a partir das colunas. Nulo enquanto a IA não leu. */
    public ClassificacaoIntimacao getClassificacao() {
        if (tipoAto == null) {
            return null;
        }
        return new ClassificacaoIntimacao(
                tipoAto,
                prazoEmDias == null ? 0 : prazoEmDias,
                tipoContagem,
                providencia,
                tipoPecaSugerida,
                urgencia,
                confianca == null ? 0.0 : confianca.doubleValue(),
                fundamentacao);
    }

    public void setClassificacao(ClassificacaoIntimacao c) {
        if (c == null) {
            return;
        }
        this.tipoAto = c.tipoAto();
        this.prazoEmDias = c.prazoEmDias();
        this.tipoContagem = c.tipoContagem();
        this.providencia = c.providencia();
        this.tipoPecaSugerida = c.tipoPecaSugerida();
        this.urgencia = c.urgencia();
        this.confianca = BigDecimal.valueOf(c.confianca());
        this.fundamentacao = c.fundamentacao();
    }

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
        if (tipoAto != null) return "PRAZO_NA_AGENDA";
        return "NAO_LIDA";
    }
}
