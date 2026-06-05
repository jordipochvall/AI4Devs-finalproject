# HU-3-BE-02 — Endpoint de *replay* (guardar-y-renderizar)

## Código
`HU-3-BE-02` — vinculado con **HU-3: El operador resuelve una reclamación con el replay**.

## Título
Endpoint de *replay* (`GET /operator/rounds/{id}/replay`) — render del registro inmutable

## Descripción
Implementar `GET /api/v1/operator/rounds/{roundId}/replay` que devuelve el **registro inmutable** de la partida para que el cliente lo **renderice tal cual** (guardar-y-renderizar; ver readme [§2.5.3](../../readme.md#253-rng-criptográficamente-fuerte-y-replay-determinista)). El `result` almacenado es **autoritativo**: es exactamente lo que vio el jugador, con **cualquier versión del motor**.

Devuelve:
- Datos del round: `roundId`, `gameId`, `gameConfigId`, `betCents`, `winCents`, `balancePre/PostCents`, `result` (`view`, `winningPaylines`, `scatterCount`, `multiplier`) y, si hubo free spins, los rounds enlazados por `triggering_round_id`.
- `rngSeed` y el `config` exacto usado (versión `gameConfigId` de `game_configs`) como **metadato forense / de verificación** — no se recalcula nada en el endpoint.

El `ReplayRoundUseCase` **no recalcula** el giro ni hace gating por recomputación: simplemente lee y devuelve el registro. La recomputación desde `seed`+`config` (si alguna vez se requiere, p. ej. forense) es una operación aparte y **no condiciona la respuesta del replay**. La verificación de que el motor sigue siendo determinista vive en el *golden-master* de CI (`HU-1-QA-01`), no aquí.

## Criterios de aceptación
- **AC1**: `GET /operator/rounds/{roundId}/replay` con `roundId` existente devuelve `200` con todos los campos descritos en la API 4.4.5; el `result` es el registrado, sin recálculo.
- **AC2**: La respuesta incluye el `config` de la versión exacta usada en el giro (`gameConfigId`), no la versión actualmente activa.
- **AC3**: Si el round tuvo free spins, todos los rounds hijos (con `triggering_round_id = roundId`) se incluyen en `result.freeSpins.rounds` en orden cronológico.
- **AC4**: El endpoint **no falla por drift del motor**: aunque el `SpinKernel` haya evolucionado desde que se jugó el round, el replay sigue devolviendo `200` con el registro inmutable (no hay gate de recomputación).
- **AC5**: `roundId` inexistente responde `404`. Otros roles distintos de `OPERATOR` reciben `403`.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-application`, `nova-web-api`, `replay`, `determinismo`, `dgoj`, `rng`

## Comentarios
- **El valor probatorio descansa en la inmutabilidad del registro** (`game_rounds` append-only por trigger, 3.2.6/2.5.2), no en recálculo en vivo. Por eso el motor puede evolucionar sin mantener N versiones: los replays históricos renderizan el registro, no lo recalculan.
- La recomputación reproducible (`createWithSeed`) queda como herramienta de verificación/forense y como base del *golden-master* del motor (`HU-1-QA-01`), no como dependencia de este endpoint.
- **Dependencias directas:** `HU-4-BE-01` (externa, auth/roles) · `HU-1-BE-02` (externa, genera los rounds). No depende del `SpinKernel` (no recalcula).

## Enlaces y referencias
- Historia: [HU-3](../../readme.md#5-historias-de-usuario).
- RNG y replay: [2.5.3 RNG criptográficamente fuerte y replay determinista](../../readme.md#25-seguridad).
- API: [4.4.5 Replay](../../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios).
- Modelo de datos: [3.2.8 game_rounds](../../readme.md#32-descripción-de-entidades-principales), 3.2.6 `game_configs`.
