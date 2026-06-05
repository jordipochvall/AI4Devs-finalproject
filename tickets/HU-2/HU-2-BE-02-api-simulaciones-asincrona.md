# HU-2-BE-02 — API de simulaciones asíncrona

## Código
`HU-2-BE-02` — vinculado con **HU-2: El matemático valida un juego con el simulador**.

## Título
API de simulaciones asíncrona (`POST .../simulations` → `202`, `GET .../simulations/{id}` polling)

## Descripción
Exponer la API de simulaciones del backoffice matemático, conectando el módulo `nova-simulator` (`HU-2-BE-01`) con la persistencia en `simulation_runs`:

- **`POST /api/v1/math/configs/{configId}/simulations`** — valida `numSpins` (1 ≤ N ≤ 10 000 000) y `betCents`, inserta una fila `simulation_runs` con `status = RUNNING`, lanza la ejecución asíncrona y responde `202 Accepted` con el `simulationId`, `status`, `startedAt` y `pollUrl`.
- **`GET /api/v1/math/simulations/{simulationId}`** — devuelve el estado y, si `COMPLETED`, todas las métricas; útil para el *polling* del frontend.
- Cuando la ejecución termina, el resultado agregado se persiste en la fila y `status` pasa a `COMPLETED` (o `FAILED` con `error_message`).

Adicionalmente:
- **`GET /api/v1/math/games`** — listar juegos disponibles con su `config` activa.
- **`GET /api/v1/math/configs/{configId}`** — detalle de una versión de `config` (necesario para seleccionar antes de simular).
- **`POST /api/v1/math/games/{gameId}/configs`** — crear una nueva versión de matemática (editor; valida invariantes de 3.3.3 y persiste el `rtp_target` **declarado** por el matemático; no lo calcula).

## Criterios de aceptación
- **AC1**: `POST .../simulations` con `numSpins = 10_000_000` responde `202` en < 1 s con el `simulationId` y un `pollUrl` válido.
- **AC2**: `numSpins` fuera de rango (≤ 0 o > 10 000 000) responde `422`.
- **AC3**: `GET .../simulations/{id}` devuelve `status = RUNNING` mientras la simulación corre, sin métricas pobladas.
- **AC4**: Cuando termina, `GET .../simulations/{id}` devuelve `status = COMPLETED` con `rtpEmpirical`, `rtpBaseGame`, `rtpFreeSpins`, `hitFrequency`, `volatility`, `maxWinMultiplier`, `freeSpinTriggerFreq`, `longestDryStreak`, `prizeDistribution` y `durationMs`.
- **AC5**: `POST .../configs` con un `config` inválido (incumple invariantes del 3.3.3) responde `422` con un `errors[]` detallado por campo.
- **AC6**: Solo usuarios con rol `MATH_ANALYST` pueden invocar estos endpoints; otros roles reciben `403`.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-application`, `nova-web-api`, `simulador`, `asincrono`, `validacion`

## Comentarios
- La ejecución asíncrona se modela con un `@Async` Spring que invoca `SimulationRunner.run()`. No se usan WebSockets (ver 4.1).
- El endpoint `POST .../math/games/{gameId}/publish` queda como **post-MVP** (ver 4.2).
- **Dependencias directas:** `HU-2-BE-01` (intra, simulator) · `HU-4-BE-01` (externa, auth/roles) · `HU-7-BE-01` (externa, necesita una versión de `config` que simular).

## Enlaces y referencias
- Historia: [HU-2](../../readme.md#5-historias-de-usuario).
- API: [4.4.4 Lanzar simulación](../../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios), catálogo [4.2](../../readme.md#42-catálogo-de-endpoints).
- Modelo de datos: [3.2.6 game_configs](../../readme.md#32-descripción-de-entidades-principales), 3.2.9 `simulation_runs`.
- Esquema del config: [3.3](../../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
