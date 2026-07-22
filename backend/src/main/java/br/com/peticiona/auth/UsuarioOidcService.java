package br.com.peticiona.auth;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Cadastra o usuário no meio do login do Google.
 *
 * <p><b>Por que OIDC e não OAuth2 puro:</b> o escopo pedido inclui {@code openid}, então o
 * Google devolve um ID token e o Spring trata o login como OpenID Connect. Nesse caminho
 * quem carrega o perfil é o {@link OidcUserService} — o {@code userService} comum
 * simplesmente não é chamado.
 *
 * <p>Esse detalhe já quebrou o login uma vez, e de um jeito difícil de ler: a autenticação
 * com o Google terminava bem, o navegador voltava para a home, e aí {@code /api/me}
 * respondia 401 porque nenhum usuário havia sido gravado. O sintoma aparecia longe da causa,
 * como um laço entre a home e a tela de login.
 */
@Service
public class UsuarioOidcService extends OidcUserService {

    private final RegistroDeUsuario registro;

    public UsuarioOidcService(RegistroDeUsuario registro) {
        this.registro = registro;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser doGoogle = super.loadUser(request);

        registro.registrar(
                doGoogle.getSubject(),
                doGoogle.getEmail(),
                doGoogle.getFullName(),
                doGoogle.getPicture());

        return doGoogle;
    }
}
