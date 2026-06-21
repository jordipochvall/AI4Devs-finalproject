# HU-21-QA-01 — Tests de informes y bloqueo ante integridad rota

## Código
`HU-21-QA-01` — vinculado con **HU-21: Generación de informes regulatorios DGOJ (RFJ)**.

## Título
Tests de generación de informes RFJ y de su *gating* por integridad

## Descripción
Verificar que los agregados del informe coinciden con el cálculo de referencia sobre un dataset semilla y que la generación se bloquea cuando la integridad del periodo falla. Integración (Failsafe + Testcontainers).

## Criterios de aceptación
- **AC1**: IT — el informe de un periodo contiene los agregados esperados.
- **AC2**: IT — con la integridad del periodo rota, la generación se bloquea.
- **AC3**: IT — solo `OPERATOR` puede generarlo; otros roles → `403`.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `dgoj`, `informes`

## Comentarios
- **Dependencias directas:** `HU-21-BE-01`, `HU-21-FE-01`.

## Enlaces y referencias
- Historia: [HU-21](../../stories/HU-21.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
