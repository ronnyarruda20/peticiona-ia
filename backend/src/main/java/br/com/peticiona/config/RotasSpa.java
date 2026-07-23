package br.com.peticiona.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Entrega o index.html nas rotas do Angular.
 *
 * <p>O roteamento é do lado do cliente: o servidor não conhece {@code /intimacoes/i1}.
 * Sem este encaminhamento, um F5 ou um link colado nessas URLs devolve 404 — e descobrir
 * isso no meio de uma apresentação é o pior momento possível.
 *
 * <p>As rotas são listadas <b>explicitamente</b>, e não com um curinga geral: um
 * {@code /**} engoliria os 404 legítimos de {@code /api} e transformaria erro de API em
 * página HTML, que é muito mais difícil de depurar.
 */
@Controller
public class RotasSpa {

    @RequestMapping({"/login", "/comecar", "/perfil", "/calculadora", "/intimacoes/**"})
    public String index() {
        return "forward:/index.html";
    }
}
