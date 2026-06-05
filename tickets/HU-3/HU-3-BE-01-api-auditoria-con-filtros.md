# HU-3-BE-01 — API de auditoría con filtros

## Código
`HU-3-BE-01` — vinculado con **HU-3: El operador resuelve una reclamación con el replay**.

## Título
API de auditoría con filtros (`GET /operator/rounds` paginado por jugador, juego y rango de fechas)

## Descripción
Implementar el endpoint `GET /api/v1/operator/rounds` que devuelve la auditoría de partidas paginada, filtrable por `playerId`, `gameId`, `from` y `to` (rango de fechas), con orden por `created_at DESC` por defecto. Se sirve desde `nova-application` (caso de uso `AuditQueryUseCase`) y `nova-web-api` (controller `OperatorController`).

Adicionalmente, expone `GET /operator/players` (listado/búsqueda paginada por email, para localizar al jugador reclamante).

La respuesta usa el envoltorio de paginación estándar: `{ content, page, size, totalElements, totalPages }` (ver 4.1).

## Criterios de aceptación
- **AC1**: `GET /operator/rounds?page=0&size=20&playerId=X&from=...&to=...` devuelve la página correspondiente con los rounds que cumplen los filtros, ordenados por `created_at DESC`.
- **AC2**: Sin filtros (solo `page`/`size`), devuelve los últimos rounds del operador.
- **AC3**: Los filtros son combinables (AND).
- **AC4**: Sólo usuarios con rol `OPERATOR` pueden invocar el endpoint; otros roles reciben `403`.
- **AC5**: `GET /operator/players?page=0&size=20&email=ana` busca jugadores cuyo email contenga el texto (búsqueda parcial case-insensitive), paginado.
- **AC6**: Las consultas usan los índices documentados en 3.2.8 (`idx_game_rounds_player_created`, `idx_game_rounds_game_created`, `idx_game_rounds_operator_created`) — verificable con `EXPLAIN ANALYZE`.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-application`, `nova-web-api`, `auditoria`, `paginacion`, `dgoj`

## Comentarios
- `GET /operator/rounds/{roundId}` (detalle de una partida sin replay) queda como **post-MVP** (4.2).
- **Dependencias directas:** `HU-4-BE-01` (externa, auth/roles) · `HU-1-BE-02` (externa, genera los rounds que se auditan).

## Enlaces y referencias
- Historia: [HU-3](../../readme.md#5-historias-de-usuario).
- API: [4.4.5 Replay](../../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios), catálogo [4.2](../../readme.md#42-catálogo-de-endpoints).
- Modelo de datos: [3.2.8 game_rounds](../../readme.md#32-descripción-de-entidades-principales).
- Principios de la API (paginación): [4.1](../../readme.md#41-principios-de-diseño-y-convenciones).
