-- =============================================================================
-- V10__game_rounds_brin.sql — Escalado de la auditoría (HU-23, decisión D4)
-- Índice BRIN sobre created_at: muy compacto y eficiente para escaneos por RANGO de fechas en una
-- tabla append-only ordenada temporalmente (la auditoría del operador filtra por rango). Es el primer
-- paso, no destructivo, hacia el particionado mensual.
--
-- NOTA OPERATIVA: la conversión de `game_rounds` a tabla PARTICIONADA por rango (mensual) y el
-- archivado en frío de particiones antiguas se documentan en docs/partitioning-and-retention.md.
-- No se aplica como migración forward automática porque exige reconstruir la tabla preservando sus
-- claves foráneas (wallet_transactions, jackpot_grants, self-FK de free spins) y sus triggers de
-- inmutabilidad e integridad (HU-20); es una migración operativa coordinada con ventana de servicio.
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_game_rounds_created_brin
    ON game_rounds USING BRIN (created_at);
