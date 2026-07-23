package br.com.peticiona.demo;

import br.com.peticiona.auth.Usuario;
import br.com.peticiona.auth.UsuarioAtual;
import br.com.peticiona.ia.FluxoIa;
import br.com.peticiona.ia.IaIndisponivelException;
import br.com.peticiona.prazo.CalculadoraPrazo;
import br.com.peticiona.prazo.Justica;
import br.com.peticiona.prazo.ResultadoPrazo;
import br.com.peticiona.prazo.TipoContagem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A API do acervo — o fluxo do aha moment, de ponta a ponta.
 *
 * <p>{@code intimação → IA classifica → prazo entra na agenda → IA rascunha → advogado revisa}
 *
 * <p><b>Onde a IA entra e onde ela não entra:</b> o LLM lê a publicação e escreve a peça,
 * do lado do n8n. A <b>data-limite</b> passa pelo {@link CalculadoraPrazo} — aritmética de
 * calendário determinística, com memória de cálculo auditável. Essa fronteira é a regra
 * número um do projeto (doc 06) e está desenhada aqui, no
 * {@link #receberResultado(String, ResultadoIa) receberResultado}: o fluxo devolve
 * <i>dias</i>, o motor devolve <i>data</i>.
 *
 * <p><b>Todo acesso é no escopo do usuário logado.</b> As buscas exigem o dono na
 * assinatura ({@code findByIdAndUsuario}), então pedir o id de outro advogado devolve 404,
 * não o dado dele. A única exceção é o callback do n8n, que não tem sessão e é protegido
 * por segredo compartilhado.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final AcervoDemo acervo;
    private final FluxoIa fluxo;
    private final CalculadoraPrazo calculadora;
    private final UsuarioAtual usuarioAtual;
    private final IntimacaoRepository intimacoes;
    private final ProcessoRepository processos;

    public DemoController(AcervoDemo acervo, FluxoIa fluxo, CalculadoraPrazo calculadora,
                          UsuarioAtual usuarioAtual, IntimacaoRepository intimacoes,
                          ProcessoRepository processos) {
        this.acervo = acervo;
        this.fluxo = fluxo;
        this.calculadora = calculadora;
        this.usuarioAtual = usuarioAtual;
        this.intimacoes = intimacoes;
        this.processos = processos;
    }

    /** O "Seu dia": contadores, fila de intimações e prazos ordenados por vencimento. */
    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        Usuario dono = usuarioAtual.obrigatorio();
        List<Intimacao> minhas = intimacoes.findByUsuarioOrderByCriadoEmAsc(dono);

        List<Map<String, Object>> linhas = new ArrayList<>();
        for (Intimacao i : minhas) {
            linhas.add(resumo(i));
        }
        // Sem prazo calculado vai para o fim da lista. Atenção: a chave existe com valor
        // nulo, então getOrDefault NÃO serve aqui — ele só cobre chave ausente.
        linhas.sort(Comparator.comparing(l -> {
            Object v = l.get("dataVencimento");
            return v == null ? "9999-12-31" : (String) v;
        }));

        long naoLidas = minhas.stream().filter(i -> i.getClassificacao() == null).count();
        long comPrazo = linhas.stream().filter(l -> l.get("dataVencimento") != null).count();
        long rascunhados = linhas.stream().filter(l -> Boolean.TRUE.equals(l.get("temRascunho"))).count();
        long revisao = linhas.stream().filter(l -> Boolean.TRUE.equals(l.get("precisaRevisao"))).count();

        return Map.of(
                "iaDisponivel", fluxo.disponivel(),
                "hoje", LocalDate.now().toString(),
                "naoLidas", naoLidas,
                "prazosNaAgenda", comPrazo,
                "rascunhosProntos", rascunhados,
                "aguardandoRevisao", revisao,
                "intimacoes", linhas);
    }

    @GetMapping("/intimacoes/{id}")
    @Transactional(readOnly = true)
    public Map<String, Object> detalhe(@PathVariable String id) {
        Intimacao i = minha(id);
        Processo p = i.getProcesso();

        Map<String, Object> corpo = new java.util.LinkedHashMap<>(resumo(i));
        corpo.put("texto", i.getTexto());
        corpo.put("processo", dadosDoProcesso(p));
        corpo.put("classificacao", i.getClassificacao());
        corpo.put("rascunho", i.getRascunho());
        // A memória de cálculo acompanha o prazo sempre. Um prazo sem o dia a dia que o
        // produziu é um número que o advogado não tem como conferir — e ele responde por ele.
        corpo.put("prazo", memoriaDeCalculo(i));
        return corpo;
    }

    /**
     * Entrega a intimação ao fluxo de IA e devolve na hora.
     *
     * <p>Um clique só cobre leitura e redação: o fluxo classifica e, quando a peça está no
     * escopo, já rascunha. Responde {@code 202} — a tela acompanha por
     * {@code GET /intimacoes/{id}} até {@code situacao} sair de PROCESSANDO.
     */
    @PostMapping("/intimacoes/{id}/processar")
    @Transactional
    public ResponseEntity<Map<String, Object>> processar(@PathVariable String id) {
        Usuario dono = usuarioAtual.obrigatorio();
        Intimacao i = minha(id);

        if (i.isProcessando()) {
            // Clicar duas vezes não pode virar duas peças cobradas.
            return ResponseEntity.accepted()
                    .body(Map.of("situacao", i.getSituacao(), "aviso", "Esta intimação já está na fila."));
        }

        // A IA escreve a peça defendendo alguém. Sem saber de que lado o advogado está, ela
        // escreveria para o lado errado — e uma contestação redigida contra o próprio
        // cliente é muito pior que nenhuma contestação.
        Processo p = i.getProcesso();
        if (p != null && p.aguardaConfirmacaoDeCliente()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "erro", "Antes de acionar a IA, diga qual das partes é seu cliente neste processo.",
                    "processoId", p.getId().toString(),
                    "situacao", i.getSituacao()));
        }

        // O cadastro é aberto e cada execução gasta crédito de modelo — dinheiro real.
        if (!dono.consumirExecucaoIa()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "erro", "Você atingiu o limite de " + Usuario.LIMITE_DIARIO_IA
                            + " execuções de IA por dia. Ele se renova amanhã.",
                    "situacao", i.getSituacao()));
        }

        fluxo.disparar(i, i.getProcesso());
        return ResponseEntity.accepted().body(Map.of("situacao", i.getSituacao()));
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
     * determinístico do resto do sistema. Fosse o n8n a mandar a data pronta, existiriam
     * dois caminhos capazes de produzir prazo, e um deles passaria por um LLM (doc 06).
     *
     * <p>Não há sessão aqui: é servidor falando com servidor. A autenticação vem do
     * cabeçalho {@code X-Peticiona-Token}, conferido antes de chegar neste método. Por
     * isso — e só por isso — a busca é por id, sem dono.
     */
    @PostMapping("/intimacoes/{id}/resultado")
    @Transactional
    public Map<String, Object> receberResultado(@PathVariable String id,
                                                @RequestBody ResultadoIa corpo) {
        Intimacao i = intimacoes.findById(uuid(id)).orElseThrow(
                () -> new IllegalArgumentException("Intimação não encontrada: " + id));

        if (corpo.classificacao() == null) {
            i.marcarFalha(corpo.motivo() == null ? "O fluxo não devolveu classificação." : corpo.motivo());
            throw new IllegalArgumentException("O resultado não trouxe classificação.");
        }
        i.setClassificacao(corpo.classificacao());
        i.marcarConcluido();

        // A data continua nascendo de um só lugar, mesmo vindo o resto de fora.
        ResultadoPrazo prazo = memoriaDeCalculo(i);
        if (prazo != null) {
            i.setDataVencimento(prazo.dataVencimento());
        }

        if (corpo.rascunho() != null && !corpo.rascunho().isBlank()) {
            i.setRascunho(corpo.rascunho());
        }

        return Map.of(
                "id", i.getId().toString(),
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

    /**
     * Processos em que ainda não sabemos de que lado o advogado está.
     *
     * <p>A API do CNJ entrega as partes com seus polos e os advogados com sua OAB, mas não
     * diz qual advogado representa qual parte. Esta é a lista do que precisa da resposta
     * humana — uma vez por processo, não a cada publicação.
     */
    @GetMapping("/processos/pendentes")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> processosPendentes() {
        Usuario dono = usuarioAtual.obrigatorio();
        return processos.findByUsuarioAndClientePoloIsNullAndOrigem(dono, "DJEN").stream()
                .map(p -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", p.getId().toString());
                    m.put("numero", p.getNumero());
                    m.put("vara", p.getVara());
                    m.put("tribunal", p.getTribunal());
                    m.put("classe", p.getClasse());
                    m.put("partesPoloAtivo", p.getPartesPoloAtivo());
                    m.put("partesPoloPassivo", p.getPartesPoloPassivo());
                    return m;
                })
                .toList();
    }

    /** O advogado diz de que lado está, e o processo passa a servir aos prompts. */
    @PostMapping("/processos/{id}/cliente")
    @Transactional
    public Map<String, Object> confirmarCliente(@PathVariable String id, @RequestBody PoloRequest req) {
        Usuario dono = usuarioAtual.obrigatorio();
        Processo p = processos.findByIdAndUsuario(uuid(id), dono).orElseThrow(
                () -> new IllegalArgumentException("Processo não encontrado: " + id));

        p.confirmarCliente(req.polo());

        return Map.of(
                "id", p.getId().toString(),
                "cliente", String.valueOf(p.getCliente()),
                "parteContraria", String.valueOf(p.getParteContraria()));
    }

    /** @param polo {@code "A"} para polo ativo, {@code "P"} para passivo */
    public record PoloRequest(String polo) {}

    /** Devolve o acervo ao estado inicial — o botão "recomeçar" entre ensaios. */
    @PostMapping("/reiniciar")
    public Map<String, String> reiniciar() {
        // Só o acervo de quem chamou. Antes isto zerava a demo para todo mundo ao mesmo tempo.
        acervo.reiniciar(usuarioAtual.obrigatorio());
        return Map.of("status", "acervo reiniciado");
    }

    // ── infraestrutura ────────────────────────────────────────────

    /**
     * Refaz a conta do prazo para exibição — mesma entrada, mesmo motor, mesmo resultado.
     *
     * <p>Recalcular em vez de guardar é de propósito: o que a tela mostra vem do
     * {@link CalculadoraPrazo} agora, não de um snapshot que pode ter envelhecido junto com
     * o calendário de feriados.
     */
    private ResultadoPrazo memoriaDeCalculo(Intimacao i) {
        ClassificacaoIntimacao c = i.getClassificacao();
        if (c == null || c.prazoEmDias() <= 0 || c.precisaRevisao()) {
            return null;
        }
        return calculadora.calcular(
                i.getDataPublicacao(),
                c.prazoEmDias(),
                "DIAS_CORRIDOS".equals(c.tipoContagem())
                        ? TipoContagem.DIAS_CORRIDOS
                        : TipoContagem.DIAS_UTEIS,
                Justica.TRABALHISTA,
                true);
    }

    /**
     * A intimação pedida, desde que seja de quem está pedindo.
     *
     * <p>Id de outro advogado cai no mesmo "não encontrada" de um id inexistente: a
     * resposta não deve revelar que aquele registro existe em algum lugar.
     */
    private Intimacao minha(String id) {
        return intimacoes.findByIdAndUsuario(uuid(id), usuarioAtual.obrigatorio()).orElseThrow(
                () -> new IllegalArgumentException("Intimação não encontrada: " + id));
    }

    private UUID uuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Intimação não encontrada: " + id);
        }
    }

    private Map<String, Object> dadosDoProcesso(Processo p) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", p.getId().toString());
        m.put("numero", p.getNumero());
        m.put("cliente", p.getCliente());
        m.put("parteContraria", p.getParteContraria());
        m.put("vara", p.getVara());
        m.put("area", p.getArea());
        m.put("fase", p.getFase());
        m.put("resumo", p.getResumo());
        return m;
    }

    private Map<String, Object> resumo(Intimacao i) {
        Processo p = i.getProcesso();
        ClassificacaoIntimacao c = i.getClassificacao();

        Map<String, Object> linha = new java.util.LinkedHashMap<>();
        linha.put("id", i.getId().toString());
        linha.put("orgao", i.getOrgao());
        linha.put("dataPublicacao", i.getDataPublicacao().toString());
        linha.put("numeroProcesso", p == null ? null : p.getNumero());
        linha.put("cliente", p == null ? null : p.getCliente());
        linha.put("situacao", i.getSituacao());
        linha.put("processando", i.isProcessando());
        linha.put("erroIa", i.getErroIa());
        linha.put("temRascunho", i.getRascunho() != null);
        linha.put("tipoAto", c == null ? null : c.tipoAto());
        linha.put("providencia", c == null ? null : c.providencia());
        linha.put("urgencia", c == null ? null : c.urgencia());
        linha.put("precisaRevisao", c != null && c.precisaRevisao());
        linha.put("dataVencimento", i.getDataVencimento() == null ? null : i.getDataVencimento().toString());
        // O botão de IA fica bloqueado enquanto isto for verdadeiro — e a tela precisa
        // saber disso antes do clique, não depois do 409.
        linha.put("aguardaCliente", p != null && p.aguardaConfirmacaoDeCliente());
        linha.put("processoId", p == null ? null : p.getId().toString());
        linha.put("link", i.getLink());
        linha.put("origem", p == null ? "DEMO" : p.getOrigem());
        return linha;
    }

    @ExceptionHandler(UsuarioAtual.NaoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> naoAutenticado(UsuarioAtual.NaoAutenticadoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", e.getMessage()));
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
