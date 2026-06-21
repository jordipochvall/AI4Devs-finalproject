# HU-14-QA-01 — Tests de historial y aislamiento

## Código
`HU-14-QA-01` — vinculado con **HU-14: El jugador consulta su historial de movimientos y partidas**.

## Título
Tests de los historiales del jugador y del aislamiento por jugador

## Descripción
Verificar, sobre un dataset semilla, que los listados de movimientos y partidas devuelven la página correcta ordenada por fecha y que **un jugador nunca ve datos de otro**. Integración (Failsafe + Testcontainers) y test de componente de las vistas.

## Criterios de aceptación
- **AC1**: IT — `wallet/transactions` y `rounds` devuelven páginas ordenadas por `created_at DESC`.
- **AC2**: IT — el aislamiento se cumple: con el token de un jugador no aparecen datos de otro.
- **AC3**: Componente — las vistas renderizan importes formateados y la paginación funciona.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `player`, `paginacion`

## Comentarios
- **Dependencias directas:** `HU-14-BE-01`, `HU-14-FE-01`.

## Enlaces y referencias
- Historia: [HU-14](../../stories/HU-14.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
