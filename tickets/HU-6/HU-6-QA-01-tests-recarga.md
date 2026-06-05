# HU-6-QA-01 — Tests de gestión de jugadores y recarga

## Código
`HU-6-QA-01` — vinculado con **HU-6: El operador gestiona jugadores y recarga su saldo**.

## Título
Tests de búsqueda de jugadores y recarga idempotente

## Descripción
- **Integration (Failsafe + Testcontainers)**: búsqueda por email (paginación, coincidencia parcial); recarga correcta (saldo actualizado + movimiento `RECHARGE` con operador); `422` por importe ≤ 0; `404` por jugador inexistente; `403` por rol no operador.
- **Test de idempotencia**: dos peticiones con la misma `Idempotency-Key` incrementan el saldo una sola vez; con payload distinto → `409`.

## Criterios de aceptación
- **AC1**: Cubiertos `200`/`422`/`404`/`403` de ambos endpoints.
- **AC2**: Verificada la idempotencia de la recarga (sin doble incremento).
- **AC3**: Verificado que el movimiento `RECHARGE` queda en el ledger con el `performed_by_user_id` correcto.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `wallet`, `idempotencia`

## Comentarios
- **Dependencias directas:** `HU-6-FE-01` (intra) — arrastra `HU-6-BE-01`.

## Enlaces y referencias
- Historia: [HU-6](../../stories/HU-6.md).
- Estrategia de tests: [2.6](../../readme.md#26-tests).
