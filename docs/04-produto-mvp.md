# 04 — Produto: MVP e Roadmap

> **Alinhado ao doc `00-premissas.md` (fonte única de verdade).** Os antigos "MVP em 4 meses" e "Fase 1 em 8 meses" estão **revogados** — eram contas de um time de 2 pessoas que não existe mais. Fase 1 = **~14 meses solo** (ago/2026 – set/2027), beta fechado com **10 advogados** nos meses 13–14, lançamento pago no **mês 15 (out/2027)**.

## Filosofia do MVP

**Não construir um "software jurídico completo menor".** Construir a **melhor versão do fluxo mais doloroso** e crescer a partir dele.

O fluxo escolhido (o "aha moment" — canônico, doc 00 §1):

```
Intimação chega → IA lê e classifica → prazo entra na agenda →
IA rascunha a peça de resposta → advogado revisa e exporta
```

Se entregarmos SÓ isso de forma impecável, já vale a assinatura.

Repare no que o fluxo **não** tem: WhatsApp e financeiro. São dores reais, mas de **retenção**, não de **conversão** — foram movidos para a Fase 2 (doc 00 §2). A promessa da Fase 1 é: *"Nunca mais perca um prazo — e sua peça rascunhada em minutos."*

Solo, essa filosofia deixa de ser preferência e vira sobrevivência: **cada hora fora do fluxo do aha atrasa o lançamento, em regime de 12,5h/semana.**

---

## Por que 14 meses, não 8 (a conta, aberta)

A versão anterior deste doc contava com 2 pessoas × 12,5h/semana = 25h/semana. O sócio saiu; a capacidade caiu pela metade e a stack mudou (Angular + Spring Boot — doc 00 §0.1). A conta refeita bloco a bloco (doc 00 §3):

| | Valor |
|---|---|
| Horas de features reestimadas (stack nova, escopo antigo) | 690h |
| Cortes de escopo aplicados (itens 1–5 da ordem de sacrifício) | −95h |
| **Features da Fase 1 (escopo solo)** | **595h** |
| Buffer de **30%** (sem revisor de código, wrapper TipTap inédito) | 180h |
| **Total** | **775h** |
| Capacidade (1 pessoa × 12,5h/semana) | 12,5h/semana |
| **Cronograma** | **775 ÷ 12,5 = 62 semanas ≈ 14 meses** |

Cenário pessimista (10h/semana reais): ~18 meses. **O intervalo honesto é 14–18.** Qualquer material que diga "4 meses" ou "8 meses" está errado e deve ser corrigido.

### Os 5 cortes que tornaram a conta possível (decisões conscientes, com custo declarado)

| Corte | Horas | O custo que aceitamos |
|---|---:|---|
| #1 Sem dark mode / polish visual não-crítico | −15 | Produto menos "bonito de demo". Aceitável: o ICP paga por prazo e peça, não por tema escuro |
| #2 **Sem push notification — alertas só por e-mail** | −15 | Alerta menos imediato no celular. E-mail escalonado (7 dias / 2 dias / no dia) cobre o essencial; push volta pós-lançamento |
| #3 **4 → 2 tipos de peça** (contestação + petições simples) | −30 | Réplica e recurso inominado ficam de fora do lançamento — ver seção do Redator abaixo. É o corte mais visível ao usuário |
| #4 Sem frase-resumo IA no "Seu dia" | −10 | Dashboard estruturado (listas, contadores, prazos) sem o parágrafo "mordomo". Charme, não função |
| #5 **Billing automático → link de pagamento manual** | −25 | Ver seção "Fundação comercial" — o custo é operacional nosso, invisível para o cliente |

---

## MVP — Fase 1 (meses 1–14, ago/2026 – set/2027)

### 1. Onboarding mágico
- Cadastro com e-mail/Google
- Digita OAB/UF → importa processos automaticamente (via DataJud/CNJ)
- IA classifica cada processo: área, fase, próxima ação provável
- **Meta: do cadastro ao "uau" em menos de 10 minutos**

### 2. Central de intimações e prazos
- Captura de publicações via **DJEN/Comunica CNJ** (fontes gratuitas; fallback pago se necessário — ver `05`)
- IA lê cada publicação e extrai: tipo de ato, prazo em dias, providência recomendada, urgência (com fila de baixa confiança para revisão)
- **Data-limite calculada por motor determinístico em código** (dias úteis + feriados forenses) — nunca pelo LLM (doc 06)
- Agenda com alertas escalonados (7 dias antes, 2 dias, no dia) via **e-mail** (sem push no lançamento — corte #2; WhatsApp só na Fase 2)

### 3. Redator IA (o coração do produto)
- A partir da intimação + dados do processo, gera rascunho de peça:
  - **1 nicho** (hipótese: trabalhista — confirmar nas 15 entrevistas), **2 tipos de peça no lançamento: contestação + petições simples** (juntada, habilitação, dilação)
  - **Réplica e recurso inominado: meses 16–17 (nov–dez/2027)**, 1–2 meses pós-lançamento — corte #3, doc 00 §2. Racional: contestação é ~80% do aha moment; cada tipo de peça custa template + prompts + casos na suite de avaliação
  - 2º nicho (previdenciário) entra na fase 1.5 (pós-lançamento)
- Editor **TipTap com wrapper Angular próprio** (bindings oficiais são React/Vue — spike no mês 1–2 para avaliar lib comunitária antes de escrever do zero; ver risco no doc 09) com IA inline ("reescreva mais formal", "adicione tese sobre X")
- Exportação em **.docx formatado** (padrão de peça) + PDF pronto para protocolo
- **Sempre com revisão humana obrigatória — ver compliance em `09`**

### 4. Gestão básica
- CRUD de processos, clientes e documentos
- Campos **"origem do cliente"** e **"indicado por"** no cadastro (v0 do controle de captação — ver roadmap; custo ~zero, valor de dado desde o dia 1)
- Dashboard "Seu dia" — daily brief **estruturado** (prazos do dia, intimações novas, peças aguardando revisão), **sem frase-resumo gerada por IA** (corte #4)

### 5. Fundação comercial
- **Cobrança por link de pagamento manual** — decisão consciente (corte #5, doc 00 §2): no lançamento, cada assinatura é um link de pagamento gerado à mão + controle em planilha/admin simples. **O custo:** ~1–2h/mês de trabalho operacional do fundador a cada dezena de assinantes, risco de erro manual em renovação/cancelamento, e nenhum self-service de upgrade. **Por que aceita:** economiza 25h de dev antes do lançamento, é invisível para o cliente (ele recebe um link e paga), e com 14 assinantes no mês 15 o volume é trivial. **Gatilho de reversão:** automatizar (Stripe × Asaas/Pagar.me — decisão #6 do doc 00 §10) ao atingir ~50 assinantes ou quando o operacional passar de ~4h/mês
- Landing page (@angular/ssr) + LGPD mínimo (política, termos, DPA)

### 6. Migração de entrada (a alavanca de conversão que faltava)

Quem sai do Astrea, MaisJurídico ou da planilha não pode começar do zero — **o custo de troca é a maior objeção de venda contra incumbentes**. Especificação:

| Versão | O quê | Quando | Esforço |
|---|---|---|---|
| **v0 — Importador CSV/XLSX** | Wizard de mapeamento de colunas para clientes, contatos e processos ("sua planilha vira seu escritório em 10 min") | Fim da Fase 1, antes do beta (usa ~20h do buffer de 180h) | ~20h |
| **v1 — Templates de concorrente** | Mapeamentos prontos para os exports do Astrea e do MaisJurídico ⚠️ verificar o que cada um permite exportar (Astrea não tem API; export via relatórios/CSV) | Fase 2, antes de escalar aquisição | ~20h |
| **Concierge (não é código)** | Founding members mandam o export → nós importamos em até 48h, manualmente se preciso | Do lançamento em diante | tempo do fundador — com 10 betas e ~14 assinantes iniciais, cabe; revisitar se a aquisição escalar |

Nota estratégica: o DataJud reidrata os **processos** pela OAB de qualquer forma; a migração de verdade é sobre **clientes, contatos e histórico** — é isso que prende o advogado no sistema antigo. E vira argumento de marketing direto: *"Saindo do Astrea? A gente importa tudo pra você."*

---

## ❌ O que fica FORA da Fase 1 (a lista honesta)

Esta seção cresceu de propósito. Cada corte tem motivo e destino:

| Feature | Por que fica fora | Quando entra |
|---|---|---|
| **Réplica + recurso inominado** (peças 3 e 4) | Corte #3 do cenário solo (−30h). Contestação é o grosso do aha | Meses 16–17 (nov–dez/2027) |
| **Push notification** | Corte #2 (−15h). E-mail escalonado cobre o essencial | Pós-lançamento |
| **Billing automático** | Corte #5 (−25h). Link manual até ~50 assinantes | Meses 16–17 |
| **Dark mode / polish visual** | Corte #1 (−15h) | Quando sobrar hora (leia-se: não cedo) |
| **Frase-resumo IA no "Seu dia"** | Corte #4 (−10h). Charme, não função | Pós-lançamento |
| **WhatsApp Business API** (atualizações automáticas, atendente IA) | Não participa do aha moment. Verificação de negócio na Meta + templates + BSP + custo por mensagem = ~80–120h que não convertem ninguém no dia 1 | Fase 2 (2028), como arma de retenção |
| **Módulo financeiro** (honorários, parcelas, cobrança Pix) | Registro passivo não gera "uau"; cobrança ativa com Pix é feature de retenção. Corta ~60h | Fase 2 |
| **Peticionamento eletrônico** (protocolar direto nos tribunais) | Ver seção dedicada abaixo — é a lacuna mais sensível do produto e merece análise própria | Spike na Fase 2; decisão go/no-go documentada |
| **Chat com os autos** | Ótimo, mas é "fase 1.5" | Meses 17–19, com limite por plano |
| **2º nicho (previdenciário)** | Cada nicho = templates + prompts + suite de avaliação | Meses 17–19 |
| Funil de captação/indicação (v1) | v0 (campos de origem) já entra na Fase 1; o funil precisa de usuários usando | Fase 2 (ver seção dedicada) |
| Timesheet / controle de horas | Dor de escritório grande, não do solo | Sem previsão |
| Multi-usuário com permissões complexas | Plano Escritório funciona com papéis simples | Fase 2+ |
| Site/página pública do advogado | Depende do módulo de captação maduro | Fase 3 |
| Relatórios gerenciais avançados | Dashboard "Seu dia" resolve o essencial | Fase 3 |
| App mobile nativo | PWA responsivo resolve no início | Fase 2+ |

**Consequência de copy (docs 01 e 07):** nada de prometer WhatsApp, financeiro, protocolo automático — nem 4 tipos de peça — no lançamento. O que vendemos na Fase 1 termina na peça revisada e exportada, e são **2 tipos de peça** no dia 1. Dizemos isso com todas as letras (ver abaixo e a tabela de objeções do doc 01).

---

## ⚖️ Peticionamento eletrônico: fosso ou armadilha?

### A lacuna que ninguém discutia

A proposta de valor diz *"seu escritório trabalhando sozinho"* — mas o fluxo da Fase 1 termina em **exportar .docx**. Na prática, o advogado ainda: abre o PJe (ou eproc, ou ESAJ...), loga com certificado digital, converte para PDF, preenche classe/tipo de documento, anexa, assina e protocola. São 10–20 minutos de fricção por peça, exatamente onde prometemos que "a IA trabalha". Um advogado revisando este material listou "peticionamento" entre as expectativas dele — **é expectativa explícita de mercado, não invenção nossa**.

### O que existe tecnicamente

**1. MNI — Modelo Nacional de Interoperabilidade (CNJ/CJF).** Padrão nacional de web services (SOAP) para intercomunicação com sistemas processuais, incluindo consulta a processos e **entrega de manifestações processuais (peticionamento)**. O PJe implementa o MNI; o eproc também expõe web services. ⚠️ Verificar na documentação do CNJ/tribunais: versão do MNI em produção por tribunal, estabilidade real dos endpoints e requisitos de **credenciamento** do sistema consumidor (vários tribunais exigem cadastro/convênio prévio para acesso sistêmico).

**2. A fragmentação é o problema — mas o nosso nicho a reduz.**

| Ramo | Sistema(s) | Leitura para nós |
|---|---|---|
| **Justiça do Trabalho** | **PJe em todos os 24 TRTs + TST** | O melhor cenário do Brasil: **um único sistema** cobre o nicho de estreia (se confirmado trabalhista) |
| Justiça Federal (previdenciário) | eproc (TRF4) + PJe (demais TRFs) ⚠️ verificar mapa atual | 2 sistemas para o 2º nicho |
| Justiças Estaduais | ESAJ (SP e outros), Projudi, eproc (SC, RS, TO...), PJe — colcha de retalhos | Armadilha clássica: cada TJ é um projeto de integração |

Não é coincidência que a estratégia de nicho salve a gente aqui de novo: **atacar "peticionamento no Brasil" é inviável para 1 dev part-time; atacar "peticionamento no PJe trabalhista" é um alvo só.**

**3. Certificado digital: a barreira real.**

- Toda petição exige assinatura **ICP-Brasil** do advogado. Peticionar é ato privativo dele — nós somos ferramenta; **ele** assina e responde pelo ato.
- **A1** (arquivo de software, validade 1 ano): pode tecnicamente ser custodiado e usado server-side — é o único modelo que permite automação real. Mas custodiar certificado de terceiro é **responsabilidade máxima**: cofre criptográfico, termo de autorização expresso, trilha de auditoria de cada uso. ⚠️ Validar com advogado se há vedação da OAB/ITI à custódia por terceiros e como players existentes tratam isso.
- **A3** (token/cartão físico): impossível de automatizar server-side; exigiria agente local ou extensão de navegador — outro produto inteiro.
- ⚠️ Levantar nas 15 entrevistas: quantos do nosso ICP têm A1 vs A3? Se o ICP for majoritariamente A3, a automação server-side morre na praia independentemente do MNI.

**4. O mercado sinaliza que dá — e que dói.** Existem players brasileiros de automação de protocolo/robôs judiciais (ex.: Oystr) ⚠️ verificar abordagem atual deles (MNI vs. RPA). RPA sobre a interface dos tribunais é notoriamente frágil (captcha, mudança de layout, bloqueios). Se for para fazer, é via MNI ou não é.

### Veredito: armadilha na Fase 1, fosso potencial a partir da Fase 2

| | Análise |
|---|---|
| ❌ **Fase 1: não entra** | Custaria +100–150h que não existem nas 775h; exige maturidade de segurança que ainda não temos (custódia de certificado antes do primeiro pentest = irresponsável); e o aha moment **não depende dele** |
| ✅ **Fase 2: spike obrigatório** | 2 semanas de spike técnico contra o MNI do PJe-JT, com certificado A1 de um advogado parceiro do beta e termo de autorização. Entregável: protocolo de UMA petição real de teste + relatório go/no-go. Solo, 2 semanas de spike = 2 semanas sem dev de mais nada — agendar com o roadmap limpo |
| 🏰 **Se o spike passar: é fosso** | "Da intimação ao protocolo sem sair do sistema" em um nicho onde o sistema é único — nenhum concorrente do nosso segmento entrega isso. Lock-in operacional fortíssimo |
| 🕳️ **Se falhar: paliativo barato** | "Protocolo guiado": exportar PDF já no padrão do tribunal + checklist de protocolo por tipo de peça + link direto para o PJe do TRT correto. ~15h, resolve 30% da fricção sem risco |

### A copy precisa ser honesta (obrigatório desde já)

Enquanto o protocolo for manual, os materiais de venda (docs 01 e 07, landing, vídeos) devem dizer explicitamente onde a IA para:

> **"A IA lê a intimação, calcula o prazo e escreve o rascunho. Você revisa, assina e protocola — o ato é seu, como manda a OAB."**

Isso transforma a limitação em postura de compliance (revisão humana obrigatória, doc 06/09) em vez de promessa quebrada. **Proibido** usar "seu escritório trabalhando sozinho" sem essa qualificação enquanto não houver protocolo integrado.

---

## Roadmap (relógio: ago/2026 = mês 1 — doc 00 §4)

### Meses 13–14 (ago–set/2027) — Beta fechado
- **10 advogados** (era 20 — solo não suporta onboarding assistido de 20), gratuito 60 dias, 1 nicho, 2 peças
- Suite de avaliação alimentada com ~20–30 pares intimação→peça do beta, complementada com peças públicas (doc 13)

### Mês 15 (out/2027) — Lançamento pago
- Oferta founding (50 primeiros: 30% off por 12 meses) para betas + lista de espera
- Cobrança por link manual; janela honesta de lançamento: out/2027–jan/2028

### Meses 16–17 (nov–dez/2027) — Devolver os cortes
- **Réplica + recurso inominado** (volta a 4 tipos de peça — corte #3)
- **Billing automático** (Stripe × Asaas/Pagar.me — corte #5)
- Push notification e frase-resumo do "Seu dia" conforme sobrar hora (cortes #2 e #4)

### Meses 17–19 (dez/2027 – fev/2028) — Fase 1.5
- **Chat com os autos** (RAG sobre PDFs do processo), com limite por plano (50/200/fair use)
- **2º nicho: previdenciário** (templates + prompts + suite de avaliação)

### Fase 2 (2028) — Retenção e expansão
- **WhatsApp Business API:** atualizações automáticas de andamento + respostas a "como está meu processo?" (custo por mensagem de template — ver `05`)
- **Módulo financeiro + cobrança ativa:** honorários, parcelas, régua de cobrança com Pix (lembrete → link Pix → conciliação)
- **Captação/indicação v1** (ver seção dedicada abaixo)
- **Spike de peticionamento** (MNI PJe-JT) + decisão go/no-go; se go, piloto com usuários A1 voluntários
- **Migração v1:** templates de import Astrea/MaisJurídico
- **Biblioteca de estilo:** IA aprende com as peças do próprio advogado (planos Escritório/Pro)
- Multi-usuário simples (advogado + estagiário)

### Fase 3 (2029+) — Plataforma
- API pública (explorar a fraqueza do Astrea)
- Captação v2: página pública do advogado + triagem de leads por IA no WhatsApp + agendamento
- Peticionamento: expansão para 2º sistema (eproc ou PJe federal), se o piloto trabalhista validar
- Add-in Word · Marketplace de templates · Portal do cliente · Novas áreas (família, consumidor)

**Aviso de realismo:** todo o pós-lançamento acima disputa as mesmas 12,5h/semana com suporte, GTM e correções do que o beta revelar. A ordem é firme; as datas do pós-lançamento são as primeiras a deslizar — e tudo é subordinado ao gate G4 (jan/2028: <35 pagantes ou churn >8% ⇒ reavaliação formal, doc 00 §8).

---

## 🤝 Captação e indicação de clientes (reposicionado: era Fase 3, agora começa na Fase 1)

**O problema:** um advogado revisando o material reclamou — *"controle de captação/indicação de novos clientes, não tem sobre isso"*. Estava escondido na Fase 3, tarde demais para uma expectativa explícita. E o doc 07 já diz que **indicação entre advogados e de clientes é o canal natural da categoria** — não medir origem de cliente é jogar fora o dado do nosso próprio motor de crescimento.

**O que NÃO vamos construir cedo:** CRM completo à la Lawmatics. O solo não gerencia pipeline de vendas; ele quer saber **de onde vêm os clientes** e **não esquecer um lead**.

| Versão | Fase | O quê | Esforço |
|---|---|---|---|
| **v0 — Origem e indicação** | **Fase 1** (dentro do CRUD já orçado) | Campos "origem do cliente" (indicação de cliente, indicação de colega, Instagram, Google, OAB/evento, outro) e "indicado por" (link para cliente/contato existente) no cadastro. Contador simples no perfil do cliente: "indicou 3 clientes" | ~0h extra |
| **v1 — Funil mínimo** | **Fase 2** | Lista de **leads** separada de clientes (nome, contato, área, origem, status: novo → consulta → proposta → contratado/perdido), lembrete de follow-up na agenda, e relatório "de onde vieram meus clientes este ano". Integra com o programa "indique um colega" do doc 07 (advogado vê no produto quem ele indicou e o crédito ganho) | ~30h |
| **v2 — Captação ativa** | Fase 3 | Página pública + formulário de triagem + IA no WhatsApp + agendamento (o plano original) | — |

**Compliance:** tudo dentro do Provimento 205/2021 da OAB (doc 09) — o módulo ajuda a **registrar** captação lícita (indicações, conteúdo), nunca a mercantilizar. ⚠️ Validar redação das telas com advogado antes do lançamento da v1.

---

## 📊 Métricas de sucesso do MVP (realinhadas ao doc 00 — beta de 10)

| Métrica | Meta | Nota |
|---|---|---|
| Tempo do cadastro ao primeiro "uau" | < 10 min | |
| Ativação (importou processos + gerou 1 peça na 1ª semana) | > 60% | Com 10 betas, isso é ≥ 6 pessoas — número pequeno: ler caso a caso, não só a taxa |
| Peças geradas por usuário ativo/semana | **≥ 2** (~8/mês) | O antigo "> 3/semana" está **revogado** — excedia a cota do Solo (12/mês) |
| % do texto gerado que sobrevive à edição do advogado | > 70% ⚠️ calibrar no beta | Métrica norte de qualidade da IA (doc 06) |
| Zero prazo errado em produção | **0** (absoluto) | Condição do gate G3 — **nunca lançar cobrando com prazo errado** |
| Conversão beta → pago no lançamento | ≥ 60% (**6 de 10**) | Premissa dos ~14 assinantes do mês 15 (doc 00 §7) |
| NPS dos betas | ≥ 40 | Condição do G3. Com n=10, tratar como sinal qualitativo, não estatística |
| Retenção mês 2 | > 80% | |
| Churn mensal | < 5% | Alinhado à premissa do ano 1 |

**Nota honesta sobre n=10:** com 10 betas, nenhuma dessas métricas tem significância estatística. O beta solo valida **fluxo, confiança e disposição a pagar** — as taxas viram termômetro de verdade só depois do lançamento.

## Decisões de produto em aberto (sincronizadas com doc 00 §10)

1. Nome e marca (ago/2026)
2. Nicho de estreia: trabalhista (hipótese) vs. previdenciário — decidir com as 15 entrevistas (set/2026)
3. **Spike do editor (set/2026):** wrapper TipTap próprio × lib comunitária (ngx-tiptap) × plano C simplificado — decisão de maior risco técnico da Fase 1 (doc 00 §9.6)
4. Modo de geração de peça: wizard guiado vs. chat livre vs. ambos
5. Trial: 14 dias com cartão vs. 7 dias sem cartão (testar no beta)
6. Gateway do billing automático pós-lançamento: Stripe × Asaas/Pagar.me (Pix/boleto pesam) — set/2027
7. **Peticionamento:** perguntar nas entrevistas sobre A1 vs. A3 e disposição de autorizar custódia de certificado; agendar o spike MNI para a Fase 2
8. Migração: mapear (com beta testers ex-Astrea/MaisJurídico) exatamente o que cada concorrente deixa exportar ⚠️
