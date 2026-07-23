package br.com.peticiona.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.peticiona.BancoDeTeste;
import br.com.peticiona.demo.IntimacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * O login do Google é OpenID Connect, e o cadastro precisa acontecer nesse caminho.
 *
 * <p>Este teste existe por causa de um bug real em produção. O {@code userService} comum
 * estava registrado, mas como o escopo pedido inclui {@code openid} o Spring usa o
 * {@code OidcUserService} — e o nosso código de cadastro nunca rodava. O sintoma aparecia
 * longe da causa: a autenticação com o Google terminava bem, o navegador voltava para a
 * home, e então {@code /api/me} respondia 401 porque não havia usuário no banco. A tela
 * ficava alternando entre a home e o login.
 *
 * <p>Nada disso quebrava nenhum teste anterior: as regras de acesso continuavam corretas,
 * o isolamento continuava provado, e o código parecia certo em revisão. Só um teste que
 * <b>entra</b> pelo caminho OIDC pega essa troca.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("br.com.peticiona.BancoDeTeste#disponivel")
@DisplayName("Login OIDC do Google")
class LoginOidcTest {

    @DynamicPropertySource
    static void configuracao(DynamicPropertyRegistry registro) {
        BancoDeTeste.configurar(registro);
        registro.add("spring.security.oauth2.client.registration.google.client-id", () -> "teste");
        registro.add("spring.security.oauth2.client.registration.google.client-secret", () -> "teste");
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private IntimacaoRepository intimacoes;
    @Autowired private UsuarioOidcService oidcService;
    @Autowired private RegistroDeUsuario registro;

    @Test
    @DisplayName("o cadastro roda pelo caminho OIDC — e o acervo nasce vazio")
    void cadastraComAcervoVazio() {
        Usuario criado = registro.registrar("sub-oidc-1", "novo@exemplo.com", "Nova Advogada", null);

        assertThat(usuarios.findByGoogleSub("sub-oidc-1")).isPresent();
        // Não semeia mais no login: o "Seu dia" enche com as publicações reais da OAB,
        // e os exemplos viraram um botão explícito.
        assertThat(intimacoes.findByUsuarioOrderByCriadoEmAsc(criado)).isEmpty();
    }

    @Test
    @DisplayName("entrar de novo não duplica usuário")
    void segundoLoginEhIdempotente() {
        Usuario primeiro = registro.registrar("sub-oidc-2", "repete@exemplo.com", "Advogado", null);
        Usuario segundo = registro.registrar("sub-oidc-2", "repete@exemplo.com", "Advogado", null);

        // Comparação por id: cada chamada roda na sua transação e devolve outra instância,
        // e a entidade não define equals().
        assertThat(segundo.getId()).isEqualTo(primeiro.getId());
        assertThat(usuarios.findByGoogleSub("sub-oidc-2")).isPresent();
    }

    @Test
    @DisplayName("o serviço registrado é o de OIDC, não o de OAuth2 comum")
    void oServicoDeOidcEstaRegistrado() {
        // Se alguém trocar oidcUserService por userService de novo, o login volta a
        // autenticar sem cadastrar — e o produto vira um laço de telas.
        assertThat(oidcService)
                .isInstanceOf(org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService.class);
    }

    @Test
    @DisplayName("com sessão OIDC válida, /api/me responde o usuário cadastrado")
    void meRespondeParaUsuarioLogado() throws Exception {
        registro.registrar("sub-oidc-3", "logado@exemplo.com", "Advogada Logada", null);

        mockMvc.perform(get("/api/me").with(oidcLogin().idToken(t -> t.subject("sub-oidc-3"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("logado@exemplo.com"));
    }

    @Test
    @DisplayName("sessão de um sub desconhecido não vira acesso")
    void subDesconhecidoNaoEntra() throws Exception {
        // Cobre o caso do usuário apagado do banco com a sessão ainda viva no navegador.
        mockMvc.perform(get("/api/me").with(oidcLogin().idToken(t -> t.subject("sub-que-nao-existe"))))
                .andExpect(status().isUnauthorized());
    }
}
