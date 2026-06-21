# HU-14-BE-01 — Endpoints de historial del jugador (movimientos y partidas)

## Código
`HU-14-BE-01` — vinculado con **HU-14: El jugador consulta su historial de movimientos y partidas**.

## Título
`GET /player/wallet/transactions` y `GET /player/rounds` paginados y aislados por jugador

## Descripción
Exponer al jugador su propio historial leyendo datos ya persistidos por el MVP: `GET /player/wallet/transactions` (movimientos de `wallet_transactions`) y `GET /player/rounds` (partidas de `game_rounds`), ambos paginados, ordenados por `created_at DESC` y **estrictamente acotados al jugador del token**. Respuesta con el envoltorio estándar (§4.1).

## Criterios de aceptación
- **AC1**: `wallet/transactions` devuelve una página con tipo (RECHARGE/BET/WIN), importe, saldo resultante y fecha, en orden descendente.
- **AC2**: `rounds` devuelve una página con apuesta, premio, saldo resultante y fecha de cada giro.
- **AC3**: **Aislamiento**: un jugador solo ve datos propios; nunca de otros (filtrado por `user_id` del token).
- **AC4**: Paginación con `page`/`size` y el envoltorio `{content,page,size,totalElements,totalPages}`.
- **AC5**: Solo rol `PLAYER`; otros roles → `403`.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `player`, `paginacion`, `fase-post-mvp`

## Comentarios
- Endpoints **post-MVP** del catálogo (§4.2); solo lectura, sin cambios en el flujo de juego.
- **Dependencias directas:** `HU-1-BE-02` (genera `game_rounds` y movimientos BET/WIN) · `HU-6-BE-01` (genera movimientos RECHARGE) · `HU-4-BE-01` (auth, externa).

## Enlaces y referencias
- Historia: [HU-14](../../stories/HU-14.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Modelo: [§3.2.8 `game_rounds`, §3.2.9 `wallet_transactions`](../../readme.md#32-descripción-de-entidades-principales) · Paginación [§4.1](../../readme.md#41-principios-de-diseño-y-convenciones).
