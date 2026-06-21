# HU-15-BE-02 — Auditoría de cambios comerciales

## Código
`HU-15-BE-02` — vinculado con **HU-15: El operador gestiona la configuración comercial de los juegos**.

## Título
Registro auditable de los cambios de configuración comercial

## Descripción
Registrar de forma **append-only** cada modificación comercial (`PUT /operator/games/{id}`) en una nueva tabla de auditoría comercial: valores **antes/después**, autor y fecha. Cierra la decisión diferida D5: el MVP audita la matemática (`game_config_publications`) pero no los cambios comerciales.

## Criterios de aceptación
- **AC1**: Cada actualización comercial inserta una entrada de auditoría con antes/después, `performed_by_user_id` y `created_at`.
- **AC2**: La tabla es **inmutable** (append-only; sin UPDATE/DELETE).
- **AC3**: Las entradas son consultables por juego y por operador.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `auditoria`, `dgoj`, `fase-post-mvp`

## Comentarios
- Requiere una **migración Flyway** para la tabla de auditoría comercial.
- **Dependencias directas:** `HU-15-BE-01` (la actualización que se audita).

## Enlaces y referencias
- Historia: [HU-15](../../stories/HU-15.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Decisión diferida: [§1.5 D5](../../readme.md#15-supuestos-y-decisiones-diferidas) · Auditoría comercial [§3.2](../../readme.md#3-modelo-de-datos).
