package br.com.peticiona.demo;

import br.com.peticiona.ia.IaIndisponivelException;
import br.com.peticiona.ia.LeitorIntimacao;
import br.com.peticiona.ia.Redator;
import br.com.peticiona.prazo.CalculadoraPrazo;
import br.com.peticiona.prazo.Justica;
import br.com.peticiona.prazo.ResultadoPrazo;
import br.com.peticiona.prazo.TipoContagem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A API do MVP de apresentação — o fluxo do aha moment, de ponta a ponta.
 *
 * <p>{@code intimação → IA classifica → prazo entra na agenda → IA rascunha → advogado revisa}
 *
 * <p><b>Onde a IA entra e onde ela não entra:</b> o LLM lê a publicação e escreve a peça.
 * A <b>data-limite</b> passa pelo {@link CalculadoraPrazo} — aritmética de calendário
 * determinística, com memória de cálculo auditável. Essa fronteira é a regra número um do
 * projeto (doc 06) e está desenhada aqui, no
 * {@link #classificar(String) classificar}: a classificação devolve <i>dias</i>, o motor
 * devolve <i>data</i>.
 *
 * <p>⚠️ Sem autenticação, com estado em memória e dados semeados. É uma demonstração, não
 * um produto multiusuário.
 */
@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoController {

    private final AcervoDemo acervo;
    private final LeitorIntimacao leitor;
    private final Redator redator;
    private final CalculadoraPrazo calculadora;

    public DemoController(AcervoDemo acervo, LeitorIntimacao leitor, Redator redator,
                          CalculadoraPrazo calculadora) {
        this.acervo = acervo;
        this.leitor = leitor;
        this.redator = redator;
        this.calculadora = calculadora;
    }

    /** O "Seu dia": contadores, fila de intimações e prazos ordenados por vencimento. */
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        List<Map<String, Object>> linhas = new ArrayList<>();
        for (Intimacao i : acervo.intimacoes()) {
            linhas.add(resumo(i));
        }
        // Sem prazo calculado vai para o fim da lista. Atenção: a chave existe com valor
        // nulo, então getOrDefault NÃO serve aqui — ele só cobre chave ausente.
        linhas.sort(Comparator.comparing(l -> {
            Object v = l.get("dataVencimento");
            return v == null ? "9999-12-31" : (String) v;
        }));

        long naoLidas = acervo.naoLidas().size();
        long comPrazo = linhas.stream().filter(l -> l.get("dataVencimento") != null).count();
        long rascunhados = linhas.stream().filter(l -> Boolean.TRUE.equals(l.get("temRascunho"))).count();
        long revisao = linhas.stream().filter(l -> Boolean.TRUE.equals(l.get("precisaRevisao"))).count();

        return Map.of(
                "iaDisponivel", leitor.disponivel(),
                "hoje", LocalDate.now().toString(),
                "naoLidas", naoLidas,
                "prazosNaAgenda", comPrazo,
                "rascunhosProntos", rascunhados,
                "aguardandoRevisao", revisao,
                "intimacoes", linhas);
    }

    @GetMapping("/intimacoes/{id}")
    public Map<String, Object> detalhe(@PathVariable String id) {
        Intimacao i = buscar(id);
        Processo p = acervo.processo(i.getProcessoId()).orElseThrow();

        Map<String, Object> corpo = new java.util.LinkedHashMap<>(resumo(i));
        corpo.put("texto", i.getTexto());
        corpo.put("processo", p);
        corpo.put("classificacao", i.getClassificacao());
        corpo.put("rascunho", i.getRascunho());
        return corpo;
    }

    /**
     * Passo 1 e 2 do fluxo: a IA lê a publicação, o motor determinístico calcula a data.
     *
     * <p>A resposta traz a memória de cálculo junto — dia a dia, com o motivo de cada
     * dia contado ou pulado. Sem ela o advogado não tem como assumir o prazo.
     */
    @PostMapping("/intimacoes/{id}/classificar")
    public Map<String, Object> classificar(@PathVariable String id) {
        Intimacao i = buscar(id);
        Processo p = acervo.processo(i.getProcessoId()).orElseThrow();

        // ── A IA lê. Ela devolve DIAS, nunca uma data. ──
        ClassificacaoIntimacao c = leitor.classificar(i, p);
        i.setClassificacao(c);

        Map<String, Object> corpo = new java.util.LinkedHashMap<>();
        corpo.put("classificacao", c);

        // ── O motor determinístico calcula. Aqui, e só aqui, nasce uma data. ──
        if (c.prazoEmDias() > 0) {
            ResultadoPrazo prazo = calculadora.calcular(
                    i.getDataPublicacao(),
                    c.prazoEmDias(),
                    "DIAS_CORRIDOS".equals(c.tipoContagem())
                            ? TipoContagem.DIAS_CORRIDOS
                            : TipoContagem.DIAS_UTEIS,
                    Justica.TRABALHISTA,
                    true);
            i.setDataVencimento(prazo.dataVencimento());
            corpo.put("prazo", prazo);
        }

        corpo.put("situacao", i.getSituacao());
        return corpo;
    }

    /** Passo 3: a IA rascunha a peça. Sempre rascunho — a revisão humana é obrigatória. */
    @PostMapping("/intimacoes/{id}/rascunhar")
    public Map<String, Object> rascunhar(@PathVariable String id) {
        Intimacao i = buscar(id);
        Processo p = acervo.processo(i.getProcessoId()).orElseThrow();

        String texto = redator.rascunhar(i, p, i.getDataVencimento());
        i.setRascunho(texto);

        return Map.of("rascunho", texto, "situacao", i.getSituacao());
    }

    /**
     * Recebe de volta o que o fluxo do n8n produziu para uma intimação.
     *
     * <p>O fluxo é assíncrono por necessidade: rascunhar uma peça leva minutos, e prender
     * uma requisição HTTP por esse tempo é convite a timeout e a retry duplicando peça. O
     * n8n confirma o recebimento na hora e chama este endpoint quando termina.
     *
     * <p><b>Repare no que o corpo NÃO traz: a data-limite.</b> Vem o número de dias e o
     * regime de contagem; a data nasce aqui, no {@link CalculadoraPrazo} — o mesmo motor
     * determinístico do fluxo síncrono. Fosse o n8n a mandar a data pronta, existiriam dois
     * caminhos capazes de produzir prazo, e um deles passaria por um LLM (doc 06).
     */
    @PostMapping("/intimacoes/{id}/resultado")
    public Map<String, Object> receberResultado(@PathVariable String id,
                                                @RequestBody ResultadoIa corpo) {
        Intimacao i = buscar(id);

        if (corpo.classificacao() == null) {
            throw new IllegalArgumentException("O resultado não trouxe classificação.");
        }
        i.setClassificacao(corpo.classificacao());

        // A data continua nascendo de um só lugar, mesmo vindo o resto de fora.
        if (corpo.classificacao().prazoEmDias() > 0 && !corpo.classificacao().precisaRevisao()) {
            ResultadoPrazo prazo = calculadora.calcular(
                    i.getDataPublicacao(),
                    corpo.classificacao().prazoEmDias(),
                    "DIAS_CORRIDOS".equals(corpo.classificacao().tipoContagem())
                            ? TipoContagem.DIAS_CORRIDOS
                            : TipoContagem.DIAS_UTEIS,
                    Justica.TRABALHISTA,
                    true);
            i.setDataVencimento(prazo.dataVencimento());
        }

        if (corpo.rascunho() != null && !corpo.rascunho().isBlank()) {
            i.setRascunho(corpo.rascunho());
        }

        return Map.of(
                "id", i.getId(),
                "situacao", i.getSituacao(),
                "dataVencimento", i.getDataVencimento() == null ? "" : i.getDataVencimento().toString());
    }

    /**
     * O que o n8n devolve ao fim do fluxo.
     *
     * @param classificacao a leitura da publicação — dias e regime, nunca data
     * @param rascunho      a minuta, ou nulo quando o fluxo não rascunhou
     * @param status        o desfecho do fluxo, para diagnóstico
     * @param motivo        por que não rascunhou, quando for o caso
     */
    public record ResultadoIa(
            ClassificacaoIntimacao classificacao,
            String rascunho,
            String status,
            String motivo
    ) {}

    /** Devolve a demo ao estado inicial — o botão "recomeçar" entre ensaios. */
    @PostMapping("/reiniciar")
    public Map<String, String> reiniciar() {
        acervo.reiniciar();
        return Map.of("status", "acervo reiniciado");
    }

    // ── infraestrutura ────────────────────────────────────────────

    private Intimacao buscar(String id) {
        return acervo.intimacao(id).orElseThrow(
                () -> new IllegalArgumentException("Intimação não encontrada: " + id));
    }

    private Map<String, Object> resumo(Intimacao i) {
        Processo p = acervo.processo(i.getProcessoId()).orElseThrow();
        ClassificacaoIntimacao c = i.getClassificacao();

        Map<String, Object> linha = new java.util.LinkedHashMap<>();
        linha.put("id", i.getId());
        linha.put("orgao", i.getOrgao());
        linha.put("dataPublicacao", i.getDataPublicacao().toString());
        linha.put("numeroProcesso", p.numero());
        linha.put("cliente", p.cliente());
        linha.put("situacao", i.getSituacao());
        linha.put("temRascunho", i.getRascunho() != null);
        linha.put("tipoAto", c == null ? null : c.tipoAto());
        linha.put("providencia", c == null ? null : c.providencia());
        linha.put("urgencia", c == null ? null : c.urgencia());
        linha.put("precisaRevisao", c != null && c.precisaRevisao());
        linha.put("dataVencimento", i.getDataVencimento() == null ? null : i.getDataVencimento().toString());
        return linha;
    }

    @ExceptionHandler(IaIndisponivelException.class)
    public ResponseEntity<Map<String, String>> iaIndisponivel(IaIndisponivelException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> erro(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }
}
