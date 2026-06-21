# HU-23 — Particionado y retención de `game_rounds`

Diseño operativo para escalar el registro auditable (`game_rounds`), que es *append-only* y crece sin
límite. Cierra la decisión diferida **D4**. La parte no destructiva (índice **BRIN** sobre
`created_at` para escaneos por rango) se aplica en la migración `V10`. La conversión a tabla
particionada es una **migración operativa** (no forward automática) por su impacto en claves foráneas
y triggers; este documento es su runbook.

## 1. Objetivo

- Particionar `game_rounds` por **rango mensual** (`PARTITION BY RANGE (created_at)`).
- Mantener **inmutabilidad** (trigger `fn_forbid_update_delete`) y la **cadena de integridad** (HU-20).
- Permitir **poda de particiones** (partition pruning) en consultas por rango de fechas.
- Archivar en frío particiones fuera de la ventana activa, preservando verificabilidad.

## 2. Por qué no es una migración forward automática

`game_rounds` ya está en producción con:
- FKs entrantes: `wallet_transactions.game_round_id`, `jackpot_grants.game_round_id`, y la self-FK
  `game_rounds.triggering_round_id` (free spins).
- Triggers `BEFORE UPDATE OR DELETE` (inmutabilidad) y `BEFORE INSERT` (hash de integridad).

PostgreSQL no convierte una tabla existente en particionada *in situ*: requiere crear una tabla
particionada nueva y migrar los datos. Hacerlo exige recrear FKs y triggers y una ventana de servicio.
Por eso se ejecuta como migración operativa coordinada, no como `V*.sql` automática.

## 3. Procedimiento (sin pérdida ni duplicados)

```sql
BEGIN;

-- 3.1 Tabla particionada con el MISMO esquema (incluye prev_hash/row_hash de HU-20).
CREATE TABLE game_rounds_part (LIKE game_rounds INCLUDING ALL) PARTITION BY RANGE (created_at);

-- 3.2 Particiones (u: uno por mes; crear las del histórico + la del mes en curso + siguientes).
CREATE TABLE game_rounds_y2026m06 PARTITION OF game_rounds_part
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
-- ... repetir por cada mes con datos ...

-- 3.3 Copia íntegra (sin pérdida). El COUNT antes/después debe coincidir (AC2).
INSERT INTO game_rounds_part SELECT * FROM game_rounds;

-- 3.4 Reapuntar FKs entrantes a la tabla particionada y swap de nombres.
ALTER TABLE wallet_transactions DROP CONSTRAINT wallet_transactions_game_round_id_fkey;
ALTER TABLE jackpot_grants      DROP CONSTRAINT jackpot_grants_game_round_id_fkey;
ALTER TABLE game_rounds RENAME TO game_rounds_legacy;
ALTER TABLE game_rounds_part RENAME TO game_rounds;
ALTER TABLE wallet_transactions ADD CONSTRAINT wallet_transactions_game_round_id_fkey
    FOREIGN KEY (game_round_id) REFERENCES game_rounds(id);
ALTER TABLE jackpot_grants ADD CONSTRAINT jackpot_grants_game_round_id_fkey
    FOREIGN KEY (game_round_id) REFERENCES game_rounds(id);

-- 3.5 Recrear triggers de inmutabilidad e integridad sobre la nueva tabla (AC3).
CREATE TRIGGER trg_game_rounds_no_update_delete BEFORE UPDATE OR DELETE ON game_rounds
    FOR EACH ROW EXECUTE FUNCTION fn_forbid_update_delete();
CREATE TRIGGER trg_game_rounds_integrity BEFORE INSERT ON game_rounds
    FOR EACH ROW EXECUTE FUNCTION fn_game_rounds_integrity_hash();

-- 3.6 Verificación: recuentos iguales y cadena íntegra.
SELECT (SELECT COUNT(*) FROM game_rounds) = (SELECT COUNT(*) FROM game_rounds_legacy) AS lossless;
-- GET /operator/audit/integrity debe seguir devolviendo consistent=true (HU-20).

COMMIT;
DROP TABLE game_rounds_legacy;  -- tras validar
```

## 4. Mantenimiento y retención

- **Creación adelantada**: un job mensual crea la partición del mes siguiente.
- **Retención en caliente**: p. ej. 13 meses; particiones más antiguas se `DETACH PARTITION` y se
  archivan en almacenamiento frío (export lógico) — siguen siendo verificables porque conservan
  `prev_hash`/`row_hash`.
- **Poda**: con el filtro `created_at BETWEEN :from AND :to` (ya usado por la auditoría y el dashboard),
  el planificador descarta particiones fuera de rango (`EXPLAIN` muestra menos *partitions scanned*).

## 5. Fuera de alcance

Réplica geográfica y *tiering* automático multi-proveedor (anotado en la historia).
