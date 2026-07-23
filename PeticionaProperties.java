package br.com.peticiona.config;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Agrupa as configurações customizadas do Peticiona.IA, lidas do application.properties.
 * <p>
 * Permite injeção de dependência type-safe das configurações, em vez de usar {@code @Value} espalhado
 * pelo código. A anotação {@code @Validated} ativa a validação das constraints nos campos.
 *
 * @param pesquisaUrl URL do formulário de pesquisa de validação (Google Forms). Opcional. Se
 *     presente, deve ser uma URL válida.
 * @param callbackToken Segredo compartilhado com o n8n para o callback do fluxo de IA.
 *     Obrigatório.
 */
@Validated
@ConfigurationProperties(prefix = "peticiona")
public record PeticionaProperties(
    @URL(message = "A URL da pesquisa ('peticiona.pesquisa-url') deve ser uma URL válida.")
        String pesquisaUrl,
    @NotBlank(message = "O token de callback ('peticiona.callback-token') não pode ser vazio.")
        String callbackToken) {}