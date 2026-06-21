# HU-25-BE-01 — Endpoints `/admin/operators` + rol `ADMIN` + aislamiento por tenant

## Código
`HU-25-BE-01` — vinculado con **HU-25: Gestión multi-operador (super-admin)**.

## Título
Alta y gestión de operadores con rol `ADMIN` y aislamiento estricto

## Descripción
Endpoints **Fase 2** `GET /admin/operators` y `POST /admin/operators` (rol `ADMIN`) para listar y dar de alta operadores junto a su usuario operador inicial, activando operativamente el modelo multi-tenant del esquema (`operators` como raíz). Se refuerza el **aislamiento por `operator_id`** en todas las consultas existentes.

## Criterios de aceptación
- **AC1**: `POST /admin/operators` crea un operador activo y su usuario operador inicial.
- **AC2**: El nuevo operador inicia sesión sobre datos vacíos y **aislados** de otros operadores.
- **AC3**: Desactivar un operador impide a sus usuarios operar hasta reactivarlo.
- **AC4**: Solo rol `ADMIN` accede a `/admin/*`; otros roles → `403`.

## Prioridad
Could Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `multi-tenant`, `admin`, `fase-2`

## Comentarios
- Endpoints **Fase 2** (nuevos; grupo `/admin` del catálogo §4.2). El aislamiento por `operator_id` ya está en el modelo del MVP; aquí se sistematiza y se prueba.
- **Dependencias directas:** `HU-25-DB-01` (rol `ADMIN`) · `HU-4-BE-01` (auth/roles, externa).

## Enlaces y referencias
- Historia: [HU-25](../../stories/HU-25.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Catálogo (grupo Admin, Fase 2): [§4.2](../../readme.md#42-catálogo-de-endpoints) · Multi-tenancy [§3.2 `operators`](../../readme.md#32-descripción-de-entidades-principales).
