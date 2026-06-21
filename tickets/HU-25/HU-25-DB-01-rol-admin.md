# HU-25-DB-01 — Rol `ADMIN` en el dominio `users.role`

## Código
`HU-25-DB-01` — vinculado con **HU-25: Gestión multi-operador (super-admin)**.

## Título
Migración Flyway: ampliar la constraint `CHECK` de `users.role` con `ADMIN`

## Descripción
Migración Flyway (aditiva) que amplía el dominio de estado de `users.role` (`CHECK ... IN (...)`, §3.2.12) para incluir el rol `ADMIN`, base de la gestión multi-operador. No afecta a los usuarios existentes.

## Criterios de aceptación
- **AC1**: `users.role` admite el valor `ADMIN` además de `PLAYER`/`OPERATOR`/`MATH_ANALYST`.
- **AC2**: La migración es **aditiva**; no modifica filas existentes.
- **AC3**: El mapeo JPA (`@Enumerated(STRING)`) reconoce el nuevo valor.

## Prioridad
Could Have

## Estimación
1 SP

## Equipo responsable
DB

## Etiquetas
`db`, `flyway`, `roles`, `multi-tenant`, `fase-post-mvp`

## Comentarios
- Recrea el `CHECK` del dominio de estado (§3.2.12), sin coste de `ALTER TYPE`.
- **Dependencias directas:** `HU-1-DB-01` (esquema base, externa).

## Enlaces y referencias
- Historia: [HU-25](../../stories/HU-25.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Dominios de estado: [§3.2.12](../../readme.md#32-descripción-de-entidades-principales).
