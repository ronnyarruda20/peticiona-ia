# 🧮 Código — fatia 1: motor de prazos

> Esta é a **primeira fatia vertical** do produto, não o MVP. O MVP da Fase 1 está
> estimado em 775h (~14 meses solo) no `00-premissas.md`. O que existe aqui são
> algumas horas de trabalho: o núcleo determinístico + uma calculadora pública.

## Por que esta fatia primeiro

Ela é a única parte que sobrevive a **todos** os cenários dos documentos:

1. **É o item "nunca cortar"** da ordem de sacrifício (`12-plano-de-execucao.md` §4).
2. **É o que o ChatGPT não faz.** Ele chuta feriado forense e erra recesso — é o
   argumento nº 1 contra o substituto gratuito (`02-analise-competitiva.md`).
3. **É o ímã de leads** que a estratégia de conteúdo pede desde já
   (`07-go-to-market.md`): calculadora grátis, sem cadastro, indexável.
4. **Não depende de nada externo** — sem chave de API, sem credencial do DJEN,
   sem banco. Roda e prova valor sozinha.

## O que roda

| | |
|---|---|
| **Back** | Spring Boot 3.3 · Java 21 · sem banco (ainda) |
| **Front** | Angular 21 · zoneless · signals · sem lib de UI |
| **Deploy** | Docker multi-stage → jar único servindo API + front · Railway |
| **Testes** | 19 testes JUnit sobre o motor de prazos |

## A regra que o código existe para respeitar

> **LLM nunca calcula data.** (`06-estrategia-ia.md`)

O cálculo é aritmética de calendário determinística, coberta por testes, e devolve a
**memória de cálculo dia a dia** — quais dias contaram, quais não contaram e por quê.
O advogado assume a responsabilidade pelo prazo; ele precisa poder conferir a conta,
não receber um número para confiar.

Erro de prazo é o risco ALTÍSSIMO do `09-riscos-e-compliance.md`. Por isso os testes
cobrem os casos que erram na vida real: recesso forense atravessando a virada do ano,
feriados móveis derivados da Páscoa, feriados próprios da Justiça Federal, prorrogação
de vencimento que cai em dia sem expediente.

## ⚠️ O que ainda NÃO está coberto

Está escrito na tela do usuário também — não escondido:

- **Feriados estaduais e municipais.** Um feriado na comarca desloca o vencimento.
- **Portarias de suspensão de expediente** (greve, luto oficial, sistema fora do ar).
- **Prazos em dobro** (Fazenda Pública, Defensoria, MP, litisconsortes com procuradores distintos).
- **Conferência jurídica.** As regras foram implementadas a partir da legislação citada
  no código, mas **precisam ser validadas por advogado** antes de qualquer uso real.
  Especialmente: Carnaval e Corpus Christi como feriados forenses (a prática varia por
  tribunal) e a regra do recesso na Justiça do Trabalho.

## Rodar localmente

**Pré-requisitos:** JDK 21 e Node ≥22.12.

⚠️ O Maven desta máquina aponta para um Nexus corporativo. Por isso o
`backend/settings.xml` e o `-s settings.xml` nos comandos.

```bash
# Back (porta 8080)
cd backend
mvn -s settings.xml spring-boot:run

# Front (porta 4200, com proxy para o back)
cd frontend
npm install
npm start
```

Testes:

```bash
cd backend && mvn -s settings.xml test
```

Build completo como em produção:

```bash
docker build -t peticiona .
docker run -p 8080:8080 peticiona
```

## API

```http
POST /api/prazos/calcular
Content-Type: application/json

{
  "dataIntimacao": "2025-12-15",
  "prazoEmDias": 15,
  "tipoContagem": "DIAS_UTEIS",     // ou DIAS_CORRIDOS
  "justica": "TRABALHISTA",         // ou ESTADUAL, FEDERAL
  "considerarRecesso": true
}
```

Devolve o vencimento, a data de início da contagem, a memória de cálculo completa
(um registro por dia analisado), a fundamentação legal aplicada e os avisos de limite.

`GET /actuator/health` — usado pelo health check do Railway.

## Próximo passo do código

Nenhum, até as entrevistas do `14-roteiro-de-entrevistas.md` acontecerem. A regra de
decisão está escrita lá e vale para o código também: se a dor de prazo não se
confirmar em campo, esta fatia não vira produto — vira calculadora grátis na internet,
que já é útil como ímã de leads e custa quase nada para manter.
