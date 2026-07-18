# 02 — Análise Competitiva: Onde os Atuais NÃO Chegam

> Números de preço/planos citados aqui obedecem ao `00-premissas.md` (nosso lado) e a informação pública dos concorrentes (lado deles). Onde não há certeza, está marcado com ⚠️ — verificar antes de usar em pitch ou copy.

## O mapa real: três camadas de concorrência

A concorrência não é uma lista de softwares de gestão. São **três camadas**, e a mais perigosa na cabeça do nosso ICP não é nenhum software jurídico:

| Camada | Quem | O que ela é na cabeça do advogado solo |
|---|---|---|
| 1. IA genérica | ChatGPT, Gemini, Claude | "IA que redige já existe e é quase de graça" |
| 2. Dados + IA jurídica | Jusbrasil (Jus IA), Lexter, Turivius, Looplex, Juit | "IA jurídica brasileira já existe" |
| 3. Software de gestão | Astrea, MaisJurídico, Advbox, Projuris, Legal One | "Software de escritório já existe" |

Nossa tese só se sustenta se respondermos às três — em especial a primeira, que será **a objeção nº 1 nas entrevistas de validação**.

---

## 🥊 Camada 1: o concorrente invisível — ChatGPT (e Gemini)

Hoje o advogado solo **já** cola a intimação no ChatGPT e pede a contestação. De graça, ou por ~R$ 100–120/mês (⚠️ preço do Plus varia com câmbio). Esse é o benchmark de "IA que redige" na cabeça dele — não o Harvey, não o Astrea. Se o nosso pitch não sobreviver à pergunta *"por que não continuo no ChatGPT?"*, não temos produto.

### Onde o ChatGPT ganha (admitir sem rodeio)

- ✅ **Custo:** grátis ou um só plano que "faz tudo" — e-mail, contrato, peça de qualquer área, texto do Instagram
- ✅ **Flexibilidade total:** sem cota de peças, sem nicho, sem limite de formato
- ✅ **Modelo de ponta sempre atualizado**, sem esperar roadmap de ninguém
- ✅ **Zero adoção:** ele já usa, já confia no jeito dele, já tem o hábito

### Onde o ChatGPT perde (o confronto, ponto a ponto)

| Momento do fluxo | ChatGPT | Nós |
|---|---|---|
| A intimação existe | **Não sabe.** O advogado precisa achar a publicação sozinho no diário | Ingestão automática do DJEN/Comunica CNJ: a intimação chega classificada, com urgência e providência |
| Contar o prazo | LLM contando data = roleta. Dias úteis? Feriado forense local? Ele confere na mão | **Motor determinístico em código** (dias úteis, feriados forenses), com memória de cálculo exibida — LLM nunca calcula data (doc 06) |
| Contexto do caso | Só sabe o que foi colado. A cada prompt, começa do zero | O sistema **já tem os autos**: partes, histórico, peças anteriores do caso e do acervo do advogado via RAG (pgvector) |
| Jurisprudência | Alucina julgado com número de processo convincente | Regra rígida: citação só de base real ou marcada "[SUGESTÃO DE TESE — VERIFICAR]" |
| Sigilo e LGPD | Conta pessoal, dados do cliente colados num chat genérico; ⚠️ uso para treinamento depende de configuração que quase ninguém ajusta | Isolamento por tenant, logs de auditoria, DPA, contrato de não-treinamento — argumento de venda documentado (docs 05, 06, 09) |
| Depois do texto | Copiar, colar no Word, reformatar, salvar em pasta, lembrar do prazo | Peça nasce **ligada ao processo e ao prazo na agenda**, editor com IA inline, exporta .docx formatado |

### A resposta de 30 segundos (para entrevistas e vendas)

> "O ChatGPT escreve bem — mas ele não sabe que a intimação chegou, não conhece os autos e não sabe contar prazo. Você acha a publicação, cola, confere os dados, conta o prazo na mão e reza. A gente lê o diário por você, calcula o prazo em código — não em IA — e rascunha a peça já com os dados do processo. O ChatGPT é um estagiário brilhante com amnésia. Nós somos o fluxo do seu escritório."

**Leitura honesta:** para o advogado com 5 processos e tempo sobrando, o ChatGPT basta — ele não é nosso ICP. Nossa aposta é que quem tem 30–150 processos sofre a dor do **fluxo** (achar, classificar, contar, não esquecer), não a dor da **redação**. Se as entrevistas mostrarem o contrário, o gate do mês 12 existe para isso.

---

## 🗄️ Camada 2: dados + IA jurídica que JÁ existem no Brasil

Fingir que essa camada não existe destruiria a credibilidade deste documento. Mapa honesto:

### Jusbrasil (Jus IA) — a maior ameaça estrutural

- **O que é:** maior detentor de dados jurídicos do país (acervo anunciado de 90+ milhões de decisões de 96 tribunais ⚠️ número do marketing deles) + distribuição gigantesca via SEO — é onde o Brasil inteiro cai ao googlar qualquer termo jurídico
- **Produto de IA:** **Jus IA** — assistente generativo sobre o acervo próprio; faz pesquisa, resume, **gera peças** e permite anexar documentos; checagem de referências contra a base real (ataca diretamente o argumento anti-alucinação). Desde abr/2026 integrado aos planos da base Jusbrasil
- **Preço:** planos com preço público, na casa de dezenas de reais/mês (anuais a partir de ~R$ 58,90/mês; promoções de entrada a R$ 1,90–9,90 no 1º mês) — ⚠️ verificar tabela vigente antes de citar em comparativo
- **Forças:** dados + marca + distribuição + preço baixo. Se decidir descer para o fluxo operacional do escritório, vira concorrente frontal
- **Fraquezas (lacunas exploráveis):**
  - ❌ É assistente + pesquisa, não sistema de trabalho: sem motor determinístico de prazos, sem agenda com alertas, sem "dia pronto" ⚠️ (têm acompanhamento processual; verificar profundidade atual)
  - ❌ A peça nasce de prompt/anexo manual, não da intimação com contexto automático dos autos
  - ❌ DNA de portal de conteúdo/audiência, não de SaaS operacional — mudar isso exige mudar a empresa

### Lexter

- **O que é:** startup brasileira de geração de peças e contratos com IA; captou ~R$ 16 milhões (⚠️ rodada de 2023; conferir status atual)
- **Preço:** plano gratuito + pagos por volume de peças, na faixa de ~R$ 99/mês (10 peças) a ~R$ 299/mês (⚠️ verificar tabela atual)
- **Forças:** foco claro em redação IA, preço público, self-service — é o player mais parecido com o nosso Redator isolado
- **Fraquezas:** ❌ redige a partir do que o usuário fornece — sem ingestão de intimações, sem prazos, sem gestão de processos ⚠️; é "o ChatGPT jurídico", não o fluxo

### Turivius

- **O que é:** pesquisa jurisprudencial + jurimetria + IA generativa (GPTuri)
- **Preço:** sob consulta (venda consultiva) ⚠️
- **Público:** escritórios estruturados e departamentos jurídicos (força histórica em tributário ⚠️) — não briga pelo solo

### Looplex

- **O que é:** automação de documentos com lógica jurídica (templates programáveis), CLM e jurimetria
- **Preço:** sob consulta ⚠️
- **Fraquezas para o nosso mercado:** ❌ exige modelagem/configuração dos templates — poderoso para operação estruturada, pesado demais para o solo

### Juit (JUIT Rimor)

- **O que é:** busca de jurisprudência + jurimetria por assinatura
- **Fraquezas para o nosso mercado:** ❌ pesquisa, não redação nem gestão ⚠️
- ⚠️ **Não confundir com Judit.io** — API de dados processuais que é candidata a *fornecedor* nosso (doc 05), outra empresa

**Síntese da camada 2:** já existe IA jurídica brasileira que **redige** (Jus IA, Lexter) e que **pesquisa/prevê** (Turivius, Juit, Looplex). O que nenhuma delas é: **o sistema onde o trabalho do advogado acontece** — onde a intimação chega sozinha, o prazo entra na agenda e a peça nasce com o contexto dos autos.

---

## 🗂️ Camada 3: softwares de gestão

### MaisJurídico
- **Preço:** R$ 57 a R$ 499/mês (planos públicos), plano grátis por 1 ano
- **Público:** advogado individual e pequenos escritórios
- **Forças:** 20+ anos de mercado, +22 mil advogados cadastrados, preço baixo, diário oficial incluso
- **Fraquezas (lacunas exploráveis):**
  - ❌ Zero IA — nem geração de peças, nem resumos, nem classificação
  - ❌ Interface datada (PHP clássico, visual anos 2010)
  - ❌ Sem automação de WhatsApp
  - ❌ Alertas apenas por e-mail e SMS

### Astrea (Aurum)
- **Preço:** plano Light grátis 1 ano; Starter ~R$ 146/mês; Pro ~R$ 327/mês
- **Público:** solo a equipes de até ~30 usuários
- **Forças:** UX elogiada, suporte forte, robôs de captura de andamento por OAB/CNJ, app mobile, envio de informações por WhatsApp Web
- **Fraquezas (lacunas exploráveis):**
  - ❌ **Sem API pública** — dados presos, sem integrações externas
  - ❌ IA limitada a monitoramento/classificação; não gera peças ⚠️ (monitorar: é o incumbente mais provável de lançar redação IA)
  - ❌ WhatsApp é semi-manual (via WhatsApp Web), não automatizado
  - ❌ Preço sobe rápido conforme cresce

### Advbox
- **Preço:** de ~R$ 89 (entrada) a R$ 1.600/mês (Elite); planos por porte
- **Público:** escritórios em crescimento com foco em produtividade/gamificação
- **Forças:** API aberta, assistente de IA que classifica intimações, metodologia de pontuação de tarefas (meritocracia), CRM integrado
- **Fraquezas (lacunas exploráveis):**
  - ❌ Complexo demais para o solo — feito para gerir EQUIPE
  - ❌ Curva de aprendizado alta (configuração inicial pesada)
  - ❌ Caro nos planos com IA de verdade
  - ❌ IA classifica mas não redige

### Projuris ADV (Softplan)
- **Preço:** sob consulta (não público) — sinal de tíquete alto
- **Público:** escritórios médios a grandes, departamentos jurídicos
- **Forças:** robustez, credibilidade Softplan, IA de jurimetria (probabilidade de êxito), ISO 27001, +5 mil escritórios
- **Fraquezas (lacunas exploráveis):**
  - ❌ Venda consultiva (demora, fricção) — inviável para solo decidir no impulso
  - ❌ Implantação assistida = onboarding lento
  - ❌ Excesso de funcionalidades que o pequeno nunca usa (paga por elas mesmo assim)

### Thomson Reuters Legal One
- **Preço:** premium, sob consulta
- **Público:** grandes escritórios e departamentos jurídicos corporativos
- **Forças:** marca global, Azure + SOC1, IA generativa para análise de casos e prazos, integração com pesquisa jurídica da Thomson Reuters
- **Fraquezas (lacunas exploráveis):**
  - ❌ Totalmente fora do alcance financeiro do solo
  - ❌ Complexidade corporativa

---

## 🎯 Síntese: as 7 lacunas, revisitadas com honestidade

A versão anterior desta tabela dizia que "ninguém" cobria as 7 lacunas. Com ChatGPT e a camada de dados/IA no mapa, algumas já não estão tão vazias. Versão honesta:

| # | Lacuna original | Status real hoje | O que segue defensável |
|---|---|---|---|
| 1 | Geração de peças com IA | **Já não é lacuna.** Jus IA e Lexter geram peças; ChatGPT também | A lacuna real é mais estreita: peça gerada **a partir da intimação, com contexto automático dos autos**, dentro do fluxo com prazo calculado. Isso ninguém entrega |
| 2 | WhatsApp verdadeiramente automatizado | Continua vazia (Astrea é semi-manual) — mas **nós também não entregamos na Fase 1** (Fase 2, meses 9–14) | Lacuna válida para a Fase 2; proibido usá-la em copy de lançamento |
| 3 | Onboarding self-service < 10 min | Astrea importa por OAB mas exige configuração; Jus IA/Lexter são instantâneos porém não montam o escritório | Onboarding que termina com **processos importados, intimações classificadas e prazos na agenda** em < 10 min |
| 4 | Preço público + IA profunda | **Enfraquecida:** Jusbrasil e Lexter têm preço público + IA que redige | Reformulada: preço público + IA que redige + **gestão do fluxo (prazos, processos, agenda)** num produto só. Quem tem as três? Ninguém |
| 5 | "Dia pronto" (daily brief inteligente) | Continua vazia — todos mostram dashboards passivos | Intacta. É a materialização do nosso posicionamento |
| 6 | Foco de nicho por área do direito | Continua vazia (Advbox parcial, via configuração; Lexter é generalista) | Intacta — 1 nicho na Fase 1 (hipótese: trabalhista), 4 tipos de peça afiados > 40 genéricos |
| 7 | Régua de cobrança de honorários (Pix + lembretes) | Continua vazia — financeiro dos concorrentes é registro passivo | Lacuna válida, mas é nossa **Fase 2**; não prometer no lançamento |

**Resumo em uma frase:** a defesa não é "ninguém tem IA" (mentira em 2026); é **"ninguém liga a IA ao fluxo real do advogado — da intimação ao prazo à peça — a preço de solo"**.

## Estratégia anti-cópia (o que fazer quando eles reagirem)

Os concorrentes **vão** adicionar (mais) IA generativa. Nossa defesa:

1. **Velocidade** — somos 2 devs sem legado; os incumbentes de gestão têm bases de código de 10–20 anos
2. **Dados de nicho** — quanto mais peças geradas e corrigidas pelos usuários, melhor nosso RAG/avaliação por área do direito (efeito de rede de dados)
3. **Marca de categoria** — ser conhecido como "o software de IA", não "o software que adicionou IA"
4. **Custo estrutural** — sem equipe de vendas nem consultores de implantação, sustentamos margem (~70% esperada) em preços que os incumbentes não sustentam
5. **Contra o Jusbrasil especificamente** — não dá para vencê-lo em dados nem em distribuição; dá para vencê-lo em **profundidade de fluxo**: o DNA dele é audiência e pesquisa, o nosso é operação. Se ele descer para gestão operacional, competimos em foco de nicho e velocidade — e monitoramos esse movimento trimestralmente
6. **Contra o ChatGPT** — não competimos em modelo (usamos os mesmos por API); competimos em **contexto e fluxo**, que ele estruturalmente não tem
