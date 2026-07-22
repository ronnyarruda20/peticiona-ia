package br.com.peticiona.ia;

import br.com.peticiona.demo.Intimacao;
import br.com.peticiona.demo.Processo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Dispara o fluxo de IA que roda no n8n e volta por callback.
 *
 * <p>Este serviço não sabe classificar nem redigir — ele só entrega o material e vai embora.
 * O trabalho acontece no workflow, e o resultado chega em
 * {@code POST /api/demo/intimacoes/{id}/resultado}.
 *
 * <p><b>Por que assíncrono:</b> rascunhar uma peça leva minutos. Segurar a requisição do
 * navegador por esse tempo estoura timeout de proxy e, pior, convida o usuário a clicar de
 * novo — o que geraria duas peças cobradas para a mesma intimação.
 *
 * <p>Duas variáveis de ambiente mandam aqui, e a ausência de qualquer uma desliga o recurso
 * em vez de derrubar a aplicação:
 * <ul>
 *   <li>{@code N8N_WEBHOOK_URL} — onde o fluxo escuta</li>
 *   <li>{@code APP_BASE_URL} — a URL pública desta API, que vira o {@code callbackUrl}.
 *       O n8n precisa saber para onde devolver, e ele não tem como adivinhar.</li>
 * </ul>
 */
@Service
public class FluxoIa {

    private static final Logger log = LoggerFactory.getLogger(FluxoIa.class);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper json = new ObjectMapper();

    private final String webhook;
    private final String baseUrl;

    public FluxoIa(@Value("${N8N_WEBHOOK_URL:}") String webhook,
                   @Value("${APP_BASE_URL:}") String baseUrl) {
        this.webhook = webhook == null ? "" : webhook.trim();
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");

        if (!disponivel()) {
            log.warn("Fluxo de IA desligado: defina N8N_WEBHOOK_URL e APP_BASE_URL. "
                    + "A calculadora de prazos continua funcionando normalmente.");
        }
    }

    public boolean disponivel() {
        return !webhook.isBlank() && !baseUrl.isBlank();
    }

    /**
     * Entrega a intimação ao fluxo e devolve o controle imediatamente.
     *
     * <p>O envio roda fora da thread da requisição: quem clicou não espera nem o handshake.
     * Uma falha aqui é registrada e propagada para a intimação como erro, nunca engolida —
     * uma intimação que some sem deixar rastro é pior que uma que falha visivelmente.
     */
    public void disparar(Intimacao intimacao, Processo processo) {
        if (!disponivel()) {
            throw new IaIndisponivelException();
        }

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("intimacaoId", intimacao.getId());
        corpo.put("callbackUrl", baseUrl + "/api/demo/intimacoes/" + intimacao.getId() + "/resultado");
        corpo.put("numero", processo.numero());
        corpo.put("cliente", processo.cliente());
        corpo.put("parteContraria", processo.parteContraria());
        corpo.put("vara", processo.vara());
        corpo.put("fase", processo.fase());
        corpo.put("resumo", processo.resumo());
        corpo.put("orgao", intimacao.getOrgao());
        corpo.put("dataPublicacao", intimacao.getDataPublicacao().toString());
        corpo.put("texto", intimacao.getTexto());

        String payload;
        try {
            payload = json.writeValueAsString(corpo);
        } catch (Exception e) {
            throw new IllegalStateException("Não consegui montar o payload do fluxo de IA.", e);
        }

        HttpRequest req = HttpRequest.newBuilder(URI.create(webhook))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        intimacao.marcarProcessando();

        http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() >= 300) {
                        log.error("O fluxo de IA recusou a intimação {}: HTTP {} — {}",
                                intimacao.getId(), resp.statusCode(), resp.body());
                        intimacao.marcarFalha("O fluxo de IA respondeu HTTP " + resp.statusCode() + ".");
                    } else {
                        log.info("Intimação {} entregue ao fluxo de IA.", intimacao.getId());
                    }
                })
                .exceptionally(e -> {
                    log.error("Não consegui falar com o fluxo de IA para a intimação {}.",
                            intimacao.getId(), e);
                    intimacao.marcarFalha("Não consegui falar com o fluxo de IA.");
                    return null;
                });
    }
}
