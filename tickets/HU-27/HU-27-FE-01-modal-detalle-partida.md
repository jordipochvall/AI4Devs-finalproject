# HU-27-FE-01 — Modal de detalle de partida en la auditoría del operador

## Código
`HU-27-FE-01` — vinculado con **HU-27: El operador consulta el detalle de una partida desde la auditoría**.

## Título
Modal de detalle de partida (importes + rejilla + líneas) sobre la auditoría

## Descripción
Añadir a la pantalla de auditoría del operador una acción **"Detalle"** por fila que abre un **modal ligero** con el detalle de la partida: importes (apuesta, premio, saldo antes/después), la **rejilla de símbolos** resultante y las **líneas ganadoras**. Consume el endpoint **`GET /operator/rounds/{roundId}`** (ya existente, `HU-16-BE-01`) mediante un *hook* de TanStack Query. Es más ligero que el *replay* (no carga la `config` ni reproduce el giro). El *replay* de HU-3 se mantiene como acción independiente en la misma fila.

## Criterios de aceptación
- **AC1**: Cada fila de la auditoría ofrece "Detalle" (además de "Replay"); al pulsarlo se abre un modal con los importes, la rejilla de símbolos y las líneas ganadoras de esa partida.
- **AC2**: El modal es **accesible** (`role="dialog"`, `aria-modal`, foco gestionado) y se cierra con el botón, el fondo o la tecla **Escape**, sin perder los filtros de la auditoría.
- **AC3**: Los importes se formatean según el `locale`; la UI funciona en **español e inglés** y sólo es accesible con rol `OPERATOR`.
- **AC4**: Un detalle de una partida ajena al operador (`404`) muestra un mensaje de error, sin romper la pantalla.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `tanstack-query`, `a11y`, `i18n`, `auditoría`

## Comentarios
- Reutiliza los tipos `WinningPayline`/`view` (de `playerApi`) y el patrón de modal existente (`.dialog`/`.dialog-backdrop`).
- No requiere backend nuevo: el endpoint y `RoundDetailDto` provienen de `HU-16-BE-01`.
- **Dependencias directas:** `HU-16-BE-01` (endpoint), `HU-3-FE-01` (pantalla de auditoría).

## Enlaces y referencias
- Historia: [HU-27](../../stories/HU-27.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
- Endpoint: [§4.3.3](../../readme.md#433-operator--apiv1operator-rol-operator) (`GET /operator/rounds/{roundId}`).
- Diseño y UX: [§1.3](../../readme.md#13-diseño-y-experiencia-de-usuario).
