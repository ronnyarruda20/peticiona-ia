package br.com.peticiona.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.peticiona.BancoDeTeste;
import br.com.peticiona.demo.AcervoDemo;
import br.com.peticiona.demo.IntimacaoRepository;
import br.com.peticiona.demo.ProcessoRepository;
import org.junit.jupiter.api.BeforeEach;
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
 * A exclusão de conta (LGPD), verificada onde importa: a guarda contra o clique acidental e
 * o isolamento entre advogados.
 *
 * <p>Excluir é irreversível e apaga processo de cliente. Dois riscos precisam de teste: um
 * clique errado apagar tudo, e um advogado conseguir apagar a conta de outro. Os dois estão
 * cobertos aqui.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("br.com.peticiona.BancoDeTeste#disponivel")
@DisplayName("Exclusão de conta")
class ExclusaoDeContaTest {

    @DynamicPropertySource
    static void configuracao(DynamicPropertyRegistry registro) {
        BancoDeTeste.configurar(registro);
        registro.add("spring.security.oauth2.client.registration.google.client-id", () -> "teste");
        registro.add("spring.security.oauth2.client.registration.google.client-secret", () -> "teste");
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private IntimacaoRepository intimacoes;
    @Autowired private ProcessoRepository processos;
    @Autowired private AcervoDemo acervo;
    @Autowired private RegistroDeUsuario registro;

    private Usuario ana;

    @BeforeEach
    void umAdvogadoComAcervo() {
        intimacoes.deleteAll();
        processos.deleteAll();
        usuarios.deleteAll();
        ana = registro.registrar("sub-del-ana", "ana.del@exemplo.com", "Ana", null);
        acervo.carregarExemplos(ana);
    }

    @Test
    @DisplayName("sem o e-mail de confirmação, recusa e não apaga nada")
    void semConfirmacaoNaoApaga() throws Exception {
        mockMvc.perform(delete("/api/me").with(oidcLogin().idToken(t -> t.subject("sub-del-ana"))))
                .andExpect(status().isBadRequest());

        assertThat(usuarios.findByGoogleSub("sub-del-ana")).isPresent();
        assertThat(intimacoes.findByUsuarioOrderByCriadoEmAsc(ana)).isNotEmpty();
    }

    @Test
    @DisplayName("com o e-mail errado, também recusa")
    void emailErradoNaoApaga() throws Exception {
        mockMvc.perform(delete("/api/me").param("confirmacao", "outro@exemplo.com")
                        .with(oidcLogin().idToken(t -> t.subject("sub-del-ana"))))
                .andExpect(status().isBadRequest());

        assertThat(usuarios.findByGoogleSub("sub-del-ana")).isPresent();
    }

    @Test
    @DisplayName("com o e-mail certo, apaga a conta e todo o acervo")
    void emailCertoApagaTudo() throws Exception {
        mockMvc.perform(delete("/api/me").param("confirmacao", "ana.del@exemplo.com")
                        .with(oidcLogin().idToken(t -> t.subject("sub-del-ana"))))
                .andExpect(status().isOk());

        assertThat(usuarios.findByGoogleSub("sub-del-ana")).isEmpty();
        assertThat(intimacoes.findByUsuarioOrderByCriadoEmAsc(ana)).isEmpty();
        assertThat(processos.findByUsuario(ana)).isEmpty();
    }

    @Test
    @DisplayName("um advogado não apaga a conta de outro — a exclusão é só da sessão")
    void naoApagaContaAlheia() throws Exception {
        Usuario bruno = registro.registrar("sub-del-bruno", "bruno.del@exemplo.com", "Bruno", null);
        acervo.carregarExemplos(bruno);

        // Ana logada, tentando confirmar com o e-mail do Bruno: o confirmacao não bate com
        // o e-mail DELA, então é recusado — e o Bruno continua intacto.
        mockMvc.perform(delete("/api/me").param("confirmacao", "bruno.del@exemplo.com")
                        .with(oidcLogin().idToken(t -> t.subject("sub-del-ana"))))
                .andExpect(status().isBadRequest());

        assertThat(usuarios.findByGoogleSub("sub-del-bruno")).isPresent();
        assertThat(usuarios.findByGoogleSub("sub-del-ana")).isPresent();
    }

    @Test
    @DisplayName("sincronizar sem OAB responde erro claro, não 500")
    void sincronizarSemOab() throws Exception {
        mockMvc.perform(post("/api/me/sincronizar")
                        .with(oidcLogin().idToken(t -> t.subject("sub-del-ana"))))
                .andExpect(status().isBadRequest());
    }
}
