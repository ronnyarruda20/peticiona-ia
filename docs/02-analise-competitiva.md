# 02 — Análise Competitiva: Onde os Atuais NÃO Chegam

> Números de preço/planos citados aqui obedecem ao `00-premissas.md` (nosso lado) e a informação pública dos concorrentes (lado deles). Onde não há certeza, está marcado com ⚠️ — verificar antes de usar em pitch ou copy.

## O mapa real: quatro camadas de concorrência

A concorrência não é uma lista de softwares de gestão. São **quatro camadas**, e a mais perigosa na cabeça do nosso ICP não é nenhum software jurídico:

| Camada | Quem | O que ela é na cabeça do advogado solo |
|---|---|---|
| 1. IA genérica | ChatGPT, Gemini, Claude | "IA que redige já existe e é quase de graça" |
| 2. Dados + IA jurídica | Jusbrasil (Jus IA), Lexter, Turivius, Looplex, Juit | "IA jurídica brasileira já existe" |
| 3. Software de gestão | Astrea, MaisJurídico, Advbox, Projuris, Legal One, **iiLEX**, LegalDash | "Software de escritório já existe" |
| 4. Calculadoras de prazo gratuitas | Prazo Fácil, Prazito, Legalcloud, ProJuris, OAB-PR, Cálculo Jurídico | "Calcular prazo já é de graça na internet" |
| **5. Micro-SaaS de IA jurídica indie** | **ApolloIA**, Estag.ia | **"Já existe um que faz tudo isso por R$ 89,90"** |

Nossa tese só se sustenta se respondermos às cinco. A camada 1 será **a objeção nº 1 nas entrevistas de validação**. A camada 4 foi acrescentada em 19/07/2026 e **não ameaça o produto, mas ameaçava o plano de aquisição** (ver `07-go-to-market.md`). **A camada 5 foi acrescentada em 20/07/2026 e é a única que ameaça a tese inteira — ver o bloco do G5 no `00-premissas.md` §8.**

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

## 🧮 Camada 4: as calculadoras de prazo gratuitas

> ⚠️ **Camada descoberta tarde — em 19/07/2026, DEPOIS de a fatia 1 do código já estar pronta.** Registrada aqui para que ninguém invista mais horas na direção que ela invalida. A busca levou 2 minutos; deveria ter sido feita antes de escrever o motor.

Cálculo de prazo processual isolado **é commodity gratuita no Brasil**. Levantamento de 19/07/2026:

| Ferramenta | Preço | Cobertura | Observação |
|---|---|---|---|
| [Prazo Fácil](https://prazofacil.com.br/) | Grátis, **sem cadastro** | Superiores, TJ, TRF, TRT · CPC/CPP/CLT · **feriados estaduais e municipais** | Ainda traz correção monetária, juros, validadores, QR Code |
| [Prazito](https://prazito.com.br/) | Grátis | — | **613.969 prazos calculados** — base instalada real |
| [Legalcloud](https://app.legalcloud.com.br/) | Grátis | CPC, CPP, JEC, CLT (antes e depois da Reforma) | |
| [ProJuris](https://www.projuris.com.br/blog/melhor-calculadora-de-prazos-processuais/) | Grátis (isca do software pago) | Suspensões municipais | **Anexa portaria oficial para comprovar tempestividade** |
| [Cálculo Jurídico](https://calculojuridico.com.br/prazos-processuais-trabalhista/) | Grátis | Trabalhista | |
| [OAB-PR](https://www.oabpr.org.br/aplicativo-gratuito-que-calcula-prazos-processuais-chega-as-lojas-hoje/) | Grátis | App nas lojas | **É a própria OAB** — autoridade institucional imbatível |
| [Calculador de Dias](https://calculadordedias.com.br/calculadora-de-prazo-legal/) | Grátis | — | |

### O que isso invalida

1. **Calculadora pública como produto de entrada / Fase 0.** Morta. Somos o oitavo entrante numa categoria gratuita.
2. **Canal de aquisição nº 2 do `07` (calculadora + SEO).** Rebaixado — ver o doc 07.
3. **A fatia 1 como ímã de leads.** Ela continua sendo o núcleo técnico certo, mas **não é isca**: várias das sete cobrem MAIS que ela hoje (o `CODIGO.md` lista feriados estaduais e municipais como *não cobertos*; o Prazo Fácil já tem, e a ProJuris ainda anexa a portaria).

### O que isso NÃO invalida (e é a distinção que importa)

> **As sete calculam um prazo que o advogado DIGITA. Nós apostamos que o prazo APARECE sem ele digitar nada — porque a IA leu a intimação que chegou.**

Nenhuma delas ingere DJEN/Comunica, classifica a intimação, deriva a providência ou joga o prazo na agenda. São calculadoras; o Peticiona é copiloto. **O gate G5 do `00` §8 NÃO disparou** — ninguém lançou geração de peças + ingestão + prazos integrados.

O motor determinístico segue no "nunca cortar". O que mudou é o papel: **é componente interno, não é produto nem isca.**

### A consequência estratégica desconfortável

O que é barato de construir neste produto é commodity. O que diferencia — a ingestão do DJEN, 90h — é caro. **Não existe versão barata e diferenciada do Peticiona.** Todo atalho de "lança uma fatia pequena para validar" esbarra nisso: a fatia pequena já existe de graça sete vezes.

Isso não mata a tese. Mata o atalho.

---

## 🔴 Camada 5: os micro-SaaS indie de IA jurídica

> Camada descoberta em **20/07/2026**, a partir de um diretório de micro-SaaS (find-my-saas.com/categories/legal). Registro do erro de método: as camadas 1–4 foram levantadas olhando para **incumbentes e para o topo do mercado**. O concorrente que mais se aproxima da nossa tese não tem marca, não tem imprensa, não tem LinkedIn e não aparece em nenhuma busca por "software jurídico" — **e já está vendendo.**

### ApolloIA — o concorrente frontal (dispara o G5)

- **O que é:** plataforma de IA jurídica self-service para advogados **e estudantes de Direito**. Preços públicos e conferidos no endpoint `apolloia.com.br/api/plans/active` em 20/07/2026
- **Preço:** Start **R$ 89,90** (anual R$ 74,90) · Pro **R$ 139,90** · Expert **R$ 269,90** · Enterprise R$ 269,90/usuário. Trial de 7 dias no Start
- **O que entrega:** geração de peças (petição inicial, **contestação cível e trabalhista**, recursos, apelações, agravos, alegações finais — 12+ tipos, 8 áreas, "37 agentes") · base própria de jurisprudência com 25M+ decisões rastreáveis · **monitoramento de publicações por OAB/termo via "fontes oficiais do CNJ"** com alerta antes do prazo e notificação por e-mail **e WhatsApp** (Pro+, 300–500 processos) · gestão de processos/clientes/financeiro/agenda · export Word e PDF · Pix e Stripe
- **Posicionamento:** anti-alucinação explícito, com comparativo direto contra o ChatGPT no site — *"cada peça nasce de jurisprudência real das fontes oficiais"*. **É o mesmo argumento que planejávamos usar.**
- **Fraquezas verificadas:**
  - ❌ **Não calcula prazo processual.** As "55+ calculadoras" são financeiras (rescisão, juros, atualização monetária). Há alerta e agenda; não há contagem com memória de cálculo
  - ❌ Sem sinal de **RAG sobre os autos do tenant** — o contexto vem do que o usuário conta no chat
  - ⚠️ **NÃO CONFIRMADO** se a peça nasce da intimação monitorada ou se o fluxo é sempre conversacional — é a pergunta que decide o tamanho da nossa lacuna
  - ⚠️ Amplitude suspeita: 37 agentes × 8 áreas é o perfil clássico de profundidade zero. **Não verificado** — exige teste em campo
  - ❌ Sem CNPJ, endereço, fundadores ou páginas institucionais; contato único por WhatsApp (DDD 73). Zero imprensa, zero LinkedIn — **pode ser produto sem clientes**

### Estag.ia — IA jurídica analítica, não redatora

- **Preço:** R$ 69,90 / R$ 149,90 / R$ 299,90 (assinatura + créditos); Escritório a partir de R$ 39,90/advogado. Trial de 7 dias + 50 créditos
- **O que entrega:** parecer jurimétrico, análise de risco de admissibilidade (Súmulas 7, 182, 211), 9 agentes por área, **arena de simulação de sustentação oral**, banco de teses e jurisprudência do STJ, OCR até 1.000 páginas
- **Não redige peças** — confirmado por ausência total dos termos "petição/peça/minuta" no site
- **O disclaimer mais revelador do mercado**, no próprio site: *"Não deve ser utilizado como ferramenta única para controle de prazos fatais ou urgentes"* — admissão pública de que a base do CNJ, sozinha, não é confiável para prazo
- Empresa não identificada; `/sobre`, `/termos` e `/politica-de-privacidade` retornam **404**

### O que a camada 5 ensina sobre a barreira de entrada

Levantamos 10 produtos indie. **Nove são operação de 1–2 pessoas**; cinco têm domínio registrado em nome de pessoa física com Gmail; quatro registraram o domínio nos últimos 8 meses. Nenhum publica CNPJ.

> **A barreira de entrada neste mercado é baixa em execução técnica e alta em confiança institucional.** Não é a tecnologia que nos protege — qualquer pessoa monta um Apollo. O que quase ninguém tem é CNPJ, endereço, termos, DPA e um rosto. Isso é diferencial barato num público que vive de formalidade (ver oportunidade #4 do panorama).

E ensina o inverso também: **se é barato para nós, é barato contra nós.** O G5 não vai disparar uma única vez; vai disparar de novo a cada seis meses.

---

## 🎯 Síntese: as 7 lacunas, revisitadas com honestidade

A versão anterior desta tabela dizia que "ninguém" cobria as 7 lacunas. Com ChatGPT e a camada de dados/IA no mapa, algumas já não estão tão vazias. Versão honesta:

| # | Lacuna original | Status real hoje | O que segue defensável |
|---|---|---|---|
| 1 | Geração de peças com IA | **Já não é lacuna.** Jus IA e Lexter geram peças; ChatGPT também; **ApolloIA gera 12+ tipos a R$ 89,90** | ⚠️ **REESCRITO 20/07/2026.** A versão anterior dizia que "peça gerada a partir da intimação, dentro do fluxo com prazo calculado, ninguém entrega" — **o Apollo entrega 2 dos 3 (peça + ingestão com alerta) e não entrega só o prazo calculado**. A lacuna sobrevivente é estreitíssima: **peça que nasce da intimação com contexto dos autos (RAG por tenant) + prazo determinístico com memória**. Se ela basta para sustentar um produto é exatamente o que o teste de campo do G5 precisa responder |
| 2 | WhatsApp verdadeiramente automatizado | Continua vazia (Astrea é semi-manual) — mas **nós também não entregamos na Fase 1** (Fase 2, meses 9–14) | Lacuna válida para a Fase 2; proibido usá-la em copy de lançamento |
| 3 | Onboarding self-service < 10 min | Astrea importa por OAB mas exige configuração; Jus IA/Lexter são instantâneos porém não montam o escritório | Onboarding que termina com **processos importados, intimações classificadas e prazos na agenda** em < 10 min |
| 4 | Preço público + IA profunda | **Morta.** Jusbrasil, Lexter e **ApolloIA (R$ 89,90 — abaixo do nosso Solo de R$ 119)** têm preço público + IA que redige | ⚠️ **REESCRITO 20/07/2026.** A pergunta "preço público + IA que redige + gestão do fluxo, quem tem as três? Ninguém" **tem resposta agora: o ApolloIA tem, e mais barato que nós.** Preço deixou de ser argumento até de conversão — ver `08` §Precificação, que precisa ser revisto |
| 5 | "Dia pronto" (daily brief inteligente) | Continua vazia — todos mostram dashboards passivos | Intacta. É a materialização do nosso posicionamento |
| 6 | Foco de nicho por área do direito | Continua vazia (Advbox parcial, via configuração; Lexter é generalista) | Intacta — 1 nicho na Fase 1 (hipótese: trabalhista), 4 tipos de peça afiados > 40 genéricos |
| 7 | Régua de cobrança de honorários (Pix + lembretes) | Continua vazia — financeiro dos concorrentes é registro passivo | Lacuna válida, mas é nossa **Fase 2**; não prometer no lançamento |

**Resumo em uma frase:** ⚠️ a frase anterior — *"ninguém liga a IA ao fluxo real do advogado — da intimação ao prazo à peça — a preço de solo"* — **está factualmente errada desde 20/07/2026**. O ApolloIA liga peça + ingestão + alerta a R$ 89,90. O que resta é: **"ninguém liga a IA aos AUTOS e ao prazo CALCULADO"** — e ainda não sabemos se isso, sozinho, vende.

## Estratégia anti-cópia (o que fazer quando eles reagirem)

> ⚰️ **Item 1 desta lista estava REVOGADO desde a reescrita solo do `00-premissas.md` e continuava aqui por descuido.** Corrigido em 20/07/2026. Ver `01-visao-e-posicionamento.md` §Estratégia anti-cópia, que é a versão canônica.

Os concorrentes **vão** adicionar (mais) IA generativa — e os indies já adicionaram. Nossa defesa:

1. ~~**Velocidade** — somos 2 devs sem legado~~ ⚰️ **REVOGADO.** Somos **1 dev a 12,5h/semana**. Em velocidade bruta perdemos para todo mundo, inclusive para um indie solo full-time como o Apollo aparenta ser. Velocidade não é defesa nossa; é vantagem deles
2. **Dados de nicho** — quanto mais peças geradas e corrigidas pelos usuários, melhor nosso RAG/avaliação por área do direito (efeito de rede de dados). **Continua válido, mas só começa a valer meses depois dos primeiros usuários** — não protege o lançamento
3. **Marca de categoria** — ser conhecido como "o software de IA", não "o software que adicionou IA". **Enfraquecido:** chegar em out/2027 numa categoria onde indies vendem desde 2026 é chegar depois, não primeiro
4. **Custo estrutural** — sem equipe de vendas nem consultores, sustentamos margem (~70%) em preços que os incumbentes não sustentam. **Continua válido contra incumbentes; inútil contra indies, que têm a mesma estrutura de custo**
5. **Contra o Jusbrasil** — não dá para vencê-lo em dados nem em distribuição; dá para vencê-lo em **profundidade de fluxo**: o DNA dele é audiência e pesquisa, o nosso é operação
6. **Contra o ChatGPT** — não competimos em modelo (usamos os mesmos por API); competimos em **contexto e fluxo**, que ele estruturalmente não tem
7. **🆕 Contra os indies (camada 5)** — não dá para vencê-los em velocidade nem em preço. As duas defesas reais são **profundidade de nicho** (eles vão largo: 8 áreas, 37 agentes) e **confiança institucional** (CNPJ, endereço, DPA, rosto — nenhum deles tem). Ambas são baratas de construir e nenhuma é copiável rápido por quem escolheu operar no anonimato
