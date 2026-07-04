# HU-28-QA-01 — No-regresión del sistema de diseño y contraste AA

## Código
`HU-28-QA-01` — vinculado con **HU-28: Sistema de diseño y tipografía coherentes**.

## Título
Suite en verde (`vitest` + `tsc`) y verificación de tipografía/contraste

## Descripción
Garantizar que la introducción del sistema de diseño y la tokenización no rompen la aplicación: la suite de componentes (`vitest`) y `tsc --noEmit` deben quedar en verde. Verificación visual de que la tipografía es coherente (títulos serif display, cuerpo sans) en jugador y backoffice, y de que los textos tenues cumplen contraste AA.

## Criterios de aceptación
- **AC1**: `vitest run` y `tsc --noEmit` en verde tras los cambios.
- **AC2**: Revisión visual: ninguna pantalla en la fuente por defecto del navegador; botones/campos/diálogos consistentes.
- **AC3**: Los textos secundarios/tenues pasan un chequeo de contraste WCAG 2.1 AA.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `frontend`, `vitest`, `a11y`, `contraste`

## Comentarios
- **Dependencias directas:** `HU-28-FE-01`, `HU-28-FE-02`.

## Enlaces y referencias
- Historia: [HU-28](../../stories/HU-28.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
