# HU-30-FE-01 — Fondos temáticos por juego

## Código
`HU-30-FE-01` — vinculado con **HU-30: Fondos temáticos por juego y giro de rodillos natural**.

## Título
`data-theme` + degradado + carátula difuminada + tinte de marco/brillo

## Descripción
Aplicar un skin por juego mediante `data-theme="egyptian|fruits|space"` en `.game-page` (a partir de `game.theme`). En `tokens.css`, cada `[data-theme]` define el degradado de fondo (`--theme-a/-b`), la carátula (`--theme-cover`) usada como telón difuminado (`::before` con `blur`), el tinte del marco de los rodillos (`--theme-frame`) y el color del brillo de premio (`--theme-win`). Estilar además el banner de jackpot que faltaba.

## Criterios de aceptación
- **AC1**: Cada juego muestra su fondo temático (degradado + carátula difuminada) y su marco/brillo teñidos.
- **AC2**: El cambio de juego cambia el skin sin recargar (deriva de `game.theme`).
- **AC3**: El banner de jackpot tiene estilo coherente (pastilla dorada, cifras tabulares).

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `css`, `theming`, `assets`

## Comentarios
- Reutiliza las carátulas ya existentes en `/assets/<tema>/cover.jpg`.
- **Dependencias directas:** `HU-28-FE-01` (tokens), `HU-29-FE-01` (layout del juego).

## Enlaces y referencias
- Historia: [HU-30](../../stories/HU-30.md).
- Especificación: [readme §5.4](../../readme.md#54-tematización-por-juego).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
