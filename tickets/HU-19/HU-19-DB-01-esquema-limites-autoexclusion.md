# HU-19-DB-01 — Esquema de límites y autoexclusión

## Código
`HU-19-DB-01` — vinculado con **HU-19: Límites de pérdida y autoexclusión impuestos en servidor**.

## Título
Migración Flyway: tablas de límites de juego y de autoexclusión

## Descripción
Migración Flyway (aditiva) que crea el modelo para los límites de juego responsable (pérdida/depósito/tiempo) por jugador y los periodos de autoexclusión, con marcas temporales de aplicación y de enfriamiento. Incluye índices por `user_id` para la verificación en el camino del giro.

## Criterios de aceptación
- **AC1**: Existen las tablas de límites (tipo, importe/periodo, vigencia, enfriamiento) y de autoexclusión (inicio/fin).
- **AC2**: La migración es **aditiva y sin pérdida**; no afecta a los datos existentes.
- **AC3**: Hay índice por `user_id` para resolver la verificación previa al giro de forma eficiente.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
DB

## Etiquetas
`db`, `flyway`, `dgoj`, `fase-post-mvp`

## Comentarios
- Amplía el esquema del MVP (`HU-1-DB-01`) sin tocar tablas existentes.
- **Dependencias directas:** `HU-1-DB-01` (esquema base, externa).

## Enlaces y referencias
- Historia: [HU-19](../../stories/HU-19.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Decisión diferida: [§1.5 D7](../../readme.md#15-supuestos-y-decisiones-diferidas) · Modelo de datos [§3](../../readme.md#3-modelo-de-datos).
