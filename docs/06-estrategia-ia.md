# 06 — Estratégia de IA (Detalhada)

> Alinhado ao doc `00-premissas.md` (fonte única de verdade; reescrito jul/2026 — projeto solo, back-end Spring Boot). Fases: **Fase 1 = meses 1–14** (ago/2026 – set/2027; 1 nicho, **2 tipos de peça**, sem WhatsApp/financeiro/chat), lançamento no mês 15 (out/2027), **réplica + recurso inominado nos meses 16–17**, **Fase 1.5 = meses 17–19** (chat com autos + 2º nicho), **Fase 2 = pós-lançamento** (WhatsApp, financeiro, estilo).

## Princípio central

**IA com revisão humana obrigatória.** O advogado é sempre o autor final. Isso é simultaneamente exigência ética (OAB), proteção jurídica nossa e argumento de venda ("você no controle").

## ☕ O achado que muda este doc: o SDK oficial Java

A troca do back-end para Spring Boot (doc 00) levantou a suspeita de que a integração de IA ficaria mais difícil fora do mundo Python/TS. **O contrário: o SDK oficial `com.anthropic:anthropic-java` é de primeira classe e casa melhor com o nosso desenho do que a cola manual que o plano antigo assumia:**

- **Structured outputs derivam o JSON Schema de um record Java** e devolvem um **objeto tipado** — a classificação de intimação vira literalmente um `record`, sem schema escrito à mão nem validação/parse manual (o SDK valida contra o schema)
- **Streaming** nativo — o redator gera a peça por seções direto para o editor
- **Tool use** — a busca de jurisprudência verificada (camada anti-alucinação 1) é um tool definido no SDK
- **Prompt caching e Batches API** — as duas alavancas de margem funcionam idênticas a qualquer outra linguagem
- **Files API** — anexos/PDFs quando o chat com os autos chegar (Fase 1.5)

```groovy
// build.gradle
implementation("com.anthropic:anthropic-java:2.34.0")
```

```java
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;

// lê ANTHROPIC_API_KEY do ambiente
AnthropicClient client = AnthropicOkHttpClient.fromEnv();
```

O que o SDK **não** cobre: embeddings (a Anthropic não oferece — ver seção de custos) e o RAG em si (que é SQL + pgvector + chamadas de API, sem framework — doc 00 §9.7).

## Os 6 casos de uso de IA, por fase de implementação

### 1. Leitura e classificação de intimações (Fase 1 — o primeiro código de IA do projeto)

**Input:** texto da publicação/comunicação. **Output:** um objeto tipado — o schema JSON é derivado do record pelo SDK:

```java
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

record ClassificacaoIntimacao(
    @JsonPropertyDescription("ex.: intimacao_contestacao, intimacao_sentenca, despacho_mero_expediente")
    String tipoAto,
    @JsonPropertyDescription("número CNJ do processo, se presente no texto")
    String numeroProcesso,
    @JsonPropertyDescription("prazo em dias mencionado no ato; 0 se não houver")
    int prazoDias,
    @JsonPropertyDescription("dias_uteis ou dias_corridos")
    String contagem,
    @JsonPropertyDescription("apenas cross-check — NUNCA usada como prazo oficial")
    String dataLimiteEstimada,
    @JsonPropertyDescription("providência recomendada, ex.: Apresentar contestação")
    String providencia,
    @JsonPropertyDescription("alta, media ou baixa")
    String urgencia,
    @JsonPropertyDescription("confiança da classificação entre 0 e 1")
    double confianca,
    @JsonPropertyDescription("resumo em 1-2 frases para quem não é advogado")
    String resumoLeigo
) {}

StructuredMessageCreateParams<ClassificacaoIntimacao> params = MessageCreateParams.builder()
    .model("claude-haiku-4-5")
    .maxTokens(1024L)
    .systemOfTextBlockParams(promptClassificacaoComFewShots) // prefixo cacheado — ver seção de caching
    .outputConfig(ClassificacaoIntimacao.class)              // schema derivado do record
    .addUserMessage(textoPublicacao)
    .build();

client.messages().create(params).content().stream()
    .flatMap(cb -> cb.text().stream())
    .forEach(typed -> {
        ClassificacaoIntimacao c = typed.text();             // objeto TIPADO, já validado
        if (c.confianca() < LIMIAR) filaRevisaoManual.enfileirar(c);
        else pipeline.processar(c);
    });
```

Isso elimina o trabalho que o plano antigo orçava para "cola de schema/validação" — é exatamente o desconto de −10h do bloco de classificação no doc 00 §3.1.

**Regras que continuam valendo:** validar a data-limite com o **motor determinístico próprio** (dias úteis, feriados forenses por tribunal) — **nunca deixar o LLM calcular data sozinho.** O campo `dataLimiteEstimada` é apenas cross-check: se divergir do motor, a intimação vai para revisão manual. A `confianca` alimenta a fila de baixa confiança (item "nunca cortar" do doc 00 §2).

### 2. Resumo de processo em linguagem leiga (Fase 1)
Para o corpo dos alertas de prazo por e-mail. Prompt com persona ("explique para quem não é advogado, 3 frases, sem juridiquês"), Haiku 4.5, chamada simples. **Nota de corte:** a frase-resumo IA do dashboard "Seu dia" foi cortada (corte #4, doc 00) — o dashboard do lançamento é estruturado, sem IA. O componente de resumo continua existindo para os alertas, e na Fase 2 é reaproveitado nas atualizações via WhatsApp.

### 3. Redator de peças (Fase 1 — o produto)
**Escopo Fase 1: 1 nicho (hipótese: trabalhista), 2 tipos de peça — contestação + petições simples** (era 4; corte #3 do doc 00 — contestação é 80% do aha; **réplica e recurso inominado entram nos meses 16–17**, logo após o lançamento).

**Pipeline RAG:**
1. Recuperar contexto: dados do processo + intimação + peças anteriores do caso + templates do nicho (pgvector via JDBC)
2. Prompt de sistema por tipo de peça, com estrutura obrigatória: endereçamento, qualificação, preliminares, mérito, pedidos — prefixo estável e cacheado (ver seção de caching)
3. **Geração em seções via streaming** para o editor:

```java
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.RawMessageStreamEvent;

MessageCreateParams params = MessageCreateParams.builder()
    .model("claude-sonnet-4-6")
    .maxTokens(8192L)
    .systemOfTextBlockParams(promptDaPecaComTemplates)  // 10–20k tokens, cacheado
    .addUserMessage(contextoDoCasoInterpolado)
    .build();

try (StreamResponse<RawMessageStreamEvent> stream = client.messages().createStreaming(params)) {
    stream.stream()
        .flatMap(event -> event.contentBlockDelta().stream())
        .flatMap(delta -> delta.delta().text().stream())
        .forEach(textDelta -> editorGateway.enviarTrecho(textDelta.text())); // → wrapper TipTap (doc 05)
}
```

4. Advogado edita; edições viram sinal de qualidade (métrica de sobrevivência, abaixo)

As regras anti-alucinação deste caso de uso estão na seção própria, abaixo — é o risco ALTÍSSIMO do doc 09 e merece mais do que bullets.

### 4. Chat com os autos (Fase 1.5 — meses 17–19, pós-lançamento)
"IA, qual foi o valor da causa? O que o perito concluiu?" — RAG sobre os PDFs do processo (OCR + chunking + embeddings; a Files API do SDK ajuda no manuseio de PDFs). Diferencial enorme e tecnicamente simples, mas **entra ~2 meses após o lançamento pago**, com limite por plano (50 / 200 / fair use perguntas·mês, conforme doc 00). Junto entra o 2º nicho (previdenciário, se confirmado).

### 5. Atendente WhatsApp (Fase 2 — pós-lançamento)
IA responde clientes finais: status do processo (dados reais do banco), agendamento, segunda via de cobrança. Guardrails: **nunca dar opinião jurídica ao cliente final**; casos ambíguos → transbordo para o advogado. Depende da integração WhatsApp Business API, inteira na Fase 2 (verificação Meta, templates, custo **por mensagem** — ver docs 00 e 05).

### 6. Aprendizado de estilo (Fase 2 — o fosso competitivo)
- Cada peça finalizada pelo advogado entra no acervo pessoal dele (RAG por tenant)
- Próximas gerações imitam estrutura, tom e teses favoritas
- **Efeito lock-in:** quanto mais usa, melhor fica, mais caro é sair
- Disponível nos planos Escritório e Pro (doc 00)

---

## 🛡️ Estratégia anti-alucinação (o risco nº 1 do produto)

Alucinação em peça jurídica não é bug cosmético: advogado que protocola julgado inexistente sofre sanção e perde o caso — e nós perdemos o negócio. A defesa tem **quatro camadas**, e nenhuma delas confia no modelo.

### Camada 1 — Jurisprudência: o modelo nunca escreve citação livre

O modelo é proibido (por prompt **e** por validação de saída) de redigir referência a julgado, súmula ou ementa por conta própria. O fluxo usa **tool use** do SDK:

1. Durante a geração, o modelo emite um **pedido de busca estruturado** — nunca o texto da citação. A tool é definida assim:

```java
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;

Tool buscarJurisprudencia = Tool.builder()
    .name("buscar_jurisprudencia")
    .description("Busca julgados e súmulas VERIFICADOS na base real. "
        + "Única fonte permitida para qualquer citação de jurisprudência na peça.")
    .inputSchema(Tool.InputSchema.builder()
        .properties(Tool.InputSchema.Properties.builder()
            .putAdditionalProperty("tese", JsonValue.from(Map.of(
                "type", "string", "description", "tese jurídica, ex.: horas in itinere")))
            .putAdditionalProperty("tribunal", JsonValue.from(Map.of(
                "type", "string", "description", "ex.: TST, TRT-12")))
            .putAdditionalProperty("tipo", JsonValue.from(Map.of(
                "type", "string", "enum", List.of("sumula", "acordao"))))
            .build())
        .required(List.of("tese"))
        .build())
    .build();
```

2. O backend recebe o `tool_use`, consulta uma **base real** (DataJud/API de jurisprudência de provedor — Escavador ou similar; ⚠️ cotação e cobertura pendentes, decisão #5 do doc 00 §10) e devolve como `tool_result` apenas resultados verificados, com identificador, órgão e data.
3. O modelo só pode citar o que veio da busca, e cada citação inserida carrega metadados (fonte, ID, data da consulta) que ficam visíveis no editor como "citação verificada ✓".
4. Se a busca não retorna nada útil, o modelo insere o marcador **`[SUGESTÃO DE TESE — VERIFICAR JURISPRUDÊNCIA]`**, renderizado em destaque no editor (NodeView custom do wrapper TipTap — doc 05).

**Validador pós-geração (independente do prompt):** um parser varre o texto final procurando padrões de citação (números CNJ, "REsp", "AgRg", "Súmula nº", padrões de ementa). Qualquer citação que não esteja no conjunto verificado da etapa 2 é bloqueada ou rebaixada a marcador de sugestão **antes** de chegar ao editor. Prompt pode falhar; regex não obedece a jailbreak.

### Camada 2 — Fatos: nomes, números e datas vêm SEMPRE do banco

O modelo nunca "lembra" dados factuais — ele escreve ao redor de **placeholders**:

- O prompt recebe os dados do caso como variáveis nomeadas (`{{NUMERO_PROCESSO}}`, `{{NOME_AUTOR}}`, `{{VARA}}`, `{{DATA_AUDIENCIA}}`, `{{VALOR_CAUSA}}`) e é instruído a usá-las literalmente, nunca reescrevê-las.
- Após a geração, o backend **interpola os valores reais direto do banco de dados** — o texto final que o advogado vê nunca contém um número de processo "digitado" pelo modelo.
- **Validação de consistência:** qualquer sequência no output que case com formato de número CNJ, CPF/CNPJ ou data é comparada com os valores do banco para aquele caso; divergência = flag visual no editor ("dado não confere com o cadastro").
- Datas de prazo vêm exclusivamente do motor determinístico (caso de uso 1) — o redator recebe a data pronta como placeholder.

### Camada 3 — Rodapé de transparência e trilha de revisão

Todo rascunho nasce com o rodapé **"Documento gerado com auxílio de IA — revisão profissional obrigatória"** e marca d'água "RASCUNHO" no editor. Como funciona na prática:

- O rodapé acompanha o documento no editor e na exportação `.docx`.
- Para exportar a versão limpa (sem marca d'água de rascunho), o advogado marca um checklist curto de revisão ("Revisei o inteiro teor e assumo a autoria"). Só então o status muda de `rascunho` para `revisado`.
- O evento fica em **log de auditoria** (quem revisou, quando, quanto do texto foi editado) — isso protege o advogado (prova de diligência) e nos protege (prova de que não somos o autor da peça; ver doc 09, responsabilidade civil).
- O rodapé de transparência em si é configurável na exportação final (a peça protocolada é do advogado), mas a trilha interna de revisão não é desativável.

### Camada 4 — Suite de avaliação como guarda de regressão

Nenhum prompt novo vai a produção sem passar pela suite (seção abaixo), que inclui **gate duro de alucinação**: zero citações não-verificadas nos casos de teste. Uma mudança de prompt que "melhora o estilo" mas deixa vazar uma citação livre é reprovada automaticamente.

---

## 💸 Modelos e custos (preços reais, verificados)

> **Fonte dos preços:** tabela oficial da API da Anthropic (verificada em jul/2026 via a documentação/skill oficial da API; cache de 24/jun/2026). **Câmbio assumido: US$ 1 = R$ 5,50** — premissa a revisar trimestralmente; variação cambial de ±10% move o custo de IA em ±10%.

### Preços de API (por milhão de tokens)

| Modelo | ID | Input | Output | Contexto |
|---|---|---|---|---|
| Claude Haiku 4.5 | `claude-haiku-4-5` | US$ 1,00 | US$ 5,00 | 200K |
| Claude Sonnet 4.6 | `claude-sonnet-4-6` | US$ 3,00 | US$ 15,00 | 1M |
| Claude Sonnet 5 | `claude-sonnet-5` | US$ 3,00 (US$ 2,00 promocional até 31/ago/2026) | US$ 15,00 (US$ 10,00 promo) | 1M |
| Claude Opus 4.8 | `claude-opus-4-8` | US$ 5,00 | US$ 25,00 | 1M |

Modificadores relevantes: **cache de prompt** (leitura ≈ 0,1× do preço de input; escrita 1,25× no TTL de 5 min) e **Batches API** (−50% em tudo, para jobs sem exigência de latência). Detalhes na seção de caching abaixo. **A promoção do Sonnet 5 expira antes do nosso lançamento (out/2027) — proibido usar o preço promocional em qualquer projeção** (doc 00 §5.1). ⚠️ Sonnet 5 usa tokenizer novo (~30% mais tokens para o mesmo texto vs. 4.6) — se migrarmos para ele, refazer a conta de tokens por operação, não só o preço.

⚠️ A Anthropic não oferece API de embeddings — o RAG usa provedor separado (OpenAI `text-embedding-3-small`, Voyage ou similar); custo marginal (centavos por processo indexado), mas precisa ser cotado e entra na camada de abstração.

### Custo por operação (nossa carga típica)

| Tarefa | Modelo | Tokens típicos (in/out) | Custo/operação |
|---|---|---|---|
| Classificação de intimação | `claude-haiku-4-5` | ~2k / ~0,3k | US$ 0,0035 ≈ **R$ 0,02** (≈ R$ 0,01 com cache + batch) |
| Resumo leigo | `claude-haiku-4-5` | ~3k / ~0,4k | ≈ **R$ 0,02–0,03** |
| Geração de peça completa | `claude-sonnet-4-6` | 8–30k / 2–8k | US$ 0,05–0,27 ≈ **R$ 0,30–1,50** (média ~R$ 0,80 com cache) |
| Peça complexa (roteamento seletivo) | `claude-opus-4-8` | ~20k / ~6k | ≈ R$ 1,40 — só quando o tipo de peça justificar |
| Chat com autos (Fase 1.5) | `claude-haiku-4-5`/`claude-sonnet-4-6` + embeddings | 8–15k / ~0,5k | **R$ 0,05–0,30/pergunta** (média ~R$ 0,15) |

Esses valores são exatamente as premissas canônicas do doc 00 §5 (peça R$ 0,30–1,50, média R$ 0,80; classificação ~R$ 0,02; chat ~R$ 0,15) — a conta fecha com os preços reais de API. **O SDK Java suporta cache, streaming e Batches — nenhuma alavanca de custo depende de Python/TS.**

### Conta de padeiro, refeita por plano (consistente com o doc 00)

Uso médio = ~65% da cota. Cotas: 12/40/120 peças·mês; chat 50/200/fair use.

| | Solo (R$ 119) | Escritório (R$ 229) | Pro (R$ 449) |
|---|---|---|---|
| Peças (média R$ 0,80) | 8 × 0,80 = R$ 6,40 | 26 × 0,80 = R$ 20,80 | 78 × 0,80 = R$ 62,40 |
| Chat com autos (R$ 0,15) | 33 × 0,15 = R$ 4,95 | 130 × 0,15 = R$ 19,50 | ~300 × 0,15 = R$ 45,00 |
| Classificações + resumos | ~R$ 3,00 | ~R$ 7,50 | ~R$ 15,00 |
| Overhead (IA inline no editor, retries) | ~R$ 3,50 | ~R$ 0 (incluído acima) | ~R$ 0 (incluído acima) |
| **IA — esperado** | **≈ R$ 18/mês** | **≈ R$ 48/mês** | **≈ R$ 110/mês** |
| **IA — teto** (cota cheia, pior preço por operação) | **≈ R$ 38** | **≈ R$ 132** | **≈ R$ 290** |

Com os demais custos variáveis (dados processuais + infra, doc 00), isso sustenta a **margem bruta esperada de ~70%, com piso de ~55% no Solo em uso intenso**. O teto por plano existe justamente para que o pior caso seja limitado — sem cota, um usuário intenso de chat destruiria a margem.

**Observação a favor (doc 00 §5.3):** no lançamento o MVP tem só 2 tipos de peça e ainda não tem chat — o custo real por usuário nos primeiros meses tende a ficar ABAIXO do esperado da tabela. A margem não é o problema deste projeto; o cronograma é.

---

## ⚡ Prompt caching e Batches: as alavancas concretas de margem

Não é otimização futura — é decisão de arquitetura do dia 1, porque nossos prompts têm o formato ideal para cache: **prefixo grande e estável** (system prompt do tipo de peça + templates do nicho + few-shots, 10–20k tokens) seguido de **sufixo volátil** (dados do processo).

**Como o cache funciona (API Anthropic):** é *prefix match* byte-exato. Marcamos um breakpoint (`cache_control: {type: "ephemeral"}`) no fim do bloco estável; leituras subsequentes daquele prefixo custam ~0,1× o preço de input (escrita inicial: 1,25×; TTL padrão 5 min, renovado a cada hit; opção de 1h a 2× a escrita). Máximo de 4 breakpoints por request.

**A forma Java (do SDK oficial):**

```java
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.TextBlockParam;

MessageCreateParams params = MessageCreateParams.builder()
    .model("claude-sonnet-4-6")
    .maxTokens(8192L)
    // .system(String) não carrega cache_control — usar a forma de blocos:
    .systemOfTextBlockParams(List.of(
        TextBlockParam.builder()
            .text(promptEstavelDaPeca)                       // prompt + templates + few-shots
            .cacheControl(CacheControlEphemeral.builder().build())  // breakpoint, TTL 5 min
            .build()))
    .addUserMessage(dadosVolateisDoProcesso)                 // depois do breakpoint
    .build();

// monitorar o hit em produção:
var usage = client.messages().create(params).usage();
metrics.record(usage.cacheReadInputTokens(), usage.cacheCreationInputTokens());
```

**Efeito nos nossos números:**
- Peça com 20k de input, dos quais ~14k são prefixo estável: input cai de US$ 0,060 para ~US$ 0,022 num cache-hit — **~25–35% de redução no custo total da peça** (o output, que domina o preço, não muda). É a diferença entre a peça média custar R$ 1,10 e custar ~R$ 0,80.
- Classificação: o system prompt + few-shots é idêntico para todas as publicações do dia; num lote, só a primeira paga escrita de cache.
- **Batches API (−50%):** a ingestão de publicações é um job noturno sem exigência de latência — classificar o lote diário via Batches (`client.messages().batches()` no SDK Java) corta o custo pela metade, cumulável com cache. É isso que leva a classificação de R$ 0,02 para ~R$ 0,01.

**Regras de engenharia para não quebrar o cache (valem para o código Spring):**
1. Prefixo estável primeiro, dados voláteis depois do breakpoint — **nunca** timestamp, UUID ou nome do usuário no system prompt.
2. Serialização determinística (Jackson com chaves ordenadas) — um byte diferente invalida tudo dali em diante.
3. Mínimo cacheável por modelo: ~2k tokens no Sonnet 4.6, ~4k no Haiku 4.5 e Opus 4.8 — o system prompt de classificação no Haiku precisa somar ≥ 4k tokens (com few-shots) para cachear; abaixo disso o marcador é silenciosamente ignorado.
4. Monitorar `usage().cacheReadInputTokens()` na resposta — se estiver zerado em requests repetidos, algo está invalidando o prefixo.

---

## 🔀 Camada de abstração multi-modelo (decisão arquitetural)

Mantida como decisão de arquitetura desde o início (também é mitigação de risco no doc 09):

- Interface interna única (ex.: `GeradorIA.gerar(Tarefa, Contexto)` — uma interface Java com implementação por provedor) com o **modelo por tarefa definido em configuração** (`application.yml`), não hardcoded — trocar `claude-sonnet-4-6` por `claude-sonnet-5` (ou por concorrente equivalente) é mudança de config + rodada da suite de avaliação, não refactor.
- Preços e modelos mudam rápido (o Sonnet 5 estreou com preço promocional; modelos são substituídos a cada 6–12 meses). A camada de abstração + suite de avaliação nos deixa capturar cada queda de preço com segurança.
- O SDK oficial fica **atrás** da abstração, não espalhado pelo código — os records de structured output e o streaming são detalhes da implementação Anthropic.
- Contratos de não-treinamento e DPA por fornecedor (ver doc 09) são pré-requisito para qualquer provedor entrar na config.

---

## 🧪 Suite de avaliação (o que impede a IA de regredir)

> O essencial está aqui; o detalhamento operacional (rubricas completas, scripts, formato dos casos) vive no doc 13, atualizado quando o beta começar (mês 13). **A suite nasce MENOR que no plano anterior** — consequência direta do beta de 10 advogados (doc 00 §2).

### Coleta dos casos de teste (~20–30 pares intimação→peça; era 40–60)

- **Fonte primária: os 10 advogados do beta fechado (meses 13–14, ago–set/2027).** Pedimos, no onboarding do beta, 2–3 peças reais protocoladas do nicho por advogado + a intimação que as originou — isso dá ~20–30 pares, filtrados por qualidade. Com 2 tipos de peça no lançamento, a densidade por tipo continua razoável (~10–15 casos por tipo).
- **Contrapartida:** o beta já é gratuito por 60 dias; quem ceder peças para a suite ganha prioridade nas 50 vagas founding. Formalizar com **termo de cessão** autorizando uso interno anonimizado (peças protocoladas são públicas em regra, mas o termo elimina ambiguidade; ⚠️ processos em segredo de justiça ficam categoricamente de fora).
- **Complemento obrigatório (mais importante agora que a base é menor):** peças públicas de consulta processual para tipos/situações sub-representados — e para réplica/recurso quando entrarem nos meses 16–17.

### Anonimização

Pipeline em duas etapas antes de o caso entrar no repositório:
1. **Automática:** NER + regex para nomes, CPF/CNPJ, endereços, números de processo → substituição por pseudônimos **consistentes** dentro do mesmo caso ("João da Silva" → "AUTOR_1" em todas as ocorrências, preservando a coerência interna da peça).
2. **Manual:** revisão humana de 100% dos casos na primeira leva (são só ~20–30 — cabe) e amostral nas seguintes.
Os casos anonimizados ficam no repositório junto dos prompts (`/evals/pecas/contestacao-trabalhista/caso-017/`).

### Métricas

| Métrica | Como mede | Tipo |
|---|---|---|
| **Zero alucinação de citação** | Validador da Camada 1 roda sobre o output: nenhuma citação fora do conjunto verificado | **Gate duro** (reprova) |
| **Integridade factual** | Validador da Camada 2: todos os placeholders interpolados, nenhum dado divergente do caso | **Gate duro** |
| **Estrutura obrigatória** | Checklist programático: endereçamento, qualificação, preliminares, mérito, pedidos presentes e na ordem | **Gate duro** |
| **Qualidade argumentativa** | LLM-as-judge com rubrica por tipo de peça (nota 1–5 por critério), calibrado contra notas de advogado parceiro numa amostra | Comparativa |
| **Taxa de sobrevivência à edição** | Ver abaixo — métrica norte, medida em produção | Norte (produção) |

Com ~20–30 casos, as métricas comparativas têm menos poder estatístico que no plano de 40–60 — leitura honesta: o judge detecta regressões grandes, não diferenças finas. Os **gates duros não sofrem** com a base menor (são invariantes, não estatística). A suite cresce com peças de produção cedidas pelos usuários pagos.

### "% do texto que sobrevive à edição" — como medir na prática

É a nossa métrica norte de qualidade porque captura o julgamento real do usuário, não o do avaliador. Implementação:

1. O editor (TipTap/wrapper Angular) grava um **snapshot imutável do rascunho v0** no momento da geração.
2. Na exportação (ou no fim da sessão de edição), computamos o diff entre v0 e a versão final — similaridade por sentença (diff de texto + fallback de similaridade de embedding para pegar paráfrases), não por caractere, para não punir ajustes cosméticos.
3. **Sobrevivência = % de sentenças do v0 presentes (exatas ou parafraseadas) na versão final**, segmentada por seção da peça (preliminares × mérito × pedidos) e por tipo de peça.
4. Leitura do sinal: sobrevivência baixa no *mérito* = problema de qualidade argumentativa; baixa nos *pedidos* = problema de template; ~100% em tudo = 🚩 advogado talvez nem esteja revisando (aciona verificação de UX/compliance, não comemoração).
5. Baseline definido no beta; meta inicial honesta: **≥ 60–70% de sobrevivência média**, refinada com dados reais.

### Critério de promoção de prompt a produção

Prompts são versionados no repositório (`/prompts/pecas/contestacao-trabalhista/v3.md`). Um prompt novo (ou troca de modelo) só é promovido se, rodando contra a suite completa:

1. **Gates duros: 100% de aprovação** em alucinação, integridade factual e estrutura — uma falha reprova, sem exceção;
2. **Nota do judge ≥ versão atual** na média (empate promove se houver ganho de custo/latência);
3. **Revisão humana de 5–10 outputs amostrais pelo Ronny** (e, quando possível, por advogado parceiro do beta) — o judge é ferramenta, não juiz final. Solo, essa revisão é a única com olhos humanos antes de produção: não pular;
4. Deploy com **rollback trivial** (prompt anterior fica taggeado; reverter é 1 commit).

Toda geração em produção loga versão do prompt + modelo + custos — sem isso não há como atribuir regressão de sobrevivência a uma mudança específica.

---

## 🔒 Dados e privacidade na IA

- API de LLM com **contrato de não-treinamento** sobre dados enviados (padrão nas APIs pagas da Anthropic) + DPA assinado (doc 09)
- Anonimizar o que for possível antes de enviar (substituir nomes por placeholders quando a tarefa permitir — a Camada 2 anti-alucinação já empurra o design nessa direção)
- Prompts sempre escopados ao tenant; nunca usar dados de um tenant para responder a outro (doc 05)
- Documentar tudo isso — vira página de "Segurança" no site e argumento de venda
