package br.com.peticiona.auth;

import br.com.peticiona.demo.AcervoDemo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transforma um login do Google num usuário nosso.
 *
 * <p>Roda a cada login: cria o usuário na primeira vez e atualiza nome e foto nas
 * seguintes. A chave é o {@code sub} do Google — estável mesmo que a pessoa troque o
 * e-mail da conta. Casar por e-mail daria um acervo vazio a quem mudasse de endereço.
 *
 * <p>O acervo de demonstração é semeado aqui, no mesmo passo do cadastro, para que o
 * primeiro login já caia numa tela com conteúdo — e não num vazio que não demonstra nada.
 *
 * <p>Quem chama é o {@link UsuarioOidcService}. Esta classe não estende nenhum serviço do
 * Spring de propósito: qual serviço o Spring usa depende do escopo pedido, e amarrar a
 * regra de cadastro a essa escolha já custou caro uma vez.
 */
@Service
public class RegistroDeUsuario {

    private final UsuarioRepository usuarios;
    private final AcervoDemo acervo;

    public RegistroDeUsuario(UsuarioRepository usuarios, AcervoDemo acervo) {
        this.usuarios = usuarios;
        this.acervo = acervo;
    }

    @Transactional
    public Usuario registrar(String sub, String email, String nome, String fotoUrl) {
        if (sub == null || email == null) {
            // Sem identificador estável não há como amarrar o acervo a ninguém.
            throw new IllegalArgumentException("A conta Google não devolveu sub e e-mail.");
        }

        Usuario usuario = usuarios.findByGoogleSub(sub)
                .orElseGet(() -> usuarios.save(new Usuario(sub, email, nome, fotoUrl)));
        usuario.registrarAcesso(nome, fotoUrl);

        // Idempotente: só semeia quando o acervo está vazio, então reentrar não duplica
        // nada e quem apagou tudo de propósito continua com o acervo vazio.
        acervo.semearSeVazio(usuario);

        return usuario;
    }
}
