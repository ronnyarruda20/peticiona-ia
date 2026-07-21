# 13 — Avaliação de IA

> O doc 06 promete suite de casos reais por tipo de peça e a métrica "% do texto que sobrevive à edição" em um parágrafo. Este doc transforma isso em processo operável por **1 dev part-time (12,5h/semana)**. Princípio, agora com mais força: **avaliação barata que roda sozinha > avaliação perfeita que ninguém roda.** Solo, tudo que exigir humano recorrente tende a morrer — então o desenho é: máquina roda sempre, humano entra em pontos fixos e curtos.
>
> Stack: **Java/Spring Boot + SDK oficial `com.anthropic:anthropic-java`** (doc 00 §0.1). ⚠️ Preços e IDs de modelo citados vêm do doc 00 §5.1 (reconferidos jul/2026) e mudam — reconferir por trimestre. Câmbio: US$1 ≈ R$5,50.

---

## 1. 📊 O que medimos (mapa geral)

| Sistema | Métrica | Alvo | Como roda | Humano? |
|---|---|---|---|---|
| Motor de prazos | acerto na tabela-verdade | **100% — sempre** | teste unitário no CI, todo commit (custo zero, é determinístico) | não |
| Classificação de intimações | precisão/recall por tipo + recall da super-classe "gera prazo" | recall "gera prazo" ≥ 0,98 (§5) | suite no CI de prompts, ~R$3/rodada | não |
| Redator de peças | sobrevivência à edição (produção) + LLM-judge pareado (offline) | baseline no beta → gate relativo (§4/§6) | telemetria contínua + suite via Batches API | promoção de prompt |
| Tudo | regressão de fornecedor | drift = alerta | canário semanal agendado (§9) | só se alertar |

**O mínimo inegociável:** o cálculo de prazo é determinístico e precisa de **100%**. Não existe "98% nos prazos" — um prazo errado em produção é a catástrofe do doc 09 e veto de lançamento (G3, doc 12 §6). Tudo o mais neste doc pode ser adiado ou simplificado; a tabela-verdade no CI, não.

---

## 2. 📥 Coletando o dataset (os 10 betas são a mina — e ela encolheu)

Era: 20 betas × ~8 peças ≈ 160 pares. Agora (doc 00 §2): **10 betas, 2 tipos de peça** → meta realista de **~20–30 pares intimação↔peça**. A contrapartida do beta gratuito (60 dias em troca de feedback + peças + depoimento, doc 07) continua.

### O que pedir a cada beta (kit de entrada, no onboarding assistido do M13)

| Item | Quanto | Para quê |
|---|---|---|
| **Contestações** reais protocoladas (trabalhista) | 2–3 por beta | Few-shot dos prompts + gabarito de estrutura/estilo |
| **Petições simples** reais | 1–2 por beta | Idem, para o segundo tipo |
| A intimação/publicação que originou cada peça | junto de cada peça | O par (input, output esperado) — um caso da suite é exatamente isso |
| 30 min de "revisão falada" | 1 sessão gravada, com **6–8 dos 10** betas | Ronny assiste o advogado revisando um rascunho e anota o que ele corta/reescreve — o qualitativo que número não dá. ~4h de trabalho total, cabe no M13 |

Pedido pessoal, sem juridiquês ("me manda 2 contestações que você tenha orgulho + a intimação de cada — é o que ensina a IA a escrever do seu jeito"). Upload em bucket R2 separado (`eval/` — NUNCA misturado com produção). **Termo simples de 1 página**: uso interno para avaliação, anonimização (§3), proibição de treinar modelo próprio, exclusão a pedido — entra no pacote LGPD do doc 09.

### O que se perde com N menor (sem anestesia)

- 160 pares permitiam 30–50 casos curados por tipo. **20–30 pares brutos viram ~12–18 casos de contestação e ~8–12 de petição simples** após curadoria (nem todo par presta).
- ⚠️ **Poder estatístico:** com ~15 casos, num comparativo pareado, diferenças pequenas entre prompts são indistinguíveis de ruído — para afirmar que v_novo > v_atual com ~90% de confiança é preciso vencer em **≥11 de 15 casos (~73%)**. Ou seja: a suite pequena detecta melhorias GRANDES e regressões GRANDES; ajuste fino de prompt vai depender mais da telemetria de produção (§4) do que da suite offline. O gate do §6 já reflete isso.
- Cobertura de subtemas fica rala (2–3 casos de horas extras, 2 de verbas rescisórias...). A matriz de tags existe para enxergar os buracos, não para fingir que não existem.
- **Complemento obrigatório:** peças públicas de consulta processual para engordar os subtemas sub-representados — meta de +5–10 casos até o lançamento. E a fila de correção manual da classificação é rotulagem grátis contínua (§5).

### Curadoria (Ronny + advogado parceiro)

Um caso = `{intimacao, dados_do_processo, peça_real_anonimizada, tipo, tags}` versionado em repo privado (`/eval/casos/contestacao/caso-011/`). Critérios de aceite: peça realmente protocolada, intimação legível, tipo dentro dos **2** da Fase 1, sem particularidade bizarra que não generaliza. Tags: subtema, complexidade (simples/média/alta). São ~25 casos — a curadoria inteira é um fim de semana, não um projeto.

---

## 3. 🕵️ Anonimização sem destruir o exemplo

O valor do exemplo está na **estrutura, nas teses e na redação** — não nos nomes.

| Substituir (sempre) | Por | Preserva |
|---|---|---|
| Nomes de pessoas físicas | placeholders consistentes (`RECLAMANTE_1`, `TESTEMUNHA_A`) — mesma entidade = mesmo placeholder no caso inteiro | Coerência referencial |
| CPF/CNPJ/RG/CTPS, endereços, e-mails, telefones | `{{CPF_1}}` etc. | Nada — é ruído mesmo |
| Nº do processo | nº sintético VÁLIDO no formato CNJ (dígito verificador correto, mesmo tribunal) | Testes de interpolação/parsing continuam reais |
| Razões sociais | nome fictício do MESMO setor ("metalúrgica", "hospital") | O contexto fático que sustenta a tese |
| Valores exatos | valores próximos arredondados | Ordem de grandeza (muda o rito!) |

**Não substituir:** vara/tribunal (regras de prazo e endereçamento dependem disso), datas (deslocar todas por offset fixo por caso — preserva intervalos e cálculo de prazo), teses, jurisprudência citada, estrutura.

Processo: passe automático (regex CPF/CNJ/OAB + NER leve) → **revisão humana de 100% dos casos** (são ~25–35: uma tarde) → registro `anonimizado_por/em`. Peça em segredo de justiça não entra na suite, ponto.

---

## 4. ✂️ "% do texto que sobrevive à edição" — definição OPERACIONAL

A métrica norte do doc 06, definida para caber num sprint:

### O que diffamos contra o quê

- **Lado A:** `peca.conteudo_gerado` — snapshot IMUTÁVEL do que a IA entregou (já existe no schema, doc 11 §3).
- **Lado B:** `peca.conteudo` na transição `estado → revisado` (ou no primeiro export, o que vier antes).
- Ambos normalizados: TipTap JSON → texto plano; remove marcadores `[VERIFICAR]`/`[DADO PRESUMIDO]` e rodapé de IA; colapsa whitespace; **exclui dados interpolados do banco** (nomes, nº de processo — não foram "escritos" pela IA, não contam a favor dela).

### O algoritmo (LCS sobre snapshot)

```
tokens_A = tokenizar_palavras(texto_gerado)
tokens_B = tokenizar_palavras(texto_revisado)
M = blocos_iguais(LCS entre A e B), descartando matches < 5 tokens
    (evita crédito por "o", "de", "não" soltos)
sobrevivencia = Σ|M| / |tokens_A|        ∈ [0,1]
```

Em Java: **java-diff-utils** (`io.github.java-diff-utils`) — `DiffUtils.diff(tokensA, tokensB)` devolve os deltas; blocos iguais = trechos fora de qualquer delta. Alternativa: LCS próprio (~50 linhas, determinístico). Roda num job Spring assíncrono no evento de transição de estado; grava `sobrevivencia`, `cobertura` e a versão do algoritmo na linha da peça.

- **Por que LCS e não embedding/semântica:** queremos medir *retrabalho do advogado*, não equivalência de significado. Se ele reescreveu com outras palavras, ele TRABALHOU — a métrica deve cair. Barato, determinístico, explicável.
- Granularidade dupla: peça inteira + **por seção** (endereçamento, preliminares, mérito, pedidos — os headings do template dão o corte). O agregado esconde que o mérito é sempre reescrito; o por-seção diz ONDE o prompt é ruim.
- Registrar junto: `tempo_de_revisao` (abrir→revisado) e nº de gerações inline usadas.

### Armadilhas conhecidas

| Armadilha | Correção |
|---|---|
| "Carimbador": revisa sem ler → sobrevivência 100% falsa | Só contam peças `revisado` E exportadas E tempo de revisão > 3 min. Cruzar com a revisão falada (§2) |
| Peça abandonada (gerou e desistiu) | Sobrevivência = 0 e flag `abandonada` — é o pior sinal e não pode sumir da média |
| Advogado adiciona MUITO texto novo (IA não errou, foi rasa) | Reportar também `cobertura = Σ|M| / |tokens_B|`. Sobrevivência alta + cobertura baixa = "certo, mas raso" |

### Baseline e alvo

- **Baseline (M13, primeiras 2 semanas de beta): medir sem meta.** Não existe número de mercado confiável para peça trabalhista em PT-BR; inventar alvo antes do dado é teatro. ⚠️ Com 10 betas o baseline vem de poucas dezenas de peças — tratar como faixa, não como número.
- Depois: alvo = **melhorar o próprio baseline** e nunca regredir >5 p.p. em promoção de prompt (§6). ⚠️ Chute a confirmar: utilizável ≈ sobrevivência ≥60%; abaixo de 40% o advogado ganharia tempo escrevendo do zero — se o baseline vier aí, o problema é de produto, não de prompt.

---

## 5. 🎯 Classificação de intimações e prazo

### Classificação (recall da classe que importa)

- **Golden set:** 150–300 publicações reais rotuladas à mão. Fontes: processos de teste dos M4–M8 + betas + **a fila de correção manual, que é rotulagem grátis** — cada `revisada_por` preenchido em produção vira caso novo sem esforço extra. Solo, essa é a única fonte que cresce sozinha; começar com ~150 e deixar a fila engordar.
- Métricas por classe (`tipo_ato`): precisão, recall, F1 + matriz de confusão; macro-F1 como agregado.
- **A métrica que manda: recall da super-classe "gera prazo"** (intimação p/ contestar, réplica, recurso...). Falso negativo aqui = prazo perdido = a catástrofe do doc 09. Gate: **recall ≥ 0,98 contando a fila de baixa confiança como acerto** (mandar para humano é OK; classificar como "ciência" uma intimação com prazo é O erro).
- Calibração do corte de `class_confianca`: escolher o threshold no golden set tal que, acima do corte, o erro da super-classe seja ~0. O % que cai na fila é métrica de produto (meta <15%), não de segurança.
- Custo por rodada: ~200 casos × ~R$0,015 (classificação no modelo econômico da config — referência atual `claude-haiku-4-5`, doc 00 §5.1) ≈ **R$3** → roda em TODA mudança de prompt, sem dó.

O classificador usa **structured outputs do SDK Java** — o schema JSON deriva de um record, sem cola manual:

```java
// O schema JSON deriva do record — validação estrutural de graça
record ClassificacaoIntimacao(
    @JsonPropertyDescription("tipo do ato, ex.: intimacao_contestacao") String tipoAto,
    @JsonPropertyDescription("dias de prazo, se houver") Integer prazoDias,
    @JsonPropertyDescription("dias_uteis ou corridos") String contagem,
    @JsonPropertyDescription("0 a 1") Double confianca) {}

StructuredMessageCreateParams<ClassificacaoIntimacao> params = MessageCreateParams.builder()
    .model(config.modeloClassificacao())   // vem da config — camada de abstração, doc 06
    .maxTokens(1024L)
    .systemOfTextBlockParams(List.of(TextBlockParam.builder()
        .text(promptClassificacao)          // prefixo estável: prompt + few-shots
        .cacheControl(CacheControlEphemeral.builder().build())  // cache = ~0,1× no input
        .build()))
    .outputConfig(ClassificacaoIntimacao.class)
    .addUserMessage(textoPublicacao)
    .build();
```

O runner da suite é um teste JUnit parametrizado sobre `/eval/casos/` que chama esse mesmo código e compara com o rótulo — mesma classe de produção, zero divergência entre o que se testa e o que roda.

### Cálculo de prazo: 100%, sem exceção

- É código determinístico (doc 06/11): a "suite" é a **tabela-verdade** — ≥100 casos `{entrada → data_limite esperada}` cobrindo recesso, feriado estadual/municipal, prorrogação por fim de semana, virada de ano, contagem CLT×CPC. JUnit puro, roda em todo commit. **Qualquer caso falhando = build vermelho.**
- **Dupla conferência sem par** (a antiga divisão "um calcula, o advogado confere" morreu): cada caso da tabela registra a **fonte externa** que o valida — print/link da calculadora oficial do tribunal — no próprio arquivo do caso; o advogado parceiro confere uma **amostra** (~20 casos) uma vez. Caso sem fonte externa não entra na tabela: o Ronny conferindo o próprio cálculo não é conferência.
- Erro achado em produção → vira caso da tabela ANTES do fix (regression test).
- O que não é testável por unidade: a **curadoria de feriados** (dado, não código). Auditoria mensal com alarme de calendário — dono: Ronny, porque não há mais ninguém (doc 12 §7.4).

---

## 6. 🚪 Gate de promoção de prompt (quantitativo, ajustado para N pequeno)

Prompts versionados no repo (`/prompts/...`, doc 06). Promover `v_novo` exige, na mesma PR:

```
CLASSIFICADOR
  ✓ recall super-classe "gera prazo" ≥ 0,98        (absoluto)
  ✓ macro-F1 ≥ macro-F1(v_atual) − 0,01            (não regride)
  ✓ % fila manual ≤ % atual + 3 p.p.

REDATOR (por tipo de peça alterado — contestação: ~12–18 casos; petições simples: ~8–12)
  ✓ suite completa do tipo rodada via Batches API
  ✓ LLM-judge pareado (§8): v_novo vence em ≥60% dos casos
      ⚠️ com N≈15, 60% ≈ 9–11 vitórias — margem mínima acima do ruído;
      vitória por 1–2 casos NÃO promove (empate técnico → fica o atual)
  ✓ zero NOVOS flags de alucinação dura no checker automático:
      citação fora de bloco [VERIFICAR] · nome/nº/data divergente
      do input · seção obrigatória ausente
  ✓ custo médio/peça ≤ 1,3× atual (margem, doc 08)

AMBOS
  ✓ resultado da rodada commitado junto (JSON no repo) — auditável
  ✓ pós-deploy: comparar sobrevivência (§4) da 1ª semana de v_novo
    vs v_atual (o campo peca.ger_prompt_ver existe pra isso);
    queda >5 p.p. com n≥20 peças ⇒ rollback
```

Com a suite pequena, **a validação pós-deploy em produção pesa MAIS que a offline** — a suite barra desastre; a telemetria decide o ajuste fino. Sem exceção "é só uma vírgula": vírgula muda saída de LLM, e o gate é barato (§7) justamente para não haver desculpa.

---

## 7. 💸 Rodando barato — em dinheiro E em tempo (1 dev, part-time)

Com a suite menor, o custo em dinheiro despencou; **o gargalo agora é o tempo do Ronny.** Tudo que puder rodar sem humano, roda sem humano.

| Alavanca | Efeito |
|---|---|
| **Batches API (−50%)** para toda rodada de suite | Suite completa do redator: ~25 casos × 2 tipos... na prática ~30 gerações × ~R$0,80 ≈ R$24 → **~R$12 em batch**. Resultado em ≤1h, disparado por workflow noturno |
| **Suite em camadas** | *Smoke* (3 casos/tipo, ~R$5): a cada mudança de prompt, no CI. *Completa* (~R$12): só na promoção (§6) e no canário mensal. *Classificador* (~R$3): sempre |
| **Prompt caching** (leitura ~0,1× input) | System prompt + template + few-shots idênticos em todos os casos → `cacheControl` no prefixo estável (código do §5) derruba o custo real bem abaixo dos ~R$12 |
| **Modelo certo por tarefa** (config, doc 06) | Classificação/judge no modelo econômico; geração no intermediário. Referência atual: `claude-haiku-4-5` (US$1/US$5 por MTok) e `claude-sonnet-4-6` (US$3/US$15) — doc 00 §5.1 ⚠️ reconferir por trimestre |
| **Produção é a maior suite grátis** | Sobrevivência (§4), % fila manual, `revisada_por` — telemetria já paga pelo uso, coletada por queries agendadas, zero horas do Ronny |
| Orçamento | **≤R$100/mês** em regime (1 promoção/mês + canários + smokes). Dinheiro não é o problema deste doc; tempo é |

**O que NÃO fazer (agora com mais razão):** plataforma de eval paga, dashboard sofisticado, fine-tuning de judge, métrica nova antes de a atual ter dado uma decisão. Um runner JUnit + JSON no repo + uma planilha resolvem até ~500 assinantes. Cada hora de tooling de avaliação é uma hora a menos de produto — e o produto está 14 meses atrasado por definição.

---

## 8. ⚖️ LLM-as-judge: quando serve, quando engana

### Serve para (usamos)

- **Comparação pareada** (v_atual × v_novo sobre o mesmo caso): "qual das duas contestações está mais completa/estruturada/adequada à intimação?" — julgamento relativo é onde judges são confiáveis.
- Checklist objetivo por rubrica: tem todas as seções? endereçamento correto? enfrenta todos os pedidos da inicial? tom formal? Cada item SIM/NÃO com justificativa — não "nota 8,3". (Implementação: structured output com um record de booleans, mesmo padrão do §5.)
- Triagem: apontar os piores casos da suite para o humano olhar primeiro — com o tempo do Ronny escasso, o judge decide ONDE o humano gasta a hora dele.

### Engana em (não delegamos)

- **Correção jurídica de tese/jurisprudência** — o judge não sabe direito trabalhista melhor que o gerador; validar citação é busca em base real ou humano, nunca outro LLM (doc 06, camada 1 anti-alucinação).
- Vieses conhecidos: posição (prefere a primeira → **alternar ordem e exigir maioria em 3 rodadas**), verbosidade (prefere a mais longa → rubrica pune prolixidade), auto-preferência (judge de modelo ≠ gerador quando possível).
- Nota absoluta ("dê 0–10") — instável entre rodadas; só pareado + checklist binário.

### Calibração contra humano (senão o judge é só um número bonito)

- **Mensal, ~1h: Ronny + advogado parceiro julgam às cegas 10 pares**; medir concordância humano×judge. (Era 15 pares — 10 é o que cabe numa hora recorrente que precisa sobreviver 14 meses.)
- Concordância ≥80% → judge segue valendo no gate. <80% → rubrica revisada e o gate do mês é decidido pelo humano.
- Registrar a concordância histórica no repo — degradação ao longo do tempo é sinal de regressão do próprio avaliador (§9).

⚠️ Honestidade solo: essa é a única cerimônia recorrente com humano deste doc. Se ela falhar dois meses seguidos, o sintoma é o do doc 12 §5.4 — registrar e recalibrar o ritual, não fingir que a calibração acontece.

---

## 9. 🌊 Regressão de fornecedor (o modelo muda debaixo dos nossos pés)

O provedor atualiza o modelo servido e os prompts "quebram" sem nenhum deploy nosso. Defesas em 4 camadas — todas automatizadas, porque não há segunda pessoa para "dar uma olhada":

1. **Fixar versão quando existir ID datado** e registrar `class_modelo`/`ger_modelo` em TODA linha (schema doc 11). Aliases (`-latest` ou nome sem data) proibidos em produção. Sem isso, nem sabemos qual modelo produziu o quê.
2. **Canário semanal agendado (~R$8):** mini-suite fixa (8 casos de redator + 50 de classificador) roda todo domingo à noite via workflow agendado (GitHub Actions cron → job Spring). Compara com a última rodada: divergência de classificação >5% ou judge pareado apontando piora em ≥3/8 casos → **alerta por e-mail para o Ronny** (não há mais "grupo"). Rodar também no dia de qualquer anúncio/changelog do fornecedor.
3. **Monitor de produção (grátis, é query agendada):** (a) % de publicações na fila de baixa confiança, (b) sobrevivência média móvel 7 dias, (c) taxa de peças abandonadas, (d) erros de parsing do output estruturado. Salto abrupto sem deploy nosso ≈ mudança do lado de lá. E-mail semanal automático com os 4 números — 2 min de leitura no review de domingo (doc 12 §5.1).
4. **Plano de resposta:** confirmou regressão → (i) re-rodar suite completa em batch; (ii) se disponível, repinar na versão datada anterior; (iii) se não, ajustar prompt contra a suite até repassar o gate §6; (iv) registrar o incidente — é o argumento para a camada de abstração multi-modelo (doc 06/09): trocar de fornecedor é a defesa final, e a suite é o que torna a troca testável em vez de um salto no escuro.

---

## 10. 📆 Rotina de avaliação em regime (uma pessoa, então: quase tudo máquina)

| Cadência | Atividade | Quem/o quê | Custo |
|---|---|---|---|
| Todo commit | Tabela-verdade de prazos no CI | máquina | R$0 |
| Toda mudança de prompt | Smoke redator + suite classificador | máquina (CI) | ~R$8 |
| Promoção de prompt (~1×/mês) | Suite completa em batch + judge pareado + gate §6 | máquina; Ronny lê o resultado | ~R$12 |
| Semanal | Canário + e-mail dos monitores de produção | máquina; Ronny lê 2 min no domingo | ~R$8 |
| Mensal | Calibração judge×humano (10 pares) + auditoria de feriados | **Ronny + advogado parceiro** — 1h + 1h | tempo |
| Contínuo (beta, M13–14) | Coleta de pares intimação↔peça + revisões faladas | Ronny (dentro das horas de beta do doc 12 §2) | tempo |

Total em dinheiro: **≈R$50–100/mês** — folgado. Total em tempo recorrente do Ronny: **~2–3h/mês** fora mudanças de prompt. É o teto do que 12,5h/semana sustenta junto com dev, GTM e suporte — e é suficiente: a linha que separa este processo de "teatro de qualidade" é que cada métrica aqui dispara uma decisão concreta (bloquear build, barrar promoção, rollback, alerta). Métrica que não dispara decisão não entra.
