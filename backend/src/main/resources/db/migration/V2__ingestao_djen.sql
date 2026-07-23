-- Publicações reais do DJEN, buscadas pela OAB do advogado.
--
-- Até aqui o acervo era semeado à mão: três casos fictícios, iguais para todo mundo.
-- Estas colunas são o que permite guardar o que veio do CNJ e não trazer de novo o que
-- já veio.

ALTER TABLE processos
    -- O número como a API devolve, sem pontuação. É por ele que se casa a publicação com
    -- um processo já conhecido; a versão com máscara serve só para exibir.
    ADD COLUMN numero_sem_mascara  TEXT,
    ADD COLUMN tribunal            TEXT,
    ADD COLUMN classe              TEXT,
    -- DEMO (semeado) ou DJEN (real). Separar os dois deixa a demonstração conviver com o
    -- acervo verdadeiro sem se misturarem na tela.
    ADD COLUMN origem              TEXT NOT NULL DEFAULT 'DEMO',
    -- As partes como vieram, agrupadas por polo. É o que a tela mostra ao perguntar de
    -- que lado o advogado está.
    ADD COLUMN partes_polo_ativo   TEXT,
    ADD COLUMN partes_polo_passivo TEXT,
    -- 'A' ou 'P'. Nulo = ainda não perguntamos. A API do CNJ entrega as partes e entrega
    -- os advogados, mas não diz qual advogado representa qual parte — então essa resposta
    -- só pode vir do próprio advogado. VARCHAR e não CHAR: o CHAR do Postgres vira bpchar
    -- e o Hibernate, que mapeia String como varchar, recusaria validar o schema.
    ADD COLUMN cliente_polo        VARCHAR(1);

-- Um processo nasce uma vez por advogado. Sem isto, cada nova publicação do mesmo
-- processo criaria um processo novo e a agenda viraria uma lista de repetições.
CREATE UNIQUE INDEX uq_processos_usuario_numero
    ON processos (usuario_id, numero_sem_mascara)
    WHERE numero_sem_mascara IS NOT NULL;

ALTER TABLE intimacoes
    ADD COLUMN djen_hash        TEXT,
    ADD COLUMN djen_id          BIGINT,
    -- Comprovante no PJe: é o que o advogado abre para conferir a publicação na fonte.
    ADD COLUMN link             TEXT,
    ADD COLUMN tipo_comunicacao TEXT;

-- A trava contra duplicação mora no banco, não na memória do código. A sincronização roda
-- todo dia sobre janelas que se sobrepõem, e uma condição de corrida entre duas execuções
-- não pode virar duas intimações — nem dois prazos na agenda.
--
-- A chave é (usuario_id, djen_hash), não só o hash: a mesma publicação chega a todos os
-- advogados do processo, e cada um precisa da sua própria cópia para trabalhar.
CREATE UNIQUE INDEX uq_intimacoes_usuario_djen
    ON intimacoes (usuario_id, djen_hash)
    WHERE djen_hash IS NOT NULL;

ALTER TABLE usuarios
    -- Até que data já buscamos. Evita repetir trabalho e, principalmente, evita deixar
    -- buraco quando a aplicação fica um tempo fora do ar.
    ADD COLUMN djen_sincronizado_ate    DATE,
    ADD COLUMN djen_ultima_sincronizacao TIMESTAMPTZ,
    -- Quando a última tentativa falhou, a tela precisa poder avisar: acervo desatualizado
    -- é pior que acervo vazio, porque parece completo.
    ADD COLUMN djen_erro                TEXT;
