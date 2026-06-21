# HU-16-FE-01 — Dashboard de actividad del operador

## Código
`HU-16-FE-01` — vinculado con **HU-16: El operador consulta el dashboard de actividad**.

## Título
Pantalla de dashboard con KPIs y filtro por fechas

## Descripción
Añadir a la superficie del operador un dashboard que muestra jugadores activos, GGR y juegos más jugados (consume `GET /operator/dashboard`), con un selector de rango de fechas. Importes formateados según `locale`. Enlace al detalle de partida desde la auditoría.

## Criterios de aceptación
- **AC1**: El dashboard pinta jugadores activos, GGR y top de juegos, recalculándose al cambiar el rango de fechas.
- **AC2**: Los importes se formatean según el `locale` (divisa, dos decimales).
- **AC3**: La UI funciona en **español e inglés** y solo es accesible con rol `OPERATOR`.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `tanstack-query`, `dashboard`, `i18n`

## Comentarios
- Gráficos ligeros (sin dependencia pesada), coherentes con el dashboard del matemático (HU-2-FE-01).
- **Dependencias directas:** `HU-16-BE-01`.

## Enlaces y referencias
- Historia: [HU-16](../../stories/HU-16.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Diseño y UX: [§1.3](../../readme.md#13-diseño-y-experiencia-de-usuario).
