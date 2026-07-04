# HU-30-QA-01 — Tests de tematización, giro de rodillos y reduce-motion

## Código
`HU-30-QA-01` — vinculado con **HU-30: Fondos temáticos por juego y giro de rodillos natural**.

## Título
Verificación de skins, animación de giro y accesibilidad de movimiento

## Descripción
Verificar que cada juego aplica su skin (fondo, marco, brillo), que el giro de rodillos se ve natural con parada escalonada, y que con `prefers-reduced-motion` el resultado se muestra sin animación. Mantener la suite de `SlotGame` en verde (la refactorización a rodillos no debe romper selectores/roles).

## Criterios de aceptación
- **AC1**: Revisión visual de los tres juegos: fondo/marco/brillo por tema; giro con parada escalonada; líneas ganadoras resaltadas.
- **AC2**: Con `prefers-reduced-motion` activo, no hay animación de giro y el resultado aparece al instante.
- **AC3**: `vitest` de `SlotGame` en verde tras el refactor a rodillos.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `frontend`, `vitest`, `a11y`, `animación`

## Comentarios
- **Dependencias directas:** `HU-30-FE-01`, `HU-30-FE-02`.

## Enlaces y referencias
- Historia: [HU-30](../../stories/HU-30.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
