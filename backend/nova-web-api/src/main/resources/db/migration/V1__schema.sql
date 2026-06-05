-- =============================================================================
-- V1__schema.sql — Esquema completo NovaCasino Studio (PostgreSQL 18)
-- 11 tablas, constraints, índices FK explícitos, GIN sobre JSONB.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. operators — raíz del multi-tenancy
-- ---------------------------------------------------------------------------
CREATE TABLE operators (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- 2. users — jugadores, operadores y matemáticos
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id              BIGSERIAL    PRIMARY KEY,
    operator_id     BIGINT       NOT NULL REFERENCES operators(id),
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('PLAYER','OPERATOR','MATH_ANALYST')),
    birth_date      DATE         NOT NULL,
    -- La verificación de mayoría de edad (≥18, DGOJ) se realiza en el caso de uso de
    -- registro, no con CHECK, porque una expresión dependiente de NOW() se
    -- re-evaluaría de forma inconsistente en un restore.
    locale          CHAR(2)      NOT NULL DEFAULT 'es' CHECK (locale IN ('es','en')),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_operator_email UNIQUE (operator_id, email)
    -- operator_id FK cubierta por el prefijo del UNIQUE (operator_id, email)
);
CREATE INDEX idx_users_operator_role ON users (operator_id, role);

-- ---------------------------------------------------------------------------
-- 3. wallets — cartera virtual de cada jugador
-- ---------------------------------------------------------------------------
CREATE TABLE wallets (
    id              BIGSERIAL    PRIMARY KEY,
    operator_id     BIGINT       NOT NULL REFERENCES operators(id),
    user_id         BIGINT       NOT NULL UNIQUE REFERENCES users(id),
    balance_cents   BIGINT       NOT NULL DEFAULT 0 CHECK (balance_cents >= 0),
    currency        CHAR(3)      NOT NULL DEFAULT 'EUR',
    version         BIGINT       NOT NULL DEFAULT 0,  -- optimistic locking JPA @Version
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    -- user_id FK cubierta por UNIQUE; operator_id necesita índice explícito:
);
CREATE INDEX idx_wallets_operator ON wallets (operator_id);

-- ---------------------------------------------------------------------------
-- 4. games — catálogo de juegos (parámetros comerciales)
--    active_config_id es nullable; la FK se añade tras crear game_configs.
-- ---------------------------------------------------------------------------
CREATE TABLE games (
    id                  BIGSERIAL     PRIMARY KEY,
    operator_id         BIGINT        NOT NULL REFERENCES operators(id),
    code                VARCHAR(50)   NOT NULL,
    name                VARCHAR(100)  NOT NULL,
    theme               VARCHAR(20)   NOT NULL CHECK (theme IN ('EGYPTIAN','FRUITS','SPACE')),
    cover_image_url     VARCHAR(255)  NOT NULL,
    min_bet_cents       BIGINT        NOT NULL CHECK (min_bet_cents > 0),
    max_bet_cents       BIGINT        NOT NULL CHECK (max_bet_cents >= min_bet_cents),
    bet_step_cents      BIGINT        NOT NULL CHECK (bet_step_cents > 0),
    -- Los importes son la apuesta TOTAL; min y step deben ser múltiplos del nº de paylines.
    allowed_currencies  CHAR(3)[]     NOT NULL DEFAULT ARRAY['EUR'::CHAR(3)],
    active              BOOLEAN       NOT NULL DEFAULT TRUE,
    active_config_id    BIGINT,       -- FK añadida más abajo; NULL hasta publicar la primera config
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_games_operator_code UNIQUE (operator_id, code)
    -- operator_id FK cubierta por prefijo del UNIQUE (operator_id, code)
);

-- ---------------------------------------------------------------------------
-- 5. game_configs — versiones inmutables de la matemática de un juego
-- ---------------------------------------------------------------------------
CREATE TABLE game_configs (
    id                  BIGSERIAL       PRIMARY KEY,
    game_id             BIGINT          NOT NULL REFERENCES games(id),
    version             INT             NOT NULL CHECK (version >= 1),
    config              JSONB           NOT NULL,
    rtp_target          NUMERIC(7,4)    NOT NULL CHECK (rtp_target BETWEEN 0 AND 1),
    volatility_target   NUMERIC(8,2),
    created_by_user_id  BIGINT          NOT NULL REFERENCES users(id),
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    -- game_id FK cubierta por prefijo del UNIQUE (game_id, version):
    CONSTRAINT uq_game_configs_game_version UNIQUE (game_id, version)
);
CREATE INDEX idx_game_configs_config_gin  ON game_configs USING GIN (config);
CREATE INDEX idx_game_configs_created_by  ON game_configs (created_by_user_id);

-- ---------------------------------------------------------------------------
-- 6. Añadir FK games.active_config_id → game_configs (ciclo resuelto)
-- ---------------------------------------------------------------------------
ALTER TABLE games
    ADD CONSTRAINT fk_games_active_config
    FOREIGN KEY (active_config_id) REFERENCES game_configs(id);
CREATE INDEX idx_games_active_config ON games (active_config_id);

-- ---------------------------------------------------------------------------
-- 7. game_config_publications — histórico de publicaciones de matemática
-- ---------------------------------------------------------------------------
CREATE TABLE game_config_publications (
    id                      BIGSERIAL   PRIMARY KEY,
    operator_id             BIGINT      NOT NULL REFERENCES operators(id),
    game_id                 BIGINT      NOT NULL REFERENCES games(id),
    game_config_id          BIGINT      NOT NULL REFERENCES game_configs(id),
    published_by_user_id    BIGINT      NOT NULL REFERENCES users(id),
    published_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_gcp_game_published ON game_config_publications (game_id, published_at DESC);
CREATE INDEX idx_gcp_operator       ON game_config_publications (operator_id);
CREATE INDEX idx_gcp_config         ON game_config_publications (game_config_id);
CREATE INDEX idx_gcp_published_by   ON game_config_publications (published_by_user_id);

-- ---------------------------------------------------------------------------
-- 8. game_rounds — registro auditable de cada giro (append-only, DGOJ)
-- ---------------------------------------------------------------------------
CREATE TABLE game_rounds (
    id                          BIGSERIAL   PRIMARY KEY,
    operator_id                 BIGINT      NOT NULL REFERENCES operators(id),
    player_id                   BIGINT      NOT NULL REFERENCES users(id),
    game_id                     BIGINT      NOT NULL REFERENCES games(id),
    game_config_id              BIGINT      NOT NULL REFERENCES game_configs(id),
    rng_seed                    BIGINT      NOT NULL,
    bet_cents                   BIGINT      NOT NULL CHECK (bet_cents >= 0),   -- 0 en free spins
    win_cents                   BIGINT      NOT NULL CHECK (win_cents >= 0),
    balance_pre_cents           BIGINT      NOT NULL CHECK (balance_pre_cents >= 0),
    balance_post_cents          BIGINT      NOT NULL CHECK (balance_post_cents >= 0),
    result                      JSONB       NOT NULL,  -- {view, winningPaylines, scatterCount, multiplier}
    is_free_spin                BOOLEAN     NOT NULL DEFAULT FALSE,
    triggering_round_id         BIGINT      REFERENCES game_rounds(id),  -- self-FK para free spins
    free_spins_remaining_after  INT         CHECK (free_spins_remaining_after >= 0),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_game_rounds_player_created   ON game_rounds (player_id,   created_at DESC);
CREATE INDEX idx_game_rounds_game_created     ON game_rounds (game_id,     created_at DESC);
CREATE INDEX idx_game_rounds_operator_created ON game_rounds (operator_id, created_at DESC);
CREATE INDEX idx_game_rounds_config           ON game_rounds (game_config_id);
CREATE INDEX idx_game_rounds_triggering       ON game_rounds (triggering_round_id)
    WHERE triggering_round_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- 9. wallet_transactions — ledger contable append-only
-- ---------------------------------------------------------------------------
CREATE TABLE wallet_transactions (
    id                      BIGSERIAL   PRIMARY KEY,
    wallet_id               BIGINT      NOT NULL REFERENCES wallets(id),
    type                    VARCHAR(20) NOT NULL CHECK (type IN ('RECHARGE','BET','WIN')),
    amount_cents            BIGINT      NOT NULL,           -- signed: positivo RECHARGE/WIN, negativo BET
    balance_after_cents     BIGINT      NOT NULL CHECK (balance_after_cents >= 0),
    game_round_id           BIGINT      REFERENCES game_rounds(id),        -- solo BET/WIN
    performed_by_user_id    BIGINT      REFERENCES users(id),              -- solo RECHARGE
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- RECHARGE ↔ operador presente; BET/WIN ↔ round presente
    CONSTRAINT chk_recharge_has_operator
        CHECK ((type = 'RECHARGE') = (performed_by_user_id IS NOT NULL)),
    CONSTRAINT chk_bet_win_has_round
        CHECK ((type IN ('BET','WIN')) = (game_round_id IS NOT NULL)),
    -- impide duplicar BET o WIN para el mismo round por bug de aplicación
    CONSTRAINT uq_wallet_tx_round_type UNIQUE (game_round_id, type)
    -- game_round_id FK cubierta por prefijo del UNIQUE (game_round_id, type)
);
CREATE INDEX idx_wallet_tx_wallet_created ON wallet_transactions (wallet_id, created_at DESC);
CREATE INDEX idx_wallet_tx_performed_by   ON wallet_transactions (performed_by_user_id);

-- ---------------------------------------------------------------------------
-- 10. simulation_runs — resultado agregado de cada simulación masiva
-- ---------------------------------------------------------------------------
CREATE TABLE simulation_runs (
    id                      BIGSERIAL       PRIMARY KEY,
    operator_id             BIGINT          NOT NULL REFERENCES operators(id),
    game_config_id          BIGINT          NOT NULL REFERENCES game_configs(id),
    launched_by_user_id     BIGINT          NOT NULL REFERENCES users(id),
    num_spins               BIGINT          NOT NULL CHECK (num_spins > 0),
    bet_cents               BIGINT          NOT NULL CHECK (bet_cents > 0),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'RUNNING'
                                            CHECK (status IN ('RUNNING','COMPLETED','FAILED')),
    rtp_empirical           NUMERIC(7,4),
    rtp_std_error           NUMERIC(7,6),
    rtp_base_game           NUMERIC(7,4),
    rtp_free_spins          NUMERIC(7,4),
    hit_frequency           NUMERIC(7,4),
    volatility              NUMERIC(8,2),
    max_win_multiplier      NUMERIC(10,2),
    free_spin_trigger_freq  NUMERIC(7,4),
    longest_dry_streak      INT,
    prize_distribution      JSONB,
    convergence_sample      JSONB,
    rtp_breakdown           JSONB,
    duration_ms             BIGINT,
    error_message           TEXT,
    started_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_simrun_config_started ON simulation_runs (game_config_id, started_at DESC);
CREATE INDEX idx_simrun_operator       ON simulation_runs (operator_id);
CREATE INDEX idx_simrun_launched_by    ON simulation_runs (launched_by_user_id);

-- ---------------------------------------------------------------------------
-- 11. simulation_explanations — Q&A con IA sobre una simulación
-- ---------------------------------------------------------------------------
CREATE TABLE simulation_explanations (
    id                  BIGSERIAL   PRIMARY KEY,
    simulation_run_id   BIGINT      NOT NULL REFERENCES simulation_runs(id),
    asked_by_user_id    BIGINT      NOT NULL REFERENCES users(id),
    question            TEXT        NOT NULL,
    answer              TEXT        NOT NULL,
    model               VARCHAR(50) NOT NULL,
    asked_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sim_expl_run      ON simulation_explanations (simulation_run_id, asked_at);
CREATE INDEX idx_sim_expl_asked_by ON simulation_explanations (asked_by_user_id);

-- ---------------------------------------------------------------------------
-- 12. idempotency_keys — deduplicación de operaciones con efecto económico
-- ---------------------------------------------------------------------------
CREATE TABLE idempotency_keys (
    id              BIGSERIAL   PRIMARY KEY,
    idem_key        UUID        NOT NULL,
    user_id         BIGINT      NOT NULL REFERENCES users(id),
    endpoint        VARCHAR(40) NOT NULL,
    request_hash    CHAR(64)    NOT NULL,
    response_status INT         NOT NULL,
    response_body   JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- user_id FK cubierta por prefijo del UNIQUE (user_id, endpoint, idem_key):
    CONSTRAINT uq_idem_user_endpoint_key UNIQUE (user_id, endpoint, idem_key)
);
