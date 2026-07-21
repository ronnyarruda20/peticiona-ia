# 05 — Arquitetura Técnica

> **Alinhado ao doc `00-premissas.md` (reescrito em jul/2026: projeto SOLO, stack Angular + Spring Boot).** Fase 1 = **meses 1–14** (ago/2026 – set/2027), beta fechado com 10 advogados nos meses 13–14, lançamento pago no mês 15 (out/2027). Fase 1 **não tem WhatsApp nem financeiro** — o diagrama e as integrações abaixo refletem isso. Se algo aqui conflitar com o doc 00, vale o doc 00.

## Princípios

1. **Boring technology — e "boring" é relativo a quem constrói** (ver seção abaixo; esta versão do doc corrige uma hipocrisia da anterior)
2. **Custo variável baixo** — cada real de infra por usuário come a margem do plano de entrada (Solo R$ 119)
3. **IA como serviço externo** — não treinar modelo próprio; usar API da Anthropic via SDK oficial Java, com camada de abstração multi-modelo (doc 06)
4. **LGPD by design** — dados jurídicos são sensíveis; isolamento e criptografia desde o dia 1
5. **Os dados são do advogado** — export completo self-service desde o MVP. Acusamos o Astrea de prender dados (doc 02); temos que cumprir o que cobramos — e é obrigação de LGPD, não favor

## 🥱 Boring technology, versão honesta

A versão anterior deste doc pregava "boring technology" e, no parágrafo seguinte, escolhia Next.js + React + Tailwind + shadcn + NestJS/FastAPI — **tecnologias que o Ronny nunca usou profissionalmente**. Aquela stack fazia sentido para um time de dois com especialista de back-end. Solo, ela seria o oposto de boring: seria aprender 4 ecossistemas novos enquanto se constrói um produto com prazo.

**Boring technology não é "a stack mais popular do Twitter". É a stack que a pessoa que vai construir domina a ponto de o framework desaparecer e sobrar só o problema.** Para o Ronny, fullstack Angular + Java, isso é:

| Critério | Angular + Spring Boot (dele) | Next/React + Nest/FastAPI (da versão anterior) |
|---|---|---|
| Curva de aprendizado | Zero | 3 frameworks + 1 linguagem de back novos |
| Velocidade de debug às 23h de uma terça | Alta — território conhecido | Baixa — cada bug é aula nova |
| Risco de decisão ruim de arquitetura sem revisor (doc 00 §9.2) | Menor — padrões que ele já viu falhar e funcionar | Maior — armadilhas de novato em ecossistema novo |
| Vantagem real do framework para ESTE produto | — | Marginal (SSR, ecossistema de exemplos) |

A 12,5h/semana, sem par para revisar, **familiaridade vale mais do que qualquer vantagem técnica marginal de framework**. O que o produto exige — SSR para a landing, editor rico, API REST, workers, RAG — tudo existe e é maduro no mundo Angular/Java. A única exceção real (bindings do TipTap) tem seção própria abaixo, com o custo na mesa.

## Stack (DECIDIDA — doc 00 §0.1)

### Front-end
- **Angular + TypeScript**, com **`@angular/ssr`** para landing/SEO e app SPA autenticado
- PWA para mobile no MVP (evita app nativo)
- **Sem dark mode no lançamento** (corte #1 do doc 00 §3.2)
- Editor de peças: **TipTap (ProseMirror) com wrapper Angular** — ver seção dedicada abaixo

### Back-end
- **Spring Boot (Java 21+)** — API REST + workers no mesmo deploy no MVP (1 container; separar quando a carga justificar)
- SDK de IA: **`com.anthropic:anthropic-java` (oficial)** — structured outputs tipados a partir de records, streaming, tool use, prompt caching, Batches (detalhes e código no doc 06)
- **PostgreSQL** — dados relacionais + **`pgvector`** para embeddings (RAG sem banco vetorial separado; acesso via JDBC/Spring Data — mais manual que um ORM de vetores, mas é SQL, e SQL é boring no bom sentido)
- **Redis** — cache e fila de jobs (ver seção "Fila de jobs" abaixo)
- Storage: **S3-compatible → Cloudflare R2** (sem custo de egress)

### Infraestrutura
- Início: **Railway / Render / Fly.io** (deploy simples) → migrar para AWS/GCP quando escalar
- **⚠️ A JVM come mais RAM que Node.** Um container Spring Boot confortável precisa de **~512MB–1GB** (contra ~256MB de um serviço Node equivalente), e os PaaS cobram por GB de RAM. Isso **cabe** no orçamento de R$ 300–800/mês do doc 00, mas consome uma fatia maior dele. Mitigações, nesta ordem: (1) API + workers no mesmo container no MVP; (2) ajustar `-Xmx`/`MaxRAMPercentage` em vez de aceitar o default; (3) GraalVM native image só se um dia for preciso — complexidade que o MVP não paga
- CDN + WAF: **Cloudflare** (grátis no início)
- CI/CD: **GitHub Actions**
- Monitoramento: **Sentry** (erros; SDK Java e Angular) + **Posthog** (analytics de produto)

## ⚙️ Fila de jobs no mundo Spring (era BullMQ/Celery)

O que o sistema realmente precisa (levantado da Fase 1, não de wishlist):

1. **Agendamento recorrente** — polling DJEN/Comunica e DataJud, disparo de alertas de prazo (7/2/0 dias), limpeza de exports expirados
2. **Jobs assíncronos com retry e idempotência** — classificação de intimação, geração de peça, import CSV, export LGPD, envio de e-mail
3. **Observabilidade mínima** — saber que um job falhou 3× antes de o advogado perder um prazo por causa disso

No mundo Node/Python isso era BullMQ/Celery. No mundo Spring:

| Opção | O que resolve | Contra |
|---|---|---|
| **`@Scheduled` (Spring nativo)** | Agendamento recorrente (item 1) com uma anotação. Com **ShedLock** se um dia houver >1 instância | Não é fila: sem retry/backoff/estado de job por si só |
| **Redis + Spring Data Redis** (lista/stream como fila) | Fila assíncrona (item 2) — equivalente direto do BullMQ; Redis já está na stack para cache | Retry, dead-letter e dashboard são código nosso (~pouco, mas nosso) |
| **JobRunr** | Fila + retry + agendamento + **dashboard** prontos, persistindo no próprio Postgres (dispensaria Redis como fila) | Dependência nova; ⚠️ conferir o que fica na versão open-source vs. paga antes de adotar |
| Quartz | Agendamento persistente robusto | Só agendamento; API datada; não é fila de jobs |
| Spring Batch | Processamento batch/ETL de grandes volumes | **Formato errado** para o nosso problema — é chunk-processing de dataset, não fila de tarefas. Descartado |

**Decisão:** **`@Scheduled` para o agendamento + Redis como fila de jobs via Spring** — é o equivalente 1:1 do que o plano antigo fazia com BullMQ, mantém a stack canônica do doc 00 (Redis fila/cache) e não adiciona dependência. ⚠️ **No spike de setembro/2026 (mesma janela do spike do editor), avaliar JobRunr como substituto**: se o retry + dashboard prontos valerem a troca, ele simplifica o item 3 e ainda remove o Redis do caminho crítico da fila (fila no Postgres) — nesse caso, atualizar o doc 00. Não é decisão que mereça mais de 1 dia de avaliação: as duas opções funcionam, e a fila não é o risco técnico deste projeto (o editor é).

## ✍️ Editor: TipTap com wrapper Angular (a exceção honesta da stack)

Único ponto onde a stack nova briga com o ecossistema: **as bindings oficiais do TipTap são React e Vue — não há binding oficial Angular.**

**Por que TipTap continua sendo a escolha mesmo assim:**

- O core do TipTap (e o ProseMirror embaixo dele) é **JavaScript puro, agnóstico de framework**. As bindings oficiais são cascas finas; o grosso do valor está no core, que funciona em qualquer lugar
- O produto depende de **nodes customizados**: o marcador `[SUGESTÃO DE TESE — VERIFICAR JURISPRUDÊNCIA]`, o dado interpolado do banco com flag visual de divergência, a marca d'água de rascunho, estados de peça (doc 06, camadas anti-alucinação 1–3). ProseMirror é a engine **mais extensível** do mercado exatamente para isso — schema próprio, NodeViews, decorações, transações interceptáveis
- Snapshot v0 + diff para a métrica de sobrevivência à edição (doc 06/13) precisa de acesso programático ao documento — trivial no ProseMirror

**O custo, na mesa:** o wrapper Angular é trabalho nosso — montar o editor num componente, NodeViews custom renderizando componentes Angular, sincronização com a change detection (ProseMirror muda o DOM por fora do Angular), inserção de texto via streaming durante a geração. O doc 00 §3.1 orça isso em **+25h sobre a estimativa antiga do bloco do editor (80h → 105h)**, e o §9.6 o marca como risco de estouro (estimativa de coisa nunca feita).

**Plano de ataque (decisão #3 do doc 00 §10, prazo set/2026):**

| Passo | O quê |
|---|---|
| Spike de 1–2 dias (set/2026) | Montar TipTap núcleo + 1 NodeView custom dentro de um componente Angular, dos dois jeitos: (a) **`ngx-tiptap`** (lib comunitária) e (b) wrapper próprio fino sobre o core |
| Critério | O que sobreviver melhor a: NodeView com componente Angular dentro, inserção streaming, e controle de estado do documento. ⚠️ Avaliar manutenção/atividade do `ngx-tiptap` antes de depender dele |
| Plano C (doc 00 §9.6) | Se o spike indicar buraco maior que 105h: beta com editor simplificado (contentEditable estruturado, sem IA inline rica) e TipTap completo pós-lançamento. Corta charme, não corta o aha |

**Alternativas com suporte Angular oficial, e por que perdem:**

- **CKEditor 5** e **TinyMCE** têm integrações Angular oficiais e maduras — esse é o único ponto em que ganham
- Mas os nossos requisitos centrais são **extensões profundas de schema** (nodes custom com comportamento e dados), não toolbar rica. Estender o modelo de documento nesses editores é possível, porém mais pesado e menos documentado que no ProseMirror, que foi desenhado para isso
- ⚠️ **Licenciamento:** ambos têm componentes/planos comerciais e mudaram termos de licença nos últimos anos. Não afirmo aqui o que é grátis ou pago em cada um — se o spike do TipTap falhar e essas alternativas entrarem em pauta, **verificar os termos vigentes antes de qualquer linha de código**. Para um SaaS bootstrap, uma licença comercial por developer/feature pode custar mais que as 25h do wrapper

## Integrações críticas (o coração do negócio) — por fase

### Fase 1 (meses 1–14)

#### 1. Dados processuais
| Fonte | O que dá | Custo |
|---|---|---|
| **API Pública DataJud (CNJ)** | Metadados e movimentações de processos de todos os tribunais | Gratuita (com limites) |
| Provedores privados (Judit.io, Escavador, Digesto, Codilo) | Capturas mais completas, push de movimentações, autos | Pago por consulta/monitoramento |
| Comunica CNJ / DJEN | Comunicações processuais eletrônicas centralizadas | Gratuita |

**Estratégia:** começar com DataJud + DJEN (grátis) e complementar com um provedor pago conforme necessidade. **Cotar Judit.io e Escavador cedo — esse é o principal custo variável do negócio junto com a IA** (decisão #5 do doc 00 §10, prazo dez/2026). A cotação também cobre API de jurisprudência para o redator (doc 06, camada anti-alucinação 1).

#### 2. Diários oficiais / publicações
- DJEN (Diário de Justiça Eletrônico Nacional) cobre a maior parte hoje — gratuito
- Fallback: provedor privado de recortes para diários ainda não centralizados

#### 3. Pagamentos (assinatura)
- **Lançamento: link de pagamento manual** (corte #5 do doc 00 §3.2) — gerar cobrança num gateway e mandar o link; operacionalmente chato para nós, invisível para o cliente; sustentável até ~50 assinantes
- Billing automático entra nos meses 16–17. Gateway: **Stripe × Asaas/Pagar.me** (nacionais lidam melhor com Pix e boleto) — decisão #6 do doc 00 §10, prazo set/2027

#### 4. Importação/migração de dados (entrada)
- Worker de import CSV/XLSX com wizard de mapeamento (clientes, contatos, processos) — v0 no fim da Fase 1 (doc 04)
- Pós-lançamento: templates de mapeamento para exports do Astrea e MaisJurídico ⚠️ verificar formatos reais de export de cada um

### Fase 2 (pós-lançamento, meses 16+)

#### 5. WhatsApp — ⚠️ modelo de cobrança da Meta mudou em 2025
- **API oficial WhatsApp Business (Cloud API via Meta)** — direta ou via BSP (Twilio etc.; Z-API/gateways não-oficiais = risco de banimento, evitar)
- **⚠️ CORREÇÃO (mantida):** versões antigas deste doc modelavam custo "por conversa iniciada". A Meta **abandonou a cobrança por conversa em jul/2025** e passou a cobrar **por mensagem de template entregue** (categorias marketing/utilidade/autenticação). Mensagens de serviço (resposta ao cliente na janela de 24h iniciada por ele) são gratuitas, e ⚠️ templates de utilidade dentro de uma janela de atendimento aberta também — verificar regras vigentes na documentação da Meta antes do build.
- **Implicação para o produto:** cada atualização proativa de andamento = 1 template de utilidade **cobrado por mensagem**. O custo escala com o nº de avisos, não de "conversas". As respostas do atendente IA a perguntas do cliente ("como está meu processo?") tendem a cair na janela de serviço = grátis — o desenho do fluxo deve **maximizar janela de serviço e agrupar avisos** (1 mensagem-resumo/dia por cliente, não 1 por movimentação).
- **⚠️ Não fixar preço unitário aqui:** cotar a tabela Brasil vigente (Meta/BSP) na entrada da Fase 2. Ordem de grandeza usada nos planos (R$ 0,04–0,50 por mensagem de utilidade/marketing) e a provisão de ~R$ 5/mês no Solo **precisam ser revalidadas** nessa cotação — as cotas de "conversas" da tabela de planos (100/500/2.000) devem ser renomeadas para **mensagens**.
- Templates pré-aprovados: atualização de processo, lembrete de pagamento, confirmação de reunião

#### 6. Cobrança de honorários (financeiro)
- Asaas/Pagar.me com Pix e split ou conta digital — avaliar take rate sobre transação (doc 08)

#### 7. Peticionamento eletrônico (condicional ao spike — ver doc 04)

O doc 04 concluiu: **fora da Fase 1** (sem horas no orçamento de 775h e sem maturidade de segurança para custodiar certificados antes do primeiro pentest); **spike obrigatório na Fase 2** contra o MNI do PJe da Justiça do Trabalho. Se o spike der go, a arquitetura preliminar é:

```
┌──────────────────────────────────────────────────────────┐
│  Serviço de Protocolo (módulo isolado, deploy separado)   │
│                                                          │
│  ┌─────────────┐   ┌──────────────┐   ┌───────────────┐ │
│  │ Fila de      │──▶│ Cliente SOAP │──▶│ MNI PJe-JT    │ │
│  │ protocolo    │   │ MNI          │   │ (24 TRTs+TST) │ │
│  │ (retry +     │   └──────┬───────┘   └───────────────┘ │
│  │ comprovante) │          │                             │
│  └─────────────┘   ┌──────▼────────┐                     │
│                    │ Cofre de       │                     │
│                    │ certificados A1│                     │
│                    │ (KMS envelope; │                     │
│                    │ HSM ao escalar)│                     │
│                    └───────────────┘                     │
└──────────────────────────────────────────────────────────┘
```

Nota a favor da stack nova: MNI é **SOAP**, e tooling SOAP maduro é um dos poucos lugares onde o ecossistema Java é francamente melhor que Node/Python.

Requisitos não-negociáveis do módulo:
- **Custódia A1:** chave privada cifrada com envelope encryption (KMS), decifrada só em memória no momento da assinatura; termo de autorização expresso do advogado; **log de auditoria imutável de cada uso do certificado**
- **A3 fora do escopo server-side** (exigiria agente local — não fazemos)
- Confirmação explícita do advogado antes de cada protocolo (nunca protocolo "automático silencioso" — o ato é dele)
- Recibo/protocolo do tribunal armazenado e anexado ao processo
- ⚠️ Credenciamento junto aos tribunais para consumo do MNI: verificar processo e prazos por TRT na documentação do CNJ/CSJT

Se o spike der **no-go**, registrar aqui o motivo e implementar o paliativo "protocolo guiado" (export PDF no padrão + checklist + deep link — ~15h, sem integração).

## Arquitetura de alto nível

Caixas com borda `╌╌` = **Fase 2** (não existem no deploy da Fase 1). No MVP, API e workers rodam **no mesmo container Spring Boot** (economia de RAM/custo); a separação em deploys distintos é evolução, não pré-requisito.

```
┌──────────────────────┐     ┌──────────────────────────────────────────────┐
│  Angular             │────▶│  API Spring Boot (Java 21)                   │
│  + @angular/ssr      │     │  Auth │ Processos │ Prazos/Agenda │ Peças    │
│  (landing/SEO + SPA) │     │  ╌╌ Financeiro (F2) ╌╌ WhatsApp (F2) ╌╌      │
└──────────────────────┘     └────────┬──────────────────┬──────────────────┘
                                      │                  │
                             ┌────────▼─────┐    ┌───────▼────────┐
                             │ PostgreSQL   │    │ Redis          │
                             │ + pgvector   │    │ (fila + cache) │
                             └──────────────┘    └───────┬────────┘
                                                         │
                                     ┌───────────────────▼───────────────────┐
                                     │ Workers (mesmo deploy no MVP)         │
                                     │ @Scheduled + consumidores da fila     │
                                     │ • polling/ingestão DataJud/DJEN       │
                                     │ • pipeline de IA (classificação/RAG/  │
                                     │   peças — via anthropic-java)         │
                                     │ • alertas por e-mail                  │
                                     │ • import CSV / export LGPD            │
                                     │ ╌ disparos WhatsApp (F2) ╌            │
                                     │ ╌ protocolo MNI (F2, se go) ╌         │
                                     └───────────────────┬───────────────────┘
                                                         │
         ┌──────────────┬──────────────────┬─────────────┼───────────────┬──────────────────┐
  ┌──────▼──────┐ ┌─────▼────────┐ ┌───────▼──────┐ ┌────▼─────────┐ ╔╌╌▼╌╌╌╌╌╌╌╌╌╌╗ ╔╌╌╌╌╌╌╌╌╌╌╌╌╗
  │ API Claude  │ │ DataJud/DJEN │ │ Cloudflare R2│ │ Link de pgto │ ╎ WhatsApp    ╎ ╎ MNI PJe-JT ╎
  │ (anthropic- │ │ + provedores │ │ (docs/export)│ │ manual (gate-│ ╎ API (Meta/  ╎ ╎ (F2, se    ╎
  │ java, SDK   │ │ pagos        │ │              │ │ way pós-lanç)│ ╎ BSP) FASE 2 ╎ ╎ spike=go)  ╎
  │ oficial)    │ └──────────────┘ └──────────────┘ └──────────────┘ ╚╌╌╌╌╌╌╌╌╌╌╌╌╌╝ ╚╌╌╌╌╌╌╌╌╌╌╌╌╝
  └─────────────┘
```

## 📦 Export e portabilidade de dados (requisito de arquitetura, não feature)

**Por quê:** o doc 02 lista "dados presos, sem API" como fraqueza número 1 do Astrea. Se a nossa saída também for "abra um ticket no suporte", viramos o vilão da nossa própria pitch. Além disso, portabilidade e acesso são **direitos do titular na LGPD (art. 18)** — e nós, como operadores, precisamos permitir que o advogado (controlador) os atenda.

**Especificação (Fase 1, dentro do bloco CRUD/LGPD já orçado):**

- **Export completo self-service:** botão nas configurações → job assíncrono → arquivo `.zip` com:
  - `clientes.csv`, `processos.csv`, `contatos.csv`, `agenda.csv` (reimportáveis em qualquer lugar)
  - `dados-completos.json` (estrutura íntegra, para migração programática)
  - Peças em `.docx` e documentos anexados (arquivos originais)
- Link de download com expiração (ex.: 72h), notificado por e-mail
- **Sem taxa, sem fricção, sem "fale com o suporte"** — mesmo para quem está cancelando (especialmente para quem está cancelando)
- **Exclusão de conta self-service** com purga em prazo definido (ex.: 30 dias) e confirmação — fecha o ciclo LGPD
- Argumento de venda explícito na landing: *"Seus dados saem com você. Export completo em 1 clique — cobre isso do seu software atual."*
- Fase 3: API pública torna a portabilidade contínua (já planejado)

## Multi-tenancy e segurança

- Isolamento por `tenant_id` em todas as tabelas + **Row Level Security do Postgres**, com **teste automatizado de isolamento no CI** — solo e sem revisor de código, esse teste é a rede de segurança contra a classe de bug mais cara do produto (doc 00 §9.2)
- Criptografia em repouso (disco) e em trânsito (TLS); campos ultra-sensíveis com criptografia em nível de aplicação
- Backups diários com retenção 30 dias, testados mensalmente
- Logs de auditoria (quem viu/alterou o quê) — argumento de venda para LGPD
- Nunca usar dados de um tenant para responder a outro; prompts de IA sempre escopados ao tenant

### 🔐 Plano de pentest (recalibrado para o cronograma solo)

| Quando | O quê | Custo estimado |
|---|---|---|
| Contínuo (desde o mês 1) | Dependabot/Renovate, SAST no CI, scan de containers, OWASP ZAP baseline no pipeline | R$ 0 (ferramentas grátis) |
| **Meses 11–12 (antes do beta, até jul/2027)** | Auto-avaliação guiada por OWASP ASVS nível 1 + revisão assistida por IA (checklist: authz por tenant, IDOR, rate limit, secrets). Sem segundo par de olhos humano — o checklist é obrigatório justamente por isso | tempo do Ronny |
| **Meses 13–14 (antes do lançamento pago, out/2027)** | **Pentest externo** (escopo: app web + API, foco em isolamento multi-tenant) — pré-condição para cobrar | ⚠️ cotar — referência de mercado R$ 10–30k para escopo enxuto; avaliar pentester independente credenciado para caber no bootstrap |
| Anual + a cada mudança grande | Retest (obrigatório antes de qualquer módulo de peticionamento/custódia de certificado — pré-condição do go do doc 04) | idem |
| Fase 2+ | Programa de disclosure responsável (página `/security` com e-mail de contato); bug bounty só com escala | R$ 0 |

### 🏅 Resposta ao ISO 27001 do Projuris (objeção de venda real)

O Projuris exibe ISO 27001 (doc 02). Não teremos certificação no lançamento — custa dezenas de milhares de reais e meses de processo, incompatível com bootstrap de R$ 300–800/mês. **O que fazemos enquanto isso** (e como respondemos na venda):

1. **Página de segurança pública** com controles reais e verificáveis: criptografia em repouso/trânsito, RLS por tenant, backups testados, logs de auditoria, DPAs com fornecedores, contrato de não-treinamento com o provedor de LLM (doc 06), export/exclusão self-service
2. **Herança de controles certificados:** nossa infra (Cloudflare, provedores de nuvem) e o provedor de IA (Anthropic) possuem certificações próprias (ISO 27001/SOC 2) — dizemos isso com precisão ("nossa infraestrutura roda em provedores certificados"), **nunca** fingindo que a certificação é nossa
3. **Política de segurança da informação escrita** + autoavaliação anual contra o Anexo A da ISO 27001 (gap analysis documentado) — quando um prospect maior perguntar, mostramos o documento
4. **Resposta de venda pronta:** *"Não temos o selo — temos os controles, publicados e auditáveis, e pentest externo anual. O selo vem quando a receita justificar."* Nosso ICP (solo/banca pequena) raramente exige ISO; a objeção aparece em bancas maiores, que não são o alvo da Fase 1 (doc 01)
5. **Gatilho de certificação:** avaliar ISO 27001 ou SOC 2 quando MRR sustentar (~ano 2–3) ou quando perdermos a 2ª venda por falta do selo — o que vier primeiro

## 🎯 Ordem de ataque e construir × comprar (substitui a antiga "divisão de trabalho")

A antiga tabela de divisão de trabalho entre duas pessoas morreu (doc 00 §0). Solo, não há trilhos paralelos — há **uma fila, ordenada por dependência** (o caminho crítico do doc 12), e uma regra: **tudo que não é diferencial se compra pronto ou se faz do jeito mais burro que funciona.**

### Ordem de ataque (uma pessoa, uma fila)

Horas pós-corte conforme doc 00 §3; datas conforme doc 00 §4.

| # | Bloco | ~h | Por que nesta ordem |
|---|---|---:|---|
| 1 | Fundações: repo, CI/CD, auth, multi-tenancy + RLS com teste de isolamento | ~60 | Tudo depende; RLS não se adiciona depois |
| 2 | Onboarding + importação DataJud por OAB | ~65 | Porta de entrada; primeiro dado real no sistema |
| 3 | Ingestão DJEN/Comunica + fila/workers | ~90 | **Insumo de todo o resto** — classificação, prazos e redator ficam sem matéria-prima sem isso (gate G1, fev/2027) |
| 4 | Classificação IA + motor determinístico de prazos + agenda/alertas e-mail | ~130 | O coração: intimação real → prazo certo em produção |
| 5 | Editor TipTap (wrapper Angular) + export .docx | ~105 | Spike já feito no mês 1–2; maior risco de estouro (doc 00 §9.6) — atacar antes do redator para o streaming ter onde chegar |
| 6 | Redator IA: RAG + prompts das 2 peças + suite de avaliação v1 | ~95 | Fecha o fluxo do aha (gate G2, jul/2027) |
| 7 | CRUD/dashboard "Seu dia" (sem frase-resumo IA) + landing + LGPD + links de pagamento | ~50 | Intercalado nos blocos anteriores conforme necessidade; nada aqui bloqueia o aha |
| — | Beta (meses 13–14): buffer vira o cronograma — bugs, prompts contra a suite, feriados | 180 (buffer) | Doc 00 §3.4 |

Critério de "pronto" (doc 00 §0.2): **demonstrável em produção com dado real + suite de testes passando** — não existe mais "demonstrável pelo outro sócio".

### Construir × comprar (a alavanca de quem não tem horas)

| Necessidade | Decisão | Racional |
|---|---|---|
| E-mail transacional (alertas de prazo) | **Comprar** (SES/Resend/Postmark — cotar) | E-mail entregável é problema resolvido; nunca operar SMTP próprio |
| Monitoramento de erros | **Comprar** (Sentry SaaS, tier grátis) | Self-host de observabilidade é hobby, não produto |
| Billing | **Não construir agora** — link manual de gateway pronto; automação meses 16–17 | Corte #5 do doc 00 |
| Auth | **Spring Security + OAuth Google** (construir fino) | Território dominado; SaaS de auth adiciona custo por usuário e dependência sem tirar trabalho relevante |
| Embeddings para RAG | **Comprar** (API externa — a Anthropic não oferece embeddings; OpenAI/Voyage, ver doc 06) | Centavos por processo indexado |
| Jurisprudência verificada | **Comprar** (DataJud + provedor pago — cotação dez/2026) | Construir base própria é um produto inteiro |
| Feriados forenses | **Construir curadoria própria** ⚠️ | Não há fonte única confiável; é pequeno, crítico e nosso — entra na rotina de manutenção (doc 09) |
| Fila de jobs | **Usar o que a stack dá** (Redis + Spring; ver seção acima) | Fila não é diferencial |
| Infra | **PaaS** (Railway/Render/Fly) | Kubernetes solo a 12,5h/semana é autossabotagem |

**Regra geral:** as ~54h/mês são gastas no que nos diferencia — ingestão confiável, motor de prazos, redator com anti-alucinação, editor. Todo o resto: pronto, gerenciado, ou do jeito simples.
