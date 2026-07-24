package br.com.peticiona.config;

import br.com.peticiona.auth.Usuario;
import br.com.peticiona.auth.UsuarioRepository;
import br.com.peticiona.demo.AcervoDemo;
import br.com.peticiona.demo.ClassificacaoIntimacao;
import br.com.peticiona.demo.IntimacaoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Perfil <b>dev</b>: roda o app inteiro localmente SEM Google e SEM DJEN, para desenvolver
 * e conferir as telas do acervo (Seu dia, intimação, perfil).
 *
 * <p><b>Nunca afeta produção.</b> Tudo aqui é {@code @Profile("dev")} e só liga com
 * {@code SPRING_PROFILES_ACTIVE=dev}. O {@link br.com.peticiona.auth.SegurancaConfig} real é
 * {@code @Profile("!dev")}, então em produção continua sendo ele — com login Google — quem
 * manda. Em dev, este arquivo substitui a segurança por um autologin fixo.
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class PerfilDeDesenvolvimento {

    /** Identidade fixa do usuário de desenvolvimento (o {@code sub} que o Google daria). */
    static final String SUB_DEV = "dev-local-sub";

    @Bean
    SecurityFilterChain filtrosDev(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(rotas -> rotas.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(new AutoLoginDev(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Injeta um usuário fixo já autenticado em toda requisição — só no perfil dev. */
    static final class AutoLoginDev extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
                throws ServletException, IOException {
            var atual = SecurityContextHolder.getContext().getAuthentication();
            if (atual == null || !atual.isAuthenticated()) {
                OAuth2User principal = new DefaultOAuth2User(
                        List.of(new SimpleGrantedAuthority("ROLE_USER")),
                        Map.of("sub", SUB_DEV,
                               "email", "dev@peticiona.local",
                               "name", "Dra. Ana Advogada (dev)"),
                        "sub");
                SecurityContextHolder.getContext().setAuthentication(
                        new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google"));
            }
            chain.doFilter(req, resp);
        }
    }

    /**
     * Cria o usuário dev (se preciso) e semeia o acervo de exemplo, deixando a intimação mais
     * recente já "lida pela IA" — com prazo e rascunho — para a tela de detalhe exibir o
     * fluxo completo sem depender do n8n.
     */
    @Bean
    ApplicationRunner seedDev(UsuarioRepository usuarios, AcervoDemo acervo, IntimacaoRepository intimacoes) {
        return args -> {
            Usuario dono = usuarios.findByGoogleSub(SUB_DEV).orElseGet(() -> {
                Usuario u = new Usuario(SUB_DEV, "dev@peticiona.local", "Dra. Ana Advogada (dev)",
                        "https://ui-avatars.com/api/?name=Ana+Advogada&background=14213d&color=ffffff");
                u.setOabNumero("123456");
                u.setOabUf("MG");
                u.registrarSincronizacao(LocalDate.now());
                return usuarios.save(u);
            });

            acervo.carregarExemplos(dono);

            intimacoes.findByUsuarioOrderByCriadoEmAsc(dono).stream()
                    .filter(i -> i.getClassificacao() == null)
                    .reduce((primeiro, ultimo) -> ultimo)
                    .ifPresent(i -> {
                        i.setClassificacao(new ClassificacaoIntimacao(
                                "Despacho de intimação para juntada de documentos",
                                5, "DIAS_UTEIS",
                                "Juntar os contratos de empreitada e os comprovantes de pagamento do período de 02/2024 a 09/2025.",
                                "PETICAO_SIMPLES", "MEDIA", 0.86,
                                "Junte a reclamada, no prazo de 5 (cinco) dias, os contratos de empreitada mencionados em sua defesa, bem como os comprovantes de pagamento correspondentes."));
                        i.marcarConcluido();
                        i.setDataVencimento(LocalDate.now().plusDays(7));
                        i.setRascunho(RASCUNHO_EXEMPLO);
                        intimacoes.save(i);
                    });
        };
    }

    private static final String RASCUNHO_EXEMPLO = """
            EXCELENTÍSSIMO(A) SENHOR(A) DOUTOR(A) JUIZ(A) DO TRABALHO DA 9ª VARA DO TRABALHO DE BELO HORIZONTE/MG

            Processo nº 0007733-05.2026.5.03.0009

            CONSTRUTORA VALE AURORA LTDA., já qualificada nos autos da reclamação trabalhista em epígrafe, vem, \
            respeitosamente, à presença de Vossa Excelência, por seu advogado que esta subscreve, em atenção ao \
            r. despacho de fls. [FOLHA], apresentar os DOCUMENTOS ora requeridos, pelos fatos e fundamentos a seguir expostos.

            1. Em cumprimento à determinação de juntada, a reclamada acosta a estes autos os contratos de empreitada \
            [DESCREVER OS CONTRATOS] celebrados com o reclamante, bem como os respectivos comprovantes de pagamento \
            relativos ao período de fevereiro de 2024 a setembro de 2025.

            2. Os documentos ora juntados demonstram [ARGUMENTO — preencher com a tese da defesa quanto à natureza \
            do vínculo e à alegada empreitada].

            Requer, por fim, a juntada dos documentos anexos e o regular prosseguimento do feito.

            Termos em que,
            Pede deferimento.

            Belo Horizonte, [DATA].

            [ADVOGADO(A)]
            OAB/MG [NÚMERO]""";
}
