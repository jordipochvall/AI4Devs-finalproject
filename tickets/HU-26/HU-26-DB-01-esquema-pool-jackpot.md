# HU-26-DB-01 — Esquema del *pool* de jackpot

## Código
`HU-26-DB-01` — vinculado con **HU-26: Jackpots progresivos**.

## Título
Migración Flyway: *pool* de jackpot por juego y registro de concesiones

## Descripción
Migración Flyway (aditiva) que crea el modelo del jackpot progresivo: el valor actual del *pool* por juego (con su valor semilla y la fracción de contribución) y el registro de concesiones (qué partida lo ganó y por cuánto). Mantiene la auditabilidad e inmutabilidad del registro de partidas.

## Criterios de aceptación
- **AC1**: Existe el *pool* por juego con valor actual, valor semilla y fracción de contribución.
- **AC2**: Existe el registro inmutable de concesiones del jackpot vinculado a la partida ganadora.
- **AC3**: La migración es **aditiva**; el jackpot es opcional por juego (config).

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
DB

## Etiquetas
`db`, `flyway`, `jackpot`, `motor`, `fase-post-mvp`

## Comentarios
- **Dependencias directas:** `HU-1-DB-01` (esquema base, externa).

## Enlaces y referencias
- Historia: [HU-26](../../stories/HU-26.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Decisión diferida: [§1.5 D11](../../readme.md#15-supuestos-y-decisiones-diferidas) · Esquema del `config` [§3.3](../../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
