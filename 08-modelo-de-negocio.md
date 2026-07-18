# 08 — Modelo de Negócio (Solo)

> Alinhado ao `00-premissas.md` (reescrito jul/2026 — **projeto solo**, lançamento no mês 15). Regra deste doc: **todo número mostra a conta**. Se um número aparecer sem conta, ele está errado por definição.

## Precificação (canônica)

| | **Solo** | **Escritório** | **Pro** |
|---|---|---|---|
| Preço/mês | **R$ 119** | **R$ 229** | **R$ 449** |
| Anual (2 meses grátis) | R$ 1.190 | R$ 2.290 | R$ 4.490 |
| Usuários | 1 | 3 | 8 |
| Processos monitorados | 100 | 400 | ilimitado (fair use) |
| Peças IA/mês | 12 | 40 | 120 (fair use) |
| Chat com autos (perguntas/mês)* | 50 | 200 | ilimitado (fair use) |
| Aprendizado de estilo (Fase 2) | — | ✅ | ✅ |
| WhatsApp automático (Fase 2) | 100 conversas | 500 | 2.000 |
| Régua de cobrança Pix (Fase 2) | — | ✅ | ✅ |
| API (Fase 3) | — | — | ✅ |

\* Chat com autos entra na "fase 1.5" (**meses 17–19, dez/2027–fev/2028** — `00` §4). Itens de Fase 2 entram **sem mudança de preço** — o preço já os embute; até lá, são roadmap anunciado, não promessa de lançamento. **No dia do lançamento, o produto tem 2 tipos de peça (contestação + petições simples) e não tem chat** — a cota de peças vale desde o dia 1; réplica e recurso inominado chegam nos meses 16–17.

**Racional:**
- R$ 119 fica ABAIXO do Starter do Astrea (R$ 146) e dos planos com IA do Advbox — entrada agressiva com IA que eles não têm. (O antigo R$ 97 morreu na auditoria: não fechava a conta de custo de IA — `00` §5.)
- Limites por **peças de IA** (nosso custo variável), não por funcionalidades capadas — o plano de entrada resolve a dor inteira (lição anti-MyCase, doc 03)
- Teste grátis de 14 dias com cartão OU 7 dias sem cartão (testar as duas variantes no beta — decisão em aberto, `00` §10)

## Unit economics por plano — a conta aberta

Premissas de custo (canônicas, `00` §5.2 — preços de API reconferidos em jul/2026, câmbio R$ 5,50): peça R$ 0,30–1,50 (média **R$ 0,80** com cache), classificação **~R$ 0,02** (~R$ 0,01 com cache+batch), chat **R$ 0,15**/pergunta. Uso médio esperado = **~65% da cota**.

**Custo de IA esperado, linha a linha (Solo):**

| Item | Conta | Valor/mês |
|---|---|---|
| Peças | 12 × 65% ≈ 8 peças × R$ 0,80 | R$ 6,40 |
| Chat com autos (fase 1.5) | 50 × 65% ≈ 33 perguntas × R$ 0,15 | R$ 4,95 |
| Classificações + resumos leigos | ~250 operações × ~R$ 0,018 (100 processos monitorados) | R$ 4,50 |
| Embeddings, OCR, retries | overhead estimado | R$ 2,00 |
| **Total esperado** | | **≈ R$ 18** |

Mesma conta para os outros planos: **Escritório** ≈ 26 peças × 0,80 + 130 chats × 0,15 + volume 4× de classificação ≈ **R$ 48**; **Pro** ≈ 78 peças × 0,80 + ~250 chats × 0,15 + classificação ≈ **R$ 110**.

**Teto (cota cheia no pior preço por operação):** Solo = 12 × 1,50 + 50 × 0,30 + ~5 = **R$ 38** · Escritório = 40 × 1,50 + 200 × 0,30 + ~12 = **R$ 132** · Pro (fair use acionado) ≈ **R$ 290**.

| | Solo | Escritório | Pro |
|---|---|---|---|
| Receita | R$ 119 | R$ 229 | R$ 449 |
| IA — esperado | R$ 18 | R$ 48 | R$ 110 |
| IA — teto | R$ 38 | R$ 132 | ~R$ 290 |
| Dados processuais (DataJud grátis + provedor) | R$ 10 | R$ 20 | R$ 40 |
| Infra rateada | R$ 3 | R$ 5 | R$ 10 |
| **Margem bruta esperada** | **R$ 88 (74%)** | **R$ 156 (68%)** | **R$ 289 (64%)** |
| Margem no pior caso | R$ 68 (57%) | R$ 72 (31%) | ~R$ 109 (24%) |

**Número canônico: margem bruta esperada blended ≈ 70%** (mix 80/17/3: 0,80×74% + 0,17×68% + 0,03×64% ≈ 73% sobre IA esperada; com folga para variação de uso, ~70%). **Piso ~55%** no Solo intenso. Observação a favor (`00` §5.3): no lançamento o produto tem só 2 tipos de peça e ainda não tem chat — o custo real por usuário nos primeiros meses tende a ficar ABAIXO da tabela, e a margem, acima. **A margem não é o problema deste projeto; o cronograma é.**

### 🔎 O pior caso do Solo, sem anestesia

Usuário que **estoura a cota todo mês** (12 peças no preço máximo + 50 chats caros): 119 − 38 − 10 − 3 = **R$ 68 de margem (57%)**. Feio para SaaS, mas não fatal — e é o **piso**, não a média, porque:

1. **A cota é dura**: a 13ª peça não existe no Solo — excedente vira conversa de upgrade, não custo nosso. Quem estoura todo mês é o candidato natural ao Escritório (R$ 229, margem esperada R$ 156).
2. **Cache de prompts**: templates, instruções de sistema e contexto do nicho se repetem — leitura cacheada custa ~0,1× do input; o SDK Java oficial suporta (`00` §5.1). Primeira otimização do backlog pós-lançamento.
3. **Modelo barato nas tarefas simples**: classificação e resumo rodam em Haiku (R$ 0,01–0,02/operação), nunca no modelo top; classificação noturna vai para a Batches API (−50%).
4. **Fair use + alertas de consumo**: chat limitado a 50/mês no Solo; consumo anômalo dispara alerta antes de virar prejuízo.

O pior caso do Escritório (31%) e do Pro (24%) é mais feio, mas exige cota cheia **no pior preço em todas as operações** — raro, limitado por cota/fair use e mitigado pelos mesmos 4 itens.

## Projeção 30 meses (canônica — `00` §7)

**Premissas, todas na mesa:**
- Mix ano 1: 80% Solo / 17% Escritório / 3% Pro → tíquete cheio = 0,80×119 + 0,17×229 + 0,03×449 = **R$ 148**
- Mix regime (ano 2+): 75/20/5 → 0,75×119 + 0,20×229 + 0,05×449 = **R$ 157**
- Founding (50 primeiros): 30% off por 12 meses → tíquete efetivo = 148 × 0,70 = **R$ 104**
- Churn: **5%/mês no ano 1; meta 4%/mês no ano 2**
- **Lançamento no mês 15 (out/2027)** — beta de 10 advogados nos meses 13–14; mês 12 ainda é dev puro

| Marco | Data | Assinantes | MRR | A conta |
|---|---|---|---|---|
| Mês 6 | jan/2027 | 0 | **R$ 0** | Em dev; lista ~80 e-mails |
| Mês 12 | jul/2027 | 0 | **R$ 0** | **Ainda em dev** (G2: feature-complete); lista ~150 |
| **Lançamento (mês 15)** | out/2027 | 14 | **R$ 1,5k** | 6 betas convertidos (60% de 10) + 8 da lista (~5% de 150); 14 × R$ 104 = R$ 1.456 |
| **Mês 18 — GATE G4** | jan/2028 | ~45 | **R$ 4,7k** | +12–13 brutos/mês, churn 5%: 14→25→36→47 (~45 conservador); 45 × ~R$ 105 |
| Mês 24 | jul/2028 | ~125 | **R$ 16k** | Adds 15–20/mês; founding (50) esgota ~mês 20; tíquete médio ~R$ 130 (mix founding R$ 104 + cheio R$ 148); 125 × 130 = R$ 16,3k |
| Mês 30 | jan/2029 | ~270 | **R$ 40k** | Adds 25–30/mês, churn 4%; descontos founding expirando → tíquete ~R$ 148; 270 × 148 ≈ R$ 40k |

- **Break-even operacional:** fixos ~R$ 800/mês (solo, tendem a R$ 300–600 até o beta). Conta: N × R$ 104 × 0,70 (líquido de ~30% de custo variável) ≥ 800 → **N ≥ 11 assinantes**. Acontece **no próprio lançamento** (14 assinantes) — mas com folga de só 3 assinantes. Um mês ruim de churn zera a folga. Break-even no dia 1 é bom; break-even **sem margem de erro** é o que ele realmente é.
- **Sanidade:** os antigos "300 assinantes / R$ 45k no mês 24" desta página viram a foto do **mês ~30 (jan/2029)**. Era o que dava para prometer com 25h/semana de time — não com 12,5h de uma pessoa. Cada corte de capacidade empurra a curva inteira; não existe mágica de funil que compense 6 meses a menos de produto no ar (`00` §7).
- **Remuneração do fundador:** retirada relevante só na faixa do **mês 24–30**, não antes. Até lá, todo o MRR paga custo e reinveste.
- **A premissa mais frágil** (registrada também no 07): os adds do ano 1 dependem de conteúdo + indicação feitos pela mesma pessoa que desenvolve, dá suporte e cobra manualmente.

**Mercado endereçável:** ~1,3M advogados ativos (OAB), ~60% autônomos/pequenas bancas ≈ **780 mil**. Meta do mês 24 (125 assinantes) = 125 ÷ 780.000 = **0,016% de penetração**; mês 30 (270) = 0,035%. No recorte honesto da Fase 1 — 1 nicho, ~10% dos autônomos ≈ 80 mil — seriam 0,16% e 0,34%. A projeção não depende de "ganhar o mercado"; depende de UMA pessoa sustentar conteúdo + indicação no nicho.

## 💸 Billing manual: o custo operacional do corte #5

O lançamento sai **sem billing automático** (`00` §2): cada assinante-mês = gerar link de pagamento, enviar, conferir se pagou, conciliar na planilha — e uma fração precisa de segunda cobrança (esqueceu, Pix errado, cartão recusado).

**A conta:** caminho feliz ~5 min/assinante·mês; inadimplente ~+30 min; com ~20% de atraso → média ≈ 5 + 0,20×30 = **~11–12 min por assinante-mês ≈ 5 assinantes por hora**.

| Assinantes | Horas/mês de cobrança | % da capacidade (54h/mês) | Leitura |
|---|---:|---:|---|
| 14 (lançamento) | ~3h | 5% | OK — cabe |
| ~27 | ~5,5h | 10% | Limite confortável |
| **45 (mês 18)** | **~9h** | **17%** | 2h/semana só cobrando — já dói |
| ~50 | ~10h | 19% | **Quebra: quase uma semana inteira de capacidade por mês** |
| 125 (mês 24) | ~25h | 46% | Impossível — nem discutir |

**Conclusão: o billing manual quebra entre 40 e 50 assinantes — exatamente a base esperada para jan/2028.** O cronograma canônico já prevê billing automático nos meses 16–17 (nov–dez/2027, `00` §4); esta conta mostra que não é nice-to-have: se deslizar para depois do mês 18, a cobrança come as horas de GTM que sustentam os próprios adds. Mitigações até lá:
- **Empurrar o plano anual** (2 meses grátis já é o incentivo): 1 cobrança/ano em vez de 12 — cada anual vendido devolve ~2h/ano de operação
- **Dia fixo de cobrança** (batch mensal único, não pulverizado pelo mês)
- Link recorrente onde a plataforma permitir (Asaas/Mercado Pago têm cobrança recorrente por link sem integração — meio-termo barato)
- Decisão de gateway definitivo (Stripe × Asaas/Pagar.me — Pix/boleto pesam) marcada para set/2027 (`00` §10)

## 🏢 Estrutura jurídica: solo — e um risco que não tem mais para quem ser empurrado

> ⚰️ **Toda a seção societária anterior está REVOGADA** (`00` §0.2): acordo de sócios, divisão 50/50, vesting de 4 anos, cliff de 1 ano, regras de saída e desempate. **Não há sociedade.** Nenhum doc deve citá-la.

O que existe no lugar:

- **Forma jurídica:** SLU (Sociedade Limitada Unipessoal), Simples Nacional, anexo V ou III conforme fator R. Abrir o CNPJ quando o primeiro pagamento se aproximar (~set/2027) — não antes, não há o que faturar.
- **O risco que mudou de natureza:** o doc 09 aponta que o **vínculo do Ronny com o setor público pode restringir atividade empresarial paralela** — em muitos regimes, servidor pode ser sócio quotista, mas **não administrador**. Na versão a dois havia um plano B implícito: o sócio seria o rosto jurídico (administrador) da empresa. **Esse plano B morreu com a sociedade.** Numa SLU, o titular é, por padrão, o administrador. Se o estatuto aplicável vedar, não há hoje ninguém para exercer o papel — e "pedir para um parente assinar" cria exatamente o tipo de risco jurídico-pessoal que o doc 09 manda evitar (laranja não é estrutura, é passivo).
- **Consequência prática:** o prazo canônico para verificar a situação funcional é out/2026 (`00` §10, item 4), "antes do CNPJ". Solo, isso está **frouxo demais**: o certo é verificar em **ago–set/2026, antes de afundar as 775h**. Se houver vedação, as opções reais são: (a) ser quotista com administrador contratado (custo mensal, e alguém de confiança real — não fachada); (b) autorização formal, se o regime previr; (c) repensar o projeto. Nenhuma dessas é boa de descobrir no mês 14.
- **O gate continua escrito — agora aqui, não num acordo de sócios:** **G4, jan/2028 (mês 18): < 35 pagantes ou churn > 8%/mês ⇒ reavaliação formal — pivotar nicho, pivotar preço ou encerrar.** Critério fixado a frio em jul/2026; não se renegocia a quente (`00` §8). Solo, isso importa MAIS, não menos: não há sócio para forçar a conversa difícil — o critério escrito e datado é o único "sócio" que restou.
- **Investimento:** R$ 300–600/mês até o beta, ~R$ 800/mês do beta em diante (infra + APIs de IA + dados + domínio). Cabe no bolso, sem investidor.

## 📉 Análise de sensibilidade (agora com as perguntas desconfortáveis)

### 1. E se o cronograma escorregar de 14 para 18 meses?

O intervalo 14–18 do doc 00 não é retórica: basta a média real cair de 12,5h para 10h/semana (775 ÷ 10 = 77,5 semanas ≈ 18 meses) — e o próprio plano de GTM já consome ~150h das mesmas horas (doc 07). Consequências do cenário 18:

| | Base (14 meses) | Deslize (18 meses) |
|---|---|---|
| Fim do dev | set/2027 | jan/2028 |
| Beta (2 meses) | ago–set/2027 | fev–mar/2028 |
| Lançamento | out/2027 (mês 15) | **~abr/2028 (mês 21) — FORA da janela honesta (out/27–jan/28)** |
| Primeiros R$ 16k de MRR | jul/2028 (mês 24) | ~jan/2029 (mês 30) |
| R$ 40k de MRR | jan/2029 (mês 30) | ~jul/2029 (**3 anos de projeto**) |

- **Custo de caixa do deslize:** +6 meses × R$ 300–600 ≈ **R$ 2–4k** — irrelevante. O custo real não é dinheiro:
- **Custo de janela:** 18+ meses de dev é tempo de sobra para um concorrente com time full-time lançar "geração de peças + ingestão + prazos" e disparar o **G5** (`00` §8) antes do nosso beta. Cada mês de deslize aumenta essa probabilidade — é o risco existencial do projeto (`00` §9.5).
- **A regra já está escrita** (G2, `00` §8): se o beta deslizar mais de 2 meses, aciona-se a conversa de viabilidade. Tradução: **18 meses não é "o plano com atraso" — é o cenário em que o projeto precisa se justificar de novo, formalmente.** Não seguir empurrando por inércia.

### 2. O custo de oportunidade das ~780h (a conta que ninguém quer fazer)

Até o lançamento: ~15 meses ≈ 65 semanas × 12,5h ≈ 810h; descontando férias e imprevistos, **~780h de trabalho antes do primeiro real de receita**. O que mais 780h comprariam:

- **Freela fullstack Angular + Java** a R$ 100–150/h (faixa sênior, mercado 2026): **R$ 78k–117k de receita quase certa** no mesmo período — contra R$ 0 até out/2027 e R$ 1,5k de MRR no lançamento.
- **Quando o projeto empata?** Receita bruta acumulada: out/27–jan/28 ≈ R$ 12k (média ~R$ 3k × 4 meses); fev–jul/28 ≈ +R$ 62k (média ~R$ 10k × 6); ago–set/28 ≈ +R$ 39k → a receita acumulada só cruza **R$ 100k por volta do mês 26–27 (set–out/2028)**. E isso é receita **da empresa** (que ainda paga custo variável e fixos) — não bolso do fundador. O freela paga o bolso desde o mês 1.
- Alternativas intermediárias com as mesmas 780h: 2–3 micro-produtos de time-to-revenue curto (~250h cada), ou simplesmente 780h de vida com família.
- **A leitura honesta:** em dinheiro esperado a 24–30 meses, **o freela ganha do projeto com folga**. O projeto só se justifica pelo que o freela não compra: um **ativo** que compõe (MRR de R$ 40k/mês no mês 30, se a projeção segurar), opcionalidade (expansão, venda) e aprendizado. Isso é uma aposta de venture feita com horas em vez de capital — **não é uma renda**. Se a motivação real for renda em até 2 anos, este é o projeto errado, e o momento de admitir é agora, não no mês 10 (`00` §9.5).

### 3. E se o churn for 8% em vez de 5%?

Churn composto: 5% = perder 46% da coorte em 12 meses (1 − 0,95¹²); 8% = perder **63%**. Com os **mesmos adds brutos** da projeção (12–13/mês até M18; 15–20 M19–24; 25–30 M25–30):

| Marco | Base (5% → 4%) | Churn 8% constante | Diferença |
|---|---|---|---|
| Mês 18 (GATE) | ~45 / R$ 4,7k | ~45 / R$ 4,7k | **invisível** — base pequena demais para o vazamento aparecer |
| Mês 24 | ~125 / R$ 16k | ~115 / R$ 15k | −8% |
| Mês 30 | ~270 / R$ 40k | ~205 / R$ 30k | **−24%** |
| Teto assintótico (adds ÷ churn, com 30 adds/mês) | 750 | **375** | metade da empresa |

**Leitura:** no gate do mês 18, churn de 8% não aparece na contagem de assinantes — é exatamente por isso que **o G4 mede o churn diretamente, não só a contagem**. 8% não mata o ano 1; condena o negócio a um teto pela metade. Se o churn medido passar de 8%, o problema é produto/nicho — mais marketing não conserta.

### 4. E se o CAC não for zero?

A projeção assume crescimento orgânico (conteúdo + indicação): CAC ≈ R$ 0 em dinheiro — mas **caro em tempo**, e agora o tempo é de uma pessoa só (a conta das horas de vídeo está no doc 07). Se o orgânico não puxar os 12–13 adds/mês, a alternativa é mídia paga:

- **LTV** = tíquete × margem bruta ÷ churn mensal → ano 1: 148 × 0,70 ÷ 0,05 = **R$ 2.072**; regime: 157 × 0,70 ÷ 0,04 = **R$ 2.748**; com churn 8%: 148 × 0,70 ÷ 0,08 = **R$ 1.295** — churn alto e CAC pago juntos quebram a conta duas vezes.
- **CAC máximo** (LTV/CAC ≥ 3): ~R$ 690 (ano 1) / ~R$ 915 (regime) / só ~R$ 430 se churn 8%.
- **Payback**: CAC ÷ margem mensal (148 × 0,70 = R$ 104/mês) → CAC R$ 300 = ~3 meses; R$ 600 = ~6 meses. Bootstrap sem caixa não sobrevive a payback longo.
- **Realidade do caixa**: comprar 12 adds/mês a CAC R$ 300 = R$ 3,6k/mês de mídia — **não cabe** num orçamento de R$ 300–800/mês. Portanto: tráfego pago só pós-PMF, financiado pelo próprio MRR (teto ~20% do MRR/mês) e só se o CAC medido em fundo de funil ficar ≤ R$ 300–400.
- Se o orgânico falhar E não houver caixa para comprar aquisição, é exatamente o cenário que o **G4 (mês 18)** existe para capturar — sem prorrogação emocional.

## Fontes de receita futuras (upside — **nada disso está na projeção acima**)

1. **Take rate sobre cobrança de honorários via Pix** — ex.: 1% sobre o transacionado. Um advogado que passa R$ 10k/mês de honorários pela régua rende R$ 100/mês, comparável à própria assinatura. **Mas depende do módulo financeiro, que é Fase 2 (pós-lançamento — realisticamente 2028)**: receita relevante só bem depois do gate do mês 18, sobre a fração da base que adotar a régua. Tratar como opção de expansão, não como plano.
2. Marketplace de templates (comissão sobre criadores) — Fase 3
3. Plano white-label para ESAs/cursos
4. Jurimetria premium (relatórios de vara/juiz)
