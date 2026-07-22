package br.com.peticiona.auth;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Diz em voz alta, na subida, o que está desligado por falta de configuração.
 *
 * <p>Sem isto o sintoma aparece só quando alguém clica em "Entrar" e recebe um erro do
 * Google — longe da causa e no pior momento. Um aviso no log da subida transforma
 * "o login está estranho" em "faltou a variável".
 */
@Component
public class AvisoDeConfiguracao {

    /** O mesmo valor de sentinela dos defaults em application.properties. */
    static final String SEM_CONFIGURACAO = "nao-configurado";

    private static final Logger log = LoggerFactory.getLogger(AvisoDeConfiguracao.class);

    private final String clientId;
    private final String callbackToken;

    public AvisoDeConfiguracao(
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String clientId,
            @Value("${peticiona.callback-token:}") String callbackToken) {
        this.clientId = clientId;
        this.callbackToken = callbackToken;
    }

    public boolean loginDisponivel() {
        return clientId != null && !clientId.isBlank() && !SEM_CONFIGURACAO.equals(clientId);
    }

    @PostConstruct
    void avisar() {
        if (!loginDisponivel()) {
            log.warn("Login com Google desligado: defina GOOGLE_CLIENT_ID e GOOGLE_CLIENT_SECRET. "
                    + "A calculadora de prazos continua pública e funcionando.");
        }
        if (callbackToken == null || callbackToken.isBlank()) {
            log.warn("N8N_CALLBACK_TOKEN ausente: o callback do fluxo de IA vai recusar todas as "
                    + "chamadas. Nenhum rascunho será gravado até a variável ser definida.");
        }
    }
}
