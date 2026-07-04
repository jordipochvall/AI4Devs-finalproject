# HU-31-BE-01 — Recalibrar las configuraciones semilla a un RTP realista

## Código
`HU-31-BE-01` — vinculado con **HU-31: Bug — RTP empírico fuera de rango en los juegos semilla**.

## Título
Recalibrar reels/paytable/scatter/free-spins de las 3 configs al `rtp_target`

## Descripción
Ajustar la matemática de las tres configuraciones semilla (`EGYPTIAN_CONFIG`, `FRUITS_CONFIG`, `SPACE_CONFIG` en `SeedDataLoader`) para que el **RTP empírico** medido por el simulador quede **cerca del `rtp_target` declarado** (0.92–0.965) dentro de una tolerancia (±3–5 pp), iterando reels/paytable/scatter/free-spins con el `SimulationRunner`. `SeedDataLoader` es la fuente de verdad para instalaciones nuevas; para la BBDD ya sembrada, aplicar una **migración Flyway** (p. ej. `V12`) que actualice `game_configs.config`/`rtp_target` (sin editar migraciones/seeder ya aplicados → checksums intactos).

## Criterios de aceptación
- **AC1**: Para cada juego semilla, una simulación (≥ 1M giros) da un RTP empírico dentro de banda del target declarado.
- **AC2**: `SeedDataLoader` refleja las configs calibradas (instalaciones nuevas nacen bien).
- **AC3**: La BBDD existente queda actualizada vía migración `V12` (o re-seed documentado), sin romper la validación de Flyway.
- **AC4**: El motor **no** se modifica (es correcto); sólo cambian datos de configuración.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Backend / Math

## Etiquetas
`backend`, `math`, `bug`, `seed`, `flyway`

## Comentarios
- Verificación iterativa con el simulador del panel del matemático o con un test de simulación.
- **Dependencias directas:** `HU-1` (motor/seeder), `HU-2` (simulador).

## Enlaces y referencias
- Historia: [HU-31](../../stories/HU-31.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
