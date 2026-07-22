package br.com.peticiona.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Quem entra onde.
 *
 * <p>O produto tem duas metades de propósito diferente e elas exigem regras diferentes:
 *
 * <ul>
 *   <li><b>A calculadora de prazos é pública.</b> Além de ser o núcleo do produto, ela é o
 *       ímã de leads da estratégia de conteúdo (doc 07) — fica aberta no site, sem
 *       cadastro. Exigir login nela mataria a porta de entrada.</li>
 *   <li><b>O acervo é privado.</b> São processos de clientes reais; nada em
 *       {@code /api/demo} responde sem sessão.</li>
 * </ul>
 *
 * <p>O callback do n8n é o caso estranho: é servidor falando com servidor, sem sessão
 * possível. Ele fica fora da autenticação por sessão e é protegido por segredo
 * compartilhado no {@link TokenDeCallbackFilter}.
 */
@Configuration
@EnableWebSecurity
public class SegurancaConfig {

    /** O caminho do callback, também usado pelo filtro de token. */
    static final String ROTA_CALLBACK = "/api/demo/intimacoes/*/resultado";

    private final UsuarioOidcService usuarioOidcService;
    private final TokenDeCallbackFilter tokenDeCallback;

    public SegurancaConfig(UsuarioOidcService usuarioOidcService, TokenDeCallbackFilter tokenDeCallback) {
        this.usuarioOidcService = usuarioOidcService;
        this.tokenDeCallback = tokenDeCallback;
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(rotas -> rotas
                // O front Angular e suas rotas de navegação.
                .requestMatchers("/", "/login", "/calculadora", "/index.html", "/favicon.ico",
                        "/*.js", "/*.css", "/assets/**", "/media/**").permitAll()
                // Ímã de leads e configuração pública.
                .requestMatchers("/api/prazos/**", "/api/interesses", "/api/config").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                // Servidor para servidor: autenticado por token, não por sessão.
                .requestMatchers(HttpMethod.POST, ROTA_CALLBACK).permitAll()
                // Todo o resto do acervo exige sessão.
                .anyRequest().authenticated())

            .oauth2Login(login -> login
                // Sem isto o Spring gera a própria página em /login e ela sequestra a rota
                // do Angular: o usuário veria um "Please sign in" cru no lugar da nossa tela.
                .loginPage("/login")
                // oidcUserService, e não userService: o escopo tem openid, então o login é
                // OpenID Connect e o userService comum nunca seria chamado.
                .userInfoEndpoint(info -> info.oidcUserService(usuarioOidcService))
                .defaultSuccessUrl("/", true))

            .logout(sair -> sair
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/login")
                .deleteCookies("JSESSIONID"))

            // Requisição de API sem sessão deve receber 401 e deixar o front decidir o
            // que fazer. O padrão do Spring é redirecionar para o Google, o que devolveria
            // um HTML de login dentro de um fetch() esperando JSON.
            .exceptionHandling(erros -> erros
                .defaultAuthenticationEntryPointFor(
                        (req, resp, ex) -> resp.sendError(HttpServletResponse.SC_UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**")))

            // CSRF desligado porque não há formulário HTML com sessão: o front chama JSON
            // e o callback do n8n tem segredo próprio. Ao introduzir qualquer POST de
            // formulário servido por nós, isto precisa voltar.
            .csrf(csrf -> csrf.disable())

            .addFilterBefore(tokenDeCallback,
                    org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
