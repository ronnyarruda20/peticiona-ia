package br.com.peticiona.djen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Lê o Diário de Justiça Eletrônico Nacional pela API pública do CNJ.
 *
 * <p>Sem autenticação: a consulta de publicações é aberta, como é aberta a consulta no
 * site {@code comunica.pje.jus.br}. Não há chave para configurar nem cota para estourar.
 *
 * <p><b>Sobre o teto de páginas:</b> uma OAB movimentada devolve milhares de publicações
 * em trinta dias — nos testes desta implementação, 1990 numa única consulta. Sem limite,
 * uma sincronização poderia rodar por minutos e encher a agenda com anos de histórico. O
 * teto existe, e quando ele corta, isso vai para o log: uma tela que parece completa sem
 * estar é pior que uma tela obviamente truncada.
 */
@Service
public class DjenClienteHttp implements DjenCliente {

    private static final Logger log = LoggerFactory.getLogger(DjenClienteHttp.class);

    private static final int ITENS_POR_PAGINA = 100;
    private static final int MAXIMO_DE_PAGINAS = 20;

    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String base;
    private final HttpClient http;

    /**
     * @param proxyHost host de um proxy no Brasil, ou vazio. A API do CNJ fica atrás de um
     *                  CloudFront que bloqueia requisições de fora do país; o Railway roda nos
     *                  EUA. Sem esse proxy, toda consulta volta 403. Vazio = chamada direta,
     *                  que funciona em desenvolvimento (máquina no Brasil) e nos testes.
     */
    public DjenClienteHttp(
            @Value("${peticiona.djen.url:https://comunicaapi.pje.jus.br/api/v1/comunicacao}") String base,
            @Value("${peticiona.djen.proxy-host:}") String proxyHost,
            @Value("${peticiona.djen.proxy-port:8888}") int proxyPort) {
        this.base = base;

        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL);

        if (proxyHost != null && !proxyHost.isBlank()) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost.trim(), proxyPort)));
            log.info("DJEN: consultas roteadas pelo proxy {}:{} (egress no Brasil).", proxyHost, proxyPort);
        } else {
            log.info("DJEN: consultas diretas, sem proxy. Fora do Brasil isso volta HTTP 403.");
        }
        this.http = builder.build();
    }

    @Override
    public List<PublicacaoDjen> buscar(String numeroOab, String ufOab, LocalDate inicio, LocalDate fim) {
        List<PublicacaoDjen> encontradas = new ArrayList<>();

        for (int pagina = 1; pagina <= MAXIMO_DE_PAGINAS; pagina++) {
            Resposta resposta = pedirPagina(numeroOab, ufOab, inicio, fim, pagina);

            if (resposta.items() == null || resposta.items().isEmpty()) {
                return encontradas;
            }
            encontradas.addAll(resposta.items());

            if (resposta.items().size() < ITENS_POR_PAGINA) {
                return encontradas;
            }
            if (pagina == MAXIMO_DE_PAGINAS) {
                log.warn("OAB {}/{}: teto de {} publicações atingido entre {} e {}. "
                                + "O CNJ informa {} no total — o restante não foi importado.",
                        numeroOab, ufOab, MAXIMO_DE_PAGINAS * ITENS_POR_PAGINA,
                        inicio, fim, resposta.count());
            }
        }
        return encontradas;
    }

    private Resposta pedirPagina(String numeroOab, String ufOab, LocalDate inicio, LocalDate fim, int pagina) {
        String url = base
                + "?numeroOab=" + codificar(numeroOab)
                + "&ufOab=" + codificar(ufOab)
                + "&dataDisponibilizacaoInicio=" + inicio
                + "&dataDisponibilizacaoFim=" + fim
                + "&itensPorPagina=" + ITENS_POR_PAGINA
                + "&pagina=" + pagina;

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new DjenIndisponivelException(
                        "O DJEN respondeu HTTP " + resp.statusCode() + " para a OAB " + numeroOab + "/" + ufOab);
            }
            return json.readValue(resp.body(), Resposta.class);

        } catch (DjenIndisponivelException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DjenIndisponivelException("Consulta ao DJEN interrompida.", e);
        } catch (Exception e) {
            throw new DjenIndisponivelException("Não consegui consultar o DJEN.", e);
        }
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor.trim(), StandardCharsets.UTF_8);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Resposta(String status, Integer count, List<PublicacaoDjen> items) {}
}
