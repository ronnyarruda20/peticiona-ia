# 11 — Modelo de Dados

> **Alinhado ao doc `00-premissas.md` (reescrito jul/2026): projeto solo, back-end Spring Boot (Java).** Quem modela, implementa e consome é a mesma pessoa — o objetivo deste doc deixa de ser "acordo entre sócios" e passa a ser **decisão registrada antes da primeira migration**: sem par para pegar modelagem ruim no mês 2 (doc 00 §3.4), o que está escrito aqui é a revisão. Banco: **PostgreSQL + pgvector**. Multi-tenancy: `tenant_id` + RLS (decidido no doc 05 — aqui detalhamos o COMO, inclusive o encaixe com o pool de conexões do Spring, §2.1).
>
> O ER, as entidades e as decisões de modelagem são agnósticos de stack e **sobreviveram intactos** à troca Node/Python → Java. O que mudou: migrations (Flyway, §7), acesso a dados (§3), RLS × pool do Spring (§2.1) e pgvector a partir de Java (§5.1).

---

## 1. 🗺️ Diagrama ER (visão geral)

```
                       ┌──────────┐
                       │  tenant  │ (escritório = unidade de cobrança e isolamento)
                       └────┬─────┘
          ┌─────────────┬───┴────────┬───────────────┬─────────────┐
          ▼             ▼            ▼               ▼             ▼
     ┌────────┐    ┌─────────┐  ┌───────────┐  ┌───────────┐ ┌───────────┐
     │usuario │    │ cliente │  │ template  │  │assinatura │ │ audit_log │
     └────┬───┘    └────┬────┘  └─────┬─────┘  └───────────┘ └───────────┘
          │             │             │
          │        ┌────▼─────┐       │ (RAG)
          │        │ processo │◄──────┼──────────────┐
          │        └────┬─────┘       │              │
          │      ┌──────┼─────────────┼──────┐       │
          │      ▼      ▼             │      ▼       │
          │ ┌─────────────┐  ┌────────▼──┐ ┌─────────┴───┐
          │ │movimentacao │  │   peca    │ │  embedding  │
          │ └─────────────┘  └─────┬─────┘ │ (pgvector)  │
          │                        │       └─────────────┘
          │  ┌────────────┐        │ (peca gerada a partir de intimacao)
          │  │ publicacao │────────┘
          │  │/intimacao  │
          │  └─────┬──────┘
          │        │ 1:N
          │        ▼
          │  ┌──────────┐     ┌──────────────────┐
          └─►│  prazo   │────►│ calculo_prazo    │ (memória auditável)
   (responsável)└────┬───┘    └──────────────────┘
                     │ consulta
                     ▼
             ┌──────────────────┐
             │ feriado_forense  │ (GLOBAL, sem tenant — dado público)
             └──────────────────┘
```

Leitura do fluxo do "aha": `publicacao` chega (ingestão DJEN) → IA classifica e liga ao `processo` → gera `prazo` (com `calculo_prazo` auditável) → advogado gera `peca` → edita → exporta. Cada seta desse fluxo é uma FK abaixo.

---

## 2. 🏢 Multi-tenancy: `tenant_id` + Row Level Security

**Decisão (doc 05):** todas as tabelas de dados de negócio carregam `tenant_id UUID NOT NULL` + RLS do Postgres. Detalhamento:

```sql
-- 1. Papel da aplicação NUNCA é superuser nem dono das tabelas
--    (dono da tabela ignora RLS por padrão; superuser/BYPASSRLS também)
CREATE ROLE app_user LOGIN;  -- usado pela API (Spring)
ALTER ROLE app_user SET row_security = on;

-- 2. Toda TRANSAÇÃO seta o tenant via GUC — nunca por sessão (ver §2.1)
--    Forma parametrizável (SET LOCAL não aceita bind):
SELECT set_config('app.tenant_id', '3f0a...-uuid-do-tenant', true);
--                                                            ^ true = escopo de transação

-- 3. Política padrão, repetida em TODA tabela tenantizada
ALTER TABLE processo ENABLE ROW LEVEL SECURITY;
ALTER TABLE processo FORCE ROW LEVEL SECURITY;   -- vale até p/ dono da tabela
CREATE POLICY tenant_isolation ON processo
  USING (tenant_id = current_setting('app.tenant_id')::uuid)
  WITH CHECK (tenant_id = current_setting('app.tenant_id')::uuid);
```

Regras de engenharia:

| Regra | Por quê |
|---|---|
| GUC com escopo de **transação** (`set_config(..., true)` / `SET LOCAL`), nunca `SET` de sessão | Pool de conexões (HikariCP) reusa conexões entre tenants; valor de sessão vazaria de um request para outro. O valor transacional morre no commit/rollback |
| Workers (jobs Spring / fila Redis) recebem `tenant_id` no payload do job e abrem transação setando o GUC antes de qualquer query | Ingestão e pipeline de IA rodam fora do request HTTP — o contexto de tenant não pode depender do filtro web |
| `WITH CHECK` obrigatório, não só `USING` | Impede INSERT/UPDATE gravando no tenant errado, não só leitura |
| Um único role `admin_user` sem RLS para migrations (Flyway roda com ele) e jobs verdadeiramente globais (ingestão do DJEN bruto, feriados) | Mínimo necessário, auditado |
| Teste automatizado de isolamento no CI: cria 2 tenants, tenta vazar em cada tabela nova | RLS esquecida numa tabela nova é o bug mais provável e o mais grave (sigilo profissional, doc 09) — e solo, sem revisor de código (doc 00 §9.2), o teste É o revisor |
| `tenant_id` é sempre a **primeira coluna dos índices compostos** (`(tenant_id, ...)`) | RLS filtra por ele em toda query; sem isso, seq scan |
| A aplicação **continua** filtrando por `tenant_id` nas queries | RLS é rede de segurança, não substituto do WHERE — defesa em profundidade e uso correto dos índices |

Por que RLS e não schema-por-tenant: centenas de tenants pequenos (ICP = solo), migrations 1x só, e RLS cobre o risco nº 1 (vazamento entre tenants) no nível do banco — bug na API não vaza dado.

### 2.1 ⚠️ RLS × pool de conexões do Spring — o problema real

Spring Boot usa **HikariCP** por padrão, e o pool **reusa a mesma conexão física para tenants diferentes**. É aqui que implementações de RLS morrem. As regras:

1. **O GUC só existe dentro da transação.** `SELECT set_config('app.tenant_id', ?, true)` como **primeiro statement** da transação — parametrizável (evita injection; `SET LOCAL` puro não aceita bind de parâmetro). Como morre no commit/rollback, a conexão volta limpa para o pool, seja qual for o próximo tenant.
2. **Mesma conexão, mesma transação.** O `set_config` precisa executar na MESMA conexão e MESMA transação das queries que protege. Na prática: toda operação tenantizada roda dentro de `@Transactional`, e um ponto único de infraestrutura executa o `set_config` no início da transação lendo o tenant do contexto de autenticação (JWT). ⚠️ O hook exato — aspect AOP ordenado para rodar DEPOIS do advisor de transação, listener de início de transação do Hibernate, ou um `TransactionTemplate` próprio que envelopa os serviços — é decisão de implementação a validar no **spike de fundações (mês 1)**, com teste provando que o GUC está presente na conexão certa. Não assumir que "funcionou no teste manual" = funciona sob pool com concorrência.
3. **Cuidado com auto-commit.** Query executada FORA de transação roda em auto-commit e não passa pelo `set_config` → a política avalia com GUC ausente. Convenção dura: repositório tenantizado só é chamado de serviço `@Transactional`. ⚠️ Vale avaliar um teste de arquitetura (ex.: ArchUnit) para enforçar isso mecanicamente — de novo, é o tipo de disciplina que um revisor humano cobraria.
4. **Fail-closed, e testado.** Com `current_setting('app.tenant_id')` (sem segundo argumento) a política dá **erro** quando o GUC não foi setado — barulhento, e é o que queremos: query tenantizada sem contexto deve FALHAR, nunca retornar tudo. (A variante `current_setting(..., true)` retorna NULL → zero linhas — silenciosa demais; preferimos o erro.) O teste de isolamento do CI cobre os dois cenários: tenant errado E GUC ausente.
5. **PgBouncer futuro:** se um dia entrar PgBouncer em transaction pooling na frente, o desenho transacional continua correto — é exatamente o modo compatível. Nada a mudar.

---

## 3. 🔌 Acesso a dados no Spring (JPA × jOOQ × JdbcTemplate)

O projeto tem duas cargas de trabalho muito diferentes: **CRUD comum** (processos, clientes, peças, usuários — o grosso das telas) e **SQL pesado e literal** (busca vetorial pgvector, upserts idempotentes da ingestão, agregação de cota, e o motor de prazos que grava memória JSONB). Nenhuma opção única serve bem às duas.

| Opção | Forças | Fraquezas para nós |
|---|---|---|
| **Spring Data JPA / Hibernate** | Produtividade máxima no CRUD (repositórios derivados, paginação, auditoria de created/updated); território conhecido do Ronny | SQL gerado opaco; lazy-loading surpresa; péssimo para `<=>` do pgvector, `ON CONFLICT` e CTEs |
| **jOOQ** | SQL tipado, gerado do schema — pega erro de coluna em compile time; ótimo exatamente para o nosso SQL pesado | Codegen no build + curva de aprendizado + mais uma dependência central para um dev solo; ⚠️ conferir licença/edição para Postgres OSS antes de qualquer adoção |
| **JdbcTemplate / JdbcClient** | Zero mágica: o SQL que está neste doc é o SQL que executa — ideal para RLS, pgvector e cálculo de prazo auditáveis | Verboso para CRUD; mapeamento manual |

### ✅ Recomendação: híbrido **Spring Data JPA (CRUD) + JdbcClient (SQL pesado)**

- **JPA/Hibernate** nas entidades de negócio comuns: `processo`, `cliente`, `usuario`, `peca` (metadados), `template`, `assinatura`, `prazo` (leitura/agenda). É onde a produtividade paga o aluguel a 12,5h/sem.
- **SQL nativo via `JdbcClient`** (API fluente sobre JDBC do Spring 6.1+ ⚠️ confirmar disponibilidade na versão adotada; `JdbcTemplate` cobre igual se não estiver) para: busca vetorial (§5.1), upserts de ingestão (`INSERT ... ON CONFLICT (hash_dedup) DO NOTHING`), incremento transacional de `uso_ia`, escrita de `calculo_prazo` e qualquer query com CTE/window. **O SQL crítico do produto não passa por ORM — fica literal, versionado e auditável.**
- **jOOQ fica como upgrade futuro**, não fundação: se o volume de SQL nativo crescer a ponto de erro de digitação virar dor recorrente, reavaliar. Não pagar codegen + curva no mês 1.

Regras acessórias:
- `spring.jpa.hibernate.ddl-auto=validate` — **o schema é do Flyway (§7), nunca do Hibernate.** `ddl-auto=update` em produção é proibido.
- Colunas JSONB (`peca.conteudo`, `calculo_prazo.passos`...) mapeadas no JPA via suporte a JSON do Hibernate 6 (⚠️ `@JdbcTypeCode(SqlTypes.JSON)` — validar mapeamento e round-trip no spike; alternativa sempre disponível: tratar como `String` e (de)serializar com Jackson na borda).
- Soft delete no JPA: `@SQLRestriction("deleted_at IS NULL")` nas entidades (⚠️ Hibernate 6.3+; substitui o `@Where` deprecado — confirmar na versão adotada). Em queries nativas o filtro é explícito — sem exceção.
- RLS não se importa com a camada de acesso: age no banco, cobre JPA e JDBC igualmente. Mas o §2.1 (transação obrigatória) vale para AMBOS.

---

## 4. 📦 Schema comentado (tabelas principais)

Convenções: PK `id UUID DEFAULT gen_random_uuid()`; `created_at/updated_at TIMESTAMPTZ`; soft delete via `deleted_at TIMESTAMPTZ NULL` (ver §7); `tenant_id` em tudo salvo indicação.

```sql
-------------------------------------------------------------------
-- TENANT: o escritório. Unidade de isolamento, cobrança e cota.
-------------------------------------------------------------------
CREATE TABLE tenant (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nome            TEXT NOT NULL,
  cnpj_cpf        TEXT,                         -- solo pode ser CPF
  plano           TEXT NOT NULL DEFAULT 'trial',-- trial|solo|escritorio|pro
  status          TEXT NOT NULL DEFAULT 'ativo',-- ativo|inadimplente|cancelado
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at      TIMESTAMPTZ
);
-- Decisão: mesmo o advogado solo é um tenant (não "usuário avulso").
-- Upgrade p/ Escritório = adicionar usuários, zero migração de dados.

-------------------------------------------------------------------
-- USUARIO: advogado ou estagiário dentro do tenant.
-------------------------------------------------------------------
CREATE TABLE usuario (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenant(id),
  email           CITEXT NOT NULL UNIQUE,       -- login global, único no sistema
  nome            TEXT NOT NULL,
  oab_numero      TEXT,                         -- pode ter várias OABs? Fase 1: uma
  oab_uf          CHAR(2),
  papel           TEXT NOT NULL DEFAULT 'advogado', -- advogado|admin (fase 1 simples)
  auth_provider   TEXT NOT NULL DEFAULT 'password', -- password|google
  senha_hash      TEXT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at      TIMESTAMPTZ
);
-- Decisão: (oab_numero, oab_uf) fica no usuário, não no tenant —
-- é a chave da importação DataJud e do matching de publicações no DJEN.

-------------------------------------------------------------------
-- CLIENTE: o cliente do advogado (dado PESSOAL — LGPD, ver §7).
-------------------------------------------------------------------
CREATE TABLE cliente (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenant(id),
  nome            TEXT NOT NULL,
  cpf_cnpj        TEXT,
  contatos        JSONB NOT NULL DEFAULT '{}',  -- {email, telefone, whatsapp} — flexível p/ Fase 2
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at      TIMESTAMPTZ
);

-------------------------------------------------------------------
-- PROCESSO: unidade central. Importado do DataJud ou criado à mão.
-------------------------------------------------------------------
CREATE TABLE processo (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenant(id),
  numero_cnj      TEXT NOT NULL,                -- 0001234-56.2026.5.12.0001, validado por dígito
  tribunal        TEXT NOT NULL,                -- 'TRT12', 'TJSC'... (chave p/ feriados!)
  grau            TEXT,                         -- G1|G2|sup
  classe          TEXT,                         -- da taxonomia CNJ
  assunto         TEXT,
  area            TEXT,                         -- trabalhista|civel|... (classificada por IA)
  polo_cliente    TEXT,                         -- ativo|passivo (onde está o cliente do advogado)
  valor_causa     NUMERIC(14,2),
  status          TEXT NOT NULL DEFAULT 'ativo',-- ativo|arquivado|encerrado
  fonte           TEXT NOT NULL DEFAULT 'datajud', -- datajud|manual|provedor_pago
  dados_fonte     JSONB,                        -- payload bruto da importação (auditoria/debug)
  monitorado      BOOLEAN NOT NULL DEFAULT true,-- conta na cota "processos monitorados" do plano
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at      TIMESTAMPTZ,
  UNIQUE (tenant_id, numero_cnj)                -- mesmo processo pode existir em 2 tenants!
);
CREATE INDEX idx_processo_tenant_status ON processo (tenant_id, status);

-- Relação processo↔cliente é N:N (litisconsórcio; cliente com vários processos)
CREATE TABLE processo_cliente (
  tenant_id   UUID NOT NULL,
  processo_id UUID NOT NULL REFERENCES processo(id),
  cliente_id  UUID NOT NULL REFERENCES cliente(id),
  papel       TEXT,                             -- reclamante|reclamado|autor|réu...
  PRIMARY KEY (processo_id, cliente_id)
);

-------------------------------------------------------------------
-- MOVIMENTACAO: andamentos vindos do DataJud (append-only).
-------------------------------------------------------------------
CREATE TABLE movimentacao (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  processo_id     UUID NOT NULL REFERENCES processo(id),
  data_movimento  TIMESTAMPTZ NOT NULL,
  codigo_cnj      INT,                          -- código da TPU/CNJ quando houver
  descricao       TEXT NOT NULL,
  fonte           TEXT NOT NULL DEFAULT 'datajud',
  hash_dedup      TEXT NOT NULL,                -- sha256(processo+data+descricao) p/ idempotência
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (processo_id, hash_dedup)              -- reimportação não duplica
);
-- Append-only: nunca UPDATE/DELETE. Re-sync compara por hash_dedup
-- (INSERT ... ON CONFLICT DO NOTHING — SQL nativo, §3).

-------------------------------------------------------------------
-- PUBLICACAO (intimação/comunicação): o gatilho do fluxo inteiro.
-------------------------------------------------------------------
CREATE TABLE publicacao (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id         UUID NOT NULL,
  processo_id       UUID REFERENCES processo(id),  -- NULL se não conseguimos vincular ainda
  fonte             TEXT NOT NULL,                 -- djen|comunica_cnj|manual
  data_publicacao   DATE NOT NULL,                 -- data de PUBLICAÇÃO (insumo do prazo!)
  data_disponibilizacao DATE,                      -- DJEN: disponibilização ≠ publicação
  texto_integral    TEXT NOT NULL,
  hash_dedup        TEXT NOT NULL,
  -- ↓ resultado da classificação IA (doc 06, output estruturado)
  class_status      TEXT NOT NULL DEFAULT 'pendente',
                    -- pendente|classificada|baixa_confianca|erro|revisada_humano
  tipo_ato          TEXT,                          -- intimacao_contestacao|sentenca|despacho|ciencia|...
  providencia       TEXT,                          -- texto curto: "Apresentar contestação"
  urgencia          TEXT,                          -- alta|media|baixa|nenhuma
  resumo_leigo      TEXT,
  class_confianca   NUMERIC(4,3),                  -- 0.000–1.000 (corte definido na eval, doc 13)
  class_modelo      TEXT,                          -- ex.: 'claude-haiku-4-5' — rastreabilidade
  class_prompt_ver  TEXT,                          -- ex.: 'classificador/v3' — bate com repo de prompts
  revisada_por      UUID REFERENCES usuario(id),   -- quando humano corrige a classificação
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at        TIMESTAMPTZ,
  UNIQUE (tenant_id, hash_dedup)
);
-- Decisões:
-- 1. Classificação mora NA publicação (1:1), não em tabela separada — simples,
--    e o histórico de correção humana fica em audit_log + revisada_por.
-- 2. class_modelo + class_prompt_ver em toda linha: quando trocarmos prompt/modelo,
--    conseguimos medir regressão sobre dados reais (doc 13). O output estruturado
--    vem do SDK anthropic-java (JSON Schema derivado de record — doc 00 §0.1).
-- 3. processo_id NULL permitido: publicação chega antes do processo às vezes;
--    fila de "vincular manualmente" na UI.

-------------------------------------------------------------------
-- PRAZO: o coração. SEMPRE derivado de cálculo determinístico.
-------------------------------------------------------------------
CREATE TABLE prazo (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id         UUID NOT NULL,
  processo_id       UUID NOT NULL REFERENCES processo(id),
  publicacao_id     UUID REFERENCES publicacao(id),  -- origem (NULL se prazo manual)
  responsavel_id    UUID REFERENCES usuario(id),
  titulo            TEXT NOT NULL,                 -- "Contestação — Maria S. × Acme"
  tipo_peca         TEXT,                          -- contestacao|replica|recurso_inominado|simples|null
                                                   -- (o PRAZO existe p/ qualquer tipo; o REDATOR na
                                                   -- Fase 1 só cobre contestacao|simples — doc 00 §2)
  data_inicio       DATE NOT NULL,                 -- início da contagem
  qtd_dias          INT NOT NULL,
  contagem          TEXT NOT NULL,                 -- dias_uteis|dias_corridos
  data_limite       DATE NOT NULL,                 -- SAÍDA do motor determinístico
  calculo_id        UUID NOT NULL,                 -- FK → calculo_prazo (memória, abaixo)
  status            TEXT NOT NULL DEFAULT 'aberto',-- aberto|cumprido|perdido|cancelado
  origem            TEXT NOT NULL DEFAULT 'auto',  -- auto|manual|auto_editado
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at        TIMESTAMPTZ
);
CREATE INDEX idx_prazo_agenda ON prazo (tenant_id, status, data_limite);
-- Decisão: a IA SUGERE qtd_dias/contagem (extraídos do texto); a data_limite
-- SÓ pode ser escrita pelo motor de cálculo (enforçado na camada de serviço:
-- único código com permissão de INSERT/UPDATE em prazo.data_limite + calculo_id NOT NULL).
-- Advogado pode editar qtd_dias/data_inicio → origem='auto_editado' → RECALCULA.

-------------------------------------------------------------------
-- CALCULO_PRAZO: memória de cálculo auditável (imutável).
-------------------------------------------------------------------
CREATE TABLE calculo_prazo (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  engine_versao   TEXT NOT NULL,       -- versão do motor (lib nossa) — reprodutibilidade
  entrada         JSONB NOT NULL,      -- {data_publicacao, regra_inicio, qtd_dias, contagem, tribunal}
  passos          JSONB NOT NULL,      -- [{data:'2026-07-02', conta:false, motivo:'sábado'},
                                       --  {data:'2026-07-09', conta:false,
                                       --   motivo:'feriado forense', feriado_id:'...'}, ...]
  resultado       DATE NOT NULL,
  feriados_snapshot_at TIMESTAMPTZ NOT NULL, -- quando a tabela de feriados foi consultada
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- IMUTÁVEL (sem UPDATE). Recalculou? Nova linha, prazo aponta p/ nova.
-- É o que a UI mostra no popover "memória de cálculo" (doc 10) e a
-- nossa defesa se alguém alegar erro de prazo.

-------------------------------------------------------------------
-- FERIADO_FORENSE: GLOBAL (sem tenant_id, sem RLS) — dado público.
-------------------------------------------------------------------
CREATE TABLE feriado_forense (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  data        DATE NOT NULL,
  escopo      TEXT NOT NULL,   -- 'nacional' | 'tribunal' | 'estadual' | 'municipal'
  tribunal    TEXT,            -- 'TRT12', 'TJSC'... NULL se nacional
  uf          CHAR(2),
  municipio   TEXT,            -- comarca, quando aplicável
  descricao   TEXT NOT NULL,   -- "Recesso forense art. 220 CPC", "Aniversário de Fpolis"
  fonte       TEXT NOT NULL,   -- URL/portaria de onde veio — auditável
  suspende_prazo BOOLEAN NOT NULL DEFAULT true, -- feriado ≠ suspensão de expediente às vezes
  valido_de   DATE, valido_ate DATE,            -- recesso é intervalo, não dia
  created_by  UUID,            -- quem cadastrou (nós, via admin)
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_feriado_lookup ON feriado_forense (data, tribunal, uf);
-- Consulta do motor: nacional + (tribunal do processo) + (uf) + recessos que
-- contenham a data. Curadoria MANUAL nossa por tribunal-alvo da Fase 1
-- (nicho trabalhista: começar por TRTs do nicho + TST). ⚠️ Não existe fonte
-- única oficial consolidada — manter a curadoria é tarefa recorrente e, solo,
-- ela sai do buffer de 30% (doc 00 §3.4).

-------------------------------------------------------------------
-- PECA: documento gerado/editado. Conteúdo ProseMirror como JSON.
-------------------------------------------------------------------
CREATE TABLE peca (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  processo_id     UUID NOT NULL REFERENCES processo(id),
  publicacao_id   UUID REFERENCES publicacao(id),   -- intimação que originou
  prazo_id        UUID REFERENCES prazo(id),
  autor_id        UUID NOT NULL REFERENCES usuario(id),
  tipo            TEXT NOT NULL,      -- contestacao|simples (Fase 1);
                                      -- replica|recurso_inominado entram pós-lançamento (doc 00 §2)
  titulo          TEXT NOT NULL,
  conteudo        JSONB NOT NULL,     -- doc TipTap/ProseMirror (JSON, não HTML — nodes custom
                                      -- [VERIFICAR]/dado interpolado do wrapper Angular, doc 10 §2d)
  estado          TEXT NOT NULL DEFAULT 'rascunho_ia',
                  -- rascunho_ia|em_revisao|revisado  (máquina de estados do doc 10)
  revisado_por    UUID REFERENCES usuario(id),
  revisado_em     TIMESTAMPTZ,
  ger_modelo      TEXT,               -- modelo que gerou (null se 100% manual)
  ger_prompt_ver  TEXT,               -- versão do prompt (doc 13)
  conteudo_gerado JSONB,              -- SNAPSHOT IMUTÁVEL do texto como a IA entregou
  tokens_in INT, tokens_out INT, custo_estimado NUMERIC(8,4), -- controle de margem (doc 08)
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at      TIMESTAMPTZ
);
-- conteudo_gerado é ESSENCIAL: é o baseline do diff "% que sobrevive à
-- edição" (doc 06/13). Sem snapshot, a métrica norte não existe.
-- Cota de peças do plano = COUNT(*) WHERE ger_modelo IS NOT NULL no mês.

-------------------------------------------------------------------
-- TEMPLATE: modelos por nicho/tipo (nossos, curados) p/ RAG e estrutura.
-------------------------------------------------------------------
CREATE TABLE template (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id   UUID,               -- NULL = template global nosso; fase 2: do tenant (estilo)
  nicho       TEXT NOT NULL,      -- trabalhista
  tipo        TEXT NOT NULL,      -- contestacao|simples (Fase 1); replica|... depois
  titulo      TEXT NOT NULL,
  conteudo    TEXT NOT NULL,      -- markdown/texto com placeholders
  versao      INT NOT NULL DEFAULT 1,
  ativo       BOOLEAN NOT NULL DEFAULT true,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- RLS especial: USING (tenant_id IS NULL OR tenant_id = current_setting(...)).

-------------------------------------------------------------------
-- EMBEDDING: pgvector. Uma tabela só, polimórfica por origem.
-------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE embedding (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     UUID,                -- NULL p/ chunks de template global
  origem_tipo   TEXT NOT NULL,       -- 'template' | 'peca' | 'publicacao' | 'auto' (fase 1.5)
  origem_id     UUID NOT NULL,
  chunk_index   INT NOT NULL,
  chunk_texto   TEXT NOT NULL,       -- guardamos o texto: reindexar sem re-chunk + citação na UI
  embedding     VECTOR(1024),        -- ⚠️ dimensão depende do modelo de embedding (decisão set/2026)
  modelo_emb    TEXT NOT NULL,       -- troca de modelo de embedding = reindexação completa
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (origem_tipo, origem_id, chunk_index, modelo_emb)
);
CREATE INDEX idx_embedding_hnsw ON embedding
  USING hnsw (embedding vector_cosine_ops);
-- Busca SEMPRE com filtro: WHERE (tenant_id = ... OR tenant_id IS NULL)
--   AND origem_tipo = ANY(...) ORDER BY embedding <=> $query LIMIT k.
-- NUNCA buscar sem filtro de tenant → doc 05: "prompts sempre escopados ao tenant".

-------------------------------------------------------------------
-- ASSINATURA / BILLING — Fase 1: cobrança por LINK MANUAL (doc 00 corte #5)
-------------------------------------------------------------------
CREATE TABLE assinatura (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id         UUID NOT NULL UNIQUE REFERENCES tenant(id),
  gateway           TEXT,                   -- NULL na Fase 1 (link manual);
  gateway_sub_id    TEXT,                   -- preenchidos quando o billing automático
                                            -- entrar (mês 16–17; gateway: decisão #6, set/2027)
  plano             TEXT NOT NULL,          -- solo|escritorio|pro
  ciclo             TEXT NOT NULL,          -- mensal|anual
  status            TEXT NOT NULL,          -- trialing|active|past_due|canceled
  founding          BOOLEAN NOT NULL DEFAULT false,
  desconto_pct      INT NOT NULL DEFAULT 0, -- 30 p/ founding ano 1 (doc 00 §6)
  desconto_ate      DATE,
  preco_congelado   NUMERIC(8,2),           -- founding: preço travado (doc 00 §6)
  periodo_atual_ate DATE,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- No lançamento, status/periodo_atual_ate são mantidos À MÃO (tela admin simples)
-- conforme os links de pagamento são pagos. A tabela já nasce no formato do
-- billing automático — quando o gateway entrar, é preencher, não migrar.

CREATE TABLE uso_ia (           -- medição de cota e custo, agregada por mês
  tenant_id   UUID NOT NULL,
  mes         DATE NOT NULL,    -- primeiro dia do mês
  pecas_geradas INT NOT NULL DEFAULT 0,
  classificacoes INT NOT NULL DEFAULT 0,
  chat_perguntas INT NOT NULL DEFAULT 0,   -- fase 1.5
  custo_estimado NUMERIC(10,4) NOT NULL DEFAULT 0,
  PRIMARY KEY (tenant_id, mes)
);
-- Atualizada por incremento transacional no momento do uso (não por job) —
-- a checagem de cota precisa ser síncrona e barata. (UPSERT nativo, §3.)

-------------------------------------------------------------------
-- AUDIT_LOG: quem viu/alterou o quê (doc 05 — argumento de venda LGPD)
-------------------------------------------------------------------
CREATE TABLE audit_log (
  id          BIGSERIAL PRIMARY KEY,     -- serial: append-only, ordenação barata
  tenant_id   UUID NOT NULL,
  usuario_id  UUID,                      -- NULL = sistema/worker
  acao        TEXT NOT NULL,             -- 'peca.revisada','prazo.editado','classificacao.corrigida',
                                         -- 'export.docx','cliente.excluido_lgpd',...
  entidade    TEXT NOT NULL, entidade_id UUID,
  detalhe     JSONB,                     -- diff/valores relevantes
  ip          INET,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- Append-only. Sem UPDATE/DELETE (revogar na role). Eventos mínimos fase 1:
-- transições de estado de peça, edição/cumprimento de prazo, correção de
-- classificação, export, login, exclusões LGPD.
```

---

## 5. 🧠 pgvector: chunking e indexação

**Onde entra (Fase 1):** RAG do redator = `template` (nossos modelos por tipo de peça) + `peca` (peças anteriores do MESMO processo) + `publicacao` (a intimação-alvo). "Chat com autos" (fase 1.5) adiciona `origem_tipo='auto'` com OCR dos PDFs — a tabela já nasce pronta para isso.

| Decisão | Valor | Racional |
|---|---|---|
| Estratégia de chunk | Por **estrutura jurídica**, não por tamanho fixo: seções da peça (preliminares, mérito por tópico, pedidos) e parágrafos numerados; alvo 300–800 tokens, overlap ~10% | Peça jurídica tem estrutura forte; chunk que corta um tópico de mérito no meio recupera lixo |
| Metadado no chunk | prefixar o texto do chunk com contexto: `[Contestação trabalhista — Mérito — horas extras]` | Melhora recall com custo ~zero |
| Índice | HNSW, `vector_cosine_ops` | Volume pequeno (milhares de chunks/tenant); HNSW dispensa tuning de listas do IVFFlat |
| Busca | híbrida simples: vetor + filtro SQL (tenant, nicho, tipo_peca) · top-k 8–12 → dedup por origem | Filtro SQL primeiro barateia e melhora precisão |
| Reindexação | `modelo_emb` na unique constraint permite manter 2 gerações durante troca de modelo | Troca de embedding sem downtime |
| ⚠️ Dimensão do vetor | Depende do modelo de embedding (via API externa — doc 00 §3.1) — **decisão set/2026, junto com o spike de RAG**. Coluna criada por migration só após a decisão | Evitar `ALTER` de dimensão depois |

### 5.1 ⚠️ pgvector a partir de Java

O tipo `vector` não é um tipo JDBC padrão — o driver Postgres não o conhece nativamente. Estratégia em camadas, da mais garantida para a mais confortável:

1. **Plano A (funciona sempre): SQL nativo com cast.** Vetor serializado como literal texto `'[0.12,0.34,...]'` e passado como parâmetro com cast: `ORDER BY embedding <=> CAST(? AS vector)`. Insert idem. Verboso, mas 100% previsível — e o nosso volume de queries vetoriais é pequeno (uma busca por geração de peça). Via `JdbcClient` (§3).
2. **Plano B (conforto): biblioteca de apoio.** ⚠️ Existe biblioteca Java da própria comunidade pgvector (artefato `com.pgvector:pgvector`) que registra o tipo no driver e evita a serialização manual — **verificar no spike** estado de manutenção e compatibilidade com a versão do driver antes de adotar.
3. **Plano C (integração ORM): Hibernate.** ⚠️ Versões recentes do Hibernate têm módulo de suporte a vetores (hibernate-vector) com mapeamento do tipo e funções de distância — meu conhecimento dos detalhes (versões, sintaxe HQL, cobertura do pgvector) é superfical demais para contar com isso no planejamento. **Não é pré-requisito de nada:** o Plano A cobre a Fase 1 inteira.

Regra: a decisão entre A/B/C sai do **spike de RAG (set/2026)**, junto com o modelo de embedding. O schema e o índice acima não mudam em nenhum dos três casos.

---

## 6. 🧮 Motor determinístico de prazos (modelagem)

O doc 06 manda: **o LLM nunca calcula data**. A modelagem separa três responsabilidades:

```
IA (extração)              MOTOR (cálculo puro)             BANCO (auditoria)
─────────────              ────────────────────             ─────────────────
publicacao.tipo_ato        f(data_inicio, qtd_dias,         calculo_prazo
publicacao → sugere        contagem, tribunal, uf,          (entrada, passos,
qtd_dias + contagem   →    feriado_forense[]) → data   →    resultado, versão)
(revisável na UI)          FUNÇÃO PURA, sem IO no meio      imutável
                           (feriados carregados antes)
```

Regras que o motor implementa (fase 1, nicho trabalhista):

1. **Início da contagem:** publicação no DJEN → considera-se publicado no 1º dia útil seguinte à disponibilização; contagem começa no dia útil seguinte à publicação (regra parametrizada em `entrada.regra_inicio` — CLT e CPC divergem em detalhes; a regra aplicada fica GRAVADA na memória).
2. **Dias úteis:** pula sábado, domingo e todo `feriado_forense` aplicável ao par (tribunal, uf, municipio) com `suspende_prazo = true`, incluindo intervalos (recesso 20/12–20/01, art. 775-A CLT / 220 CPC).
3. **Vencimento em dia não útil → prorroga** para o próximo dia útil (também registrado como passo).
4. Cada dia avaliado vira um item em `calculo_prazo.passos` com motivo — é o popover da UI (doc 10).

Engenharia:
- Motor = **biblioteca Java pura** (sem IO, sem Spring) com bateria de testes de tabela-verdade — testes parametrizados com casos reais por tribunal, incluindo virada de ano/recesso. É o código mais testado do produto (as 65h do bloco no doc 00 §3.1 incluem "testes pesados") — e, solo, essa suite é o substituto do revisor (doc 00 §9.2).
- `engine_versao` gravada em cada cálculo. Bug corrigido no motor → job recalcula prazos `status='aberto'`, gera novas linhas de `calculo_prazo`, e **notifica** o advogado (por e-mail — Fase 1 não tem push, doc 00 corte #2) se alguma `data_limite` mudou. Nunca mudar prazo silenciosamente.
- Curadoria de `feriado_forense` é processo operacional: checklist mensal (portarias dos tribunais-alvo) + alerta interno se um tribunal ficar >60 dias sem revisão. ⚠️ Único ponto do motor que depende de dado externo — se o feriado não está na tabela, o cálculo sai errado com memória "certa". Mitigação: cobertura mínima obrigatória (nacionais + recesso + tribunais do nicho) antes do beta.

---

## 7. 🔁 Migrations, soft delete e retenção (LGPD)

### Migrations — ✅ decisão: **Flyway** (não Liquibase)

Por quê:
- **Nosso repositório de migrations é SQL puro por natureza:** políticas RLS, `CREATE EXTENSION vector`, índice HNSW, GUCs — nada disso ganha com a camada de abstração (XML/YAML/changelog) do Liquibase. Flyway é "arquivos `V###__descricao.sql` versionados, aplicados em ordem" — exatamente o modelo mental deste doc.
- **Integração de primeira classe com Spring Boot** (starter oficial; roda no startup em dev, e como passo explícito do pipeline em produção).
- O que o Liquibase daria a mais — suporte multi-banco e rollbacks gerenciados — não usamos: é um banco só, e a regra da casa já era "rollback é plano escrito no PR", não automação.
- ⚠️ A edição community do Flyway basta para o nosso uso (migrations versionadas + repeatable); recursos pagos (undo etc.) não entram na conta. Conferir o licenciamento vigente da versão adotada na hora do setup.

Requisitos (mantidos da versão anterior — independem da ferramenta):
- Migrations SQL versionadas no repo, aplicadas por pipeline (nunca DDL de ORM em prod — `ddl-auto=validate`, §3);
- Migrations rodam com o role `admin_user` (dono), nunca `app_user` (§2);
- **Template de migration** que já inclui RLS (`ENABLE` + `FORCE` + política) + índice `(tenant_id, ...)` para tabela nova — e o teste de isolamento do CI quebra se esquecer;
- Toda migration reversível ou com plano de rollback escrito no PR;
- Seeds separados e versionados: `feriado_forense` e `template` globais como repeatable migrations (`R__seed_*.sql`) ou job de seed próprio.

### Soft delete
- Padrão: `deleted_at` em entidades de negócio (processo, cliente, peca, prazo, publicacao). No JPA, via `@SQLRestriction` (§3); em SQL nativo, filtro explícito sempre.
- **Exceções:** `movimentacao`, `calculo_prazo`, `audit_log` são append-only sem soft delete (imutáveis por natureza); `feriado_forense` usa correção com nova linha.
- Soft delete NÃO é resposta LGPD — é UX (lixeira, undo). LGPD exige o hard path abaixo.

### Retenção e o conflito LGPD × guarda

O conflito real: titular (cliente do advogado) pode pedir exclusão, mas o **advogado tem dever de guarda** dos autos/documentos do mandato, e nós somos **operadores** — o controlador é o advogado (doc 09). Tradução para o sistema:

| Cenário | O que fazemos |
|---|---|
| Titular pede exclusão AO ADVOGADO | Ferramenta "excluir/anonimizar cliente": hard-delete de `cliente.contatos` e `cpf_cnpj`, `nome` → pseudônimo ("Cliente excluído a pedido"), MAS peças e processos permanecem (base legal: exercício regular de direito/obrigação do mandato). Ação logada em `audit_log` com base legal |
| Tenant cancela assinatura | Dados retidos **90 dias** (janela de arrependimento/export), depois job de expurgo: hard delete de dados de negócio do tenant + embeddings; `audit_log` e registros fiscais/billing retidos por 5 anos (obrigação legal) |
| Tenant pede exclusão imediata | Mesmo expurgo, sem janela, mediante confirmação forte. Export .zip (docx das peças + CSV de processos/prazos) oferecido antes — portabilidade |
| Dados enviados a LLMs | Não persistem lá (contrato de não-treinamento, doc 06); nosso lado: `dados_fonte`/payloads brutos de ingestão expurgáveis após 12 meses (job) |

- **Expurgo tem que alcançar TUDO:** tabelas de negócio, `embedding` (fácil esquecer!), objetos no R2 (docx exportados, PDFs), backups. Backups: expurgo lógico + política de expiração de backup em 30 dias (doc 05) fecha o ciclo — documentar isso na política de privacidade.
- Tabela `lgpd_request (tenant_id, tipo, solicitante, status, executado_em)` para rastrear pedidos — vira o "processo de atendimento a direitos do titular" do checklist do doc 09.

---

## 8. ✅ Checklist de aceite deste modelo (antes da 1ª migration)

- [ ] **Spike de fundações (mês 1):** mecanismo `@Transactional` + `set_config` validado sob pool (HikariCP), com teste provando GUC na conexão certa e comportamento fail-closed sem GUC (§2.1)
- [ ] Teste de isolamento RLS escrito **ANTES da segunda tabela existir** — é o revisor de código que não temos
- [ ] Cada tela do doc 10 tem todas as colunas de que precisa (em especial: memória de cálculo, estados de peça, filas de classificação, contadores do "Seu dia" estruturado)
- [ ] Estratégia de acesso validada na prática: JPA p/ CRUD, JdbcClient p/ SQL pesado; mapeamento JSONB round-trip testado (§3)
- [ ] Modelo de embedding escolhido (spike de RAG, set/2026) → fixar `VECTOR(n)` e o plano A/B/C do §5.1
- [ ] Seed inicial de `feriado_forense` (nacionais + recesso + TRTs do nicho) revisado por advogado (entrevistado/beta)
