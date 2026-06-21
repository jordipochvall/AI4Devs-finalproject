# HU-16-QA-01 — Tests de métricas agregadas y aislamiento

## Código
`HU-16-QA-01` — vinculado con **HU-16: El operador consulta el dashboard de actividad**.

## Título
Tests del dashboard: exactitud de agregados, filtro de fechas y aislamiento

## Descripción
Verificar, contra un dataset semilla conocido, que los agregados (GGR, activos, top) coinciden con el cálculo de referencia, que el filtro de fechas acota correctamente y que las métricas no mezclan operadores. Integración (Failsafe + Testcontainers).

## Criterios de aceptación
- **AC1**: IT — los agregados del dashboard coinciden con el cálculo de referencia sobre el dataset.
- **AC2**: IT — el filtro por rango de fechas excluye la actividad fuera de la ventana.
- **AC3**: IT — el detalle de partida devuelve `404` para id inexistente y `403` para roles no operador.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `operator`, `metricas`

## Comentarios
- **Dependencias directas:** `HU-16-BE-01`, `HU-16-FE-01`.

## Enlaces y referencias
- Historia: [HU-16](../../stories/HU-16.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
