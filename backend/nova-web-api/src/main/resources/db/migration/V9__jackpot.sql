-- =============================================================================
-- V9__jackpot.sql — Jackpot progresivo (HU-26, decisión D11)
-- Aditiva. Pool por juego (valor actual, semilla y fracción de contribución en bps) y registro
-- inmutable de concesiones vinculado a la partida ganadora. Opcional por juego (config).
-- =============================================================================

CREATE TABLE jackpot_pools (
    id                  BIGSERIAL    PRIMARY KEY,
    game_id             BIGINT       NOT NULL UNIQUE REFERENCES games(id),
    current_cents       BIGINT       NOT NULL CHECK (current_cents >= 0),
    seed_cents          BIGINT       NOT NULL CHECK (seed_cents >= 0),
    contribution_bps    INTEGER      NOT NULL CHECK (contribution_bps >= 0),  -- fracción de la apuesta (1 bps = 0,01 %)
    odds_denominator    BIGINT       NOT NULL CHECK (odds_denominator > 0),   -- prob. de concesión = 1 / N
    version             BIGINT       NOT NULL DEFAULT 0,                       -- bloqueo optimista
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE jackpot_grants (
    id              BIGSERIAL    PRIMARY KEY,
    operator_id     BIGINT       NOT NULL REFERENCES operators(id),
    game_id         BIGINT       NOT NULL REFERENCES games(id),
    game_round_id   BIGINT       NOT NULL REFERENCES game_rounds(id),
    player_id       BIGINT       NOT NULL REFERENCES users(id),
    amount_cents    BIGINT       NOT NULL CHECK (amount_cents >= 0),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_jackpot_grants_game ON jackpot_grants (game_id, created_at DESC);

-- Inmutabilidad del histórico de concesiones (append-only).
CREATE TRIGGER trg_jackpot_grants_no_update_delete
    BEFORE UPDATE OR DELETE ON jackpot_grants
    FOR EACH ROW EXECUTE FUNCTION fn_forbid_update_delete();
