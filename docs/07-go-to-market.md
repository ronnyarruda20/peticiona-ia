# 07 — Go-to-Market: Captação Solo em 14–18 Meses

> Alinhado ao `00-premissas.md` (reescrito jul/2026 — **projeto solo**). Cronograma canônico: dev ago/2026–set/2027 (**14 meses**, intervalo honesto 14–18), beta fechado com **10 advogados** ago–set/2027, **lançamento pago out/2027 (mês 15)**, janela até jan/2028. A mudança que reorganiza este doc inteiro: **GTM e dev saem das MESMAS 12,5h/semana.** A versão anterior tratava entrevistas, landing e conteúdo como "fora das horas de dev" porque o outro sócio cobria o desenvolvimento. Não cobre mais. Cada hora de conteúdo é uma hora a menos de código — e é o código que destrava a receita.

## 🔢 A restrição que manda em tudo: o orçamento de horas

Capacidade total: 12,5h/semana ≈ 54h/mês. O doc 00 §4 avisa que essas horas precisam ser "QUASE todas de dev" — e que o cenário pessimista de 18 meses existe exatamente porque não serão. Este doc, então, começa pela conta que a versão anterior nunca fez: **quanto o GTM pré-lançamento custa em horas, e quanto isso atrasa o lançamento.**

| Item de GTM pré-lançamento | Conta | Horas |
|---|---|---:|
| 15 entrevistas de validação | recrutar + conduzir + sintetizar ≈ 2h cada | ~30 |
| Landing + captura de e-mail | aproveita o @angular/ssr que a Fase 1 já exige | ~15 |
| "Diário de construção": 2 posts/mês + 1 e-mail/mês à lista | ~2h/mês × 14 meses | ~28 |
| Calculadora de prazos pública | só o wrapper público — o motor é o do produto | ~20 |
| 3–4 artigos SEO de fundação | 2–3h cada | ~10 |
| Recrutar 10 betas + preparar onboarding | seleção na lista + material | ~15 |
| Onboarding assistido + suporte do beta | 10 advogados × ~1,5h + grupo de feedback | ~35 |
| **Total pré-lançamento** | | **~150** |

**150h ÷ 12,5h/semana = 12 semanas ≈ 3 meses de dev.** Essa é, quase exatamente, a distância entre os 14 e os 17–18 meses do intervalo do doc 00. Não é coincidência: o cenário pessimista **é** a fatura do GTM. Com este orçamento (que já é o piso — não dá para cortar entrevistas nem beta), a data realista de lançamento fica entre **out/2027 e jan/2028** — dentro da janela canônica, mas na borda dela. Cortar GTM abaixo deste piso para "proteger out/27" é falso ganho: lançar sem lista e sem betas é lançar para ninguém.

**Regra operacional: GTM em janelas, não em regime.** Concentrado nos meses 1–2 (validação), 8–10 (calculadora + SEO), 11–14 (recrutamento + beta) e 15+ (lançamento). Nos meses de miolo (3–7), o GTM cai para ~2h/mês e o resto é código.

### 🎥 A conta que mata o canal principal antigo

O plano anterior previa **3–5 vídeos verticais/semana** como canal principal. A conta de um vídeo decente (roteiro 30min + gravação 30–60min + edição 60–90min + publicação/respostas 15min): **~2,5–3h por vídeo**.

| Ritmo | Horas/semana | % das 12,5h |
|---|---:|---:|
| 3–5 vídeos/semana (plano antigo) | 7,5–15h | **60–120% da capacidade TOTAL** |
| 1 vídeo/semana | ~2,5h | 20% — sustentado por 14 meses = ~155h = **+3 meses de atraso** |
| 1 vídeo/mês | ~2,5h/mês | ~5% — cabe |

**Veredito: 3–5 vídeos/semana está morto.** Não é "difícil", é aritmeticamente impossível — consome de 60% a 120% de tudo que existe. O trade-off explícito, para decidir caso a caso: **cada vídeo ≈ 2,5h ≈ 0,2 semana de dev ≈ 1,5 dia de atraso no lançamento.** Vídeo antes de existir produto compra pouco (não há nada para demonstrar) e custa caro. Vídeo **com o produto real na tela** (a partir do beta) compra muito e custa menos — o produto faz o show. Portanto: vídeo praticamente zero até o mês 12; 1–2/semana a partir do lançamento, quando as horas de dev liberam (ver rebalanceamento abaixo).

## Fase 0 — Validação (meses 1–2, ago–set/2026)

1. **15 entrevistas com advogados autônomos** (trabalhista/previdenciário — as entrevistas decidem o nicho, `00` §10)
   - Onde achar: conhecidos, indicações, grupos de WhatsApp/Telegram de advogados, Comissões de Jovem Advogado da OAB, LinkedIn
   - Roteiro: "Me conta como foi seu dia ontem" → mapear onde o tempo vai → mostrar protótipo → três perguntas-chave:
     1. "Quanto você pagaria por isso?"
     2. **"Você já usa ChatGPT para redigir peças? Por que pagaria por outra coisa?"** — a **objeção nº 1** do mercado, enfrentada de frente. Nossa tese de resposta: ChatGPT escreve texto genérico; não lê o diário oficial, não calcula prazo, não conhece os autos, inventa jurisprudência e não guarda nada. Nós somos o **fluxo** (intimação → prazo → peça com os dados reais do processo), não o chat. Se as entrevistas não sustentarem essa tese, o posicionamento inteiro precisa ser revisto **antes** de gastar as 775h de dev.
     3. "Posso te avisar quando lançar?"
2. **Landing mínima + captura de e-mail** — no ar, mas sem meta agressiva (ver "Lista de espera", abaixo)
3. Perfil social criado, mas com expectativa honesta: 2 posts/mês de bastidores, não uma operação de conteúdo

## 📧 Lista de espera: 14 meses é tempo demais para manter e-mail quente

A versão anterior mandava juntar 200 e-mails no mês 2 e aquecê-los por 8 meses. Agora seriam **13–14 meses** — e aqui vai a parte desconfortável: **captar lista agressivamente no mês 1 para um produto do mês 15 é provavelmente um erro.** Engajamento de lista decai ~2–3%/mês sem nutrição forte; um e-mail captado em set/2026 e acionado em out/2027 é, na prática, um contato frio com nome de lead quente. Nutrição forte custa horas que não existem.

O desenho novo, em três camadas:

| Camada | Quando | O que | Por quê |
|---|---|---|---|
| Landing passiva | meses 1–2 | Captura das entrevistas + orgânico residual (~15h de custo, âncora para as conversas) | Barata; não é "construção de audiência" |
| **Calculadora de prazos** (o ímã real) | no ar meses 8–9 | Motor de prazos fica pronto pós-G1 (fev/2027); o wrapper público custa ~20h | Lead captado 6 meses antes do lançamento, não 13 — chega morno, não gelado |
| Nutrição mínima | 1 e-mail/mês | "Diário de construção": o que ficou pronto, 1 print, 1 aprendizado (~1h/mês) | Quem se desinscreve não seria cliente; quem fica é o candidato a beta/founding |

- **Meta canônica: ~150 e-mails até o beta** (`00` §9.4 — não 200; solo, 150 é o realista).
- **Conversão assumida no lançamento, honesta: 8 de ~150 ≈ 5%.** Se a lista converter mais, ótimo; a projeção não depende disso.

## Beta fechado (meses 13–14, ago–set/2027)

- **10 advogados** (era 20 — onboarding assistido de 20 consome ~30h/mês e não existe dentro de 12,5h/semana junto com hardening, `00` §2), gratuitos por 60 dias, 1 nicho, **2 tipos de peça**, em troca de: feedback semanal, peças de exemplo para a suite de avaliação (~20–30 pares intimação→peça), depoimento se gostarem
- Critério de seleção: alto volume de processos (dor real), nicho-alvo, ativos em comunidades (viram evangelistas)
- Grupo no WhatsApp com o fundador (grupo comum, não API) — suporte direto cria fãs; com 10 pessoas, cabe na agenda
- Ao final, oferta founding (política canônica, `00` §6): **os 50 primeiros assinantes pagos ganham 30% de desconto por 12 meses + preço de tabela congelado enquanto a assinatura estiver ativa** + badge + canal direto com o fundador. Betas convertidos contam nas 50 vagas.

> ⚠️ A antiga oferta "50% vitalício para os 100 primeiros" continua **extinta**. Não usar em nenhuma copy.

## 📅 O calendário da espera — agora em 14 meses (e com o custo em horas à vista)

| Fase | Meses | Foco de GTM | Horas GTM | Metas |
|---|---|---|---:|---|
| **Validação** | 1–2 (ago–set/26) | 15 entrevistas · landing mínima · nicho decidido | ~45h | Dor confirmada; nicho batido (G0) |
| **Silêncio produtivo** | 3–7 (out/26–fev/27) | Quase zero: 2 posts/mês + 1 e-mail/mês. O resto é código | ~10h | G1 (fev/27): intimação→prazo de pé; lista ~80 |
| **Calculadora + SEO** | 8–10 (mar–mai/27) | Calculadora de prazos pública no ar (reusa o motor) · 3–4 artigos ("prazo de contestação", "feriados forenses 2028") | ~30h | Lista crescendo com lead morno |
| **Recrutamento** | 11–12 (jun–jul/27) | Selecionar 10 betas na lista · material de onboarding · G2: feature-complete | ~15h | Lista ~150; 10 betas compromissados |
| **Beta** | 13–14 (ago–set/27) | Onboarding assistido + suporte + depoimentos e gravações reais ("contestação em 4 minutos") | ~35h | NPS ≥ 40; ≥ 6 dispostos a pagar (G3) |
| **Lançamento** | 15–16 (out–nov/27) | Oferta founding para betas + lista · primeiros vídeos com produto real (1–2/sem) · 1º workshop OAB/ESA agendado | rebalanceado | **14 assinantes / R$ 1,5k** |
| **Tração** | 17–18 (dez/27–jan/28) | Conteúdo com casos reais · programa de indicação no ar · esgotar vagas founding | ~4h/sem | **~45 assinantes / R$ 4,7k → GATE G4** |

**Rebalanceamento pós-lançamento:** com a Fase 1 entregue, as 12,5h/semana viram ≈ 6h produto (bugs, réplica/recurso, billing automático) + 4h GTM + 2,5h suporte e cobrança manual (conta no doc 08). É só a partir daí que existe "operação de conteúdo" — e ela é de 1–2 vídeos/semana, não 3–5.

**Vigilância G5 (permanente, ~30min/mês):** checar lançamentos de Astrea, Advbox e entrantes. Se alguém lançar **geração de peças + ingestão + prazos integrados antes do nosso beta**, o gate G5 dispara e a tese é reavaliada imediatamente (`00` §8). GTM não conserta janela fechada.

## Canais (ordem nova, pela realidade solo)

### 1. 🤝 Programa de indicação (NOSSO canal de aquisição nº 1)
- "Indique um colega: vocês dois ganham 1 mês grátis"
- Advogados andam em bando (fóruns, audiências, grupos de OAB) — boca a boca é o canal natural da categoria, e é o único canal que **não consome horas por lead**. Solo, isso o promove a canal principal.

> **Não confundir com o módulo de captação DO advogado — são duas coisas distintas:**
> **(a)** o programa acima é **nosso funil**: advogado indica colega para assinar o produto;
> **(b)** o **controle de captação/indicação de novos clientes do próprio advogado** (quem indicou cada cliente, funil de leads, página pública, triagem) é **feature de produto** — reclamação literal de um advogado que revisou este material: *"controle de captação/indicação de novos clientes, não tem sobre isso"*. Está no roadmap (doc 04, Fase 3: módulo à la Clio Grow/Lawmatics). Usar como argumento de venda ("está no roadmap, e nenhum concorrente nacional faz bem"), sem prometer data.

### 2. 🔍 Calculadora de prazos + SEO (o canal que trabalha enquanto o fundador codifica)
- Calculadora pública com a marca — ímã de leads permanente que o advogado compartilha com colegas; no ar a partir do mês 8–9
- Artigos-ferramenta: "modelo de contestação trabalhista 2028", "prazo de contestação no novo CPC" — 3–4 na fundação, 1/mês depois
- É o único canal pré-lançamento que escala sem horas recorrentes — por isso sobe para o topo

### 3. 🎥 Conteúdo vertical (Instagram/TikTok/YouTube Shorts) — rebaixado de "principal" para "pós-lançamento"
- Formato: "Gerei uma contestação em 4 minutos com IA — olha isso" (screen recording do produto real)
- Ganchos: medo de perder prazo, horas redigindo a mesma contestação, **"pedi jurisprudência ao ChatGPT e ele inventou o julgado"** (ataca a objeção nº 1)
- Ritmo: **1–2/semana a partir do mês 15** — nunca 3–5 (ver conta acima). Antes do beta, só se sobrar hora, sabendo que cada vídeo ≈ 1,5 dia de atraso
- 1 vídeo viral no nicho = centenas de cadastros — continua verdade; a loteria só pode ser jogada quando o bilhete não custa o cronograma

### 4. 👨‍🏫 Influenciadores jurídicos e professores de prática (pós-lançamento)
- Micro-influenciadores (10k–100k) com cupom/afiliação (20–30% recorrente no 1º ano) — o afiliado produz o conteúdo que nós não temos horas para produzir
- Professores de cursos de prática do nicho: recém-formado = ICP perfeito

### 5. 🏛️ OAB e ESAs (1 workshop/trimestre, pós-lançamento)
- Cada workshop = preparo + deslocamento + entrega ≈ 6–10h — a 12,5h/semana, é quase uma semana inteira. **1 por trimestre**, começando por SC (proximidade) e MT (rede de contatos), não "circuito de palestras"
- Cada sala = 30–80 advogados-alvo; colher e-mails na hora

### 6. 💰 Tráfego pago (só depois do product-market fit)
- Google Ads fundo de funil: "software jurídico com IA", "gerador de petições IA"; Meta Ads com os vídeos que performaram
- Regra inalterada: só com LTV/CAC > 3, financiado pelo próprio MRR (contas no doc 08)

## Motor de crescimento embutido no produto (versão Fase 1)

- **Calculadora de prazos pública com a marca** — ímã de leads e ferramenta que o advogado compartilha
- **Memória de cálculo de prazo exportável em PDF** com marca discreta no rodapé — o advogado manda para colega/cliente, a marca viaja
- **Tela de indicação dentro do produto** ("indique um colega, ganhem 1 mês cada")
- **Badge founding member** — escassez (50 vagas) que os próprios usuários anunciam
- Peças exportadas **sem marca** (jamais melar a peça do cliente) — inegociável
- Fase 2+: rodapé "Enviado via [Produto]" no WhatsApp; Fase 3: página pública do advogado com "powered by"

## Metas de captação (canônicas — `00` §7)

| Mês | Data | Assinantes | Tíquete efetivo | MRR | A conta |
|---|---|---|---|---|---|
| 6 | jan/2027 | 0 | — | **R$ 0** | Em desenvolvimento. Lista ~80 e-mails |
| 12 | jul/2027 | 0 | — | **R$ 0** | **Ainda em desenvolvimento** (G2: feature-complete). Lista ~150 |
| **15 (lançamento)** | out/2027 | 14 | R$ 104 | **R$ 1,5k** | 6 betas convertidos (60% de 10) + 8 da lista (~5% de 150); 14 × R$ 104 ≈ R$ 1.456 |
| **18 (GATE G4)** | jan/2028 | ~45 | ~R$ 105 | **R$ 4,7k** | +12–13 brutos/mês (indicação + conteúdo), churn 5%: 14→25→36→47 |
| 24 | jul/2028 | ~125 | ~R$ 130 | **R$ 16k** | Adds 15–20/mês; founding esgota ~mês 20; mix founding/cheio |
| 30 | jan/2029 | ~270 | ~R$ 148 | **R$ 40k** | Adds 25–30/mês, churn 4%, descontos founding expirando |

- As metas antigas deste doc (lançamento abr/2027; mês 12 = 48 assinantes / R$ 5k; mês 24 = 300 / R$ 45k) estão **revogadas** — assumiam 2 pessoas e 8 meses de dev. Os R$ 45k viram a foto do **mês ~30**.
- **Gate G4 (mês 18, jan/2028): < 35 pagantes ou churn > 8%/mês ⇒ reavaliação formal** (pivotar nicho, preço ou encerrar) — `00` §8. Escrito a frio; não se renegocia a quente.
- **A premissa mais frágil desta tabela** (dita com todas as letras, `00` §9.4): os 12–13 adds/mês do ano 1 dependem de conteúdo + indicação feitos pela MESMA pessoa que desenvolve, dá suporte e cobra manualmente. Se as 4h/semana de GTM pós-lançamento não se sustentarem, os adds caem — e o G4 existe para capturar isso sem autoengano.

## Mensagens que vendem (copy da Fase 1 — só o que o produto entrega no dia 1)

1. **"Nunca mais perca um prazo."** (medo — a mais forte)
2. **"Sua contestação rascunhada em minutos, não em 3 horas."** (tempo)
3. **"Abra o sistema e o dia já está pronto: intimações lidas, prazos na agenda, peça rascunhada."** (paz — o "dia pronto" que nenhum concorrente entrega)
4. **"O estagiário que não dorme: R$ 3,97 por dia."** (ancoragem: R$ 119/mês ÷ 30 dias = R$ 3,97)

**A ancoragem nº 4, verificada:** estagiário não é CLT (Lei 11.788) — a comparação honesta é com a bolsa: R$ 800–1.500/mês + auxílio-transporte ≈ R$ 40–75 por dia útil. Auxiliar CLT de salário mínimo passa de R$ 2 mil/mês com encargos (> R$ 90/dia útil). Nos dois casos: somos ~1/10 do estagiário mais barato. Variante para a objeção nº 1: *"ChatGPT escreve texto. A gente lê a intimação, calcula o prazo e rascunha a peça com os dados do SEU processo — sem julgado inventado."*

> 🚫 **Copy proibida na Fase 1:**
> - "Seu cliente atualizado no WhatsApp sem você digitar nada" — WhatsApp é Fase 2; guardar para campanha de upsell quando lançar.
> - Qualquer menção a **"4 tipos de peça"** ou a réplica/recurso inominado como coisa existente. O lançamento tem **2 tipos: contestação + petições simples** (`00` §2). Réplica e recurso entram nos meses 16–17 — são roadmap, "em breve", nunca promessa com data na copy de venda.
