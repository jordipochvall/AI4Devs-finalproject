# HU-20-DB-01 — Hash encadenado en `game_rounds`

## Código
`HU-20-DB-01` — vinculado con **HU-20: Integridad *tamper-evident* de la auditoría**.

## Título
Migración Flyway: columna de hash encadenado en `game_rounds`

## Descripción
Migración Flyway (aditiva) que añade a `game_rounds` la columna del hash de integridad (hash del contenido del round encadenado con el hash de la fila anterior). Define el orden de encadenado (por `id`/`created_at`) y permite recalcular y verificar la cadena.

## Criterios de aceptación
- **AC1**: `game_rounds` tiene una columna para el hash encadenado.
- **AC2**: La migración es **aditiva**; el histórico existente puede *backfillearse* con su cadena.
- **AC3**: El encadenado está definido de forma determinista (orden y campos que entran en el hash).

## Prioridad
Could Have

## Estimación
1 SP

## Equipo responsable
DB

## Etiquetas
`db`, `flyway`, `integridad`, `dgoj`, `fase-post-mvp`

## Comentarios
- Compatible con la inmutabilidad por trigger del MVP (no se modifican filas; solo se añade el hash al insertar).
- **Dependencias directas:** `HU-1-DB-01` (esquema `game_rounds`, externa).

## Enlaces y referencias
- Historia: [HU-20](../../stories/HU-20.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Inmutabilidad: [§2.5 y §3.2.12](../../readme.md#25-seguridad) · Decisiones diferidas [§1.5 D3, D12](../../readme.md#15-supuestos-y-decisiones-diferidas).
