# HU-37-QA-01 — Test del límite de concurrencia de simulaciones

## Código
`HU-37-QA-01` — vinculado con **HU-37: Acotar la concurrencia de simulaciones para proteger el VPS del demo**.

## Título
Test que lanza más simulaciones que el límite y verifica el rechazo

## Descripción
Añadir un test (unitario sobre `SimulationUseCase` con un `SimulationLaunchPort` de prueba que simule N simulaciones `RUNNING`, y/o un IT si aplica) que verifique que, alcanzado el límite configurado de simulaciones concurrentes, una petición adicional se rechaza con el error esperado, y que por debajo del límite se sigue aceptando con normalidad.

## Criterios de aceptación
- **AC1**: Test que, con el límite alcanzado, verifica el rechazo de una nueva simulación con el error esperado.
- **AC2**: Test que, por debajo del límite, verifica la aceptación normal.
- **AC3**: Test que confirma que el executor configurado no crece sin límite (tamaño de *pool* acotado).

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `backend`, `concurrencia`

## Comentarios
- **Dependencias directas:** `HU-37-BE-01`.

## Enlaces y referencias
- Historia: [HU-37](../../stories/HU-37.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
