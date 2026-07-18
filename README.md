# ⚖️ Projeto SaaS Jurídico com IA — Documentação Completa

> Plataforma de gestão jurídica com IA nativa, focada em advogados solo e pequenos escritórios no Brasil.
> **Projeto solo:** Ronny, fullstack Angular + Java, 12,5h/semana.
> **Codinome sugerido:** "Advogado.AI" / "JusFlow" / "Peticiona" (a definir — ver decisões em aberto no `00`)

---

## 📁 Estrutura da documentação

**Comece pelo `00`.** Ele é a fonte única de verdade: preços, cronograma, escopo e projeções. Se algum outro documento contradisser o `00`, o `00` vence e o outro está desatualizado.

| Arquivo | Conteúdo |
|---|---|
| `00-premissas.md` | ⭐ **Premissas canônicas**: escopo do MVP solo, cronograma, stack, preços, unit economics, projeção de MRR, gates, decisões em aberto |
| `01-visao-e-posicionamento.md` | Visão do produto, proposta de valor, posicionamento de mercado, estratégia anti-cópia (reescrita para o cenário solo) |
| `02-analise-competitiva.md` | Concorrentes em 3 camadas: ChatGPT genérico, camada de dados/IA brasileira (Jusbrasil et al.), softwares de gestão |
| `03-benchmarks-internacionais.md` | Lições dos sistemas de fora (Clio, Smokeball, Spellbook, Harvey) |
| `04-produto-mvp.md` | Especificação do MVP, roadmap, peticionamento eletrônico, migração de entrada |
| `05-arquitetura-tecnica.md` | Stack, integrações, infraestrutura, segurança |
| `06-estrategia-ia.md` | Casos de uso, RAG, custos reais por modelo, anti-alucinação, prompts |
| `07-go-to-market.md` | Estratégia de captação, canais, copy, calendário da espera até o lançamento |
| `08-modelo-de-negocio.md` | Precificação, unit economics, projeções, análise de sensibilidade |
| `09-riscos-e-compliance.md` | LGPD, OAB, responsabilidade civil, riscos técnicos, de mercado e do cenário solo |
| `10-design-e-ux.md` | Princípios de design, wireframes dos fluxos, design system, UX de confiança na IA |
| `11-modelo-de-dados.md` | Modelo ER, schema, multi-tenancy/RLS, pgvector, retenção LGPD |
| `12-plano-de-execucao.md` | Cronograma mês a mês, caminho crítico, ordem de sacrifício |
| `13-avaliacao-de-ia.md` | Dataset de avaliação, métricas, gates de promoção de prompt, detecção de regressão |
| `14-roteiro-de-entrevistas.md` | 🔴 **Comece por aqui na prática.** Roteiro das 15 entrevistas, ficha de coleta e a **regra de decisão escrita antes de ouvir as respostas** — decide se o projeto continua, encolhe ou morre |

---

## 🎯 Resumo executivo (elevator pitch)

O Brasil tem **+1,3 milhão de advogados** — o maior número per capita do mundo — e **mais de 60% atuam como autônomos ou em pequenas bancas**. Esse público está mal atendido:

- Os softwares baratos (MaisJurídico) **não têm IA real**
- Os softwares com IA profunda (Projuris, Legal One, Harvey) **são caros e voltados para grandes escritórios**
- Os intermediários (Astrea, Advbox) tratam IA como **feature acessória**, não como núcleo
- E o substituto que ele já usa — **ChatGPT** — não lê as intimações dele, não calcula prazo, não conhece os autos

**Nossa tese:** construir a primeira plataforma brasileira onde a IA é o produto — o advogado solo abre o sistema e a IA já leu as intimações do dia, classificou os prazos e rascunhou as peças de resposta. O advogado só revisa e aprova.

**O que nos separa do ChatGPT** (a objeção nº 1 — ver `02`): a IA genérica é um chat que espera ser chamado. A nossa ingere o DJEN sozinha, calcula o prazo em código determinístico, conhece os autos por RAG e entrega o trabalho pronto antes de ser pedida.

**A ressalva honesta:** o projeto é solo, a 12,5h/semana — são **~15 meses até a primeira receita** e a janela competitiva pode fechar antes (gate G5, ver `00` e `09`). A tese só vale se ninguém ocupar o quadrante antes do nosso beta.

## ⚖️ Escopo honesto da Fase 1

Somos **deliberadamente estreitos** no começo — 12,5h/semana de um dev solo não constroem um ERP jurídico:

| Fase 1 (meses 1–14) | Fica para depois |
|---|---|
| Onboarding por OAB + importação (DataJud) | Réplica + recurso e billing automático (meses 16–17) |
| Ingestão DJEN + classificação IA de intimações | Chat com autos e 2º nicho (fase 1.5, meses 17–19) |
| Motor determinístico de prazos + agenda com alertas por **e-mail** | WhatsApp automatizado (Fase 2, 2028) |
| Redator IA com RAG — **1 nicho, 2 tipos de peça** (contestação + petições simples) | Módulo financeiro e régua de cobrança Pix (Fase 2) |
| Editor TipTap (wrapper Angular) + export .docx | Peticionamento eletrônico (spike na Fase 2 — ver `04`) |
| Dashboard "Seu dia" + cobrança por **link de pagamento manual** | Captação/funil de clientes (Fase 2–3) · push notification · dark mode |

O protocolo no PJe continua sendo do advogado na Fase 1 — e a copy tem que dizer isso (ver `04`).

## 👥 Equipe

| Pessoa | Responsabilidade |
|---|---|
| Ronny | **Tudo:** programador fullstack (Angular + Java/Spring Boot), produto, GTM, suporte |

**Capacidade real:** ~12,5h/semana (empregado; projeto solo). Todo o cronograma sai dessa conta — e o **bus factor = 1** é o risco estrutural nº 1 do projeto (ver `09`).

## 🛠️ Stack (decidida — ver `00` §0.1)

Angular + `@angular/ssr` · TipTap com wrapper Angular próprio · Spring Boot + SDK oficial `com.anthropic:anthropic-java` · PostgreSQL/pgvector · Redis · Railway/Render/Fly · Cloudflare R2 · GitHub Actions. *(Next.js, React, Tailwind, shadcn, NestJS e FastAPI estão revogados.)*

## 📅 Marcos

| Quando | O quê |
|---|---|
| Meses 1–14 (ago/2026 – set/2027) | Desenvolvimento da Fase 1 (775h ÷ 12,5h/semana; intervalo honesto 14–18 meses) |
| Mês 12 (jul/2027) | **Ainda em desenvolvimento. MRR R$ 0.** Gate G2: feature-complete do fluxo do aha |
| Meses 13–14 (ago–set/2027) | Beta fechado com **10 advogados** |
| **Mês 15 (out/2027)** | **Lançamento pago** — 50 founding members (30% off por 12 meses + preço congelado); meta: 14 assinantes / R$ 1,5k MRR |
| Mês 18 (jan/2028) | **Gate G4:** ~45 assinantes / R$ 4,7k MRR esperados — **<35 pagantes ou churn >8% ⇒ reavaliação formal** |
| Mês 24 (jul/2028) | Meta: ~125 assinantes / R$ 16k MRR |
| Mês 30 (jan/2029) | Meta: ~270 assinantes / R$ 40k MRR |
| **Contínuo** | **Gate G5:** concorrente lança geração de peças + prazos integrados antes do nosso beta ⇒ reavaliar a tese imediatamente (ver `00` §8 e `09`) |

## 🚦 Próximos passos imediatos

1. **Validar a dor com 15 advogados reais — roteiro pronto em `14`.** É o passo nº 1 em qualquer cenário (projeto de 14 meses, de 5, ou nenhum): ~30h, 4% do plano, e a regra de decisão já está escrita. Não escreva código antes disso.
2. **Confirmar o nicho da Fase 1** — trabalhista é hipótese, não decisão (ver `00`)
3. **Spike do editor (set/2026):** wrapper TipTap próprio × ngx-tiptap × plano C simplificado — maior risco técnico da Fase 1 (ver `00` §9.6 e `09`)
4. **Verificar a situação funcional do Ronny** (restrições do vínculo público a atividade empresarial) com advogado de direito administrativo — **antes de abrir CNPJ** (ver `09`; risco elevado no cenário solo)
5. Definir nome, registrar domínio e checar INPI
6. Prototipar o fluxo core: intimação → classificação IA → rascunho de peça (ver `04` e `10`)
7. Landing page com lista de espera (meta: 150 e-mails até o beta) + calculadora de prazos gratuita como ímã de leads (ver `07`)
