# HU-39-QA-01 — Test del `ErrorBoundary`

## Código
`HU-39-QA-01` — vinculado con **HU-39: Un error de render no deja la pantalla en blanco**.

## Título
Test que fuerza un error de render y verifica el *fallback*

## Descripción
Añadir un test (Testing Library) que monte el `ErrorBoundary` con un componente hijo que lance una excepción durante el render, y verifique que se muestra el mensaje de *fallback* en vez de una pantalla en blanco, y que la acción de recuperación está presente y es funcional.

## Criterios de aceptación
- **AC1**: Test que fuerza un error de render y verifica que se muestra el mensaje de `ErrorBoundary`.
- **AC2**: Test que verifica que la acción de recuperación (recargar/volver al inicio) está presente.

## Prioridad
Could Have

## Estimación
1 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `frontend`, `resiliencia`

## Comentarios
- **Dependencias directas:** `HU-39-FE-01`.

## Enlaces y referencias
- Historia: [HU-39](../../stories/HU-39.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
