# HU-15-BE-01 — Endpoints de configuración comercial de los juegos

## Código
`HU-15-BE-01` — vinculado con **HU-15: El operador gestiona la configuración comercial de los juegos**.

## Título
`GET /operator/games` y `PUT /operator/games/{gameId}` con validaciones de coherencia

## Descripción
Exponer al operador la configuración **comercial** de sus juegos (apuestas mín/máx, paso, monedas, `active`) y permitir actualizarla, sin tocar la matemática. La validación exige coherencia con el número de líneas activas: `min_bet_cents` y `bet_step_cents` deben ser múltiplos del nº de paylines de la `config` activa (§3.3.3).

## Criterios de aceptación
- **AC1**: `GET /operator/games` lista los juegos del operador con su configuración comercial.
- **AC2**: `PUT /operator/games/{gameId}` actualiza apuestas/monedas/`active` y persiste el cambio.
- **AC3**: Si `min_bet`/`step` no son múltiplos del nº de líneas activas → `422` con detalle.
- **AC4**: Solo rol `OPERATOR`; otros roles → `403`.
- **AC5**: Aislamiento por operador: solo se ven y editan juegos del operador del token.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `operator`, `validacion`, `fase-post-mvp`

## Comentarios
- Endpoints **post-MVP** del catálogo (§4.2). No modifica la matemática (eso es HU-7).
- **Dependencias directas:** `HU-6-BE-01` (superficie/gestión de operador) · `HU-4-BE-01` (auth, externa).

## Enlaces y referencias
- Historia: [HU-15](../../stories/HU-15.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Catálogo: [§4.2](../../readme.md#42-catálogo-de-endpoints) (`/operator/games`, `PUT`, *post-MVP*) · Apuesta por línea [§3.3.3](../../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
