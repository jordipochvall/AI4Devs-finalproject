# HU-16-BE-01 — Endpoint de dashboard de actividad + detalle de partida

## Código
`HU-16-BE-01` — vinculado con **HU-16: El operador consulta el dashboard de actividad**.

## Título
`GET /operator/dashboard` (métricas agregadas) y `GET /operator/rounds/{roundId}` (detalle)

## Descripción
Calcular y exponer las métricas de actividad del operador a partir de `game_rounds`: jugadores activos, GGR (apostado − premiado) y juegos más jugados, con filtro por rango de fechas; y el detalle de una partida concreta sin invocar el replay completo. Aprovecha los índices `idx_game_rounds_operator_created`/`game_created`.

## Criterios de aceptación
- **AC1**: `GET /operator/dashboard` devuelve jugadores activos, GGR y juegos más jugados, acotables por rango de fechas.
- **AC2**: `GET /operator/rounds/{roundId}` devuelve importes, símbolos resultantes y líneas ganadoras de la partida.
- **AC3**: **Aislamiento**: las métricas agregan únicamente la actividad del operador del token.
- **AC4**: Solo rol `OPERATOR`; otros roles → `403`. Partida inexistente → `404`.

## Prioridad
Should Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `operator`, `metricas`, `fase-post-mvp`

## Comentarios
- Endpoints **post-MVP** del catálogo (§4.2). Solo lectura/agregación.
- **Dependencias directas:** `HU-3-BE-01` (auditoría y datos de `game_rounds`).

## Enlaces y referencias
- Historia: [HU-16](../../stories/HU-16.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Catálogo: [§4.2](../../readme.md#42-catálogo-de-endpoints) (`/operator/dashboard`, `/operator/rounds/{id}`, *post-MVP*) · Índices [§3.2.8](../../readme.md#32-descripción-de-entidades-principales).
