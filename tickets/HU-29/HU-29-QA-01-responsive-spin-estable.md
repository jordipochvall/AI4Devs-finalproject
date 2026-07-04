# HU-29-QA-01 — Tests de responsive y estabilidad del botón Spin

## Código
`HU-29-QA-01` — vinculado con **HU-29: La pantalla de juego se adapta al viewport sin scroll**.

## Título
Verificación sin scroll (portátil/landscape) y Spin estable

## Descripción
Verificar en distintos viewports (portátil, móvil horizontal via DevTools, móvil vertical) que la pantalla de juego cabe sin scroll, que en ancho aparece el layout de dos columnas y en vertical el apilado, y que el botón Spin permanece fijo al aparecer/desaparecer mensajes. Mantener la suite de `SlotGame`/`GamePage` en verde.

## Criterios de aceptación
- **AC1**: Sin scroll vertical en portátil y móvil landscape para 3x3 y 5x3.
- **AC2**: Dos columnas en ancho/horizontal; apilado en vertical.
- **AC3**: El Spin no se desplaza al provocar premio, tirada gratis y error (apuesta > saldo).
- **AC4**: `vitest` de `SlotGame`/`GamePage` en verde.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `frontend`, `responsive`, `vitest`

## Comentarios
- **Dependencias directas:** `HU-29-FE-01`.

## Enlaces y referencias
- Historia: [HU-29](../../stories/HU-29.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
