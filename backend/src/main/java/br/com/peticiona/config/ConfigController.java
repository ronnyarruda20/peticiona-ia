package br.com.peticiona.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configuração que o front lê em tempo de execução.
 *
 * <p>Existe para que a URL da pesquisa possa mudar sem rebuild — basta editar a variável
 * no painel do Railway. Se {@code PESQUISA_URL} não estiver definida, o front esconde o
 * convite: melhor não ter chamada do que ter uma que leva a lugar nenhum.
 *
 * <p>⚠️ Nada de segredo aqui. Este endpoint é público e tudo que ele devolve é visível
 * no navegador de qualquer visitante.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final String pesquisaUrl;

    public ConfigController(@Value("${peticiona.pesquisa-url:}") String pesquisaUrl) {
        this.pesquisaUrl = pesquisaUrl;
    }

    @GetMapping
    public Map<String, String> config() {
        return Map.of("pesquisaUrl", pesquisaUrl == null ? "" : pesquisaUrl.trim());
    }
}
