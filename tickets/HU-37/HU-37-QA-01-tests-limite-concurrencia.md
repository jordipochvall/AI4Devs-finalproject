# HU-37-QA-01 — Test del límite de concurrencia de simulaciones

## Código
`HU-37-QA-01` — vinculado con **HU-37: Acotar la concurrencia de simulaciones para proteger el VPS del demo**.

## Título
Test que lanza más simulaciones que el límite y verifica el rechazo

## Descripción
Añadidos dos tests en `SimulationUseCaseTest` (`SimulationLaunchPort` mockeado con `countRunning()`): con el límite alcanzado, rechaza y ni siquiera consulta la config (`ownedConfigPaylineCount`/`createAndLaunch` nunca se llaman); por debajo del límite, acepta y lanza con normalidad. Añadido `EngineConfigTest.simulationExecutorIsBoundedToTheConfiguredSizes` que construye el `ThreadPoolTaskExecutor` con los tamaños configurados y verifica `corePoolSize`/`maxPoolSize`/capacidad de la cola.

## Criterios de aceptación
- **AC1**: Test que, con el límite alcanzado, verifica el rechazo de una nueva simulación con el error esperado. ✅ `launch_atConcurrencyLimit_throwsAndSkipsConfigLookup`.
- **AC2**: Test que, por debajo del límite, verifica la aceptación normal. ✅ `launch_belowConcurrencyLimit_proceeds`.
- **AC3**: Test que confirma que el executor configurado no crece sin límite (tamaño de *pool* acotado). ✅ `EngineConfigTest`.

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
