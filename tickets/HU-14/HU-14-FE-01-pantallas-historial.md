# HU-14-FE-01 — Pantallas de historial del jugador

## Código
`HU-14-FE-01` — vinculado con **HU-14: El jugador consulta su historial de movimientos y partidas**.

## Título
Vistas de movimientos del wallet y de partidas propias

## Descripción
Añadir a la superficie del jugador dos vistas paginadas: **movimientos** del wallet y **partidas** propias, con importes formateados según el `locale` (separador de miles, dos decimales, divisa) y paginación. Consumen `GET /player/wallet/transactions` y `GET /player/rounds`.

## Criterios de aceptación
- **AC1**: La vista de movimientos lista tipo, importe (formateado por `locale`) y fecha, con paginación funcional.
- **AC2**: La vista de partidas lista apuesta, premio, saldo y fecha de cada giro.
- **AC3**: Los controles de paginación deshabilitan correctamente "anterior"/"siguiente" en los extremos.
- **AC4**: La UI funciona en **español e inglés**.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `tanstack-query`, `player`, `i18n`

## Comentarios
- Reutiliza el envoltorio de paginación y el formateo de dinero existentes.
- **Dependencias directas:** `HU-14-BE-01` · `HU-5-FE-01` (superficie de jugador, externa).

## Enlaces y referencias
- Historia: [HU-14](../../stories/HU-14.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Paginación: [§4.1](../../readme.md#41-principios-de-diseño-y-convenciones).
