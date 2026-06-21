-- =============================================================================
-- V4__refresh_tokens.sql — Soporte de refresh tokens (HU-13, post-MVP D2)
-- Tabla mutable (el campo `revoked` cambia): NO lleva trigger de inmutabilidad.
-- =============================================================================

CREATE TABLE refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id),
    token_hash  CHAR(64)     NOT NULL UNIQUE,   -- SHA-256 (hex) del token opaco; nunca se guarda en claro
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    -- user_id necesita índice explícito (no hay UNIQUE que lo cubra):
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
