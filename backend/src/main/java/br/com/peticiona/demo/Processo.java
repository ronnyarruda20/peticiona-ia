package br.com.peticiona.demo;

import br.com.peticiona.auth.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Um processo do escritório, pertencente a um advogado.
 *
 * <p>Na Fase 1 real isto vem do DataJud/CNJ pela OAB do advogado (doc 04, §1). Hoje é
 * semeado no primeiro login para que a demonstração tenha o que mostrar.
 *
 * <p>O {@code usuario} não é decoração: é o que impede o acervo de um advogado de
 * aparecer para outro. Toda consulta a processos passa por ele.
 */
@Entity
@Table(name = "processos")
public class Processo {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String numero;

    private String cliente;

    @Column(name = "parte_contraria")
    private String parteContraria;

    private String vara;
    private String area;
    private String fase;

    @Column(columnDefinition = "text")
    private String resumo;

    // ── Origem no DJEN ────────────────────────────────────────────
    @Column(name = "numero_sem_mascara")
    private String numeroSemMascara;

    private String tribunal;
    private String classe;

    @Column(nullable = false)
    private String origem = "DEMO";

    @Column(name = "partes_polo_ativo", columnDefinition = "text")
    private String partesPoloAtivo;

    @Column(name = "partes_polo_passivo", columnDefinition = "text")
    private String partesPoloPassivo;

    /** {@code 'A'} ou {@code 'P'}. Nulo enquanto o advogado não disser de que lado está. */
    @Column(name = "cliente_polo")
    private String clientePolo;

    protected Processo() {
        // exigido pelo JPA
    }

    /** Processo descoberto numa publicação do DJEN — ainda sem saber quem é o cliente. */
    public static Processo doDjen(Usuario usuario, String numeroComMascara, String numeroSemMascara,
                                  String vara, String tribunal, String classe,
                                  String partesPoloAtivo, String partesPoloPassivo) {
        Processo p = new Processo(usuario, numeroComMascara, null, null, vara, "Trabalhista", null, null);
        p.numeroSemMascara = numeroSemMascara;
        p.tribunal = tribunal;
        p.classe = classe;
        p.origem = "DJEN";
        p.partesPoloAtivo = partesPoloAtivo;
        p.partesPoloPassivo = partesPoloPassivo;
        return p;
    }

    public Processo(Usuario usuario, String numero, String cliente, String parteContraria,
                    String vara, String area, String fase, String resumo) {
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.numero = numero;
        this.cliente = cliente;
        this.parteContraria = parteContraria;
        this.vara = vara;
        this.area = area;
        this.fase = fase;
        this.resumo = resumo;
    }

    /**
     * O advogado disse de que lado está; agora o processo serve aos prompts.
     *
     * <p>Enquanto isto não acontece, {@code cliente} e {@code parteContraria} são nulos — e
     * é por isso que a IA fica bloqueada. O redator escreve a peça <i>defendendo</i> alguém;
     * sem saber quem, ele escreveria para o lado errado, que é pior do que não escrever.
     *
     * @param polo {@code 'A'} para o polo ativo, {@code 'P'} para o passivo
     */
    public void confirmarCliente(String polo) {
        if (!"A".equals(polo) && !"P".equals(polo)) {
            throw new IllegalArgumentException("Polo deve ser 'A' (ativo) ou 'P' (passivo).");
        }
        this.clientePolo = polo;
        boolean ativo = "A".equals(polo);
        this.cliente = ativo ? partesPoloAtivo : partesPoloPassivo;
        this.parteContraria = ativo ? partesPoloPassivo : partesPoloAtivo;
        // A fase entra no prompt; sem ela o redator não sabe em que momento do rito está.
        this.fase = ativo ? "Polo ativo (reclamante)" : "Polo passivo (reclamado)";
    }

    /** Falta perguntar de que lado o advogado está? Processos semeados já nascem prontos. */
    public boolean aguardaConfirmacaoDeCliente() {
        return "DJEN".equals(origem) && clientePolo == null;
    }

    public UUID getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getNumero() { return numero; }
    public String getCliente() { return cliente; }
    public String getParteContraria() { return parteContraria; }
    public String getVara() { return vara; }
    public String getArea() { return area; }
    public String getFase() { return fase; }
    public String getResumo() { return resumo; }

    public String getNumeroSemMascara() { return numeroSemMascara; }
    public String getTribunal() { return tribunal; }
    public String getClasse() { return classe; }
    public String getOrigem() { return origem; }
    public String getPartesPoloAtivo() { return partesPoloAtivo; }
    public String getPartesPoloPassivo() { return partesPoloPassivo; }
    public String getClientePolo() { return clientePolo; }
}
