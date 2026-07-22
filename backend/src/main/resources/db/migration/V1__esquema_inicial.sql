-- Esquema inicial: identidade e acervo por usuário.
--
-- Até aqui o acervo vivia num LinkedHashMap compartilhado por todos os visitantes.
-- A coluna usuario_id em cada tabela de dados é o que torna o isolamento verificável
-- numa query, em vez de depender de disciplina no código de aplicação.

CREATE TABLE usuarios (
    id               UUID PRIMARY KEY,
    -- O 'sub' do Google é o identificador estável da conta. O e-mail pode mudar;
    -- o sub não. Por isso a identificação é por ele, e o e-mail é só um atributo.
    google_sub       TEXT        NOT NULL UNIQUE,
    email            TEXT        NOT NULL UNIQUE,
    nome             TEXT,
    foto_url         TEXT,
    -- Preenchidos depois, no onboarding. É por aqui que a ingestão do DJEN vai
    -- descobrir quais publicações são deste advogado.
    oab_numero       TEXT,
    oab_uf           TEXT,
    -- Teto diário de execuções de IA. Cadastro é aberto, e crédito de modelo é
    -- dinheiro real: sem teto, uma conta só esvazia a conta de todos.
    execucoes_no_dia INTEGER     NOT NULL DEFAULT 0,
    data_contagem    DATE,
    criado_em        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ultimo_acesso_em TIMESTAMPTZ
);

CREATE TABLE processos (
    id              UUID PRIMARY KEY,
    usuario_id      UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    numero          TEXT NOT NULL,
    cliente         TEXT,
    parte_contraria TEXT,
    vara            TEXT,
    area            TEXT,
    fase            TEXT,
    resumo          TEXT
);

CREATE INDEX idx_processos_usuario ON processos (usuario_id);

CREATE TABLE intimacoes (
    id               UUID PRIMARY KEY,
    usuario_id       UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    processo_id      UUID REFERENCES processos (id) ON DELETE CASCADE,
    data_publicacao  DATE NOT NULL,
    orgao            TEXT,
    texto            TEXT NOT NULL,

    -- A classificação da IA, achatada em colunas. Achatar em vez de guardar JSON
    -- é deliberado: prazo_em_dias e tipo_contagem alimentam a CalculadoraPrazo e
    -- precisam ser consultáveis e tipados, não campos soltos dentro de um blob.
    tipo_ato           TEXT,
    prazo_em_dias      INTEGER,
    tipo_contagem      TEXT,
    providencia        TEXT,
    tipo_peca_sugerida TEXT,
    urgencia           TEXT,
    confianca          NUMERIC(3, 2),
    fundamentacao      TEXT,

    -- Nasce só na CalculadoraPrazo, nunca no modelo. A regra número um do projeto.
    data_vencimento  DATE,
    rascunho         TEXT,

    -- Estado do fluxo assíncrono do n8n.
    processando      BOOLEAN     NOT NULL DEFAULT FALSE,
    erro_ia          TEXT,

    criado_em        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_intimacoes_usuario ON intimacoes (usuario_id);
-- A tela "Seu dia" ordena por vencimento dentro do acervo de um usuário.
CREATE INDEX idx_intimacoes_agenda ON intimacoes (usuario_id, data_vencimento);

-- Até agora InteresseService validava o lead e o descartava. Aqui ele passa a ter
-- para onde ir. Sem usuario_id: o interessado ainda não é usuário — é exatamente
-- por isso que ele preencheu o formulário.
CREATE TABLE interesses (
    id        UUID PRIMARY KEY,
    nome      TEXT        NOT NULL,
    email     TEXT        NOT NULL,
    interesse TEXT        NOT NULL,
    mensagem  TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
