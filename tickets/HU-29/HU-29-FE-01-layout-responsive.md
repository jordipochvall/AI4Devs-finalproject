# HU-29-FE-01 — Pantalla de juego responsive sin scroll y barra de acción estable

## Código
`HU-29-FE-01` — vinculado con **HU-29: La pantalla de juego se adapta al viewport sin scroll**.

## Título
`100dvh` + dos columnas en ancho + Spin centrado fijo

## Descripción
Rediseñar el layout de `GamePage`/`SlotGame` para que quepa sin scroll: `.game-page` como contenedor flex a `100dvh`, rejilla dimensionada por el espacio disponible (celdas cuadradas, ancho acotado por alto con `min()` y `--cols`/`--rows`). En pantallas anchas/horizontales (`min-width:900px` y `min-aspect-ratio:1/1`), layout en **dos columnas** (rodillos + panel lateral con apuesta/Spin/auto/estado). Región de estado de **altura reservada** y barra de acción con el **Spin centrado** (rejilla de 3 columnas) para que no reflujen los controles.

## Criterios de aceptación
- **AC1**: En portátil y en móvil horizontal el juego cabe sin scroll vertical (juegos 3x3 y 5x3).
- **AC2**: En viewport ancho/horizontal aparecen dos columnas (rodillos + panel lateral); en vertical se apila con barra inferior.
- **AC3**: Al aparecer/desaparecer mensajes (premio, tirada gratis, error), el botón Spin no cambia de posición.
- **AC4**: El banner de juego responsable adopta variante compacta en landscape bajo.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `css`, `responsive`, `layout`

## Comentarios
- Se conservan `data-testid`/roles/`data-symbol` de la rejilla para no romper tests.
- **Dependencias directas:** `HU-28-FE-01` (tokens), `HU-1` (pantalla de juego).

## Enlaces y referencias
- Historia: [HU-29](../../stories/HU-29.md).
- Especificación: [readme §5.5](../../readme.md#55-responsive-y-layout-sin-scroll).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
