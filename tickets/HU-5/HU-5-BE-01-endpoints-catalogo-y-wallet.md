# HU-5-BE-01 — Endpoints de catálogo y wallet

## Código
`HU-5-BE-01` — vinculado con **HU-5: El jugador accede al lobby y consulta su saldo**.

## Título
Endpoints de catálogo de juegos y consulta de saldo

## Descripción
Implementar los endpoints de lectura del jugador en `nova-application` + `nova-web-api`:

- `GET /api/v1/player/games` — catálogo de juegos `active = true` con `id`, `name`, `theme`, `coverImageUrl` y `grid`.
- `GET /api/v1/player/games/{gameId}` — detalle del juego + su `config` activa (versión `active_config_id`, estructura del apartado 3.3) para renderizar el `<SlotGame>`.
- `GET /api/v1/player/wallet` — saldo virtual del jugador autenticado (`balanceCents`, `currency`).

## Criterios de aceptación
- **AC1**: `GET /player/games` devuelve solo los juegos activos, sin exponer la matemática completa.
- **AC2**: `GET /player/games/{id}` devuelve el `config` de la versión activa; un juego inexistente o inactivo → `404`.
- **AC3**: `GET /player/wallet` devuelve el saldo del jugador del token (no el de otros).
- **AC4**: Los tres endpoints exigen rol `PLAYER`; otros roles → `403`.

## Prioridad
Must Have

## Estimación
2 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-application`, `nova-web-api`, `catalogo`, `wallet`

## Comentarios
- **Dependencias directas:** `HU-4-BE-01` (externa, auth/roles). El `config` que sirve lo define el modelo de `HU-7`/semilla.

## Enlaces y referencias
- Historia: [HU-5](../../stories/HU-5.md).
- API: catálogo [4.2](../../readme.md#42-catálogo-de-endpoints).
- Modelo: [3.2.3 wallets, 3.2.5 games, 3.2.6 game_configs](../../readme.md#32-descripción-de-entidades-principales).
