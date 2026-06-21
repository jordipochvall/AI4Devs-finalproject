-- =============================================================================
-- V7__integrity_chain.sql — Cadena de integridad tamper-evident en game_rounds (HU-20, D3)
-- Aditiva. Cada round encadena el hash de la fila anterior del MISMO operador (orden por id).
-- El hash se calcula en un trigger BEFORE INSERT (no toca filas existentes → compatible con la
-- inmutabilidad por trigger del MVP). Un lock por operador serializa el encadenado.
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- VARCHAR (no CHAR): CHAR(64) padding would turn the genesis '' prev_hash into 64 spaces.
ALTER TABLE game_rounds ADD COLUMN prev_hash VARCHAR(64);
ALTER TABLE game_rounds ADD COLUMN row_hash  VARCHAR(64);

-- Cadena determinista: prev_hash || '|' || campos estables del round, SHA-256 en hex.
CREATE OR REPLACE FUNCTION fn_game_rounds_integrity_hash()
RETURNS TRIGGER AS $$
DECLARE prev TEXT;
BEGIN
    -- Serializa el encadenado por operador (evita bifurcaciones bajo concurrencia).
    PERFORM pg_advisory_xact_lock(NEW.operator_id);

    SELECT row_hash INTO prev FROM game_rounds
        WHERE operator_id = NEW.operator_id
        ORDER BY id DESC LIMIT 1;
    prev := COALESCE(prev, '');  -- génesis: cadena vacía

    NEW.prev_hash := prev;
    NEW.row_hash := encode(digest(
        prev || '|' || NEW.id || '|' || NEW.player_id || '|' || NEW.game_id || '|' ||
        NEW.bet_cents || '|' || NEW.win_cents || '|' || NEW.balance_post_cents || '|' || NEW.rng_seed,
        'sha256'), 'hex');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_game_rounds_integrity
    BEFORE INSERT ON game_rounds
    FOR EACH ROW EXECUTE FUNCTION fn_game_rounds_integrity_hash();
