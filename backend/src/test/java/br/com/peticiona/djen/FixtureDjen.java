package br.com.peticiona.djen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.InputStream;
import java.util.List;

/**
 * Lê o JSON real do CNJ gravado em {@code test/resources/djen/resposta.json}.
 *
 * <p>Usar uma captura de verdade, e não um objeto montado à mão no teste, é o que garante
 * que o mapeamento aguenta o formato real: o texto com {@code <br>}, os vinte destinatários,
 * o número com e sem máscara, os dois nomes de campo para a data. Um mock idealizado
 * provaria que o código funciona com o mock, não com o DJEN.
 */
final class FixtureDjen {

    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private FixtureDjen() {
    }

    static List<PublicacaoDjen> publicacoes() {
        try (InputStream in = FixtureDjen.class.getResourceAsStream("/djen/resposta.json")) {
            if (in == null) {
                throw new IllegalStateException("Fixture /djen/resposta.json não encontrada no classpath de teste.");
            }
            return JSON.readValue(in, Resposta.class).items();
        } catch (Exception e) {
            throw new IllegalStateException("Não consegui ler a fixture do DJEN.", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Resposta(List<PublicacaoDjen> items) {}
}
