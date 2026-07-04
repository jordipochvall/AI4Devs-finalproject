# HU-30-FE-02 — Giro de rodillos natural (reels + parada escalonada)

## Código
`HU-30-FE-02` — vinculado con **HU-30: Fondos temáticos por juego y giro de rodillos natural**.

## Título
Refactor de `SlotGame` a columnas + animación de giro con parada escalonada

## Descripción
Reestructurar el render de `SlotGame` de celdas `rows×cols` a **columnas (reels)**: cada rodillo es una ventana `overflow:hidden` con una tira de símbolos. Sustituir el parpadeo de opacidad por un **giro vertical** (capa superpuesta que se desplaza con desenfoque de movimiento) que **para de forma escalonada** columna a columna (ease-out) al asentar el resultado, resaltando las líneas ganadoras. Conservar `animationToken`, el cinemático de free-spins y los `data-testid`/roles/`data-symbol`.

## Criterios de aceptación
- **AC1**: Al pulsar Spin, los rodillos giran verticalmente con desenfoque y paran escalonados asentando el resultado.
- **AC2**: Las líneas ganadoras se resaltan al asentar; el cinemático de free-spins sigue funcionando.
- **AC3**: Con `prefers-reduced-motion` no hay animación y el resultado se muestra al instante.
- **AC4**: Los tests existentes de `SlotGame` siguen en verde (rejilla, cuenta de celdas, roles).

## Prioridad
Could Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `animación`, `a11y`

## Comentarios
- `view[col][row]` ya es column-major → encaja con el modelo de rodillos.
- **Dependencias directas:** `HU-30-FE-01`, `HU-1` (`SlotGame`).

## Enlaces y referencias
- Historia: [HU-30](../../stories/HU-30.md).
- Especificación: [readme §5.6](../../readme.md#56-animación-de-los-rodillos).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
