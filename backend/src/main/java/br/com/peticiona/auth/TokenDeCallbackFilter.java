package br.com.peticiona.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Confere o segredo compartilhado com o n8n no callback do fluxo de IA.
 *
 * <p>O callback é a única rota que escreve dados sem sessão de usuário — é o n8n
 * devolvendo a classificação e o rascunho. Sem esta trava, qualquer pessoa na internet
 * poderia sobrescrever a leitura e a peça de qualquer intimação com o que quisesse.
 *
 * <p><b>Segredo em branco recusa tudo</b>, em vez de liberar tudo. Esquecer de configurar
 * a variável deve quebrar o fluxo de forma visível, nunca abrir a porta em silêncio.
 */
@Component
public class TokenDeCallbackFilter extends OncePerRequestFilter {

    public static final String CABECALHO = "X-Peticiona-Token";

    private final AntPathMatcher caminhos = new AntPathMatcher();
    private final String esperado;

    public TokenDeCallbackFilter(@Value("${peticiona.callback-token:}") String esperado) {
        this.esperado = esperado == null ? "" : esperado.trim();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(req.getMethod())
                || !caminhos.match(SegurancaConfig.ROTA_CALLBACK, req.getRequestURI())) {
            chain.doFilter(req, resp);
            return;
        }

        if (esperado.isBlank() || !confere(req.getHeader(CABECALHO))) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token de callback inválido.");
            return;
        }

        chain.doFilter(req, resp);
    }

    /** Comparação em tempo constante: comparar segredo com equals() vaza o prefixo certo. */
    private boolean confere(String recebido) {
        if (recebido == null) {
            return false;
        }
        return MessageDigest.isEqual(
                recebido.getBytes(StandardCharsets.UTF_8),
                esperado.getBytes(StandardCharsets.UTF_8));
    }
}
