# 12 — Plano de Execução (meses 1–14, solo)

> Deriva 100% da conta do doc 00 §3–§4: **595h de features + 180h de buffer (30%) = 775h ÷ 12,5h/semana ≈ 62 semanas ≈ 14 meses** (intervalo honesto 14–18). Relógio: **mês 1 = ago/2026**, beta fechado ago–set/2027 (meses 13–14), lançamento pago out/2027 (mês 15). Time: **só o Ronny** (fullstack Angular + Java). Se algo aqui conflitar com o doc 00, vale o doc 00.

---

## 1. 🧮 A conta, aberta (uma pessoa só)

Capacidade mensal: 12,5h/semana × ~4,3 semanas = **~54h/mês**. Em 14 meses: ~756h nominais para 775h necessárias — a fração que falta (≈0,3 mês) é exatamente por isso que o intervalo honesto do doc 00 é 14–18, não 14 cravado. **E dessas ~54h/mês saem TAMBÉM validação, GTM e suporte de beta** — não existe mais "o Ronny cobre a validação enquanto o primo desenvolve".

Blocos do doc 00 §3, já com os cortes 1–5 aplicados:

| Bloco | h (pós-corte) | Corte aplicado |
|---|---:|---|
| Fundações (repo, CI/CD, auth, multi-tenancy + RLS) | 60 | #1 parcial (−10 de polish) |
| Onboarding + importação DataJud | 65 | — |
| Ingestão DJEN/Comunica + workers e filas | 90 | — |
| Classificação IA de intimações | 40 | — |
| Motor de prazos determinístico + testes pesados | 65 | — |
| Agenda + alertas **só e-mail** | 25 | #2 (−15, sem push) |
| Redator IA (RAG, prompts, suite) — **2 peças** | 95 | #3 (−30, era 4 peças) |
| Editor TipTap (wrapper Angular) + IA inline + .docx | 105 | — (inclui +25 do wrapper) |
| CRUD + dashboard "Seu dia" **sem frase-resumo IA** | 35 | #1 parcial (−5) + #4 (−10) |
| Cobrança por **link manual** + landing + LGPD mínimo | 15 | #5 (−25, sem billing automático) |
| **Subtotal features** | **595** | (690 − 95, doc 00 §3.2–3.3) |
| Buffer 30% (sem revisor — doc 00 §3.4) | 180 | |
| **Total** | **775** | |

⚠️ A divisão do corte #1 (−15) entre Fundações (−10) e CRUD (−5) é alocação deste doc; o canônico é o total (doc 00).

**Orçamento de horas não-feature dentro dos mesmos 14 meses** (sai do buffer + do calendário):

```
Validação (15 entrevistas + landing/lista)   ~30h   (meses 1–2)
Dezembro/2026 rende ~70%                     ~16h perdidas
Suporte beta (10 advogados, meses 13–14)     ~40h
GTM contínuo (conteúdo, lista de espera)     ~2–4h/mês a partir do mês 10
```

A conta fecha por pouco. Não há mês bom para ficar doente.

---

## 2. 📅 Mês a mês, com marco verificável

**Critério de "pronto", versão solo** (o antigo — "demonstrável pelo OUTRO sócio" — morreu; não há outro sócio):

1. **Checklist escrito ANTES de começar o bloco**, commitado no repo (`/docs/dod/bloco-X.md`). Pronto = todos os itens marcados. Escrever o critério a frio impede o "tá bom assim" a quente.
2. **Demonstrável em produção com dado real** (OAB real, publicação real do DJEN) — não seed, não mock, não "na minha máquina".
3. **Suite de testes passando no CI** — no motor de prazos e RLS, o teste É o revisor.
4. **Demo gravada de 2–5 min por marco mensal** (screen recording, sem edição). Serve de: prova de que funcionou de verdade, material para mostrar a entrevistados/betas, e registro de estado para retomada pós-hiato. Se dá vergonha de gravar, não está pronto.

| Mês | Data | Trabalho (h efetivas ≈ 54 salvo nota) | ✅ Marco verificável |
|---|---|---|---|
| 1 | ago/26 | Fundações: repo, CI/CD, deploy (Railway/Render), auth ~35h · **validação: entrevistas + landing ~19h** | Login em URL pública; landing coletando e-mails; ≥8 das 15 entrevistas feitas |
| 2 | set/26 | Fundações: RLS + teste de isolamento no CI ~25h · **spike TipTap** (ngx-tiptap × wrapper próprio × plano C — decisão #3 do doc 00) ~10h · entrevistas restantes ~10h · início onboarding ~9h | **G0**: 15 entrevistas, nicho decidido; 2 tenants sem vazamento (teste automatizado); decisão do editor tomada e registrada em ADR |
| 3 | out/26 | Onboarding + DataJud (job assíncrono, dedup, `dados_fonte`) 54h | Digitar OAB real (de um entrevistado) → processos importados e listados, em produção |
| 4 | nov/26 | Onboarding: telas 4 passos, fim ~11h · Ingestão DJEN início: polling, matching ~43h | Onboarding completo ponta a ponta; polling DJEN capturando publicações reais em staging |
| 5 | dez/26 ⚠️ ~38h (dezembro rende 70%) | Ingestão DJEN: dedup, fila, retry ~38h | Publicação real do DJEN vinculada ao processo certo em <12h — ainda sem monitoramento completo |
| 6 | jan/27 | Ingestão: monitoramento (Sentry), fim ~9h · Classificação IA (prompt + structured output + fila de baixa confiança) 40h · motor de prazos início ~5h | Intimação real → classificada com JSON validado + fila de baixa confiança funcionando; suite do classificador (doc 13 §5) rodando no CI |
| 7 | fev/27 | Motor de prazos: engine, feriados, tabela-verdade no CI 54h | **G1 (gate, doc 00): intimação real → classificada → prazo calculado com memória de cálculo, em produção.** Se não estiver de pé, replanejar JÁ para 18 meses |
| 8 | mar/27 | Motor: recesso/virada de ano, fim ~11h · Agenda + alertas e-mail 25h · CRUD/dashboard ~18h | Prazo atravessando o recesso 20/12–20/01 calculado certo (caso de teste real); alertas 7/2/0 dias chegando por e-mail |
| 9 | abr/27 | CRUD/dashboard fim ~17h · Editor TipTap: wrapper Angular, doc base, nodes custom ~37h | "Seu dia" mostrando prazos reais; dogfooding: Ronny usando o sistema com processos de conhecidos |
| 10 | mai/27 | Editor: estados de peça, IA inline, export .docx ~54h | Editor completo: abrir doc → editar → nodes `[VERIFICAR]` → exportar .docx válido |
| 11 | jun/27 | Editor fim ~14h · Redator IA: RAG (pgvector), prompt contestação, streaming ~40h | Primeira contestação gerada de intimação real, decente o bastante para mostrar a um entrevistado (demo gravada) |
| 12 | jul/27 | Redator: petições simples, ajuste contra a suite (doc 13) ~35h · Billing por link + landing venda + LGPD 15h · hardening ~4h | **G2 (gate): feature-complete do fluxo do aha** — intimação → classificação → prazo → rascunho → edição → .docx, com as 2 peças. Recrutamento dos 10 betas começa (lista ~150) |
| 13 | ago/27 | **BETA fechado (10 advogados)**: onboarding assistido ~15h · buffer: bugs, prompts vs. suite ~39h | ≥7 betas ativos; ≥60% importou processos E gerou 1 peça na 1ª semana; coleta de pares intimação↔peça (doc 13 §2) rodando |
| 14 | set/27 | Beta: suporte ~25h · buffer: hardening, curadoria feriados, oferta founding ~29h | **G3 (gate de lançamento)**: NPS ≥ 40; zero prazo errado; ≥2 peças/sem/usuário ativo; ≥6 betas dispostos a pagar |

Mês 15 (out/2027): **lançamento pago** — janela honesta até jan/2028 (doc 00 §4). Se G3 falhar, desliza dentro da janela. **Nunca lançar cobrando com prazo errado em produção.**

⚠️ Buffer restante ao fim do mês 12: ~76h dentro do calendário de 14 meses. Se os estouros (wrapper TipTap é o candidato nº 1) comerem mais que isso, o plano oficial é deslizar para o cenário de 15–18 meses — não é comprimir os meses 13–14.

---

## 3. 🔗 Caminho crítico e dependências (tudo é sequencial)

Com uma pessoa, **não existe paralelismo** — o diagrama antigo de dois trilhos morreu. O caminho crítico É o cronograma: cada bloco atrasado empurra todos os seguintes, hora por hora.

```
Fundações(M1–2) ─► DataJud/Onboarding(M3–4) ─► Ingestão DJEN(M4–6) ─► Classificação(M6)
      ─► Motor de prazos(M6–8) ─► Agenda(M8) ─► CRUD/"Seu dia"(M8–9)
      ─► Editor TipTap(M9–11) ─► Redator IA(M11–12) ─► Billing/landing(M12)
      ─► BETA(M13–14) ─► Lançamento(M15)

Únicas coisas fora da fila de dev (mas dentro das MESMAS 12,5h/semana):
  validação(M1–2) · spike TipTap(M2) · recrutamento beta(M12) · GTM(M10+)
```

| Bloqueio | Vítima | Mitigação |
|---|---|---|
| **Ingestão DJEN atrasa** | TUDO — classificação, prazos e redator ficam sem insumo. Continua sendo O bloqueio | Vem cedo (M4–6) e tem 90h intocadas de corte. Fallback: cotar provedor pago (Judit.io/Escavador — decisão #5 do doc 00, dez/2026) ANTES de precisar |
| Wrapper TipTap estoura as 105h | Editor e, por arrasto, redator e beta | Spike no M2 decide entre lib comunitária, wrapper próprio ou plano C (editor simplificado no beta). Estouro >30h ⇒ acionar plano C, não insistir |
| Ronny para (doença, emprego, família) | O projeto inteiro — bus factor 1 | Não tem mitigação de verdade. Buffer + ritual de retomada (§5) + cronograma pessimista de 18 meses como cenário realista |
| Recrutamento dos 10 betas atrasa | Beta inteiro | Começa no M12, não no M13; entrevistados do M1–2 são os primeiros convites; meta de lista: 150 e-mails |
| Billing (link manual) atrasa | Só o lançamento | 15h, risco mínimo — é gerar link no gateway e conferir planilha. Era exatamente o plano C antigo; agora é o plano |

**Nota sobre "sequencial":** dentro de um mês o Ronny alterna back e front do mesmo bloco (é uma cabeça só, zero custo de contrato de API). O que NÃO existe é dois blocos andando na mesma semana. WIP = 1 bloco.

---

## 4. ✂️ Ordem de sacrifício: a gordura ACABOU

Os itens 1–5 da ordem antiga **já foram gastos** para fazer a conta de 14 meses fechar (doc 00 §2/§3.2):

| # | Corte | Status |
|---|---|---:|
| 1 | Dark mode + polish visual | ✅ já cortado (−15h) |
| 2 | Push notification (fica e-mail) | ✅ já cortado (−15h) |
| 3 | 4 → 2 tipos de peça | ✅ já cortado (−30h) |
| 4 | Frase-resumo IA do "Seu dia" | ✅ já cortado (−10h) |
| 5 | Billing automático → link manual | ✅ já cortado (−25h) |

**O que resta, se ainda assim estourar** (decidido a frio, agora):

| # | Corte restante | Economia | Custo real |
|---|---|---:|---|
| 6 | **Deslizar dentro da janela** (lançamento até jan/2028) | até ~3 meses | Nenhum estrutural — é a opção oficial e a PRIMEIRA a usar. Cada mês deslizado = 1 mês a mais de janela para o G5 fechar |
| 7 | Beta de 10 → 5 advogados | ~15h de suporte | Suite de avaliação nasce com ~10–15 pares (doc 13 já opera no limite com 20–30); sinal de NPS/ativação vira anedota. Dói de verdade |
| 8 | "Seu dia" vira lista mínima de prazos (sem dashboard) | ~10h | Feio mas funcional — o aha não passa pelo dashboard |
| 9 | Petições simples fora: **só contestação** no beta | ~15h | Mexe no aha (contestação é 80%, mas 80% ≠ 100%). Último corte antes da linha vermelha |

Depois do item 9 não há mais corte — há **decisão de tese** (G4/G5, §6): mais horas semanais, outro sócio, ou parar. Cortar ingestão, classificação, motor determinístico, revisão humana obrigatória ou export .docx **mata a tese** (doc 00 §2) e não está nesta lista de propósito.

Curadoria contínua sem dono nomeado agora tem um dono só: **feriados, prompts e copy = Ronny.** Auditoria de feriados entra como evento mensal de calendário (§5) — senão ninguém faz, porque "ninguém" agora é uma pessoa.

---

## 5. 🧍 Ritual de trabalho solo (12,5h/semana por 14 meses, sem ninguém cobrando)

O risco não é técnico, é de regime: manter constância por 62 semanas sem sync, sem par, sem cobrança externa. O ritual abaixo substitui o §5 antigo inteiro (sync semanal, ADR com 72h de objeção, trilhos, backlog não-bloqueado — tudo morto).

### 5.1 Cadência

- **Blocos fixos no calendário**, não "quando der": ex. 2 noites de 3h + 1 bloco de 6–7h no fim de semana ≈ 12,5h. O formato exato importa menos que ser o MESMO toda semana. Hora vaga não usada não "acumula" — a conta do §1 já assume a média.
- **Review semanal de 15 min consigo mesmo** (domingo, escrito, no repo ou numa nota): o que fechou · o que travou · plano da próxima semana em ≤3 itens. É a versão solo do sync — e é o que detecta deriva ANTES de virar mês perdido.
- **Fim de mês = marco do §2 conferido contra o checklist + demo gravada.** Marco furado não gera culpa, gera recálculo por escrito (ver 5.4).

### 5.2 Não perder contexto entre sessões curtas

Sessões de 3h não sobrevivem a "onde eu estava mesmo?". Regras mecânicas:

1. **Últimos 5 min de TODA sessão: diário de bordo** (`NOTES.md` ou comentário na issue): "parei em X, falta Y, cuidado com Z". 3 linhas. É a mensagem do Ronny de hoje para o Ronny de terça.
2. **Nunca encerrar sessão com build quebrado** — melhor reverter os últimos 20 min do que reabrir num estado indecifrável.
3. **WIP = 1.** Um bloco, uma issue, um branch curto. Trunk-based, feature flag para coisa grande. Branch de 3 semanas solo = arqueologia.
4. **ADRs continuam existindo** — 10 linhas, sem prazo de objeção (não há quem objete). O leitor é o Ronny do mês 8 tentando lembrar por que RLS e não schema-por-tenant. Também é o que barateia onboarding de um eventual sócio futuro ou revisor pontual.

### 5.3 Compensar a falta de revisor (o problema nº 1 do regime solo)

Nenhum item abaixo substitui um par. A soma reduz o dano:

| Camada | O quê, concretamente |
|---|---|
| **Testes como revisor** | Motor de prazos: tabela-verdade com 100% no CI (doc 13 §5). RLS: teste de isolamento entre tenants em TODO build. Já orçados nos blocos — não são extra |
| **CI implacável** | Nada entra em produção sem build verde. Deploy contínuo — bug pequeno descoberto cedo é a versão barata do revisor |
| **IA como primeiro revisor** | Todo diff relevante passa por revisão assistida (Claude Code): "procure bugs de isolamento de tenant / off-by-one em datas / null-handling neste diff". Pega a classe de erro que o autor não vê por estar perto demais. É revisor incansável e barato — e não é par: não conhece o contexto de produto nem briga com a arquitetura |
| **Regra das 24h para código crítico** | Multi-tenancy, cálculo de prazos, interpolação de dados em peça, cobrança: escrever num dia, **reler o diff no dia seguinte** antes de merge. Distância temporal é o substituto pobre (mas real) do segundo par de olhos |
| **Amigo dev para revisão pontual** | 3–4 sessões pagas ou trocadas no projeto inteiro, nos pontos onde erro é catastrófico: RLS/isolamento (M2), motor de prazos (M7), wrapper TipTap (M9–10), pipeline DJEN (M6). Escopo fechado, 2h por sessão, com checklist do que revisar. ⚠️ combinar isso ANTES de precisar |
| **Pentest externo antes de cobrar** | Já obrigatório (doc 05). Continua |
| **Advogado parceiro** | Revisor do domínio: tabela-verdade de prazos e qualidade de peça (doc 13 §5/§8). Erro jurídico o dev não pega sozinho, com ou sem IA |

### 5.4 Quando sumir (porque VAI sumir)

Emprego aperta, alguém adoece, cansa. O plano assume que isso acontece; o que ele não perdoa é fingir que não aconteceu.

1. **Sumir não exige justificativa** — exige registro: uma linha no diário ("semanas X–Y: parado").
2. **Regra de re-entrada** (o retorno é o momento frágil): ler o diário → rodar a suite → pegar a MENOR tarefa aberta e fechá-la na primeira sessão. Recomeçar por algo grande é o jeito clássico de não recomeçar.
3. **2 semanas paradas = cronograma desliza 2 semanas, por escrito** (atualizar §2). Proibido "compensar" com sprint heroico — o burnout do doc 00 §9.3 começa exatamente aí. O intervalo 14–18 meses existe para absorver isso.
4. **>6 semanas paradas ⇒ mini-gate pessoal**: sentar e responder por escrito se o projeto continua, em que ritmo, e o que isso faz com as datas de G2–G4. Silêncio prolongado sem decisão é a pior versão de parar.

---

## 6. 🚦 Gates de decisão (recalibrados — doc 00 §8)

| Gate | Quando | Critério | Se falhar |
|---|---|---|---|
| G0 — Validação | fim set/2026 (M2) | 15 entrevistas; dor confirmada; nicho decidido | Pivotar nicho ANTES de escrever prompt/template |
| G1 — Insumo | fim fev/2027 (M7) | Intimação real → classificada → prazo calculado, em produção | Replanejar para o cenário de 18 meses; considerar cortes 7–9 (§4) |
| G2 — Feature-complete | fim jul/2027 (M12) | Fluxo do aha ponta a ponta com as 2 peças | Beta desliza; deslize >2 meses ⇒ conversa de viabilidade |
| G3 — Lançamento | fim set/2027 (M14) | NPS betas ≥ 40; zero prazo errado; ≥2 peças/sem/usuário ativo; ≥6 betas dispostos a pagar | Lançamento desliza (janela até jan/2028). **Nunca lançar cobrando com prazo errado** |
| **G4 — Continuidade** | **jan/2028 (mês 18)** | **≥35 pagantes E churn ≤8%/mês** | **Reavaliação formal: pivotar nicho, preço ou encerrar. Escrito a frio em jul/2026 — não se renegocia a quente** |
| **G5 — Competição (novo, permanente)** | contínuo | Concorrente relevante (Astrea/Advbox/novo entrante) lança **geração de peças + ingestão + prazos integrados** antes do nosso beta | Reavaliar a tese IMEDIATAMENTE: cortar mais e antecipar beta, ou encerrar cedo e barato. A janela de 14–18 meses só existe se ninguém a fechar antes |

**Vigilância do G5 é rotina, não evento**: 30 min/mês varrendo changelog/pricing dos concorrentes (entra no review semanal de fim de mês). Solo, é fácil passar 6 meses de cabeça baixa no código e não ver a janela fechar.

**Sanidade entre lançamento e G4** (curva do doc 00 §7: 14 → ~25 → ~36 → ~45): dois meses seguidos >30% abaixo ⇒ antecipar a conversa do G4, não esperar janeiro.

---

## 7. ⚠️ Riscos de gente (agora é UMA gente)

| # | Risco | Leitura honesta | Mitigação (parcial) |
|---|---|---|---|
| 7.1 | **Bus factor = 1.** Doença, acidente, pico no emprego: o projeto PARA. Não há trilho paralelo nem quem cubra | 2 semanas paradas = 2 semanas de atraso, sempre, sem exceção | Buffer 30% + cenário 18 meses como realista; documentação (ADRs, diário, demos gravadas) para retomada barata — inclusive por outra pessoa, num cenário de venda/sociedade futura |
| 7.2 | **Burnout.** 12,5h/semana TODA semana por 14–18 meses, em cima de um emprego, sem sócio para dividir frustração | O cronograma já é o anti-burnout: assume 12,5h e não 20, proíbe sprint heroico (§5.4) | Marcos mensais pequenos e demonstráveis (dopamina de progresso); demo gravada como celebração mínima; deslizar é sempre preferível a esmagar |
| 7.3 | **GTM e dev competem pelas MESMAS horas.** As 15 entrevistas, a lista de espera, o conteúdo e o suporte do beta saíam "de graça" no plano antigo porque o primo cobria o dev. Não cobre mais | Validação ≈ 30h ≈ 3 semanas de capacidade; suporte de beta ≈ 40h ≈ 1 mês. Está tudo DENTRO do §2 — se o GTM crescer além disso, o dev atrasa na mesma proporção | GTM em janelas concentradas (M1–2 validação; M12+ recrutamento/conteúdo); meta de lista reduzida (150, não 200); aceitar funil menor no ano 1 (doc 00 §7 já assume) |
| 7.4 | **Curadoria contínua sem redundância.** Feriados, prompts, suite — antes dividido, agora tudo do Ronny | Feriado municipal errado = prazo errado = a catástrofe do doc 09 | Auditoria mensal de feriados em calendário com alarme; erro achado vira caso da tabela-verdade ANTES do fix (doc 13 §5) |
| 7.5 | **Isolamento de decisão.** Sem segunda opinião de arquitetura, decisão ruim do M2 aparece como retrabalho no M8 | O buffer de 30% existe em parte por isso (doc 00 §3.4) | ADRs + revisão pontual do amigo dev nos pontos críticos (§5.3) + spike antes de compromissos caros (TipTap) |
