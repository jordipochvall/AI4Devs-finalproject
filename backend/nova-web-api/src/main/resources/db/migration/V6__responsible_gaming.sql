-- =============================================================================
-- V6__responsible_gaming.sql — Límites y autoexclusión server-side (HU-19, decisión D7)
-- Aditiva: no toca tablas existentes. Índices por user_id para la verificación previa al giro.
-- =============================================================================

-- Límites de juego responsable (uno por jugador + tipo + periodo).
CREATE TABLE player_limits (
    id                    BIGSERIAL    PRIMARY KEY,
    user_id               BIGINT       NOT NULL REFERENCES users(id),
    limit_type            VARCHAR(20)  NOT NULL CHECK (limit_type IN ('LOSS','DEPOSIT','SESSION_TIME')),
    period                VARCHAR(20)  NOT NULL CHECK (period IN ('DAILY','WEEKLY','MONTHLY')),
    amount_cents          BIGINT       NOT NULL CHECK (amount_cents >= 0),  -- límite vigente
    effective_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    -- Relajación diferida (enfriamiento): el nuevo límite (mayor) entra en pending_effective_at.
    pending_amount_cents  BIGINT,
    pending_effective_at  TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_player_limit UNIQUE (user_id, limit_type, period)
);
CREATE INDEX idx_player_limits_user ON player_limits (user_id);

-- Periodos de autoexclusión del jugador.
CREATE TABLE self_exclusions (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id),
    start_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    end_at      TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_self_exclusions_user ON self_exclusions (user_id);
