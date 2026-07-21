package br.com.peticiona.ia;

/**
 * A chave da API não está configurada neste ambiente.
 *
 * <p>Vira 503 na borda, com uma mensagem que diz o que fazer — em vez de um stack trace
 * que não ajuda ninguém no meio de uma apresentação.
 */
public class IaIndisponivelException extends RuntimeException {

    public IaIndisponivelException() {
        super("Os recursos de IA estão indisponíveis: a variável ANTHROPIC_API_KEY não está "
                + "configurada neste ambiente. A calculadora de prazos segue funcionando.");
    }
}
