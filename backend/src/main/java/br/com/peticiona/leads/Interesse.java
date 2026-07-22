package br.com.peticiona.leads;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Um lead do formulário de interesse.
 *
 * <p>Sem vínculo com {@code usuarios}: quem preenche o formulário ainda não tem conta — é
 * exatamente por isso que preencheu.
 */
@Entity
@Table(name = "interesses")
public class Interesse {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String interesse;

    @Column(columnDefinition = "text")
    private String mensagem;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    protected Interesse() {
        // exigido pelo JPA
    }

    public Interesse(String nome, String email, String interesse, String mensagem) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.email = email;
        this.interesse = interesse;
        this.mensagem = mensagem;
        this.criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getInteresse() { return interesse; }
    public String getMensagem() { return mensagem; }
    public Instant getCriadoEm() { return criadoEm; }
}
