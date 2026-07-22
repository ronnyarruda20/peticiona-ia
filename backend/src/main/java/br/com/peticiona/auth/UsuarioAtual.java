package br.com.peticiona.auth;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * Quem está logado agora.
 *
 * <p>Ponto único por onde os controllers descobrem o dono dos dados. Concentrar isso aqui
 * é o que permite dizer, olhando um lugar só, que nenhuma tela busca dado sem saber de
 * quem ele é.
 */
@Component
public class UsuarioAtual {

    private final UsuarioRepository usuarios;

    public UsuarioAtual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /**
     * O usuário da sessão.
     *
     * @throws NaoAutenticadoException quando não há sessão válida — vira 401 na borda
     */
    public Usuario obrigatorio() {
        return opcional().orElseThrow(NaoAutenticadoException::new);
    }

    public Optional<Usuario> opcional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof OAuth2User principal)) {
            return Optional.empty();
        }
        String sub = principal.getAttribute("sub");
        return sub == null ? Optional.empty() : usuarios.findByGoogleSub(sub);
    }

    /** Sessão ausente, expirada ou de um usuário que não existe mais no banco. */
    public static class NaoAutenticadoException extends RuntimeException {
        public NaoAutenticadoException() {
            super("Faça login para acessar seu acervo.");
        }
    }
}
