package br.com.peticiona.ia;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Constrói o cliente da API Claude uma vez, na subida da aplicação.
 *
 * <p>A chave vem de {@code ANTHROPIC_API_KEY} no ambiente — no Railway, em Variables.
 * Nunca no código, nunca no repositório, nunca num endpoint de config (o
 * {@code /api/config} é público e devolve só o que pode ser visto por qualquer visitante).
 *
 * <p>Sem chave, o bean vira {@code null} e os endpoints de IA respondem 503 com uma
 * mensagem clara. É melhor a aplicação subir e a calculadora continuar funcionando do que
 * derrubar tudo por causa de uma variável ausente.
 */
@Configuration
public class ClienteClaude {

    private static final Logger log = LoggerFactory.getLogger(ClienteClaude.class);

    @Bean
    public AnthropicClient anthropicClient() {
        String chave = System.getenv("ANTHROPIC_API_KEY");
        if (chave == null || chave.isBlank()) {
            log.warn("ANTHROPIC_API_KEY ausente — os recursos de IA ficarão indisponíveis (503). "
                    + "A calculadora de prazos continua funcionando normalmente.");
            return null;
        }
        return AnthropicOkHttpClient.fromEnv();
    }
}
