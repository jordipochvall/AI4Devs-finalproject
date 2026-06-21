-- =============================================================================
-- V5__commercial_audit.sql — Auditoría de cambios comerciales (HU-15, decisión D5)
-- Tabla append-only con el snapshot antes/después de cada PUT /operator/games/{id}.
-- Reutiliza fn_forbid_update_delete() (V2) para la inmutabilidad regulatoria.
-- =============================================================================

CREATE TABLE game_commercial_audits (
    id                    BIGSERIAL    PRIMARY KEY,
    operator_id           BIGINT       NOT NULL REFERENCES operators(id),
    game_id               BIGINT       NOT NULL REFERENCES games(id),
    performed_by_user_id  BIGINT       NOT NULL REFERENCES users(id),
    before_value          JSONB        NOT NULL,   -- snapshot comercial previo
    after_value           JSONB        NOT NULL,   -- snapshot comercial resultante
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_gca_game_created ON game_commercial_audits (game_id, created_at DESC);
CREATE INDEX idx_gca_operator     ON game_commercial_audits (operator_id);

-- Inmutabilidad (append-only): prohíbe UPDATE/DELETE.
CREATE TRIGGER trg_game_commercial_audits_no_update_delete
    BEFORE UPDATE OR DELETE ON game_commercial_audits
    FOR EACH ROW EXECUTE FUNCTION fn_forbid_update_delete();
