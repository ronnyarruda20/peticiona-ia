package br.com.peticiona.leads;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InteresseController.class)
@Import(InteresseService.class)
class InteresseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveRegistrarInteresseComSucesso() throws Exception {
        var request = new InteresseController.InteresseRequest(
                "Ana Souza",
                "ana@example.com",
                "Contestação",
                "Quero acompanhar o projeto"
        );

        mockMvc.perform(post("/api/interesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.mensagem").value("Obrigado por se interessar!"));
    }

    @Test
    void deveRecusarEmailInvalido() throws Exception {
        var request = new InteresseController.InteresseRequest(
                "Ana Souza",
                "email-invalido",
                "Contestação",
                "Quero acompanhar o projeto"
        );

        mockMvc.perform(post("/api/interesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
