package br.com.peticiona.auth;

import br.com.peticiona.demo.AcervoDemo;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transforma um login do Google num usuário nosso.
 *
 * <p>Roda a cada login: cria o usuário na primeira vez e atualiza nome e foto nas
 * seguintes. A chave é o {@code sub} do Google — estável mesmo que a pessoa troque o
 * e-mail da conta. Casar por e-mail daria um acervo vazio para quem mudasse de endereço.
 *
 * <p>O acervo de demonstração é semeado aqui, no mesmo passo do cadastro, para que o
 * primeiro login já caia numa tela com conteúdo — e não num vazio que não demonstra nada.
 */
@Service
public class RegistroDeUsuario extends DefaultOAuth2UserService {

    private final UsuarioRepository usuarios;
    private final AcervoDemo acervo;

    public RegistroDeUsuario(UsuarioRepository usuarios, AcervoDemo acervo) {
        this.usuarios = usuarios;
        this.acervo = acervo;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User doGoogle = super.loadUser(request);

        String sub = doGoogle.getAttribute("sub");
        String email = doGoogle.getAttribute("email");
        String nome = doGoogle.getAttribute("name");
        String foto = doGoogle.getAttribute("picture");

        if (sub == null || email == null) {
            // Sem identificador estável não há como amarrar o acervo a ninguém.
            throw new OAuth2AuthenticationException("A conta Google não devolveu sub e e-mail.");
        }

        Usuario usuario = usuarios.findByGoogleSub(sub)
                .orElseGet(() -> usuarios.save(new Usuario(sub, email, nome, foto)));
        usuario.registrarAcesso(nome, foto);

        // Idempotente: só semeia quando o acervo está vazio, então reentrar não duplica
        // nada e quem apagou tudo de propósito continua com o acervo vazio.
        acervo.semearSeVazio(usuario);

        return doGoogle;
    }
}
