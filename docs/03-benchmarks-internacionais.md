# 03 — Benchmarks Internacionais: O Que Roubar (de Bom) de Fora

## Panorama do mercado internacional (2026)

O mercado legaltech lá fora está anos à frente e se divide em camadas claras:

| Categoria | Exemplos | Lição para nós |
|---|---|---|
| Practice management completo | Clio, MyCase, PracticePanther, Smokeball | Modelo de produto e pricing |
| IA enterprise | Harvey, Legora, CoCounsel (Thomson Reuters) | O que a IA jurídica consegue fazer no estado da arte |
| IA acessível "Word-native" | Spellbook | Modelo de adoção instantânea |
| Automação administrativa | alfred_ (email/agenda), Lawmatics (intake/CRM) | Dores adjacentes monetizáveis |

---

## Clio (Canadá) — o padrão-ouro de plataforma

**O que é:** líder mundial em practice management, 150 mil+ advogados, 250+ integrações, produtos modulares (Clio Manage + Clio Grow para captação/CRM) e assistente Clio Duo AI.

**O que copiar:**
- ✅ **Separação captação vs. gestão** — Clio Grow cuida do funil de novos clientes (intake forms, agendamento, e-sign). Podemos ter um módulo "captação" leve: formulário público de triagem + agendamento
- ✅ **Ecossistema de integrações + API pública** desde cedo — vira plataforma, não ferramenta
- ✅ **Preço por usuário transparente** no site

**O que evitar:**
- ❌ Modularidade excessiva — usuários reclamam de trabalhar em vários produtos separados e de add-ons que encarecem
- ❌ Dependência de integrações de terceiros para funções básicas

## Smokeball (EUA/Austrália) — automação de documentos como núcleo

**O que é:** foco em pequenos e médios escritórios; time tracking automático (registra o tempo passivamente enquanto o advogado trabalha no Word/Outlook) e biblioteca com 20.000+ modelos de formulários jurídicos.

**O que copiar:**
- ✅ **Biblioteca de modelos por área do direito** — é o motivo de compra; nossa versão: modelos brasileiros (petições, contratos, procurações) alimentando a IA
- ✅ **Captura passiva de trabalho** — no nosso caso: registrar automaticamente andamentos, peças produzidas e tempo por processo sem o advogado digitar nada
- ✅ Posicionamento "all-in-one para pequeno escritório"

## Spellbook (Canadá) — a lição de adoção mais importante

**O que é:** copiloto de redação que roda dentro do Microsoft Word; 3.000+ equipes jurídicas; instala o plugin e começa a usar — sem processo de vendas enterprise, sem implantação demorada.

**O que copiar (CRÍTICO para nós):**
- ✅ **Zero fricção de adoção** — o advogado brasileiro vive no Word e no WhatsApp. Se a IA chegar ONDE ELE JÁ ESTÁ, a adoção explode
- ✅ **Aprender com o acervo do usuário** — o Spellbook Library aprende com os precedentes do escritório para manter o estilo da casa. Nossa versão: IA aprende com as peças anteriores do advogado (estilo, teses favoritas) — Fase 2
- ✅ Preço por usuário sem vendas consultivas

**⚠️ A tensão com o nosso cronograma (admitir e resolver):**

A lição do Spellbook diz "vá aonde o usuário já está" — e nós **cortamos o WhatsApp da Fase 1** (`00-premissas.md`, §2). Há tensão real aqui. A resolução é entender o princípio direito: o que o Spellbook prova não é "use o canal X", é **"elimine a fricção de adoção"**. Na Fase 1, aplicamos o princípio sem o WhatsApp:

- **A intimação vem até o advogado, não ele até nós:** alerta por e-mail/push com link direto para a intimação classificada + rascunho — o produto o interrompe no momento da dor, como um plugin interromperia
- **Onboarding OAB em < 10 min** — o equivalente ao "instala o plugin e usa" do Spellbook
- **Export .docx** — ele revisa e finaliza no Word, onde já vive; não brigamos com o hábito
- O bot de WhatsApp ("manda a foto da intimação → recebe resumo + prazo + rascunho") continua sendo uma ideia forte — **de Fase 2**, quando vira acelerador de adoção e retenção sobre um produto que já converte sem ele
- Add-in Word: fase 2+

Se o beta (fev–mar/2027) mostrar que e-mail/push não geram o hábito diário, o WhatsApp sobe de prioridade na Fase 2 — essa é a válvula de escape, não o inchaço da Fase 1.

## Harvey (EUA) — o teto da categoria

**O que é:** plataforma IA enterprise usada por grandes firmas globais (A&O Shearman etc.), +US$ 1 bi captados; pesquisa jurídica profunda, due diligence multi-documento, modelos customizados por firma.

**Lição principal:** Harvey prova que advogados **pagam caro** por IA que realmente funciona — mas é explicitamente não-construído para solo e pequenas firmas. O "Harvey do advogado de porta de rua" **não existe nem lá fora**. No Brasil, muito menos.

## MyCase / PracticePanther — simplicidade vende

**O que copiar:**
- ✅ Planos a partir de US$ 39–49/usuário — âncora de preço agressiva com upsell por tier
- ✅ Foco em comunicação com cliente (portal do cliente, mensagens integradas ao caso)
- ✅ Pagamentos nativos (PantherPayments) — no Brasil: **Pix nativo com conciliação automática de honorários** seria matador

**O que evitar:**
- ❌ Tier básico "capado" demais (MyCase é criticado por esconder o essencial nos planos caros) — nosso plano de entrada precisa resolver a dor principal sozinho

## alfred_ / Lawmatics — dores adjacentes

- **alfred_:** ataca o gap de 12h semanais perdidas em e-mail/agenda/follow-up. Insight: **a dor administrativa é tão valiosa quanto a jurídica**
- **Lawmatics:** CRM + automação de captação para escritórios. Insight: advogado brasileiro não faz marketing; um módulo simples de "página do advogado + triagem automática de leads via WhatsApp" diferencia muito

**⚠️ Upgrade desta lição (feedback de validação):** um advogado revisando nosso material apontou espontaneamente que *"controle de captação/indicação de novos clientes, não tem sobre isso"*. Ou seja: captação **não é luxo de fase 3 na cabeça do mercado — é expectativa explícita**. O que Clio Grow e Lawmatics já provaram lá fora, o nosso ICP confirma aqui: ajudar a GANHAR cliente vende tanto quanto ajudar a gerenciar. Consequências práticas:

1. Captação continua **fora da Fase 1** (não cabe nas 840h e não participa do aha moment) — mas sobe de "ideia de plataforma" para **compromisso nomeado de roadmap (Fase 3)**, com resposta pronta no discurso de venda (ver tabela de objeções no doc 01)
2. Versão mínima barata a considerar na Fase 3: página pública do advogado + formulário de triagem + agendamento — o "Clio Grow de bolso", muito antes de CRM completo
3. Atenção ao Provimento 205/2021 da OAB (doc 09): o módulo deve ajudar o advogado a captar **dentro** das regras — isso em si é diferencial

---

## 🧭 Síntese: os 5 princípios importados (com a fase em que cada um entra)

1. **Adoção instantânea à la Spellbook** — zero fricção, não "implantação". **Fase 1** via onboarding OAB + alerta que traz o advogado até a intimação + export .docx; WhatsApp/Word como canais entram na Fase 2 (ver tensão admitida acima)
2. **Modelos e automação de documentos à la Smokeball** — biblioteca brasileira por área do direito. **Fase 1** (1 nicho, 4 peças), expandindo por nicho depois
3. **Pix e cobrança nativa à la PantherPayments** — dinheiro na mão do advogado = retenção. **Fase 2** (meses 9–14)
4. **Módulo de captação à la Clio Grow/Lawmatics** — ajudar o advogado a GANHAR clientes, não só gerenciar. **Fase 3, com prioridade elevada**: o mercado pergunta por isso espontaneamente (feedback de validação) — é a expectativa não atendida mais citada depois de financeiro/WhatsApp
5. **IA que aprende o estilo do usuário à la Spellbook Library** — lock-in positivo: quanto mais usa, melhor fica, mais difícil sair. **Fase 2**
