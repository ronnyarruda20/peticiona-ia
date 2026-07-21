# 16 — Parecer: o que eu faria no seu lugar

> **Este documento é opinião, não levantamento.** Todos os outros docs deste repositório tentam ser neutros. Este não tenta. Foi pedido em 20/07/2026, depois do acionamento do G5, com a instrução de dizer o que eu acho.
>
> Você pode discordar dele inteiro. Mas ele fica escrito e datado, para que a discordância seja uma decisão e não um esquecimento.

---

## 1. O parecer, em cinco linhas

**Não construa a Fase 1 como está especificada.** 775h e 15 meses até a primeira receita, sem renda, contra um concorrente que já vende o mesmo fluxo por R$ 89,90, é uma aposta com prazo longo demais e janela curta demais. A margem está certa, a arquitetura está certa, os documentos estão certos. **A conta do tempo não fecha, e agora a da concorrência também não.**

Mas o motivo real do parecer não é o Apollo. É o item 3.

---

## 2. O que este repositório revela sobre o projeto

Inventário honesto do que existe, em julho de 2026:

| Produzido | Estado |
|---|---|
| 16 documentos de estratégia, alguns excelentes | ✅ |
| Motor de prazos, 19 testes, deploy em produção | ✅ |
| Análise competitiva em 5 camadas, com preços conferidos | ✅ |
| Gates de decisão escritos a frio, incluindo critérios de encerramento | ✅ |
| Auditoria de 27 bugs em workflows n8n, 12 corrigidos | ✅ |
| Conector MCP validado | ✅ |
| **Conversas com um advogado que não é seu parente** | ⛔ **zero** |
| **Entrevistas do G0** | ⛔ **zero** |
| **Candidaturas enviadas** | ⛔ **zero** |

A coluna da esquerda tem centenas de horas de trabalho competente. A da direita tem zero, e cada linha dela custa menos de duas horas.

**O padrão não é preguiça — é o contrário.** Toda vez que aparece uma tarefa de contato, surge uma tarefa técnica adjacente que parece pré-requisito dela: para aplicar em vagas, primeiro o robô de vagas; para falar com advogados, primeiro o motor de prazos; para validar o mercado, primeiro a análise competitiva. Cada uma dessas construções foi bem-feita e nenhuma foi desnecessária. Mas nenhuma delas era pré-requisito de nada.

Isso importa mais que o Apollo porque **é o fator que decide qualquer versão deste projeto**. Um SaaS de nicho vendido a advogado autônomo é um negócio de conversa: 15 entrevistas, 10 betas com onboarding assistido, conteúdo, indicação, suporte no WhatsApp. O doc `08` já registra que *"a premissa mais frágil é que os adds do ano 1 dependem de conteúdo + indicação feitos pela mesma pessoa que desenvolve"*. É a premissa mais frágil **e é a única que nunca foi testada**.

Se as 15 entrevistas não acontecerem, não é o Peticiona que falha. É que ele nunca foi tentado.

---

## 3. O que eu faria — 30 dias, com o resultado pré-comprometido

Não proponho matar o projeto. Proponho **forçar a decisão a acontecer por dado, e não por desgaste.**

**Até 20/08/2026:**

| # | O quê | Horas |
|---|---|---:|
| 1 | Teste de campo do Apollo (`15-teste-de-campo-apollo.md`) | ~4h |
| 2 | **8 entrevistas** — não 15. O §7 do `14` já manda parar em 8 se o placar for inequívoco | ~16h |
| 3 | 10 candidaturas enviadas para vagas Java sênior | ~5h |
| 4 | Verificar a vedação à administração da SLU | ~2h |

**~27h em 30 dias.** Cabe em 12,5h/semana com folga, e nenhuma das quatro é desenvolvimento.

**A regra, escrita agora e não renegociável em 20/08:**

| Situação em 20/08/2026 | Decisão |
|---|---|
| Entrevistas feitas, dor confirmada, Apollo fraco | ✅ O projeto vive. Refazer escopo e cronograma com dado real, não com hipótese |
| Entrevistas feitas, dor confirmada, Apollo forte | ⚠️ Repensar a tese inteira. Existe cliente, mas o lugar já está ocupado |
| Entrevistas feitas, dor não confirmada | ✅ **Encerrar — e isso é vitória.** Você economizou 750h e descobriu por ~R$ 90 |
| **Entrevistas não feitas** | ⛔ **Encerrar.** Não porque a tese é ruim: porque um SaaS que exige 15 conversas para nascer e 100 para crescer não vai ser tocado por alguém que não conseguiu ter 8 em um mês. Isso é informação sobre o projeto **certo** para você, não sobre o seu valor |

A quarta linha é a que importa e é a única que não depende de nenhum advogado responder nada.

---

## 4. O que sobrevive se o projeto encerrar

Encerrar não é perder tudo. Fica:

- **A calculadora de prazos, pública e gratuita.** Custa quase nada para manter, é útil, e é portfólio real de Angular + Spring Boot em produção — o que serve diretamente ao trilho da recolocação.
- **O motor como componente.** Dos 10 concorrentes levantados, **nenhum calcula prazo processual** e dois avisam no próprio site que não servem para prazo fatal. Existe aí uma venda de API B2B — mas sejamos honestos sobre o tamanho: são talvez 10 a 20 compradores possíveis no Brasil, todos indies sem caixa. É salvamento de ativo, não pivô. Não construa nada novo para isso; só ofereça o que já existe.
- **Os documentos.** Este repositório é uma demonstração de raciocínio de produto que a maioria das startups financiadas não produz. Isso tem valor em entrevista de emprego, e valor real no próximo projeto.

---

## 5. Onde este parecer pode estar errado

Por honestidade, o que enfraquece o que escrevi acima:

1. **Não usei o ApolloIA.** Julguei por site e API pública. Se as peças dele forem ruins, a janela é maior do que suponho — e o item 1 dos 30 dias existe exatamente para me contradizer.
2. **Bootstrap lento é uma estratégia legítima.** Existem SaaS de nicho que levaram 3 anos e deram certo. O que torna o caso aqui pior não é a lentidão, é a lentidão **somada** à ausência de renda e a um concorrente já no ar.
3. **Você conhece o mercado jurídico melhor que eu por convivência** — 12 anos entre MP-MT, Polícia Judiciária e TRE-MT. Se a sua intuição de campo contradiz esta análise de escritório, sua intuição provavelmente vale mais. Mas então ela precisa virar dado: são as mesmas 8 entrevistas.
4. **Eu erro por não verificar.** Já aconteceu neste projeto — recomendei "vender o motor de prazos como produto" e você derrubou em uma frase, com razão, usando um argumento que estava no seu próprio doc `02`. Trate este parecer com a mesma desconfiança.

---

## 6. A frase que resume

> O Peticiona não vai ser decidido por arquitetura, por preço, por stack ou pelo ApolloIA. Vai ser decidido por 8 conversas de 40 minutos que custam R$ 0 e estão disponíveis desde o primeiro dia.
>
> Enquanto elas não acontecerem, tudo que este repositório contém — inclusive este parecer — é especulação bem escrita.
