# ⚖️ Peticiona

> Copiloto jurídico para o advogado autônomo brasileiro: a IA lê as intimações, calcula
> os prazos e rascunha as peças — o advogado revisa.
>
> **Projeto solo:** Ronny, fullstack Angular + Java, ~12,5h/semana.
> **Status:** validação de mercado em curso · fatia 1 do código no ar.
> **Nome:** provisório (ver decisões em aberto em [`docs/00-premissas.md`](docs/00-premissas.md)).

---

## 📂 O que tem aqui

```
├── docs/          ← toda a documentação: estratégia, produto, arquitetura, execução
├── backend/       ← Spring Boot 3.3 · Java 21 · motor de prazos
├── frontend/      ← Angular 21 · calculadora pública
├── Dockerfile     ← build multi-stage: front vai empacotado dentro do jar
└── railway.json   ← deploy
```

**Regra:** documentação vive em `docs/`. Só este README fica na raiz.

## 🚦 Por onde começar

| Se você quer… | Vá para |
|---|---|
| Entender a aposta e os números | [`docs/00-premissas.md`](docs/00-premissas.md) — ⭐ fonte única de verdade |
| Saber o que fazer **agora** | [`docs/14-roteiro-de-entrevistas.md`](docs/14-roteiro-de-entrevistas.md) — 🔴 as 15 entrevistas |
| Rodar o código | [`docs/CODIGO.md`](docs/CODIGO.md) |
| Ver o produto desenhado | [`docs/10-design-e-ux.md`](docs/10-design-e-ux.md) |

> ⚠️ Se algum documento contradisser o `00`, o `00` vence e o outro está desatualizado.

---

## 🎯 A tese, em um parágrafo

O Brasil tem +1,3 milhão de advogados e mais de 60% atuam sozinhos ou em bancas
pequenas. Os softwares baratos não têm IA real; os com IA profunda custam caro e miram
grandes escritórios; e o substituto que o advogado já usa — **ChatGPT** — não lê as
intimações dele, não calcula prazo e não conhece os autos. A aposta é ser a plataforma
onde a IA é o produto, não um botão no menu.

**A ressalva honesta:** projeto solo a 12,5h/semana são **~15 meses até a primeira
receita**, e a janela competitiva pode fechar antes (gate G5). A tese só vale se ninguém
ocupar o quadrante antes do nosso beta — por isso as entrevistas vêm antes do código.

## ⚖️ Escopo da Fase 1 (deliberadamente estreito)

| Tem | Fica para depois |
|---|---|
| Onboarding por OAB + importação (DataJud) | Réplica, recurso e billing automático |
| Ingestão DJEN + classificação IA de intimações | Chat com autos e 2º nicho |
| Motor determinístico de prazos + alertas por e-mail | WhatsApp automatizado |
| Redator IA — **1 nicho, 2 tipos de peça** | Módulo financeiro e cobrança Pix |
| Editor TipTap + export .docx | Peticionamento eletrônico · captação |
| Dashboard "Seu dia" + cobrança por link manual | Push notification · dark mode |

O protocolo no PJe continua sendo do advogado — e a copy precisa dizer isso.

## 💻 Código: o que já roda

A **fatia 1** é o motor de prazos determinístico exposto como calculadora pública.
Não é o MVP (775h) — é o núcleo que sobrevive a todos os cenários, não depende de
nenhuma integração externa, e já serve de ímã de leads.

```bash
cd backend && mvn -s settings.xml test          # 19 testes do motor
cd backend && mvn -s settings.xml spring-boot:run
cd frontend && npm install && npm start
```

Detalhes, limites e o que ainda **não** está coberto: [`docs/CODIGO.md`](docs/CODIGO.md).

> ⚠️ **As regras de contagem ainda não foram conferidas por advogado.** Não divulgue a
> calculadora antes disso — errar prazo alheio é o risco ALTÍSSIMO do `docs/09`.

## 📅 Marcos

| Quando | O quê |
|---|---|
| Meses 1–14 (ago/2026 – set/2027) | Desenvolvimento da Fase 1 (775h ÷ 12,5h/semana; janela honesta 14–18 meses) |
| Mês 12 (jul/2027) | **Ainda em dev. MRR R$ 0.** Gate G2: feature-complete do fluxo do aha |
| Meses 13–14 (ago–set/2027) | Beta fechado com 10 advogados |
| **Mês 15 (out/2027)** | **Lançamento pago** — 50 founding members · meta 14 assinantes / R$ 1,5k |
| Mês 18 (jan/2028) | **Gate G4:** <35 pagantes ou churn >8% ⇒ reavaliação formal |
| Mês 24 (jul/2028) | ~125 assinantes / R$ 16k MRR |
| Mês 30 (jan/2029) | ~270 assinantes / R$ 40k MRR |
| **Contínuo** | **Gate G5:** concorrente lança geração de peças + prazos antes do nosso beta ⇒ reavaliar a tese |

## ✅ Próximos passos

1. **15 entrevistas** ([`docs/14`](docs/14-roteiro-de-entrevistas.md)) — inclusive a pergunta que decide tudo: *"você já usa ChatGPT pra redigir peça? por que pagaria por outra coisa?"*
2. **Conferir o motor de prazos com advogado** antes de divulgar a calculadora
3. **Confirmar o nicho** — trabalhista é hipótese, não decisão
4. **Verificar a situação funcional** (vínculo público × atividade empresarial) antes de abrir CNPJ
5. Spike do editor: wrapper TipTap próprio × `ngx-tiptap` — maior risco técnico da Fase 1
6. Definir nome, domínio e INPI

---

## 📚 Índice da documentação

### Estratégia
- [`00-premissas.md`](docs/00-premissas.md) — ⭐ premissas canônicas: escopo, cronograma, stack, preços, MRR, gates
- [`01-visao-e-posicionamento.md`](docs/01-visao-e-posicionamento.md) — visão, proposta de valor, anti-cópia
- [`02-analise-competitiva.md`](docs/02-analise-competitiva.md) — ChatGPT, Jusbrasil e os softwares de gestão
- [`03-benchmarks-internacionais.md`](docs/03-benchmarks-internacionais.md) — Clio, Smokeball, Spellbook, Harvey
- [`07-go-to-market.md`](docs/07-go-to-market.md) — canais, copy, calendário da espera
- [`08-modelo-de-negocio.md`](docs/08-modelo-de-negocio.md) — preços, unit economics, sensibilidade

### Produto e técnica
- [`04-produto-mvp.md`](docs/04-produto-mvp.md) — MVP, roadmap, peticionamento, migração
- [`05-arquitetura-tecnica.md`](docs/05-arquitetura-tecnica.md) — stack, integrações, infra, segurança
- [`06-estrategia-ia.md`](docs/06-estrategia-ia.md) — casos de uso, RAG, custos, anti-alucinação
- [`10-design-e-ux.md`](docs/10-design-e-ux.md) — princípios, wireframes, design system
- [`11-modelo-de-dados.md`](docs/11-modelo-de-dados.md) — ER, schema, RLS, pgvector, LGPD
- [`13-avaliacao-de-ia.md`](docs/13-avaliacao-de-ia.md) — dataset, métricas, gates de prompt

### Execução e risco
- [`09-riscos-e-compliance.md`](docs/09-riscos-e-compliance.md) — LGPD, OAB, responsabilidade civil, riscos do solo
- [`12-plano-de-execucao.md`](docs/12-plano-de-execucao.md) — cronograma mês a mês, caminho crítico, cortes
- [`14-roteiro-de-entrevistas.md`](docs/14-roteiro-de-entrevistas.md) — 🔴 roteiro, ficha e regra de decisão
- [`CODIGO.md`](docs/CODIGO.md) — como rodar, o que o código cobre e o que não cobre
- [`apresentacao/`](docs/apresentacao/) — deck de 7 slides para as entrevistas
