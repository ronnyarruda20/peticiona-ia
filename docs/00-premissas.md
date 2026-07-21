# 00 — Premissas Canônicas (Fonte Única de Verdade)

> **Este documento manda em todos os outros.** Se qualquer número, data ou escopo nos docs 01–13 conflitar com o que está aqui, **vale o que está aqui** — e o outro doc deve ser corrigido.
> Criado em jul/2026 após auditoria de contradições entre os docs 04, 06, 07 e 08. **Reescrito em jul/2026 após duas mudanças estruturais: (1) a saída do sócio — o projeto agora é SOLO — e (2) a correção do perfil técnico do Ronny — fullstack Angular + Java, não "UI/UX + front" — com troca completa da stack.**

---

## 🔴 0. O que mudou (e por que este doc foi reescrito)

| # | Mudança | Consequência |
|---|---|---|
| 1 | **O projeto passou a ser solo.** | Capacidade caiu de 25h/semana (2 × 12,5h) para **12,5h/semana**. Não há mais: revisão de código por outra pessoa, paralelismo entre trilhos front/back, backlog "não-bloqueado" para cobrir ausências, divisão de GTM/dev. Toda a conta de 840h ÷ 25h/semana ≈ 8 meses está **morta**. |
| 2 | **O perfil técnico real do Ronny é outro.** Os docs o descreviam como "UI/UX, front-end". Errado: ele é **programador fullstack Angular + Java**. | A stack do doc 05 (Next.js/React/Tailwind/shadcn + NestJS/FastAPI) foi escolhida para um time de dois com especialista de back. Nenhuma dessas tecnologias é dele. Stack trocada (ver §0.1). A decisão "NestJS × FastAPI" deixou de existir. |

### 0.1 Stack nova (DECIDIDA — não é decisão em aberto)

| Camada | Escolha | Observação |
|---|---|---|
| Front | **Angular** + `@angular/ssr` (landing/SEO) | Stack que o Ronny domina. PWA para mobile continua valendo |
| Editor | **TipTap/ProseMirror** com **wrapper Angular próprio** | As bindings oficiais do TipTap são React/Vue; o core é JS agnóstico de framework. O wrapper é **custo novo de horas** (ver §3). Spike no mês 1–2: avaliar lib comunitária (ex.: ngx-tiptap) antes de escrever wrapper do zero |
| Back | **Spring Boot** (Java) + SDK oficial **`com.anthropic:anthropic-java`** | SDK de primeira classe: structured outputs derivam JSON Schema de POJO/record, streaming, tool use, prompt caching — tudo suportado |
| Dados | PostgreSQL + pgvector · Redis (fila/cache) | Inalterado |
| Infra | Railway/Render/Fly · Cloudflare R2 · GitHub Actions | Inalterado |

**Efeito líquido da troca em duas direções (as duas são reais):**
- **Mais rápido:** zero curva de aprendizado de React/Next/Nest; SDK Java oficial barateia a cola de IA (classificação, redator); uma pessoa só = zero overhead de contrato de API entre front e back de pessoas diferentes.
- **Mais lento:** sem paralelismo nenhum; wrapper Angular do TipTap é atrito real; menos exemplos públicos de RAG/IA em Java do que em Python/TS.

### 0.2 ⚰️ Revogado nesta reescrita (não usar em nenhum doc)

| Revogado | Substituído por |
|---|---|
| Capacidade de **25h/semana** (2 pessoas) | **12,5h/semana** (só Ronny) |
| Conta **700h + 140h buffer = 840h ÷ 25h/sem ≈ 8 meses** | 595h + 180h = **775h ÷ 12,5h/sem ≈ 14 meses** (§3–§4) |
| Stack **Next.js + React + Tailwind + shadcn / NestJS ou FastAPI / BullMQ ou Celery** | Angular + Spring Boot (§0.1) |
| Decisão em aberto "NestJS × FastAPI" (antigo §8 #4) | Não existe mais — back é Spring Boot |
| **Toda a seção societária do doc 08**: acordo de sócios, 50/50, vesting 4 anos, cliff 1 ano, regra de saída/desempate; e a decisão #1 do antigo §8 | Virou pó. Projeto solo, sem sociedade. (O doc 08 deve remover a seção; enquanto não for corrigido, ela está revogada por este parágrafo) |
| Divisão de trabalho por trilhos (doc 05/12), rituais de 2 sócios (doc 12 §5), "demonstrável pelo OUTRO sócio" | Sem par. Critério de "pronto" vira: demonstrável **em produção com dado real** + suite de testes passando |
| Beta com **20 advogados** | **10 advogados** (§2) — solo não suporta onboarding assistido de 20 |
| Lançamento pago **abr/2027 (mês 9)** e todas as datas derivadas | Novas datas no §4 |
| Marco "MVP em 4 meses" (doc 04, já revogado antes) | Continua revogado — agora com mais razão |

---

## 1. 🎯 Premissa zero: o "aha moment" (inalterada)

```
Intimação chega → IA lê e classifica → prazo entra na agenda →
IA rascunha a peça de resposta → advogado revisa e exporta
```

Se uma feature não é necessária para esse fluxo acontecer no primeiro uso, **ela não entra na Fase 1**. O fluxo não passa por WhatsApp nem financeiro — dores de retenção, não de conversão. Isso não mudou; solo, vale ainda mais: **cada hora fora do fluxo do aha é uma hora que atrasa o lançamento em regime de 12,5h/semana.**

---

## 2. ✂️ Escopo da Fase 1 — cortado DE NOVO (MVP solo)

A 12,5h/semana, nem o MVP da versão anterior sai em tempo aceitável. Aplicamos **desde já** os itens 1–5 da ordem de sacrifício do doc 12 §4 (que era o plano de contingência — agora é o plano):

### Fica na Fase 1 (MVP solo)

| Feature | Mudança vs. versão anterior |
|---|---|
| Onboarding: cadastro + importação por OAB/UF (DataJud) | Igual |
| Ingestão de publicações (DJEN/Comunica CNJ) | Igual — continua sendo o insumo de tudo |
| Classificação IA de intimações (tipo, providência, urgência, fila de baixa confiança) | Igual |
| Motor **determinístico** de prazos + agenda com **alertas por e-mail** | **Sem push** no lançamento (corte #2 — e-mail cobre o essencial) |
| Redator IA com RAG — 1 nicho, **2 tipos de peça: contestação + petições simples** | **Era 4 peças** (corte #3 — contestação é 80% do aha; réplica e recurso inominado entram 1–2 meses pós-lançamento) |
| Editor TipTap (wrapper Angular) + IA inline + export .docx | Igual — inegociável (revisão humana obrigatória) |
| CRUD processos/clientes/docs + dashboard "Seu dia" **estruturado, sem frase-resumo IA** | Corte #4 (charme, não função) |
| Cobrança por **link de pagamento manual** + landing (@angular/ssr) + LGPD mínimo | **Sem billing automático** no lançamento (corte #5 — operacional chato para nós, invisível para o cliente; automatizar até ~50 assinantes) |
| **Sem dark mode / polish visual não-crítico** | Corte #1 |

### Sai da Fase 1 (inalterado)

WhatsApp Business API, módulo financeiro/Pix, chat com os autos (fase 1.5, pós-lançamento), 2º nicho, timesheet, multi-usuário complexo, site do advogado, relatórios, app nativo.

### Beta fechado: 10 advogados (era 20)

Onboarding assistido + suporte + grupo de feedback de 20 advogados consome ~30h/mês — não existe dentro de 12,5h/semana junto com hardening. **10 advogados.** Consequência para o doc 13: a suite de avaliação nasce com ~20–30 pares intimação→peça (era 40–60); complementar com peças públicas de consulta processual.

**Nunca cortar (a tese morre):** ingestão de publicações · classificação com fila de baixa confiança · motor determinístico de prazos com memória de cálculo · revisão humana obrigatória + estados de peça · export .docx.

---

## 3. 🧮 Capacidade e estimativa de horas (a conta, aberta e refeita bloco a bloco)

**Capacidade:** 1 pessoa × **12,5h/semana** (média honesta para quem tem emprego). ~54h/mês.

### 3.1 Reestimativa por bloco — solo + Angular/Spring Boot

Cada bloco foi reavaliado contra dois vetores: (a) stack dominada + SDK oficial + zero overhead de integração entre duas pessoas (barateia); (b) trabalho solo + wrapper TipTap + menos referência de RAG em Java (encarece).

| Bloco | Antes | Agora | Δ | Por quê |
|---|---:|---:|---:|---|
| Fundações: repo, CI/CD, auth, multi-tenancy + RLS, base visual | 80 | 70 | −10 | Spring Security e Angular CLI são território conhecido; sem coordenação de setup entre 2 pessoas. RLS + teste de isolamento continua pesado — não cortar aí |
| Onboarding + importação DataJud por OAB | 70 | 65 | −5 | Some o custo de contrato de API + mocks entre front e back de pessoas diferentes. A integração DataJud em si não fica mais fácil |
| Ingestão DJEN/Comunica + workers e filas | 90 | 90 | 0 | A complexidade é externa (polling, matching, dedup, retry, monitoramento). Spring + Redis equivale a BullMQ/Celery — nem ganha nem perde |
| Classificação IA (prompts + structured output + telas) | 50 | 40 | −10 | O SDK Java oficial deriva o JSON Schema de um record — some a cola manual de schema/validação; prompt + telas na mesma cabeça |
| Motor de prazos determinístico + testes pesados | 70 | 65 | −5 | Lógica pura + suite de testes: independe de stack. Ganho pequeno por um único dono do domínio |
| Agenda + alertas | 40 | 40 | 0 | (antes do corte de push — ver 3.2) |
| Redator IA: RAG, prompts, suite de avaliação | 120 | 125 | +5 | SDK ajuda (streaming, tool use, cache), mas há menos exemplos de RAG em Java que em Python/TS; pgvector via JDBC é mais manual; embeddings via API externa |
| Editor TipTap + IA inline + export .docx | 80 | 105 | **+25** | **Custo novo: wrapper Angular do TipTap.** Bindings oficiais são React/Vue; NodeViews custom (`[VERIFICAR]`, dado interpolado), sincronização com a change detection do Angular, inserção streaming no editor — tudo por nossa conta |
| CRUD + dashboard "Seu dia" | 60 | 50 | −10 | CRUD é o ponto forte do Angular; sem integração entre 2 pessoas |
| Billing + landing + LGPD mínimo | 40 | 40 | 0 | (antes do corte de billing — ver 3.2) Landing em @angular/ssr é rotina |
| **Subtotal reestimado (escopo antigo)** | **700** | **690** | −10 | A troca de stack quase se paga: o que o SDK e o domínio da stack economizam, o wrapper TipTap devolve |

**Leitura honesta:** a stack nova NÃO salva o cronograma — salva ~10h em 700. Quem mata o cronograma é a capacidade pela metade. Por isso o corte de escopo do §2 não é opcional.

### 3.2 Cortes de escopo aplicados (ordem de sacrifício, itens 1–5)

| Corte | Horas |
|---|---:|
| #1 Dark mode + polish visual não-crítico (de Fundações/CRUD) | −15 |
| #2 Push notification — só e-mail (Agenda 40 → 25) | −15 |
| #3 4 → 2 tipos de peça (Redator 125 → 95) | −30 |
| #4 Frase-resumo IA do "Seu dia" (Dashboard) | −10 |
| #5 Billing automático → links de pagamento manuais (Billing 40 → 15) | −25 |
| **Total cortado** | **−95** |

### 3.3 A conta final

| | Horas |
|---|---:|
| Features (690 − 95) | **595** |
| **Buffer de 30%** (ver §3.4) | **180** |
| **Total Fase 1** | **775** |

### 3.4 Por que buffer de 30% (era 20%)

Solo, o risco de retrabalho **sobe**, não desce:
1. **Sem revisor de código.** Bug que o autor não enxerga só aparece no beta — e correção tardia custa 3–5× mais. Mitigação parcial (revisão assistida por IA, testes), mas mitigação não é par.
2. **Sem segunda opinião de arquitetura.** Decisão ruim de modelagem descoberta no mês 8 é retrabalho grande, e não há ninguém para pegá-la no mês 2.
3. **O wrapper TipTap é estimativa de coisa nunca feita** — categoria clássica de estouro.
4. O buffer também absorve o que antes era "trabalho do outro": curadoria de feriados, ajuste de prompts contra a suite, bugs do beta.

20% era defensável com dois pares de olhos. Com um, 30% é o mínimo honesto. (Não inflamos mais porque o escopo já foi cortado ao osso — buffer não é desculpa para escopo mal cortado.)

---

## 4. 📅 Cronograma canônico (relógio: ago/2026 = mês 1)

**A conta:**

```
775h ÷ 12,5h/semana = 62 semanas ≈ 14,3 meses  (base)
Pessimista (10h/semana reais): 775 ÷ 10 = 77,5 semanas ≈ 18 meses
```

**Intervalo honesto: 14–18 meses de desenvolvimento.** E atenção: as 12,5h/semana precisam ser QUASE todas de dev. Entrevistas de validação, conteúdo, recrutamento e suporte do beta saem das mesmas horas — é exatamente por isso que o intervalo pessimista existe e é provável.

| Marco | Mês | Data | O que acontece |
|---|---|---|---|
| Validação | 1–2 | ago–set/2026 | 15 entrevistas + landing/lista de espera (meta: 150 e-mails até o beta) — **compete com dev nas mesmas 12,5h** |
| Desenvolvimento Fase 1 | 1–14 | ago/2026 – set/2027 | 775h conforme §3 |
| Gate interno G1 — fluxo intimação→prazo de pé | 7 | fev/2027 | Se não estiver, replanejar já (ver §8 gates) |
| **Mês 12 do projeto** | 12 | jul/2027 | **Ainda em desenvolvimento. MRR R$ 0.** Gate interno G2: feature-complete do fluxo do aha |
| **Beta fechado** | 13–14 | ago–set/2027 | **10 advogados**, gratuito 60 dias, 1 nicho, 2 peças |
| **Lançamento pago** | **15** | **out/2027** | Oferta founding para betas + lista. Janela honesta: **out/2027 – jan/2028** |
| Réplica + recurso (4 peças) · billing automático | 16–17 | nov–dez/2027 | Devolve os cortes #3 e #5 |
| Chat com autos + 2º nicho ("fase 1.5") | 17–19 | dez/2027 – fev/2028 | |
| **Mês 18 — GATE de continuidade** | 18 | jan/2028 | ~45 assinantes / MRR ~R$ 4,7k esperados (§7). **< 35 pagantes ou churn > 8%/mês ⇒ reavaliação formal** |
| **Mês 24** | 24 | jul/2028 | ~125 assinantes / MRR ~R$ 16k |
| Mês 30 | 30 | jan/2029 | ~270 assinantes / MRR ~R$ 40k (a antiga foto do "mês 24") |

**Sem anestesia:** o que era "8 meses até lançar" virou **~15 meses até a primeira receita** e **~2,5 anos até MRR na casa de R$ 40k**. Essa é a conta real de um SaaS solo a 12,5h/semana. Se esse prazo for inaceitável, as alternativas são aumentar horas (trocar de emprego / reduzir jornada), achar outro sócio, ou não fazer. O que NÃO existe é fazer a mesma Fase 1 em 8 meses sozinho.

---

## 5. 💰 Planos, custos de IA e margem (preços REAIS reconferidos)

### 5.1 Preços de API (reconferidos em jul/2026 via documentação oficial da API Anthropic; cache de 24/jun/2026)

**Câmbio assumido: US$ 1 = R$ 5,50** (revisar trimestralmente; ±10% de câmbio move o custo de IA em ±10%).

| Modelo | ID | Input /M tok | Output /M tok |
|---|---|---|---|
| Claude Haiku 4.5 | `claude-haiku-4-5` | US$ 1,00 | US$ 5,00 |
| Claude Sonnet 4.6 | `claude-sonnet-4-6` | US$ 3,00 | US$ 15,00 |
| Claude Sonnet 5 | `claude-sonnet-5` | US$ 3,00 (promo US$ 2,00 até 31/ago/2026) | US$ 15,00 (promo US$ 10,00) |
| Claude Opus 4.8 | `claude-opus-4-8` | US$ 5,00 | US$ 25,00 |

Modificadores: **cache de prompt** (leitura ≈ 0,1× input; escrita 1,25× no TTL de 5 min) e **Batches API** (−50%, para jobs sem exigência de latência — ex.: classificação noturna). A promoção do Sonnet 5 expira antes do nosso lançamento (out/2027) — **não usar o preço promocional em nenhuma projeção**.

### 5.2 Custo por operação (conta refeita com os preços acima)

| Tarefa | Modelo | Tokens (in/out) | Custo |
|---|---|---|---|
| Classificação de intimação | Haiku 4.5 | ~2k / ~0,3k | (2k×1 + 0,3k×5)/M = US$ 0,0035 ≈ **R$ 0,02** (~R$ 0,01 com cache+batch) |
| Resumo leigo | Haiku 4.5 | ~3k / ~0,4k | ≈ R$ 0,02–0,03 |
| Peça completa | Sonnet 4.6 | 8–30k / 2–8k | US$ 0,05–0,21 (+retries) ≈ **R$ 0,30–1,50, média ~R$ 0,80 com cache** |
| Chat com autos (fase 1.5) | Haiku/Sonnet + embeddings | 8–15k / ~0,5k | ~R$ 0,15/pergunta |

**As premissas do doc 06 (peça R$ 0,80 média; classificação R$ 0,015–0,02; chat R$ 0,15) fecham com os preços reais.** Continuam canônicas. O SDK Java oficial suporta cache e batches — as alavancas de margem não dependem de Python/TS.

### 5.3 Planos, cotas e margem (INALTERADOS — só as datas deslizam)

Preços **R$ 119 / R$ 229 / R$ 449**, cotas **12 / 40 / 120 peças·mês**, chat 50/200/fair use, anual com 2 meses grátis — tabela completa no doc 08. Custo de IA esperado por plano (uso ~65% da cota): **R$ 18 / R$ 48 / R$ 110**; teto R$ 38 / R$ 132 / ~R$ 290.

| | Solo | Escritório | Pro |
|---|---|---|---|
| Margem bruta esperada | R$ 88 (74%) | R$ 156 (68%) | R$ 289 (64%) |
| Margem no pior caso | R$ 68 (57%) | R$ 72 (31%) | ~R$ 109 (24%) |

**Número canônico mantido: margem bruta esperada ~70% (blended), piso ~55% no Solo intenso.** Observação a favor: no lançamento o MVP tem só 2 tipos de peça e ainda não tem chat — o custo real por usuário nos primeiros meses tende a ficar ABAIXO do esperado da tabela. A margem não é o problema deste projeto; o cronograma é.

---

## 6. 🏅 Política founding member (inalterada)

**Founding = os 50 primeiros assinantes pagos: 30% off por 12 meses + preço de tabela congelado enquanto ativo + badge + canal direto com o fundador.** Betas que converterem contam nas 50 vagas. Tíquete efetivo founding ano 1: tíquete cheio × 0,70. Nunca mais "50% vitalício para 100". Racional completo no histórico do doc 08 — nada muda além das datas.

---

## 7. 📈 Projeção de MRR refeita (lançamento no mês 15)

**Premissas que continuam válidas:** mix ano 1 80/17/3 → tíquete cheio **R$ 148**; regime 75/20/5 → **R$ 157**; founding efetivo **R$ 104**; churn **5%/mês ano 1, meta 4% ano 2**.

**O que muda:** as datas deslizam ~6 meses e a base de partida encolhe — beta de 10 (não 20) ⇒ ~6 conversões (60%) + ~8 da lista = **~14 assinantes no lançamento** (era 20).

| Mês | Data | Assinantes | MRR | A conta |
|---|---|---|---|---|
| 6 | jan/2027 | 0 | **R$ 0** | Em dev; lista ~80 e-mails |
| 12 | jul/2027 | 0 | **R$ 0** | Em dev (G2: feature-complete); lista ~150 |
| **15 (lançamento)** | out/2027 | 14 | **R$ 1,5k** | 6 betas convertidos + 8 da lista; 14 × R$ 104 ≈ R$ 1.456 |
| **18 (GATE)** | jan/2028 | ~45 | **R$ 4,7k** | +12–13 brutos/mês (conteúdo + indicação), churn 5%: 14→25→36→47; 45 × ~R$ 105 |
| 24 | jul/2028 | ~125 | **R$ 16k** | Adds sobem p/ 15–20/mês; founding esgota (~mês 20); tíquete médio ~R$ 130 (mix founding/cheio) |
| 30 | jan/2029 | ~270 | **R$ 40k** | Adds 25–30/mês, churn 4%, descontos founding expirando; ≈ a antiga foto do mês 24 |

- **Break-even operacional** (fixos ~R$ 800/mês): ~11 assinantes descontando ~30% de custo variável — acontece **no próprio lançamento**, mas sem folga. Solo, os fixos tendem a ficar na banda de R$ 300–600/mês até o beta.
- **Sanidade:** os R$ 45k de MRR que a versão anterior prometia para jul/2028 agora são a foto de **jan/2029**. Cada corte de capacidade empurra a curva inteira — não existe mágica de funil que compense 6 meses a menos de produto no ar.
- **Consequência para GTM (docs 01/07):** os 12–13 adds/mês do ano 1 dependem de conteúdo + indicação feitos pela MESMA pessoa que desenvolve e dá suporte. É a premissa mais frágil desta tabela e está marcada como risco no §9.

---

## 8. 🚦 Gates de decisão (recalibrados)

| Gate | Quando | Critério | Se falhar |
|---|---|---|---|
| G0 — Validação | fim set/2026 | 15 entrevistas; dor confirmada; nicho decidido | Pivotar nicho antes de escrever prompt/template |
| G1 — Insumo | fim fev/2027 (mês 7) | Intimação real → classificada → prazo calculado, em produção | Replanejar para o cenário de 18 meses; considerar cortes adicionais |
| G2 — Feature-complete | fim jul/2027 (mês 12) | Fluxo do aha ponta a ponta com as 2 peças | Beta desliza; se deslizar > 2 meses, acionar conversa de viabilidade (§9.5) |
| G3 — Lançamento | fim set/2027 (mês 14) | NPS betas ≥ 40; zero prazo errado; ≥ 2 peças/sem/usuário ativo; ≥ 6 betas dispostos a pagar | Lançamento desliza (janela até jan/2028). **Nunca lançar cobrando com prazo errado em produção** |
| **G4 — Continuidade** | **jan/2028 (mês 18)** | **≥ 35 pagantes E churn ≤ 8%/mês** | **Reavaliação formal: pivotar nicho, preço ou encerrar. Critério escrito a frio em jul/2026 — não se renegocia a quente** |
| **G5 — Competição (permanente)** | contínuo | Concorrente relevante (Astrea/Advbox/novo entrante) lança **geração de peças + ingestão + prazos integrados** antes do nosso beta | Reavaliar a tese IMEDIATAMENTE: a janela de 14–18 meses só faz sentido se ninguém a fechar antes. Opções: cortar mais escopo e antecipar beta, ou encerrar cedo e barato |

### 🔴 G5 — ACIONADO EM 20/07/2026 (status: amarelo, pendente de verificação em campo)

O **ApolloIA** (apolloia.com.br) vende **hoje**, a **R$ 89,90–269,90/mês** com trial de 7 dias:

- geração de peças por IA — petição inicial, **contestação cível e trabalhista**, recursos, apelações, agravos, alegações finais (12+ tipos, 8 áreas, "37 agentes")
- base própria de jurisprudência (25M+ decisões, rastreáveis à origem) com posicionamento anti-alucinação explícito
- **monitoramento de publicações por OAB/termo via "fontes oficiais do CNJ"**, 300–500 processos conforme plano
- **alerta antes do prazo**, notificação por e-mail **e WhatsApp**, agenda, gestão de processos/clientes/financeiro
- export Word/PDF · Pix e Stripe · servidores no Brasil, LGPD declarada

Preços conferidos no endpoint público `apolloia.com.br/api/plans/active` em 20/07/2026. Empresa não identificada (sem CNPJ, sem endereço, sem fundadores; contato único por WhatsApp DDD 73) — porém com execução técnica madura (Next.js, microserviço de documentos, portal de assinatura completo).

**O que isso significa, sem anestesia:**

| Premissa nossa | Status |
|---|---|
| "Ninguém liga a IA ao fluxo real do advogado — da intimação ao prazo à peça — a preço de solo" (doc 02) | **FALSA desde antes de ser escrita.** O Apollo entrega os três |
| Solo a R$ 119 é "entrada agressiva, abaixo do Astrea (R$ 146)" (doc 08) | **Enfraquecida.** Apollo Start custa R$ 89,90 (R$ 74,90 no anual) |
| WhatsApp é arma de retenção da Fase 2 (2028) | **Já é tabela de entrada do concorrente** (plano Pro, R$ 139,90) |
| Escopo de lançamento: 1 nicho, 2 tipos de peça | **Menor que o do concorrente que já está no ar** (8 áreas, 12+ peças) |

**O que ainda NÃO foi verificado (e decide o gate):**

1. **Qualidade real das peças.** 37 agentes em 8 áreas é o perfil clássico de amplitude sem profundidade. Não usamos o produto.
2. **A peça nasce da intimação?** O fluxo descrito é conversacional ("conte os fatos do seu jeito") — não confirmado que o monitoramento alimente o redator automaticamente. Se não alimenta, a integração ponta a ponta continua vaga.
3. **Contexto dos autos.** Sem sinal de RAG por tenant sobre o acervo do advogado.
4. **Prazo.** Não calcula contagem — as "55+ calculadoras" são financeiras (rescisão, juros, atualização). Alerta de prazo existe; cálculo com memória, não.
5. **Tração.** Zero cobertura de imprensa, zero LinkedIn, zero menção em ConJur/Migalhas. Pode ser produto sem clientes.

**Ação obrigatória antes de mais qualquer hora de dev:** assinar o Apollo Start (R$ 89,90, 1 mês), gerar **5 contestações trabalhistas com casos reais** e responder por escrito os itens 1–3 acima. É uma decisão de R$ 90 que destrava uma decisão de 775h.

**Regra de decisão, escrita a frio:**

| Achado do teste | Consequência |
|---|---|
| Peças **boas** e nascem da intimação | **G5 disparou em vermelho.** A tese como está morreu. Encerrar cedo e barato, ou repivotar para algo que o Apollo estruturalmente não faz |
| Peças **boas** mas o fluxo é manual (colar/conversar) | Amarelo permanece. Nosso diferencial encolhe para "integração + autos + prazo". Escopo e cronograma precisam ser refeitos para chegar em 2027, não 2028 |
| Peças **medíocres** (genéricas, sem profundidade de nicho) | Janela aberta. A tese de nicho ganha força — mas o preço de R$ 119 e o cronograma de 15 meses continuam de pé para revisão |

Registrado também: **iiLEX** (iilex.com.br, empresa de 2015, ~18 pessoas, Porto Alegre) faz captura automática de intimações dos diários + criação automática de prazos + agenda + IA que resume publicações. Não gera peças. Preço sob consulta, sem trial, ICP de escritório estruturado — não é o Dr. João, mas fecha o flanco de cima.

---

## 9. ⚠️ Riscos novos do cenário solo (não existiam na versão anterior)

| # | Risco | Severidade | Mitigação (parcial — nenhuma resolve) |
|---|---|---|---|
| 9.1 | **Bus factor = 1.** Doença, acidente, pico no emprego, família: o projeto PARA. Não há trilho paralelo nem quem cubra. 2 semanas paradas = 2 semanas de atraso, sempre | Alta | Buffer de 30% já assume perdas; cronograma pessimista de 18 meses é o cenário realista, não o catastrófico. Documentar tudo (ADRs, README) para retomada barata |
| 9.2 | **Sem revisor de código.** Bugs de isolamento multi-tenant ou de cálculo de prazo são os que mais custam — e são os que um segundo par de olhos pegaria | Alta | Testes pesados no motor de prazos e RLS (já orçados); revisão assistida por IA; pentest externo antes de cobrar (doc 05) continua obrigatório |
| 9.3 | **Burnout.** 12,5h/semana TODA semana por 14–18 meses, em cima de um emprego, sem sócio para dividir frustração | Alta | Ritmo sustentável > ritmo heroico; marcos mensais pequenos e demonstráveis; o cronograma já assume 12,5h e não 20 — não planejar sprints heroicos |
| 9.4 | **GTM e dev competem pelas mesmas horas.** Conteúdo, entrevistas, recrutamento de beta, suporte — tudo sai do dev. O ano 1 de adds (12–13/mês) depende disso | Média-alta | Concentrar GTM em janelas (validação no início, recrutamento no mês 12+); métricas de funil simples; aceitar lista de espera menor (150, não 200) |
| 9.5 | **Cronograma longo demais para a tese.** 14–18 meses até lançar é tempo de sobra para um concorrente com time full-time fechar a janela de "IA que gera peça + prazos" | **Alta — é o risco existencial** | Gate G5 (§8). E honestidade: se o Ronny não aceita a hipótese de trabalhar 15 meses e o mercado fechar antes, **o momento de decidir é agora, não no mês 10** |
| 9.6 | Wrapper TipTap estourar a estimativa (105h é chute educado de coisa inédita) | Média | Spike de 1–2 dias no mês 1–2 (avaliar ngx-tiptap/comunidade vs. próprio); plano C: editor mais simples no beta (contentEditable estruturado) e TipTap completo pós-lançamento |
| 9.7 | RAG/IA em Java com menos referência pública — mais tempo em tentativa e erro | Baixa-média | SDK oficial cobre o grosso (structured outputs, streaming, cache); RAG é SQL + pgvector + chamadas de API — não é framework-dependente; suite de avaliação (doc 13) pega regressão |

---

## 10. 🚧 Registro de decisões em aberto (atualizado)

| # | Decisão | Prazo |
|---|---|---|
| 1 | Nome e domínio | ago/2026 |
| 2 | Nicho de estreia: trabalhista (hipótese) × previdenciário — decidir com as 15 entrevistas | set/2026 |
| 3 | Spike do editor: wrapper TipTap próprio × lib comunitária (ngx-tiptap) × plano C simplificado | set/2026 |
| 4 | Situação funcional do Ronny (restrições a atividade empresarial) — antes do CNPJ | out/2026 |
| 5 | Cotação de provedor de dados pago (Judit.io, Escavador) para fallback DataJud/DJEN | dez/2026 |
| 6 | Gateway para o billing automático pós-lançamento: Stripe × Asaas/Pagar.me (Pix/boleto pesam) — no lançamento é link manual | set/2027 |
| 7 | Trial: 14 dias com cartão × 7 sem cartão (testar no beta) | set/2027 |
| 8 | Gate G4 escrito e datado (jan/2028, ≥35 pagantes, churn ≤8%) — **decidido, não renegociável a quente** | — |

*(Decisões extintas: acordo de sócios/vesting — sem sociedade; NestJS × FastAPI — back é Spring Boot.)*

---

## 11. 📌 Resumo canônico (cole isto nos outros docs)

| Premissa | Valor canônico |
|---|---|
| Time | **Solo: Ronny, fullstack Angular + Java, 12,5h/semana** |
| Stack | **Angular + @angular/ssr · TipTap com wrapper Angular · Spring Boot + `com.anthropic:anthropic-java` · PostgreSQL/pgvector · Redis · Railway/Render/Fly · R2 · GitHub Actions** |
| Escopo Fase 1 | Onboarding OAB + ingestão DJEN + classificação IA + motor de prazos + alertas **e-mail** + redator IA (1 nicho, **2 peças**: contestação + petições simples) + editor/export .docx + CRUD/dashboard + **cobrança por link manual**. Sem WhatsApp, financeiro, chat, push, billing automático, dark mode |
| Esforço Fase 1 | **595h features + 180h buffer (30%) = 775h ÷ 12,5h/sem ≈ 14 meses (14–18 no intervalo honesto)** |
| Beta fechado | **10 advogados**, meses 13–14 (**ago–set/2027**) |
| Lançamento pago | **mês 15 (out/2027)**; janela honesta até jan/2028 |
| Preços | Solo **R$ 119** / Escritório **R$ 229** / Pro **R$ 449** |
| Cotas de IA | 12 / 40 / 120 peças·mês; chat 50 / 200 / fair use (fase 1.5) |
| Mix / tíquete | Ano 1: 80/17/3 → **R$ 148**; regime 75/20/5 → **R$ 157**; founding efetivo **R$ 104** |
| Custo IA | Peça ~R$ 0,80 (R$ 0,30–1,50) · classificação ~R$ 0,02 · chat ~R$ 0,15 — preços API reconferidos jul/2026, câmbio R$ 5,50 |
| Margem bruta | **~70% esperada; piso ~55%** (Solo intenso) — reconfirmada com preços reais |
| Founding | 50 primeiros: 30% off 12 meses + preço congelado |
| Churn | 5%/mês ano 1; meta 4% ano 2 |
| MRR | Lançamento (out/27): R$ 1,5k (14) · M18 (jan/28): R$ 4,7k (~45) · M24 (jul/28): R$ 16k (~125) · M30 (jan/29): R$ 40k (~270) |
| Gates | G2 feature-complete jul/2027 · G3 lançamento set/2027 · **G4 jan/2028: <35 pagantes ou churn >8% ⇒ reavaliar** · **G5 🔴 ACIONADO 20/07/2026 (amarelo) — ApolloIA já vende peças + ingestão + alerta de prazo a R$ 89,90. Teste de campo obrigatório antes de retomar o dev (§8)** |
| Veredito | ⚠️ **EM REVISÃO desde 20/07/2026.** O veredito anterior — *"viável na margem e no custo; o risco é o tempo"* — pressupunha janela aberta. Com o G5 em amarelo, o risco deixou de ser só o tempo e passou a ser **a existência da lacuna**. Nenhuma hora nova de dev antes do teste de campo do §8 |
