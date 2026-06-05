-- =============================================================================
-- V2__immutability_triggers.sql
-- Función compartida fn_forbid_update_delete() y 4 triggers BEFORE UPDATE OR DELETE
-- sobre las tablas histórico-regulatorias (requisito DGOJ de inmutabilidad).
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_forbid_update_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Registro inmutable: operación % sobre la tabla % está prohibida. '
                    'El historial regulatorio no puede modificarse.',
        TG_OP, TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;

-- game_rounds — registro auditable de cada giro
CREATE TRIGGER trg_game_rounds_no_update_delete
    BEFORE UPDATE OR DELETE ON game_rounds
    FOR EACH ROW EXECUTE FUNCTION fn_forbid_update_delete();

-- wallet_transactions — ledger contable
CREATE TRIGGER trg_wallet_transactions_no_update_delete
    BEFORE UPDATE OR DELETE ON wallet_transactions
    FOR EACH ROW EXECUTE FUNCTION fn_forbid_update_delete();

-- game_configs — versiones matemáticas (cambiar = nueva versión, no modificar)
CREATE TRIGGER trg_game_configs_no_update_delete
    BEFORE UPDATE OR DELETE ON game_configs
    FOR EACH ROW EXECUTE FUNCTION fn_forbid_update_delete();

-- game_config_publications — histórico de quién activó qué config y cuándo
CREATE TRIGGER trg_game_config_publications_no_update_delete
    BEFORE UPDATE OR DELETE ON game_config_publications
    FOR EACH ROW EXECUTE FUNCTION fn_forbid_update_delete();
