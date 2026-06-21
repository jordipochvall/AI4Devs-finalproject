# HU-23-BE-01 — Ajuste de consultas a particiones + archivado en frío

## Código
`HU-23-BE-01` — vinculado con **HU-23: Escalado de la auditoría: particionado y retención**.

## Título
Consultas de auditoría con poda de particiones y archivado de particiones antiguas

## Descripción
Garantizar que las consultas de auditoría/dashboard aprovechan la **poda de particiones** (filtros por fecha) y definir el proceso de **archivado en frío** de particiones fuera de la ventana activa, manteniéndolas inmutables y verificables.

## Criterios de aceptación
- **AC1**: Las consultas de auditoría por rango de fechas usan poda de particiones (verificable con `EXPLAIN`).
- **AC2**: Existe un proceso para archivar particiones antiguas en almacenamiento frío.
- **AC3**: Los datos archivados siguen siendo inmutables y su cadena de integridad permanece intacta.

## Prioridad
Could Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `particionado`, `escalado`, `retencion`

## Comentarios
- **Dependencias directas:** `HU-23-DB-01`.

## Enlaces y referencias
- Historia: [HU-23](../../stories/HU-23.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Decisión diferida: [§1.5 D4](../../readme.md#15-supuestos-y-decisiones-diferidas).
