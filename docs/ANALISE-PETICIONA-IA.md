# ANÁLISE — Peticiona IA
### Briefing para revisão completa via Claude Code · 18/07/2026

> **INSTRUÇÃO PARA A IA NO TERMINAL:** Você está na raiz do projeto
> `peticiona-ia`. Leia este arquivo por inteiro ANTES de tocar em qualquer
> código. Ele contém o contexto do dono do projeto, a estratégia definida,
> o cenário competitivo e exatamente o que se espera desta análise.
> Execute as fases na ordem. Ao final, gere os entregáveis listados.

---

## 1. Contexto do dono do projeto

- **Ronny de Arruda** — dev Java sênior full-stack, +12 anos (Java 11–21,
  Spring Boot 3, Angular 15–19, Oracle, Keycloak/OAuth2, RabbitMQ,
  Docker/Jenkins, GraphQL). Forte em setor público/jurídico (Ministério
  Público de MT) e financeiro (Poupex/integração CIP).
- Em recolocação profissional; o Peticiona IA é o projeto de negócio
  próprio, tocado solo — visão: fonte de renda recorrente escalável.
- Restrições importantes: **custo mínimo de infraestrutura** (sem renda
  fixa no momento), e o dono **não gosta de vender** — o produto precisa
  se vender por demonstração e onboarding simples.

## 2. O que é o Peticiona IA (visão)

SaaS jurídico brasileiro de geração de petições com IA para advogados
solo e escritórios pequenos. Este repositório é o estado atual — parte
da sua missão é descobrir e documentar o quanto da visão já existe.

## 3. Cenário competitivo (pesquisado em jul/2026 — não ignorar!)

Gerador de petição genérico já é commodity no Brasil: ChatADV (200k+
advogados, parcerias com OABs), Jus IA/Jusbrasil (maior base de dados
jurídicos do país, respostas com ementas e links), Jurídico AI, JusDocs,
Advoga IA, entre dezenas. **Competir de frente é perder.**

### Estratégia definida (as 4 apostas — orientar TODA recomendação por elas)

1. **Nicho + fluxo completo:** dominar UMA vertical de alto volume
   (previdenciário ou trabalhista) e cobrir o fluxo inteiro: intake do
   cliente via WhatsApp (existe projeto irmão de assistente de triagem
   em n8n + Evolution API + Claude) → fatos estruturados → rascunho de
   petição → revisão do advogado. A integração intake→peça é o
   diferencial defensável; ninguém atende escritório pequeno com isso.
2. **Anti-alucinação como bandeira:** toda citação de jurisprudência com
   link verificável para a fonte real; distinção visual entre fato
   informado pelo advogado vs. sugestão da IA. Posicionamento: "a IA que
   não inventa jurisprudência" (advogados já sofreram sanções por
   citações inventadas — é a maior objeção de compra).
3. **RAG com o estilo do escritório:** o advogado sobe suas peças
   antigas; o sistema gera no estilo, estrutura e teses da casa. Efeito
   lock-in de assinatura.
4. **Go-to-market por consultoria:** os primeiros clientes virão da
   consultoria de automação do Ronny (funil de prospecção já montado em
   n8n) — o produto precisa de onboarding self-service e demo gravável.

## 4. FASE 1 — Mapeamento (sem alterar nada)

- [ ] Inventário: linguagens, frameworks, estrutura de pastas, serviços,
      banco de dados, dependências (versões e vulnerabilidades conhecidas)
- [ ] O que já funciona vs. o que está incompleto/quebrado (tentar
      buildar/rodar; documentar passos e erros)
- [ ] Como a IA é chamada hoje: provedor, modelos, onde vivem os prompts,
      há histórico/memória, streaming, tratamento de erro?
- [ ] Segurança: procurar segredos hardcoded (chaves, senhas, .env
      versionado), auth existente, exposição de dados
- [ ] Aderência: quanto do código atual serve às 4 apostas acima?

## 5. FASE 2 — Revisão crítica

Avaliar e anotar (com severidade alta/média/baixa e arquivo:linha):

- [ ] Arquitetura: acoplamento, separação de camadas, pontos de escala
- [ ] Qualidade: duplicação, ausência de testes, tratamento de erros
- [ ] Segurança e LGPD: dados de clientes/casos são sensíveis — como são
      armazenados, criptografia, logs vazando conteúdo de petições?
- [ ] Engenharia de prompts: os prompts atuais previnem alucinação?
      Pedem fontes? Separam fatos de sugestões?
- [ ] Custo: chamadas de IA desperdiçadas, modelo caro onde um barato
      resolveria, ausência de cache

## 6. FASE 3 — Plano de enriquecimento

Para cada uma das 4 apostas, propor o caminho técnico concreto DENTRO da
base atual (o que reaproveitar, o que criar, o que descartar), em ordem
de menor esforço → maior valor. Definir o **MVP mínimo demonstrável**:
o corte mais enxuto que permita gravar uma demo de 2 minutos e colocar
na mão de um advogado beta.

## 7. Entregáveis (criar na raiz do projeto)

1. `docs/REVISAO.md` — relatório das fases 1 e 2: inventário, achados
   com severidade, riscos de segurança/LGPD
2. `docs/ROADMAP.md` — plano da fase 3: MVP demonstrável + backlog
   priorizado (esforço × valor), cada item com critério de pronto
3. `CLAUDE.md` — arquivo de contexto permanente do projeto para sessões
   futuras: como rodar, arquitetura, decisões tomadas, convenções
4. Se encontrar segredos expostos: alertar IMEDIATAMENTE no topo do
   REVISAO.md com instrução de rotação das chaves

## 8. Regras desta análise

- Fases 1 e 2 são **somente leitura** — nenhuma alteração de código sem
  aprovação explícita do Ronny após ele ler o REVISAO.md
- Honestidade acima de conforto: se algo deve ser descartado/reescrito,
  dizer com clareza e justificar
- Toda recomendação amarrada a uma das 4 apostas ou a um risco concreto
  (segurança, custo, LGPD) — nada de refactor por estética
- Preferir soluções de custo zero/baixo (self-hosted, free tiers)
- O advogado revisa toda peça gerada — o produto NUNCA se posiciona como
  substituto do advogado (limite ético e de responsabilidade)
