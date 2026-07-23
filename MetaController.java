package br.com.peticiona.meta;

import br.com.peticiona.config.PeticionaProperties;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expõe metadados da aplicação para o frontend.
 */
@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final PeticionaProperties peticionaProperties;

    /*
     * Antes, o código poderia ser assim, usando @Value:
     *
     *   @Value("${peticiona.pesquisa-url}")
     *   private String pesquisaUrl;
     *
     * A injeção de PeticionaProperties via construtor é preferível por ser
     * mais segura (type-safe), mais fácil de testar e por centralizar as
     * configurações da aplicação.
     */
    public MetaController(PeticionaProperties peticionaProperties) {
        this.peticionaProperties = peticionaProperties;
    }

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of("pesquisaUrl", peticionaProperties.pesquisaUrl());
    }
}