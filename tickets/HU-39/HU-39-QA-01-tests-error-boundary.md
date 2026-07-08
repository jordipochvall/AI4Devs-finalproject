# HU-39-QA-01 — Test del `ErrorBoundary`

## Código
`HU-39-QA-01` — vinculado con **HU-39: Un error de render no deja la pantalla en blanco**.

## Título
Test que fuerza un error de render y verifica el *fallback*

## Descripción
Añadido `ErrorBoundary.test.tsx` (Testing Library): un caso confirma que, sin errores, los hijos se renderizan con normalidad; otro monta un componente que lanza una excepción y verifica que aparece el `role="alert"` con el botón de recuperación en vez de una pantalla en blanco.

## Criterios de aceptación
- **AC1**: Test que fuerza un error de render y verifica que se muestra el mensaje de `ErrorBoundary`. ✅
- **AC2**: Test que verifica que la acción de recuperación (recargar/volver al inicio) está presente. ✅
- **Verificado también**: suite completa del frontend (32 ficheros, 114 tests) y `tsc --noEmit` en verde tras el cambio; `vite build` genera el bundle sin errores.

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
