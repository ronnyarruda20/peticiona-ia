package br.com.peticiona.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * O advogado dono de um acervo.
 *
 * <p>A identidade é o {@code sub} do Google, não o e-mail: o e-mail de uma conta pode
 * mudar, o {@code sub} não. Identificar por e-mail criaria um usuário novo — e um acervo
 * vazio — no dia em que alguém trocasse o endereço.
 *
 * <p>Não guardamos senha. Não há hash para vazar, fluxo de recuperação para explorar nem
 * bloqueio por tentativa para implementar.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    /** Teto diário de execuções de IA por usuário. Cadastro é aberto; crédito é dinheiro. */
    public static final int LIMITE_DIARIO_IA = 20;

    @Id
    private UUID id;

    @Column(name = "google_sub", nullable = false, unique = true)
    private String googleSub;

    @Column(nullable = false, unique = true)
    private String email;

    private String nome;

    @Column(name = "foto_url")
    private String fotoUrl;

    /** Preenchida no onboarding — é a chave da futura ingestão do DJEN. */
    @Column(name = "oab_numero")
    private String oabNumero;

    @Column(name = "oab_uf")
    private String oabUf;

    @Column(name = "execucoes_no_dia", nullable = false)
    private int execucoesNoDia;

    @Column(name = "data_contagem")
    private LocalDate dataContagem;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "ultimo_acesso_em")
    private Instant ultimoAcessoEm;

    protected Usuario() {
        // exigido pelo JPA
    }

    public Usuario(String googleSub, String email, String nome, String fotoUrl) {
        this.id = UUID.randomUUID();
        this.googleSub = googleSub;
        this.email = email;
        this.nome = nome;
        this.fotoUrl = fotoUrl;
        this.criadoEm = Instant.now();
    }

    /**
     * Consome uma execução de IA do saldo do dia.
     *
     * <p>A contagem se reseta sozinha na virada do dia, comparando {@code dataContagem}
     * com hoje — assim não é preciso um job de madrugada só para zerar contadores.
     *
     * @return {@code false} quando o teto do dia já foi atingido
     */
    public boolean consumirExecucaoIa() {
        LocalDate hoje = LocalDate.now();
        if (!hoje.equals(dataContagem)) {
            dataContagem = hoje;
            execucoesNoDia = 0;
        }
        if (execucoesNoDia >= LIMITE_DIARIO_IA) {
            return false;
        }
        execucoesNoDia++;
        return true;
    }

    public void registrarAcesso(String nome, String fotoUrl) {
        // O nome e a foto vêm do Google a cada login: se o usuário os mudar lá, mudam aqui.
        this.nome = nome;
        this.fotoUrl = fotoUrl;
        this.ultimoAcessoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public String getGoogleSub() { return googleSub; }
    public String getEmail() { return email; }
    public String getNome() { return nome; }
    public String getFotoUrl() { return fotoUrl; }

    public String getOabNumero() { return oabNumero; }
    public void setOabNumero(String oabNumero) { this.oabNumero = oabNumero; }

    public String getOabUf() { return oabUf; }
    public void setOabUf(String oabUf) { this.oabUf = oabUf; }

    public int getExecucoesNoDia() { return execucoesNoDia; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getUltimoAcessoEm() { return ultimoAcessoEm; }
}
