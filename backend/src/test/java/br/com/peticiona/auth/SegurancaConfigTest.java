package br.com.peticiona.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import br.com.peticiona.BancoDeTeste;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Quem entra onde — verificado, não presumido.
 *
 * <p>O produto tem duas metades com regras opostas de propósito: a calculadora é isca
 * pública (doc 07) e o acervo é sigiloso. Um erro de configuração que troque as duas passa
 * despercebido em revisão de código, mas não passa aqui.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("br.com.peticiona.BancoDeTeste#disponivel")
@DisplayName("Regras de acesso")
class SegurancaConfigTest {

    @DynamicPropertySource
    static void configuracao(DynamicPropertyRegistry registro) {
        BancoDeTeste.configurar(registro);
        registro.add("spring.security.oauth2.client.registration.google.client-id", () -> "teste");
        registro.add("spring.security.oauth2.client.registration.google.client-secret", () -> "teste");
        registro.add("peticiona.callback-token", () -> "segredo-do-teste");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("a calculadora responde sem login — ela é a porta de entrada")
    void calculadoraEhPublica() throws Exception {
        mockMvc.perform(post("/api/prazos/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataIntimacao":"2026-07-21","prazoEmDias":15,"tipoContagem":"DIAS_UTEIS"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("o acervo recusa quem não entrou")
    void acervoExigeLogin() throws Exception {
        mockMvc.perform(get("/api/demo/dashboard")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API sem sessão responde 401, e não um HTML de redirecionamento")
    void apiNaoRedirecionaParaOGoogle() throws Exception {
        // Um 302 aqui devolveria a página de login do Google dentro de um fetch() que
        // espera JSON — e o front trataria isso como erro sem sentido.
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("o callback do n8n recusa quem não traz o segredo")
    void callbackSemTokenEhRecusado() throws Exception {
        mockMvc.perform(post("/api/demo/intimacoes/{id}/resultado", java.util.UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RASCUNHO_PRONTO\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("o callback recusa também um segredo errado")
    void callbackComTokenErradoEhRecusado() throws Exception {
        mockMvc.perform(post("/api/demo/intimacoes/{id}/resultado", java.util.UUID.randomUUID())
                        .header(TokenDeCallbackFilter.CABECALHO, "chute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RASCUNHO_PRONTO\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("com o segredo certo o callback passa da segurança")
    void callbackComTokenCertoPassa() throws Exception {
        // A intimação não existe, então a resposta é 400 — e isso é justamente a prova de
        // que a requisição atravessou a segurança e chegou ao controller.
        mockMvc.perform(post("/api/demo/intimacoes/{id}/resultado", java.util.UUID.randomUUID())
                        .header(TokenDeCallbackFilter.CABECALHO, "segredo-do-teste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RASCUNHO_PRONTO\"}"))
                .andExpect(status().isBadRequest());
    }
}
