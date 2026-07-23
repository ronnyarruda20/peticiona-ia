package br.com.peticiona.meta;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testa o MetaController, garantindo que as propriedades de configuração
 * são expostas corretamente para o frontend.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "peticiona.pesquisa-url=https://example.com/test-survey",
    "peticiona.callback-token=test-token-for-controller"
})
class MetaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarConfiguracoesCorretamente() throws Exception {
        mockMvc.perform(get("/api/meta/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesquisaUrl").value("https://example.com/test-survey"));
    }
}