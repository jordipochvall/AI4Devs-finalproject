# HU-17-BE-01 — Endpoints de versiones y publicación de matemática

## Código
`HU-17-BE-01` — vinculado con **HU-17: El matemático publica y versiona la matemática activa**.

## Título
`GET /math/games/{gameId}/configs` y `POST /math/games/{gameId}/publish`

## Descripción
Listar las versiones de matemática de un juego (paginado, con cuál está activa) y **publicar** una versión: mover `games.active_config_id` a la versión elegida y registrar la publicación inmutable en `game_config_publications`. A partir de ahí, los nuevos giros del jugador usan esa versión. Cierra la activación que el MVP dejó diferida (§4.2).

## Criterios de aceptación
- **AC1**: `GET .../configs` devuelve las versiones con número, `rtp_target` y marca de activa.
- **AC2**: `POST .../publish` mueve `active_config_id` a la versión indicada y registra la publicación (autor, fecha).
- **AC3**: Publicar la versión ya activa → `409` (conflicto de estado).
- **AC4**: Tras publicar, un nuevo giro del jugador usa la nueva versión (verificable en el detalle/replay).
- **AC5**: Solo rol `MATH_ANALYST`; otros roles → `403`.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `math`, `versionado`, `fase-post-mvp`

## Comentarios
- Endpoints **post-MVP** del catálogo (§4.2). `game_config_publications` ya existe en el esquema del MVP.
- **Dependencias directas:** `HU-7-BE-01` (edición/versionado de `config`).

## Enlaces y referencias
- Historia: [HU-17](../../stories/HU-17.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Catálogo y nota de activación: [§4.2](../../readme.md#42-catálogo-de-endpoints) · Modelo [§3.2 `game_configs`, `game_config_publications`](../../readme.md#32-descripción-de-entidades-principales).
