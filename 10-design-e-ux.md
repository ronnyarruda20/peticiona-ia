# 10 — Design e UX

> **Alinhado ao doc `00-premissas.md` (reescrito jul/2026): projeto solo, stack Angular, cortes #1 (dark mode), #3 (2 tipos de peça) e #4 (frase-resumo IA) já aplicados aqui.** Tudo neste doc deriva da tese do doc 01 ("o sistema entrega o dia pronto, o advogado só revisa") e das obrigações éticas do doc 09 (revisão humana obrigatória, transparência de IA). Se um componente não serve a essas duas coisas, ele não precisa existir. Não há segundo par de olhos de design — como validar UX sozinho está no §9.

---

## 1. 🧭 Princípios de design (derivados da tese, não de gosto)

A tese do produto tem consequências diretas de UX. Cada princípio abaixo é uma consequência, não uma preferência estética:

| # | Princípio | Por que (consequência da tese) |
|---|---|---|
| 1 | **O sistema fala primeiro.** Toda tela abre com o que o sistema JÁ FEZ, nunca com um formulário vazio ou menu | Se o advogado precisa procurar, viramos "dashboard passivo" — exatamente o que o Astrea já é |
| 2 | **Toda saída de IA é um rascunho até um humano dizer o contrário.** Estado visual explícito: rascunho ≠ revisado ≠ aprovado | Requisito ético OAB (doc 09) + proteção jurídica nossa. Não é enfeite |
| 3 | **Prazo é sagrado.** Informação de prazo nunca fica a mais de 1 clique de distância, nunca é ambígua, sempre mostra a memória de cálculo | O maior medo do ICP é perder prazo (doc 01). Um prazo escondido atrás de 3 cliques é um churn |
| 4 | **Revisar deve ser mais rápido que fazer.** O fluxo de revisão (ler → editar → aprovar) é otimizado ao milímetro; se revisar uma peça leva quase o tempo de escrevê-la, o produto não tem razão de existir | A economia de tempo É o produto |
| 5 | **Densidade sem sufoco.** Advogado lê muito texto e quer ver muito de uma vez — mas com hierarquia clara de urgência | ICP vive de texto; UI "arejada" de startup B2C esconde informação que ele precisa |
| 6 | **Nunca mentir sobre o que a IA sabe.** Incerteza é exibida, não escondida. "[VERIFICAR]" visível vale mais que uma alucinação bonita | Alucinação protocolada = risco ALTÍSSIMO (doc 09) e morte da confiança |
| 7 | **Zero implantação.** Cada tela do onboarding assume que o usuário nunca viu um vídeo, nunca falou com a gente e está no celular entre duas audiências | "Onboarding em minutos" é diferencial declarado (doc 01) |

**Anti-princípios** (o que NÃO somos):

- ❌ Chat genérico com a IA como interface principal. A IA age no fluxo; o chat é acessório (fase 1.5).
- ❌ Gamificação, confete, mascote. Público conservador, assunto sério.
- ❌ Esconder complexidade jurídica atrás de "simplicidade". O advogado quer ver o inteiro teor da intimação, não um resumo fofo — o resumo é atalho, o original está sempre a 1 clique.

---

## 2. 🖼️ Fluxos de tela centrais (wireframes)

### (a) Onboarding — "digitou OAB → importou → IA classificou" em <10 min

Fluxo em 4 passos. Barra de progresso sempre visível. Cada passo tem UMA ação primária.

```
Passo 1: Cadastro                       Passo 2: OAB
┌──────────────────────────────┐        ┌──────────────────────────────┐
│  [logo]                      │        │  ●━━●━━○━━○                  │
│  Crie sua conta              │        │  Importar seus processos     │
│                              │        │                              │
│  [Continuar com Google]      │        │  Nº OAB   [ 12345      ]     │
│  ──────── ou ────────        │        │  UF       [ SC ▾       ]     │
│  E-mail   [           ]      │        │                              │
│  Senha    [           ]      │        │  Vamos buscar seus processos │
│                              │        │  nos tribunais (DataJud/CNJ).│
│  [Criar conta]               │        │  Leva 1–3 minutos.           │
│                              │        │                              │
│  Sem cartão de crédito.      │        │  [Buscar meus processos]     │
└──────────────────────────────┘        └──────────────────────────────┘

Passo 3: Importação (assíncrona,        Passo 4: O "uau"
mas o usuário VÊ progresso real)        ┌──────────────────────────────┐
┌──────────────────────────────┐        │  ●━━●━━●━━●                  │
│  ●━━●━━●━━○                  │        │  Pronto, Dr(a). João ✓       │
│  Encontramos 87 processos    │        │                              │
│                              │        │  87 processos importados     │
│  ▸ TRT-12 ........ 54 ✓      │        │  ├─ 61 trabalhistas          │
│  ▸ TJSC ......... 28 ✓      │        │  ├─ 19 cíveis                │
│  ▸ TRF-4 ........  5 ⣷      │        │  └─  7 outros                │
│                              │        │                              │
│  [████████████░░░]  82%      │        │  ⚠ 3 intimações com prazo    │
│                              │        │    correndo AGORA            │
│  Enquanto isso: a IA já está │        │                              │
│  classificando o que chegou. │        │  [Ver meu dia →]             │
└──────────────────────────────┘        └──────────────────────────────┘
```

Decisões:
- **Passo 3 nunca bloqueia.** Se a importação demorar >3 min, botão "Continuar enquanto importamos" leva ao dashboard com skeleton + banner de progresso. O tempo-até-uau não pode ficar refém do DataJud.
- **Passo 4 termina em prazo, não em tour.** O CTA final leva direto ao "Seu dia" com as 3 intimações urgentes no topo. Nada de tour de 12 passos.
- Métrica instrumentada desde o dia 1: `tempo_cadastro_ate_primeiro_prazo_visto` (meta < 10 min, doc 04).

### (b) Dashboard "Seu dia" — o coração da tese

Ver seção 3, que é dedicada a ele.

### (c) Central de intimações e prazos

```
┌────────────────────────────────────────────────────────────────────┐
│ Intimações                    [Todas ▾] [Pendentes ▾] [🔍 buscar]  │
├────────────────────────────────────────────────────────────────────┤
│ ⛔ VENCE EM 2 DIAS                                                 │
│ ┌────────────────────────────────────────────────────────────────┐ │
│ │ Contestação · 0001234-56.2026.5.12.0001 · Maria S. × Acme LTDA │ │
│ │ Publicada 01/07 · Prazo 15 dias úteis · ⏰ Vence 18/07 (sex)   │ │
│ │ 🤖 classificada como INTIMAÇÃO P/ CONTESTAR (conf. alta)       │ │
│ │ [Ver intimação] [Memória de cálculo] [✍ Rascunhar contestação] │ │
│ └────────────────────────────────────────────────────────────────┘ │
│ 🟡 VENCE EM 9 DIAS                                                 │
│ ┌────────────────────────────────────────────────────────────────┐ │
│ │ Réplica · 0009876-11.2026.5.12.0003 · ...                      │ │
│ │ [Ver intimação] [Memória de cálculo] [Abrir processo]          │ │
│ └────────────────────────────────────────────────────────────────┘ │
│ ⚪ SEM PRAZO / CIÊNCIA                                             │
│ │ Despacho de mero expediente · ... [arquivar]                    │
│ ├────────────────────────────────────────────────────────────────┤ │
│ │ ❓ NÃO CONSEGUI CLASSIFICAR (2)          [revisar manualmente]  │ │
└────────────────────────────────────────────────────────────────────┘
```

Decisões:
- Ordenação **sempre por data-limite**, nunca por data de publicação. O que vence primeiro aparece primeiro.
- Semáforo de urgência: ⛔ ≤3 dias úteis · 🟡 4–10 · ⚪ sem prazo. Cores + ícone + texto (nunca só cor — acessibilidade).
- A fila "não consegui classificar" é **explícita e visível** — melhor admitir do que classificar errado em silêncio (princípio 6).
- "Memória de cálculo" abre popover: publicação → início da contagem → dias úteis descontados → feriados forenses aplicados → data-limite. Cada linha com a fonte (tabela de feriados do TRT-12, art. do CPC/CLT). Isso vende confiança e é nossa defesa.
- **Botão de rascunho a 1 clique só existe para os tipos que o redator cobre na Fase 1: contestação + petições simples (corte #3, doc 00).** Todos os OUTROS prazos aparecem igual — classificados, calculados, com memória — mas o CTA é [Abrir processo], não [Rascunhar]. **Nunca esconder um prazo porque não sabemos redigir a peça** (princípio 3 vence). Quando réplica e recurso inominado entrarem (pós-lançamento), o botão aparece sem mudança de layout.

### (d) Redator de peças — IA inline no editor (TipTap/ProseMirror com wrapper Angular)

```
┌──────────────────────────────────────────────────────────────────────┐
│ ← Contestação · Proc. 0001234-56.2026.5.12.0001    [🟠 RASCUNHO IA]  │
├───────────────────────────────────────────────┬──────────────────────┤
│  EXCELENTÍSSIMO SENHOR DOUTOR JUIZ DA 1ª      │ CONTEXTO             │
│  VARA DO TRABALHO DE FLORIANÓPOLIS/SC         │ ▸ Intimação (íntegra)│
│                                               │ ▸ Peça inicial       │
│  ACME LTDA, já qualificada nos autos...       │ ▸ Últimas moviment.  │
│                                               │                      │
│  I — PRELIMINARES                             │ ESTRUTURA            │
│  1. Da inépcia da inicial █████████ ⣷         │ ✓ Endereçamento      │
│     (gerando…)                                │ ✓ Qualificação       │
│                                               │ ⣷ Preliminares       │
│  II — MÉRITO                                  │ ○ Mérito             │
│  ...                                          │ ○ Pedidos            │
│                                               │                      │
│  ┌─────────────────────────────────────┐      │ ⚠ 2 pontos p/ revisar│
│  │ [SUGESTÃO DE TESE — VERIFICAR       │      │ · Jurisprudência não │
│  │  JURISPRUDÊNCIA] Súmula sobre horas │      │   verificada (§II.3) │
│  │  in itinere pós-reforma…            │      │ · Valor da causa     │
│  └─────────────────────────────────────┘      │   presumido (§III)   │
│                                               │                      │
│  ── seleção de texto ──                       │ [Exportar .docx]     │
│  [✦ Reescrever] [Mais formal] [Resumir]       │ [✓ Marcar revisado]  │
└───────────────────────────────────────────────┴──────────────────────┘
```

Decisões:
- **Geração por seção, em streaming** — a estrutura (endereçamento, preliminares, mérito, pedidos) aparece como checklist no painel lateral e vai sendo preenchida. O advogado pode começar a revisar o endereçamento enquanto o mérito gera.
- Blocos incertos são **nodes customizados do schema ProseMirror** com fundo âmbar e borda: `[SUGESTÃO DE TESE — VERIFICAR JURISPRUDÊNCIA]`, `[DADO PRESUMIDO — CONFIRMAR]`. Não são texto simples: o editor sabe contá-los, e o export avisa se algum sobrar. **É por isso que ProseMirror fica mesmo com a troca de stack** — o sistema de confiança do §4 depende de o documento ser um schema estruturado que a gente controla, não um HTML de contentEditable.
- Dados do processo (nomes, números, datas) são **interpolados do banco** e visualmente marcados (sublinhado pontilhado) — também um node customizado, com o `id` da origem como atributo. Clique mostra a fonte. Nunca gerados pelo LLM (doc 06).
- **Wrapper Angular:** o core do TipTap/ProseMirror é JS agnóstico de framework; as bindings oficiais são React/Vue. O spike de set/2026 (decisão #3 do doc 00) decide entre wrapper próprio × lib comunitária (ex.: `ngx-tiptap`) × plano C (editor simplificado no beta). Pontos que o spike TEM que provar antes da decisão: (1) NodeViews custom renderizando componentes Angular dentro do documento (⚠️ a ponte ProseMirror↔Angular — criação dinâmica de componentes e sincronização com a change detection — é o risco técnico central, ninguém a resolveu "de fábrica"); (2) inserção de texto em streaming via transactions sem quebrar cursor/undo; (3) serialização estável do JSON do documento (o banco guarda o doc ProseMirror como JSONB, doc 11).
- IA inline por seleção: menu flutuante com 3–4 ações fixas + campo livre. Ações fixas primeiro — advogado com pressa não quer redigir prompt.
- Painel de contexto mostra O QUE a IA usou para gerar (intimação, inicial, movimentações). Transparência = confiança.

### (e) Revisão e export

```
┌──────────────────────────────────────────────────────────┐
│  Revisão final — Contestação                             │
├──────────────────────────────────────────────────────────┤
│  Checklist antes de exportar:                            │
│  ✓ Nenhum bloco [VERIFICAR] pendente                     │
│  ✗ 1 bloco [DADO PRESUMIDO] no §III      [ir até ele →]  │
│  ✓ Partes e nº do processo conferem com os autos         │
│  ✓ Prazo da peça: vence 18/07 (2 dias úteis)             │
│                                                          │
│  ⚠ O rodapé "gerado com auxílio de IA" acompanha apenas  │
│    RASCUNHOS. Ao marcar como revisado, ele é removido    │
│    e você assume a autoria (Prov. OAB / doc 09).         │
│                                                          │
│  [Voltar e corrigir]     [Marcar revisado e exportar ▾]  │
│                           └ .docx (padrão)               │
└──────────────────────────────────────────────────────────┘
```

Decisões:
- **Export com bloco `[VERIFICAR]` pendente não é proibido, mas exige confirmação explícita** ("Exportar mesmo assim") e mantém o rodapé de IA. Nunca tratamos o advogado como criança, mas deixamos rastro.
- Marcar como revisado é ato explícito, logado (quem, quando — vai para o log de auditoria do doc 11). É a transição rascunho → documento do advogado.

---

## 3. ☀️ "Seu dia" — anatomia do daily brief

É a tela inicial de todo login. É a diferença entre "sistema que trabalha" e "dashboard passivo". Regra de ouro: **o advogado deve saber, em 5 segundos, se hoje é um dia calmo ou um incêndio.**

> **Corte #4 (doc 00): a frase-resumo redigida por IA saiu.** O topo do brief agora é uma **linha de status estruturada** — contadores vindos de query, rótulos fixos, zero LLM. Perde o charme do "bom dia" redigido; não perde nenhuma informação. A versão redigida por IA pode voltar pós-lançamento se os betas sentirem falta — o layout já reserva o lugar.

```
┌──────────────────────────────────────────────────────────────────────┐
│  Bom dia, Dr. João — quinta, 16 de julho          [🔍] [🔔 3] [👤]   │
│                                                                      │
│  ▍2 prazos críticos  ▍3 intimações novas  ▍1 audiência amanhã       │
│  ▍2 rascunhos aguardando sua revisão                                 │
├──────────────────────────────────────────────────────────────────────┤
│  🔥 PRECISA DE VOCÊ HOJE (2)                                         │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │ ⛔ Contestação vence AMANHÃ · Maria S. × Acme                  │  │
│  │    Rascunho pronto (IA) — falta sua revisão   [Revisar agora →]│  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │ ⛔ Réplica vence em 2 dias · José P. × Beta SA                 │  │
│  │    Sem rascunho automático p/ este tipo    [Abrir processo →] │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  📬 CHEGOU HOJE (3)                          📅 AGENDA               │
│  · Intimação p/ contestar (15du)  [ver]      · Amanhã 14h Audiência  │
│  · Sentença publicada — proc 0033 [ver]        1ª VT Fpolis [🗺]     │
│  · Despacho mero expediente       [ok]       · Seg: vence réplica    │
│                                              · [semana completa →]   │
│  ✅ O QUE O SISTEMA FEZ POR VOCÊ (madrugada)                         │
│  · Li 14 publicações do DJEN · 3 viraram tarefas acima               │
│  · Calculei 3 prazos (memórias de cálculo disponíveis)               │
│  · Rascunhei 1 contestação (aguardando revisão)                      │
│  · 11 publicações eram ciência/sem ação — arquivadas [conferir]      │
└──────────────────────────────────────────────────────────────────────┘
```

Por que cada bloco existe:

| Bloco | Função na tese |
|---|---|
| **Linha de status** (contadores de query + rótulos fixos — **sem IA**, corte #4) | Responde "dia calmo ou incêndio?" em 5 segundos. Números nunca inventados porque nunca passam por um modelo; custo zero por login |
| **Precisa de você hoje** | A fila de trabalho REAL. Cada card termina em verbo: Revisar, Gerar, Abrir. Nunca é só informação — é sempre ação. Prazos de tipos que o redator ainda não cobre (Fase 1: só contestação + petições simples) aparecem AQUI do mesmo jeito, com CTA [Abrir processo] |
| **Chegou hoje** | Novidade sem urgência inflada. O que tem prazo já subiu para o bloco de cima |
| **Agenda** | Contexto temporal mínimo; a agenda completa é outra tela |
| **O que o sistema fez por você** | **O bloco mais importante para retenção.** Torna o trabalho invisível visível — justifica a mensalidade toda manhã. Também é honestidade: mostra o que foi arquivado automaticamente, com link para conferir (auditabilidade). As linhas são **templates fixos preenchidos com números de query** ("Li {n} publicações") — parecem redigidas, não são |

Estados do "Seu dia":
- **Dia calmo:** "Nenhum prazo crítico hoje 🎉 — 2 intimações de ciência arquivadas." Celebrar o vazio é ok aqui (única exceção ao anti-princípio de confete: uma linha de texto, sem animação).
- **Primeiro login pós-onboarding:** o bloco "o que o sistema fez" mostra a importação ("importei 87 processos, encontrei 3 prazos correndo") — o uau do passo 4 continua aqui.
- **Ingestão atrasada/fora do ar:** banner âmbar no topo: "Última leitura do DJEN: ontem 23h. Estamos reprocessando." Nunca fingir que está tudo em dia (ver §6).

---

## 4. 🤝 Comunicando confiança e incerteza da IA

Requisito do doc 09, não estética. Sistema de 3 estados + 2 marcadores, usado de forma idêntica no produto inteiro:

### Estados de documento (badge no topo + cor da borda)

| Estado | Badge | Significado | Quem transiciona |
|---|---|---|---|
| Rascunho IA | 🟠 `RASCUNHO IA` | Gerado por IA, não revisado. Rodapé automático "gerado com auxílio de IA — revisão profissional obrigatória" | Sistema cria |
| Em revisão | 🔵 `EM REVISÃO` | Advogado abriu e editou | Automático ao editar |
| Revisado | 🟢 `REVISADO` | Advogado declarou revisão. Rodapé de IA removido. Logado em auditoria | **Só o humano**, ato explícito |

### Marcadores inline (dentro do texto)

| Marcador | Visual | Quando |
|---|---|---|
| `[SUGESTÃO DE TESE — VERIFICAR JURISPRUDÊNCIA]` | bloco âmbar, borda tracejada | IA sugere tese/julgado que NÃO veio de base verificada (doc 06: nunca citar julgado direto do modelo) |
| `[DADO PRESUMIDO — CONFIRMAR]` | idem | IA precisou assumir fato que não estava nos autos |
| Dado interpolado do banco | sublinhado pontilhado discreto | Nome, nº de processo, data vindos do banco — clique mostra a fonte |

Os três marcadores são **nodes do schema ProseMirror** (ver §2d), não formatação: o checklist de export conta os pendentes, e o snapshot `conteudo_gerado` (doc 11) os preserva para a métrica de sobrevivência à edição.

### Confiança da classificação de intimações

- Classificação com confiança alta: badge 🤖 discreto + tipo do ato. Sem porcentagem — advogado não sabe o que fazer com "87%".
- Confiança baixa (abaixo do corte definido na suite de avaliação, doc 13): **não classifica**. Vai para a fila "não consegui classificar" com o texto bruto. Errar admitindo > acertar às vezes.
- **Prazo NUNCA carrega badge de IA.** O cálculo é determinístico (doc 06); a UI mostra "calculado pela tabela oficial de feriados do TRT-12" com memória de cálculo. Misturar visualmente "IA leu a intimação" com "sistema calculou o prazo" destruiria a confiança na parte determinística — são selos diferentes: 🤖 para leitura, 🧮 para cálculo.

---

## 5. 🎨 Design system (mundo Angular)

> **shadcn/ui e Tailwind foram revogados junto com a stack React (doc 00 §0.2).** A seção abaixo decide a base de componentes para Angular. Critérios, em ordem: (1) risco de manutenção para UM dev a 12,5h/sem por 14–18 meses; (2) acessibilidade — o §Acessibilidade exige AA/AAA e navegação por teclado, e não temos horas para implementar ARIA à mão; (3) densidade de informação — o produto é listas e texto corrido, não formulários espaçados de B2C; (4) custo de customização para chegar na estética sóbria do §7.

### Avaliação: Angular Material × PrimeNG × Spartan UI

| | **Angular Material + CDK** | **PrimeNG** | **Spartan UI (porte do shadcn)** |
|---|---|---|---|
| Manutenção/risco | Mantido pelo time do Angular, versão acompanha o framework. Risco mínimo de abandono no horizonte do projeto | Empresa terceira (PrimeTek); histórico de breaking changes agressivos entre majors ⚠️ | Projeto comunitário, ⚠️ pré-1.0 na última verificação — confirmar maturidade antes de qualquer aposta |
| Acessibilidade | A melhor do ecossistema; o CDK (`a11y`: FocusTrap, LiveAnnouncer, roving tabindex) resolve o que o §Acessibilidade exige | Desigual entre componentes historicamente ⚠️ | Herda a filosofia headless (boa base), mas cobertura a verificar ⚠️ |
| Densidade | Escala de densidade nativa do tema (compacto sem hack). Componentes de dados básicos (mat-table é simples) | Ponto forte: p-table com sort/filtro/virtual scroll pronto — o melhor catálogo para CRUD pesado | Você estiliza tudo — densidade é a que você fizer |
| Custo de customização | Real: tirar a "cara de Google" custa horas de tema. Mitigado: theming por tokens (M3) e a estética que queremos é sóbria, não exótica ⚠️ detalhes da API de theming mudam entre versões — validar na versão adotada | Theming reformulado para design tokens nas versões recentes ⚠️ conhecimento superficial do estado atual — verificar antes de adotar | Máximo controle e máxima conta: componentes copiados para o repo viram manutenção SUA — e **dependem de Tailwind, que foi revogado no doc 00**. Adotá-lo reintroduziria a dependência pela porta dos fundos |

### ✅ Recomendação: **Angular Material + CDK**, com tema próprio

Racional:
1. **O produto precisa de poucos widgets prontos e muitos componentes PRÓPRIOS.** Semáforo de prazo, popover de memória de cálculo, badges de estado de IA, filas com navegação j/k, chrome do editor — nada disso existe em catálogo nenhum. O CDK (overlay, portal, a11y, virtual scroll) é a melhor base *headless* do ecossistema Angular para construí-los sem reinventar foco, sobreposição e teclado.
2. **Acessibilidade é requisito, não polish** (§Acessibilidade, doc 09). Com Material/CDK ela vem de fábrica; com qualquer alternativa ela vira horas nossas.
3. **Risco de manutenção ≈ zero** — para um projeto solo de 14–18 meses, uma dependência central que quebra num major custa uma semana que não existe.
4. O que perdemos do PrimeNG (p-table rica) pesa pouco: nossas telas centrais são **listas densas e cards com hierarquia**, não grids de planilha. Filtro e ordenação das filas são simples e escritos à mão de qualquer forma.

Custo assumido de olhos abertos: **neutralizar a estética Material** (tipografia própria, raios menores, paleta sóbria, sem ripple exagerado) — trabalho de tema feito UMA vez, no mês 1–2. **Plano B registrado:** se no G1 (mês 7) o custo de compor listas/filtros com Material+CDK estiver estourando, reavaliar PrimeNG para as telas de dados — decisão barata de tomar cedo, cara de tomar no mês 10.

Regra que sobrevive à troca de lib: **não inventar componente que o Material/CDK já tem.**

### Tokens

CSS custom properties próprias, semânticas, por cima do tema Material (o tema consome os tokens, não o contrário — se um dia a lib mudar, os tokens ficam):

```css
/* Cores semânticas — nomeadas por FUNÇÃO, nunca por cor */
--brand:            /* azul-petróleo profundo — sobriedade sem ser o azul-banco de sempre */
--surface / --surface-raised / --border
--text-primary / --text-secondary / --text-muted

--deadline-critical:  /* vermelho — SÓ para prazo ≤3du e erro. Se tudo é vermelho, nada é */
--deadline-warning:   /* âmbar — prazo 4–10du e blocos [VERIFICAR] */
--deadline-ok:        /* verde — revisado, em dia */
--ai-accent:          /* violeta discreto — TUDO que a IA tocou usa este e só este */

/* Espaçamento: escala 4px. Densidade padrão "compacta":
   linha de tabela 40px, não os 56px de SaaS B2C */
```

Regra dura: **violeta = IA, vermelho = prazo/erro, e nada mais usa essas cores.** A consistência do código de cores É o sistema de confiança da seção 4.

### Tipografia

| Uso | Fonte | Racional |
|---|---|---|
| UI (labels, nav, cards) | **Inter** (ou similar humanista) | Legibilidade em densidade alta, números tabulares p/ datas e prazos |
| Peças no editor | **Serifada** (Source Serif 4 / Lora) | A peça tem que PARECER peça. Advogado lê 20 páginas no editor; serifada em 16–18px/1.6 |
| Nº de processo, OAB | mono ou tabular | Conferência dígito a dígito |

Corpo mínimo 14px na UI, 16px+ em texto corrido. ICP inclui 40+ anos lendo o dia inteiro.

### Densidade de informação

- Tabelas e listas: modo compacto por padrão, com toggle "confortável".
- Truncar ementa/resumo em 2 linhas com expand — nunca esconder o inteiro teor a mais de 1 clique.
- Colunas de data SEMPRE com dia da semana ("18/07 · sex") — advogado pensa em dias úteis.

### Acessibilidade

- Contraste AA mínimo em tudo; AAA nos prazos e estados de IA (são a informação crítica).
- Urgência nunca só por cor: cor + ícone + texto (⛔ "vence em 2 dias").
- Navegação por teclado no editor e nas filas (j/k para navegar intimações, E para abrir — power users vivem nessas telas).
- Focus visível sempre; targets ≥44px no mobile (PWA — advogado revisa prazo no corredor do fórum).

### Dark mode — ❌ CORTADO da Fase 1 (corte #1, doc 00: −15h)

- **Não entra no lançamento.** A justificativa antiga ("Tailwind torna barato") morreu junto com o Tailwind — e mesmo barato, é charme, não função: nenhum item do fluxo do aha depende dele, e a 12,5h/sem cada hora de polish é uma hora a menos de motor de prazos.
- **O que fica desde o dia 1 (custo ~zero):** tokens semânticos (acima) e proibição de cor hardcoded em componente. É isso que torna o dark mode pós-lançamento um trabalho de tema, não uma reforma.
- Quando voltar (backlog pós-lançamento): reduzir saturação dos semáforos (vermelho puro em fundo escuro vibra) e manter o editor de peças em superfície levemente mais clara que o app (papel).

---

## 6. ⏳ Padrões de estado

### Geração em streaming (redator)

- Esqueleto da estrutura da peça aparece **inteiro e imediatamente** (títulos das seções), texto preenche seção a seção — percepção de velocidade e de método.
- Cursor de geração ⣷ na seção ativa; seções concluídas ganham ✓ no painel lateral.
- **Botão "Parar geração" sempre visível.** Interrupção mantém o que já foi gerado.
- Falha no meio: mantém o parcial, oferece [Continuar de onde parou] / [Tentar de novo a seção]. Nunca descartar texto silenciosamente.

### Erro de ingestão (DataJud/DJEN fora, doc 09 prevê)

| Situação | UI |
|---|---|
| Fonte instável há <6h | Banner âmbar global: "DJEN instável desde 9h. Última leitura completa: hoje 6h. Reprocessando automaticamente." |
| Fonte fora há >24h | Banner vermelho + e-mail proativo aos afetados + link para status page |
| Falha em processo específico | Badge ⚠ no processo, com detalhe e [Tentar novamente] |

Regra: **mostrar sempre o timestamp da última leitura bem-sucedida.** O medo do advogado é "o que eu NÃO estou vendo?" — a resposta honesta ("dados até ontem 23h") é o que mantém a confiança num produto cujo trabalho é vigiar.

### Cota de IA esgotada

- 80% da cota: toast discreto + contador no menu ("10/12 peças este mês").
- 100%: modal no momento da geração — nunca antes, nunca bloqueando leitura:

```
┌─────────────────────────────────────────────┐
│  Você usou as 12 peças do plano Solo 😕     │
│  Renova em 01/08 (em 16 dias).              │
│                                             │
│  [Fazer upgrade p/ Escritório — 40 peças]   │
│  [Lembrar na renovação]                     │
│                                             │
│  Classificação de intimações e prazos       │
│  continuam funcionando normalmente.         │
└─────────────────────────────────────────────┘
```

- **Nunca degradar prazo/classificação por cota de peças.** Cota limita geração; a vigilância de prazos é a promessa central e não pode parecer refém do upsell.
- Vazio ≠ erro ≠ carregando: cada lista tem os 3 estados desenhados (vazio com próximo passo, erro com retry, skeleton).

---

## 7. 🎭 Referências visuais e tom da marca

### A tensão real

O público é **conservador na confiança** ("meu processo está seguro?") e **aspiracional na imagem** ("quero parecer moderno para o cliente", doc 01, persona 2). Resolver assim: **estrutura conservadora, execução moderna.** Layout, hierarquia e vocabulário sóbrios; tipografia, micro-interações e velocidade modernos. Nada de gradiente roxo + emoji de foguete; nada de cinza-repartição também.

### Referências (o que roubar de cada uma)

| Referência | Roubar | Ignorar |
|---|---|---|
| **Linear** | Densidade, velocidade percebida, atalhos de teclado, restraint visual | Estética dark-first agressiva |
| **Stripe Dashboard** | Como exibir número crítico com hierarquia; documentação-como-UI (memória de cálculo) | Complexidade de configuração |
| **Superhuman** | Fila com verbo (triage), esvaziar a fila como satisfação | Elitismo do tom |
| **Notion** | Editor confortável para longas horas de texto | UI de blocos genéricos |
| **Astrea/Advbox** (anti-referência) | Estudar onde o usuário se perde nos 15 menus | Tudo o mais — parecer com eles é falhar |

### Tom de voz (microcopy)

- **Português direto, sem juridiquês na UI** (o juridiquês fica nas peças): "Vence sexta", não "Termo final do prazo".
- Sistema fala em 1ª pessoa **apenas** no bloco "o que fiz por você" ("Li 14 publicações") — e são templates fixos preenchidos com números de query, não texto gerado. No resto, voz neutra — IA onipresente na voz cansaria e soaria gimmick.
- Nunca prometer certeza que não temos: "rascunhei", "sugeri", "encontrei" — o advogado "decide", "aprova", "assina".
- Erros sem culpa e com próximo passo: "Não consegui ler esta publicação. [Tentar de novo] [Ver texto bruto]".
- ⚠️ Nome do produto indefinido (doc 00, decisão #1) — este doc usa tokens neutros de propósito; a identidade visual final (logo, brand accent definitivo) trava junto com o nome, prazo ago/2026.

---

## 8. 📋 Prioridade de construção (o que desenhar primeiro)

Meses no relógio do doc 00 §4 (ago/2026 = mês 1; G1 = mês 7; G2 = mês 12):

| Ordem | Entrega | Quando |
|---|---|---|
| 1 | Tokens + tema Material customizado + componentes de prazo (badge, semáforo, memória de cálculo) | Mês 1–2 |
| 2 | **Spike do editor** (wrapper próprio × ngx-tiptap × plano C — decisão #3, doc 00) | Mês 1–2 |
| 3 | Onboarding (a) — protótipo testável nas 15 entrevistas de validação | Mês 2–3 |
| 4 | Central de intimações (c) — primeira tela com dados reais (alinhada ao G1: fluxo intimação→prazo de pé) | Mês 5–7 |
| 5 | "Seu dia" (b) — versão estruturada (sem frase IA, corte #4) | Mês 7–8 |
| 6 | Redator/editor (d) + estados de streaming | Mês 8–11 |
| 7 | Revisão/export (e) + estados de cota e erro (fecha o G2: feature-complete) | Mês 11–12 |

Ferramenta: Figma só para exploração rápida; a verdade vive no código (storybook leve dos componentes críticos). Com 12,5h/semana não há orçamento para manter dois artefatos fiéis.

---

## 9. 🔍 Validar UX sem par (projeto solo)

O sócio que revisava design saiu (doc 00). Não há segunda opinião interna — então a validação vira processo com gente de fora, barato e agendado:

| Mecanismo | Quando | O que valida |
|---|---|---|
| **15 entrevistas de validação** (G0, doc 00 §8) com protótipo do onboarding na mão | Mês 1–2 | Fluxo (a): a promessa "OAB → processos → prazo" é compreendida sem explicação? Teste dos 5 segundos no "Seu dia" |
| **2–3 dos entrevistados como painel recorrente** (30 min/mês, informal) | Contínuo | Cada tela nova vista por pelo menos 2 advogados reais ANTES de polir. É o substituto do "olha isso aqui" que o sócio fazia |
| **10 betas** (ago–set/2027) | Beta | O produto inteiro em uso real. Onboarding assistido = observação direta de onde travam |
| **Instrumentação desde o dia 1** | Contínuo | `tempo_cadastro_ate_primeiro_prazo_visto` (<10 min), % de peças exportadas com [VERIFICAR] pendente, taxa de rascunhos revisados vs. abandonados, uso da memória de cálculo. Números não substituem observação, mas não mentem |
| **Heurística de autodefesa** | Sempre | Na dúvida entre duas soluções, escolher a mais densa e mais explícita (princípios 5 e 6) — o viés de quem projeta sozinho é embelezar; o ICP quer ler |

Regra dura: **nenhuma tela do fluxo do aha vai para o beta sem ter sido vista por pelo menos 2 advogados de fora.** É o mínimo que compensa a ausência de revisão interna — e cabe em 12,5h/sem porque usa gente já recrutada (entrevistados, lista de espera).
