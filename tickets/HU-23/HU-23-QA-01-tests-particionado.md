# HU-23-QA-01 — Tests de poda de particiones e integridad tras migrar

## Código
`HU-23-QA-01` — vinculado con **HU-23: Escalado de la auditoría: particionado y retención**.

## Título
Tests de particionado: poda, migración sin pérdida e integridad

## Descripción
Verificar que las consultas por rango descartan particiones fuera de rango (`EXPLAIN`), que la migración del histórico no pierde ni duplica partidas y que la cadena de integridad sigue intacta tras particionar. Integración (Failsafe + Testcontainers).

## Criterios de aceptación
- **AC1**: IT — una consulta acotada por fechas no escanea particiones fuera de rango.
- **AC2**: IT — el recuento de partidas antes y después de migrar coincide (sin pérdida/duplicados).
- **AC3**: IT — la verificación de integridad (HU-20) sigue confirmando la cadena tras la migración.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `particionado`, `integridad`

## Comentarios
- **Dependencias directas:** `HU-23-DB-01`, `HU-23-BE-01`.

## Enlaces y referencias
- Historia: [HU-23](../../stories/HU-23.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
