# 15 — Teste de Campo: ApolloIA (destrava o gate G5)

> **O que este documento decide:** se a lacuna que o Peticiona pretende ocupar ainda existe.
> Custo: **R$ 89,90 e ~4h**. Trava: **775h de desenvolvimento**.
> A regra de decisão já está escrita no `00-premissas.md` §8 — **leia antes de gerar a primeira peça**, senão você vai encaixar o que vir na conclusão que já quer.
>
> Criado em 20/07/2026, junto com o acionamento do G5.

---

## 1. Por que três geradores, e não um

Testar só o Apollo responde "ele é bom?". Não responde a pergunta que importa, que é **"ele é bom por causa de algo que eu não consigo copiar?"**.

Rode **o mesmo caso** nos três:

| # | Gerador | O que isolar |
|---|---|---|
| **A** | **ApolloIA** (plano Start, R$ 89,90) | O concorrente |
| **B** | **ChatGPT ou Claude puro**, mesmo texto colado, sem instrução especial | O substituto grátis |
| **C** | **Claude via API com um prompt de nicho seu**, escrito por você em ~1h | O que VOCÊ consegue fazer sem construir nada |

**A leitura cruzada é o dado, não a nota de cada um:**

| Se… | Então |
|---|---|
| **A ≈ B** | O Apollo é um wrapper. Não tem fosso — mas isso significa que a **sua camada de IA também não será diferencial**. Sobra contexto e fluxo, exatamente o que o `01` já defende |
| **A > B** | Ele investiu em template e prompt de nicho. É concorrente de verdade, e a barreira que ele construiu você também teria que construir |
| **C ≈ A** | Uma hora de prompt empata com o produto dele. Isso é ótimo para você e péssimo para a categoria: significa que **qualquer um entra**, e o G5 vai disparar de novo |

O caso C é o mais desconfortável e o mais informativo. Não pule.

---

## 2. Preparação

- [ ] **Um caso trabalhista real e completo**: a reclamação inicial + os fatos da defesa. Origem: **consulta processual pública** (processos trabalhistas não sigilosos são acessíveis no PJe-JT), ou um advogado da rede de MT. Anonimizar nomes antes de colar em qualquer ferramenta.
- [ ] Assinar o **Apollo Start** — trial de 7 dias, R$ 89,90/mês depois. ⚠️ **Anotar a data de cancelamento na agenda no dia da assinatura.**
- [ ] Ter o motor de prazos rodando local para o item 4.

---

## 3. As 5 perguntas (respondíveis sem ser advogado)

### 3.1 Especificidade — o teste do nome trocado

Conte os parágrafos de fundamentação. Quantos citam **fatos concretos do caso** e quantos serviriam para qualquer processo trabalhista?

| Gerador | Parágrafos totais | Específicos do caso | % específico |
|---|---:|---:|---:|
| A — Apollo | | | |
| B — ChatGPT puro | | | |
| C — Claude + prompt meu | | | |

> Leitura: abaixo de ~30% de específico, é template com nome trocado. Não importa quão bem escrito esteja.

### 3.2 Jurisprudência — o teste que pode derrubar o argumento dele sozinho

O Apollo promete *"jurisprudência real das fontes oficiais, rastreável até a origem"*, com comparativo explícito contra o ChatGPT no site dele. **É o mesmo argumento anti-alucinação que nós planejávamos usar.**

Pegue **3 citações** de cada peça e procure o número do processo no site do tribunal:

| Gerador | Citação 1 | Citação 2 | Citação 3 |
|---|---|---|---|
| A — Apollo | ( ) existe ( ) não | ( ) existe ( ) não | ( ) existe ( ) não |
| B — ChatGPT puro | ( ) existe ( ) não | ( ) existe ( ) não | ( ) existe ( ) não |
| C — Claude + prompt | ( ) existe ( ) não | ( ) existe ( ) não | ( ) existe ( ) não |

> **Se uma única citação do Apollo não existir, a promessa central dele cai** — e vira o argumento de venda mais forte que você tem contra ele. Guarde o print.

### 3.3 Fluxo — a pergunta que dimensiona a lacuna inteira

**É o item mais importante e o mais lento. Comece por ele**, porque depende de uma intimação chegar de verdade.

- [ ] Cadastrar monitoramento por OAB no plano Pro (R$ 139,90 — só se o Start não permitir)
- [ ] Esperar uma intimação cair
- [ ] Observar: **ela vira peça com um clique, ou você tem que redigitar os fatos no chat?**

| Resposta | Consequência para a tese |
|---|---|
| A intimação alimenta o redator automaticamente | A integração ponta a ponta **já existe no mercado**. Nossa lacuna encolhe para autos + prazo |
| O redator é sempre conversacional, ignora o monitoramento | **A lacuna principal continua aberta.** É o cenário em que o projeto se justifica |

Resposta observada: _______________________________________________

### 3.4 Prazo — o único terreno onde você já ganhou

Peça a ele para contar um prazo com **feriado forense no meio** (ex.: intimação em 15/12, 15 dias úteis, Justiça do Trabalho, atravessando o recesso). Compare com o seu motor.

| | Data que devolveu | Mostra a memória de cálculo? | Correto? |
|---|---|---|---|
| Apollo | | ( ) sim ( ) não | ( ) sim ( ) não |
| Meu motor | | ( ) sim ( ) não | ( ) sim ( ) não |

> Verificado em 20/07/2026: as "55+ calculadoras" do Apollo são **financeiras** (rescisão, juros, atualização). Este teste é para confirmar que ele não tem contagem de prazo, e para medir o quanto isso incomoda na prática.

### 3.5 Retrabalho — a métrica norte do `04`

Sua meta declarada é **>70% do texto sobrevivendo à edição**. Quanto de cada peça você jogaria fora?

| Gerador | % que sobreviveria | O que você jogaria fora, e por quê |
|---|---:|---|
| A — Apollo | | |
| B — ChatGPT puro | | |
| C — Claude + prompt | | |

⚠️ **Limite honesto deste teste:** você é dev, não advogado. Você consegue julgar especificidade, existência de citação e coerência — **não consegue julgar acerto jurídico**. Se o resultado for ambíguo, pague 1h de um advogado para ler as três peças às cegas, sem saber qual veio de onde.

---

## 4. Veredito

Preencher **antes** de reler a tabela do `00` §8, para não negociar com ela:

```
TESTE DE CAMPO APOLLO          Data: ___/___/2026
Caso usado: _______________________________________

Especificidade   A:____%   B:____%   C:____%
Citações falsas  A:__/3    B:__/3    C:__/3
Fluxo: intimação alimenta o redator?  ( )S ( )N ( )não consegui testar
Prazo: calcula com memória?           ( )S ( )N
Sobrevivência    A:____%   B:____%   C:____%

A ≈ B?  ( )S ( )N        C ≈ A?  ( )S ( )N

FRASE HONESTA DE 1 LINHA — eu pagaria R$ 89,90/mês por isso?
_________________________________________________________

GATE G5 (aplicar a tabela do 00 §8, sem renegociar):
( ) VERMELHO — peças boas E nascem da intimação → encerrar cedo e barato
( ) AMARELO  — peças boas MAS fluxo manual → refazer escopo para 2027
( ) VERDE    — peças genéricas e rasas → aposta de nicho ganha força
```

## 5. Depois

1. Colar o veredito no `00-premissas.md` §8, embaixo do bloco do G5.
2. Aplicar a decisão **sem negociar com ela**.
3. Cancelar a assinatura do Apollo se não for mais usar (a data está na sua agenda desde o dia 1).
4. **Independente do resultado, as 15 entrevistas do `14` continuam de pé.** Este teste mede o concorrente; as entrevistas medem o cliente. Um não substitui o outro, e nenhum dos dois é bloqueado pelo outro — rodam em paralelo.
