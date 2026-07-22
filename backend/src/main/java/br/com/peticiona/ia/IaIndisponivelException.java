package br.com.peticiona.ia;

/**
 * O fluxo de IA não está configurado neste ambiente.
 *
 * <p>Vira 503 na borda, com uma mensagem que diz o que fazer — em vez de um stack trace
 * que não ajuda ninguém no meio de uma apresentação.
 */
public class IaIndisponivelException extends RuntimeException {

    public IaIndisponivelException() {
        super("Os recursos de IA estão indisponíveis: configure N8N_WEBHOOK_URL (onde o fluxo "
                + "escuta) e APP_BASE_URL (para onde ele devolve o resultado). A calculadora de "
                + "prazos segue funcionando.");
    }
}
