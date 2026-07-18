# 09 — Riscos e Compliance

> Alinhado ao doc `00-premissas.md`. Projeto **solo** (Ronny, 12,5h/semana). Fase 1 = meses 1–14 (ago/2026 – set/2027; 1 nicho, 2 peças, sem WhatsApp/financeiro); beta com **10 advogados** nos meses 13–14; lançamento pago no **mês 15 (out/2027)**; WhatsApp e financeiro na Fase 2 (2028).

## LGPD (crítico — dados jurídicos são sensíveis)

Processos envolvem dados pessoais e frequentemente **dados sensíveis** (saúde em ações previdenciárias, relações familiares, dados trabalhistas).

**Checklist mínimo antes do lançamento (out/2027):**
- [ ] Política de privacidade e termos de uso redigidos (ironicamente, contratar um advogado — ou recrutar um beta tester para trocar serviço por assinatura)
- [ ] Definir papéis: nós = **operadores**; o advogado = **controlador** dos dados dos clientes dele. Deixar isso explícito no contrato
- [ ] DPA (acordo de tratamento de dados) com todos os fornecedores (LLM APIs, provedor de embeddings, cloud; BSP de WhatsApp entra na Fase 2)
- [ ] Registro das operações de tratamento
- [ ] Processo de atendimento a direitos do titular (exclusão, portabilidade)
- [ ] **Export nativo funcionando desde o dia 1** (ver seção de portabilidade abaixo)
- [ ] Criptografia + logs de auditoria (já previsto na arquitetura)
- [ ] Dados hospedados no Brasil ou com salvaguardas de transferência internacional documentadas

**Nota do cenário solo:** o checklist não encolheu porque o time encolheu. Ele consome horas das mesmas 12,5h/semana — está coberto pelo bloco "billing + landing + LGPD mínimo" e pelo buffer (doc 00 §3). O que não dá para comprimir: revisão dos termos por advogado de verdade antes do beta.

### Portabilidade e saída de dados (promessa explícita, não favor)

O doc 02 aponta como fraqueza central do Astrea justamente **prender os dados do usuário** (sem API pública, sem export decente). Se cobramos isso deles, temos de cumprir — e a LGPD (direito de portabilidade do titular) nos obriga de qualquer forma na ponta dos dados pessoais.

Compromisso de produto, público desde o lançamento:
- **Export completo em um clique**: processos, clientes, prazos e documentos em formatos abertos (CSV/JSON + .docx/PDF das peças), sem pedir para "falar com o suporte" e sem carência
- Export disponível **inclusive após o cancelamento** (janela de 30–60 dias antes do expurgo definitivo)
- Publicar isso na página de preços/segurança — a facilidade de sair é argumento de entrada ("não te prendemos pelo cadeado, te prendemos pela qualidade")
- Risco assumido conscientemente: export fácil reduz custo de churn — a retenção tem que vir do valor (prazos + peças + estilo), não do sequestro de dados. É coerência com o posicionamento do doc 01. No cenário solo tem um bônus sombrio: se o projeto morrer (bus factor, ver abaixo), os usuários saem com os dados deles — é a coisa decente e reduz o dano reputacional/jurídico de um encerramento

## Ética profissional / OAB

- **Provimento 205/2021 do Conselho Federal da OAB** regula **publicidade e marketing** na advocacia — afeta o módulo de captação (Fase 3): nada de mercantilização, captação de clientela indevida ou promessa de resultado. O produto deve ajudar o advogado a ficar DENTRO das regras (ex.: validador de posts). Também limita **nosso** marketing: não podemos prometer resultado processual ("ganhe mais causas") nem induzir o advogado a violar o provimento
- **Sigilo profissional** (Estatuto da Advocacia e Código de Ética): reforça isolamento por tenant, não-treinamento sobre dados de clientes e cuidado com o que sai para APIs externas — é dever do advogado e, por tabela, requisito de quem o serve
- **IA generativa — o que existe hoje (jul/2026), com o grau de certeza marcado:**
  - ⚠️ **Não identificamos provimento nacional vinculante do Conselho Federal da OAB específico sobre IA generativa em vigor.** Existem recomendações e diretrizes seccionais (a OAB-SP publicou recomendações sobre uso de IA generativa na advocacia em 2024 — ⚠️ confirmar teor e vigência antes de citar em material público) e comissões/observatórios de IA no Conselho Federal. **Ação: verificação formal do estado normativo antes do beta (mês 13, ago/2027) e de novo antes do lançamento (mês 15, out/2027)** — se sair provimento, ele vira requisito de produto. O cronograma longo (14–18 meses) **aumenta** a chance de o quadro normativo mudar antes de lançarmos — mais um motivo para a re-verificação em dois pontos
  - Os princípios recorrentes nessas diretrizes convergem com o que já fazemos por decisão própria: **responsabilidade final do advogado, dever de revisão, sigilo (não inserir dados de cliente em ferramentas que treinem sobre eles), transparência no uso**. Nossa postura — revisão humana obrigatória + trilha de revisão auditável + rodapé de transparência + contrato de não-treinamento — nos coloca do lado certo de qualquer regulação plausível
  - **CNJ:** a Resolução 332/2020 disciplina IA no âmbito do Judiciário (⚠️ há atualização de 2025 sobre IA generativa nos tribunais — confirmar número e teor). Não nos vincula diretamente (vale para tribunais), mas sinaliza o padrão regulatório: transparência, supervisão humana, auditabilidade
  - **Marco legal da IA (PL 2338/2023):** aprovado no Senado em dez/2024, ⚠️ acompanhar tramitação na Câmara e status atual — pode criar obrigações de classificação de risco e transparência para nós como fornecedores. Idem: 14–18 meses é tempo de sobra para virar lei antes do nosso lançamento
  - Já há registros públicos de advogados **sancionados por protocolar jurisprudência inexistente gerada por IA** (⚠️ compilar casos concretos BR para conteúdo de marketing) — isso é simultaneamente o nosso maior risco (se a nossa IA causar) e o nosso melhor argumento de venda (nossa citação é verificada em base real; ChatGPT genérico não faz isso — ver riscos de mercado)

## ⚖️ Responsabilidade civil: quando a IA contribui para perder um caso

Cenário: a peça gerada contém tese equivocada ou omissão relevante, o advogado protocola sem revisar direito, o cliente perde e processa o advogado — que se volta contra nós em regresso. Esse risco não é eliminável; é **administrável em quatro camadas**:

| Camada | O quê | Status |
|---|---|---|
| **Produto** | Revisão obrigatória com checklist e trilha auditável (quem revisou, quando, % editado — doc 06); validação de citações e dados factuais; rodapé de transparência; motor de prazos determinístico. A trilha de auditoria é a nossa prova de que o output era rascunho e foi assumido pelo advogado | Já desenhado (doc 06) |
| **Contratual** | Termos de uso com (i) definição expressa: somos **ferramenta de apoio à redação**, não prestadores de serviço jurídico; (ii) revisão profissional como condição de uso; (iii) **limitação de responsabilidade** com teto (padrão SaaS: valores pagos nos últimos 12 meses) e exclusão de danos indiretos. ⚠️ Cláusulas limitativas podem ser mitigadas judicialmente (sobretudo se caracterizada relação de consumo) — a redação precisa de advogado de verdade, não de template | Redigir antes do beta (até jul/2027) |
| **Seguro** | **Seguro E&O / RC profissional para empresa de tecnologia** (erros e omissões). ⚠️ Cotar antes do lançamento pago (mês 15, out/2027); prêmio para SaaS pequeno costuma ser acessível, mas verificar cobertura específica para output de IA | Cotar até set/2027 |
| **Fronteira "ferramenta × serviço jurídico"** | Nunca assinar peça, nunca protocolar em nome do advogado (ver risco de peticionamento abaixo), nunca dar parecer definitivo ao cliente final (guardrail do atendente WhatsApp na Fase 2), e marketing disciplinado: vendemos "rascunho em minutos, você no controle", **nunca** "peça pronta para protocolar". Cruzar essa fronteira poderia ainda configurar exercício irregular da advocacia por sociedade não inscrita na OAB | Regra permanente |

## Riscos técnicos

| Risco | Impacto | Mitigação |
|---|---|---|
| Alucinação de jurisprudência/lei em peça | ALTÍSSIMO (advogado protocola erro) | Pipeline de citação verificada em base real + validador pós-geração + gates duros na suite de avaliação + revisão obrigatória — detalhado no doc 06 (estratégia anti-alucinação em 4 camadas). Nota solo: a suite de avaliação nasce menor (~20–30 pares, beta de 10 — doc 00 §2); complementar com peças públicas antes do lançamento |
| Erro no cálculo de prazo | ALTÍSSIMO | Cálculo determinístico em código (não LLM), testes extensivos, exibir memória de cálculo, disclaimer; data do LLM usada só como cross-check. **Este risco conversa diretamente com "sem revisor de código" abaixo** — por isso o motor de prazos é o bloco com a suite de testes mais pesada do projeto (doc 00 §3.1) e "zero prazo errado" é condição do gate G3 |
| **Wrapper Angular do TipTap (risco novo da stack)** | Alto | As bindings oficiais do TipTap são **React/Vue**; o core é JS agnóstico. O editor — **coração do produto** — vai depender de um wrapper Angular **nosso**: NodeViews custom (`[VERIFICAR]`, dados interpolados), sincronização com a change detection do Angular, inserção streaming. 105h estimadas para coisa nunca feita = categoria clássica de estouro (doc 00 §9.6). Mitigação: spike de 1–2 dias no mês 1–2 (avaliar ngx-tiptap/lib comunitária antes de escrever do zero); plano C: editor simplificado no beta e TipTap completo depois; buffer de 30% já precifica parte do estouro |
| **Menos referência pública de RAG/IA em Java (risco novo da stack)** | Baixo-médio | A maioria dos exemplos, libs e tutoriais de RAG é Python/TS; pgvector via JDBC é mais manual. Mitigação: o SDK oficial `com.anthropic:anthropic-java` cobre o grosso (structured outputs, streaming, tool use, cache); RAG no nosso caso é SQL + pgvector + chamadas de API, não framework-dependente; a suite de avaliação (doc 13) pega regressão de qualidade |
| Instabilidade das fontes (DataJud/DJEN/tribunais) | Alto | Multi-fonte com fallback para provedor pago; status page transparente |
| Custo de IA estourar | Médio | Cotas por plano com teto conhecido (R$ 38/132/~290 — doc 00), prompt caching (~25–35% de economia por peça) + Batches API (−50% na classificação), roteamento de modelo por tarefa, alertas de consumo por tenant (doc 06) |
| Banimento de número WhatsApp | **Baixo agora — risco de Fase 2 (2028)** | WhatsApp saiu da Fase 1 (doc 00), então não há exposição até bem depois do lançamento. Ao entrar na Fase 2: somente API oficial (nunca gateways não-oficiais), templates aprovados, opt-in dos destinatários; iniciar verificação de negócio na Meta com ~2 meses de antecedência |

## Riscos de mercado

### 🔴 O risco existencial: a janela competitiva (promovido — era um item de tabela, agora é O risco)

**14–18 meses até o beta é muito tempo.** A tese inteira depende de um quadrante vazio (doc 01): "IA que redige, integrada ao fluxo e aos autos, em preço de solo". Esse vazio não é defendido por patente nem por fosso — só pelo fato de ninguém tê-lo ocupado ainda. Um Jusbrasil, Astrea ou Advbox com time full-time fecha essa janela em menos tempo do que levaremos para chegar ao beta.

| | |
|---|---|
| **Probabilidade** | Média-alta e crescente — geração de peças é a feature mais óbvia do mercado em 2026, e todos os citados já têm ingestão de publicações ou dados processuais |
| **Impacto** | **Existencial.** Se um concorrente relevante lançar **geração de peças + ingestão + prazos integrados** antes do nosso beta, não somos mais "os primeiros no quadrante" — somos um clone atrasado, part-time, sem marca |
| **Mitigação real** | Não existe mitigação que nos faça mais rápidos. O que existe: (1) **Gate G5 (doc 00 §8), permanente**: monitoramento trimestral de lançamentos dos concorrentes; se a janela fechar antes do beta ⇒ reavaliar a tese IMEDIATAMENTE — cortar mais escopo e antecipar beta, ou encerrar cedo e barato; (2) nicho + profundidade + estilo por tenant tornam racional continuar mesmo mais lento **enquanto** a janela está aberta (doc 01, estratégia anti-cópia); (3) a decisão de aceitar esse risco foi tomada a frio, por escrito, antes de começar — se o Ronny não aceita a hipótese de trabalhar 15 meses e o mercado fechar antes, o momento de decidir é agora, não no mês 10 (doc 00 §9.5) |

### Os demais riscos de mercado

| Risco | Mitigação |
|---|---|
| **Jusbrasil entra em geração de peças** — a ameaça maior que o Astrea: maior detentor de dados processuais do país, tráfego orgânico gigante e já opera produtos de IA sobre a própria base | Não competir onde ele é imbatível (busca/dados), competir onde ele não está: o Jusbrasil é **destino de consulta**, não o sistema operacional diário do escritório. Nosso fosso é o fluxo intimação→prazo→peça integrado à rotina + nicho + aprendizado de estilo por tenant. Monitorar lançamentos trimestralmente (alimenta o G5); se entrarem em workflow, é gatilho direto do G5 |
| Astrea/Advbox lançam geração de peças | ⚠️ A defesa antiga ("velocidade — 2 devs sem legado") **está revogada**: somos 1 dev part-time, mais lentos que qualquer incumbente. O que resta: nicho e profundidade (o generalista não afia um nicho primeiro), aprendizado de estilo (fosso de dados que só cresce com uso), marca de categoria (IA-nativo vs. "botão de IA") e custo estrutural ~zero (sobrevivemos de um mercado que não paga uma squad deles). Se lançarem **antes do nosso beta** ⇒ G5 |
| **ChatGPT/Gemini genérico como substituto gratuito** — o advogado solo já usa ChatGPT grátis para rascunhar peça; para ele, o concorrente do redator pode ser "de graça" | Nosso valor não é "gerar texto": é a peça nascer **da intimação, com os dados reais do processo, prazo calculado e citação verificada** — no ChatGPT ele copia/cola contexto, digita fatos à mão e corre risco de julgado inventado (casos de sanção já existem — ver seção OAB). Dois ângulos de defesa: (i) produto = fluxo integrado que o chat genérico não replica; (ii) conteúdo educativo martelando alucinação + sigilo (colar autos no ChatGPT gratuito = dado do cliente virando dado de treino — problema ético e de LGPD). E preço ancorado no pacote gestão+prazos+IA, nunca em "IA" isolada |
| Player internacional entra no Brasil | Improvável no curto prazo (direito brasileiro é idiossincrático — nossa barreira natural) |
| Dependência de 1 fornecedor de LLM | Camada de abstração multi-modelo desde o início (doc 06) + suite de avaliação que valida troca de modelo com segurança |
| Advogados desconfiarem de IA | Educação via conteúdo + revisão humana + citação verificada como diferencial visível + casos de sucesso de betas |

## Riscos estratégicos e de produto

| Risco | Impacto | Mitigação |
|---|---|---|
| **Concentração em 1 nicho na Fase 1 (hipótese trabalhista errada)** — 14 meses de dev apostados em templates, prompts e suite de um nicho que a validação pode derrubar. Solo, a aposta ficou mais cara: são 14 meses, não 8 | Alto | (1) A hipótese é testada **antes** do código pesado: as 15 entrevistas de validação (ago–set/2026) decidem trabalhista × previdenciário (decisão #2 do doc 00 §10) — templates do nicho só entram depois disso; (2) arquitetura nicho-agnóstica: templates, prompts e suite são **dados versionados**, não código — trocar/adicionar nicho custa conteúdo, não refactor; (3) sinais de alerta no beta (meses 13–14): ativação < 60%, sobrevivência à edição muito baixa, peças do nicho pouco usadas ⇒ antecipar o 2º nicho (previsto para os meses 17–19) ou trocar; (4) o gate G4 (jan/2028 — doc 00 §8) já contempla pivô de nicho como saída formal |
| **Peticionamento eletrônico (protocolar em nome do advogado)** — não está no roadmap atual (docs 00 e 04 não o preveem), mas será pedido recorrente de usuários ("já que gerou a peça, protocola") | Alto se entrar sem critério | Decisão registrada: **fora de escopo nas Fases 1–3.** Protocolar exige assinatura com certificado **ICP-Brasil do advogado** — custodiar certificado/chave privada de terceiro é risco de segurança e jurídico inaceitável para nós; além disso, protocolo errado ou indisponibilidade nossa viraria **perda de prazo por nossa culpa** (responsabilidade direta, sem a camada de revisão humana que nos protege), e a fragmentação PJe/eproc/e-SAJ/Projudi multiplica a superfície de erro. Se um dia entrar: assinatura sempre na máquina do advogado (agente local/desktop, certificado nunca sai dele), confirmação explícita por protocolo, termos específicos — e seguro E&O revisto antes. ⚠️ Reavaliar formalmente só se a demanda dos usuários provar disposição a pagar pelo risco |

## 🧍 Riscos do projeto (solo)

A seção antiga ("vocês dois") está morta junto com a sociedade. **Não há "vocês dois" — há o Ronny, 12,5h/semana, e mais ninguém.** Acordo de sócios, vesting, regra de saída: tudo revogado (doc 00 §0.2), removido deste doc. O que entra no lugar é pior de administrar:

| Risco | Impacto | Mitigação (parcial — nenhuma resolve de verdade) |
|---|---|---|
| **Bus factor = 1.** Doença, acidente, pico no emprego, questão familiar: o projeto **para** — e não há quem assuma o código. Não existe trilho paralelo, não existe backlog "não-bloqueado" para um segundo par de mãos. 2 semanas parado = 2 semanas de atraso, sempre, sem exceção | **Alto** | Buffer de 30% já assume perdas; o cenário de 18 meses é o realista, não o catastrófico. Documentar tudo (ADRs, README técnico, runbooks) — não para "alguém assumir" (não há alguém), mas para o **próprio Ronny** retomar barato depois de uma pausa. E honestidade com os betas/clientes: SLA compatível com um humano só |
| **Sem revisor de código.** Bug que o autor não enxerga só aparece em produção — e os bugs mais caros deste produto são exatamente os que um segundo par de olhos pegaria: **isolamento multi-tenant e cálculo de prazo** (o risco ALTÍSSIMO da tabela técnica acima). Correção tardia custa 3–5× mais | **Alto** | Testes pesados no motor de prazos e RLS (já orçados como os blocos mais caros — doc 00 §3.1); revisão assistida por IA (mitigação real, mas não é par); pentest externo antes de cobrar (doc 05) continua obrigatório; "zero prazo errado" é condição dura do G3. Nada disso equivale a um revisor — por isso o buffer subiu de 20% para 30% |
| **Sem quem cubra.** Férias, doença, entrega grande no emprego: antes o outro sócio absorvia; agora todo imprevisto vira atraso direto. E o suporte do beta/lançamento não tem plantão B | Alto | Aceitar e planejar: cronograma já usa média honesta de 12,5h (não pico); beta reduzido a 10 advogados exatamente para caber o suporte; janela de lançamento com folga (out/2027–jan/2028) |
| **Burnout.** 12,5h/semana, TODA semana, por 14–18 meses, em cima de um emprego, **sem receita no meio do caminho** (mês 12 = MRR R$ 0) e sem sócio para dividir frustração ou comemorar marco | **Alto** | Ritmo sustentável > ritmo heroico: o plano já assume 12,5h, não 20 — proibido planejar sprint heroico; marcos mensais pequenos e demonstráveis (dopamina de progresso); gates escritos a frio (G1, G2, G4) para a decisão de parar não depender do estado emocional do mês; e a pergunta honesta do doc 00 §4: se 15 meses sem receita for inaceitável, decidir **agora** |
| **GTM e dev competem pelas mesmas horas.** Entrevistas, conteúdo, lista de espera, recrutamento de beta, suporte — tudo sai das mesmas 12,5h que constroem o produto. Os 12–13 adds/mês do ano 1 (doc 00 §7) dependem de marketing feito pela mesma pessoa que desenvolve e dá suporte — é a premissa mais frágil da projeção de MRR | Médio-alto | Concentrar GTM em janelas (validação nos meses 1–2, recrutamento do beta no mês 12+, conteúdo em lote); metas de funil modestas e explícitas (lista de 150, não 200); aceitar que toda hora de GTM é hora a menos de dev — está precificado no intervalo 14–18 meses |

### ⚠️ Conflito de interesse com o vínculo público do Ronny (risco ELEVADO no cenário solo)

Este risco **piorou** com a saída do sócio. Antes, havia uma pessoa sem vínculo público que poderia ser o rosto formal da empresa — sócio-administrador, titular do CNPJ, assinante de contratos. **Agora não há.** Se houver restrição, não existe mais a saída "o outro sócio assume formalmente".

O que sabemos e o que não sabemos, separado com honestidade:

- ⚠️ **Não sabemos (e não vamos chutar) qual é a regra aplicável ao caso concreto.** As restrições a atividade empresarial de quem tem vínculo com o setor público **variam conforme a natureza do vínculo** (estatutário × celetista × outras formas), o ente e o regime jurídico específico — em regimes estatutários é comum haver vedação a exercer **administração/gerência** de empresa, com participação como sócio cotista tratada de forma diferente, mas **isso precisa ser verificado no estatuto/regime concreto do Ronny, não assumido a partir de regra geral**.
- **O que precisa ser verificado, com quem, e quando:**
  1. Ler o estatuto/regime jurídico do vínculo do Ronny (o documento, não resumo de internet) — **ago–set/2026**;
  2. Consultar **advogado de direito administrativo** e, se existir canal, a área de gestão de pessoas/comissão de ética do próprio órgão — **antes de abrir o CNPJ (decisão #4 do doc 00 §10, out/2026)**;
  3. Mapear as alternativas societárias possíveis **conforme o que a consulta disser** (e não antes dela): que papel formal o Ronny pode ocupar, o que exigiria terceiro de confiança, o que é simplesmente vedado.
- **Por que é bloqueante e não "resolvível depois":** faturar assinaturas exige CNPJ; CNPJ exige definir quem administra; e uma infração funcional descoberta no mês 20 — com clientes pagando — é um desfecho muito pior do que um "não pode" descoberto no mês 3. **Se a consulta disser que não há forma lícita viável, isso é gate de parada do projeto, no mesmo nível do G4.**
- Enquanto a verificação não acontece: nada de faturamento, nada de CNPJ, nada de contrato assinado — beta gratuito e validação não geram receita e ⚠️ presumimos que não configuram atividade empresarial, mas **incluir essa pergunta na mesma consulta jurídica**.

### O que morreu junto com a sociedade (registro explícito)

- ❌ Acordo de sócios, vesting 4 anos, cliff, regra de saída/desempate — **não existem mais** (doc 00 §0.2). Não redigir, não pagar advogado por isso.
- ❌ Risco "sociedade entre familiares" — extinto por falta de sociedade.
- ⚠️ Se um dia entrar **novo** sócio ou colaborador relevante, o tema volta inteiro (aí sim: acordo escrito, vesting, papéis) — mas isso é decisão futura, não pendência atual.
