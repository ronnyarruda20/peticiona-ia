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

    protected Processo() {
        // exigido pelo JPA
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

    public UUID getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getNumero() { return numero; }
    public String getCliente() { return cliente; }
    public String getParteContraria() { return parteContraria; }
    public String getVara() { return vara; }
    public String getArea() { return area; }
    public String getFase() { return fase; }
    public String getResumo() { return resumo; }
}
