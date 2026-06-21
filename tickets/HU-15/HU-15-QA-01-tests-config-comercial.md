# HU-15-QA-01 — Tests de configuración comercial y auditoría

## Código
`HU-15-QA-01` — vinculado con **HU-15: El operador gestiona la configuración comercial de los juegos**.

## Título
Tests de edición comercial, validaciones y registro de auditoría

## Descripción
Verificar que la actualización comercial persiste, que crea la entrada de auditoría con antes/después, que la validación de múltiplos del nº de líneas rechaza valores incoherentes y que el endpoint respeta el rol. Integración (Failsafe + Testcontainers).

## Criterios de aceptación
- **AC1**: IT — `PUT /operator/games/{id}` persiste el cambio y devuelve la configuración actualizada.
- **AC2**: IT — el cambio genera una entrada de auditoría comercial con autor, fecha y antes/después.
- **AC3**: IT — `min_bet`/`step` no múltiplos del nº de líneas → `422`.
- **AC4**: IT — un rol distinto de `OPERATOR` → `403`; otro operador no ve/edita el juego.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `operator`, `auditoria`

## Comentarios
- **Dependencias directas:** `HU-15-BE-01`, `HU-15-BE-02`, `HU-15-FE-01`.

## Enlaces y referencias
- Historia: [HU-15](../../stories/HU-15.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
