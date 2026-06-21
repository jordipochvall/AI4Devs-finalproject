# HU-25-FE-01 — Superficie de administración de operadores

## Código
`HU-25-FE-01` — vinculado con **HU-25: Gestión multi-operador (super-admin)**.

## Título
Pantalla de administración (`/admin`) para listar y dar de alta operadores

## Descripción
Añadir una nueva superficie `/admin` (rol `ADMIN`) con el listado de operadores y el formulario de alta (operador + usuario operador inicial), así como la activación/desactivación. Protegida por el `ProtectedRoute` para el rol `ADMIN`.

## Criterios de aceptación
- **AC1**: Solo usuarios con rol `ADMIN` acceden a `/admin` (otros roles → redirección/`403`).
- **AC2**: El administrador da de alta un operador y su usuario inicial desde la UI.
- **AC3**: Se puede activar/desactivar un operador.
- **AC4**: La UI funciona en **español e inglés**.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `admin`, `multi-tenant`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-25-BE-01`.

## Enlaces y referencias
- Historia: [HU-25](../../stories/HU-25.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Roles y superficies: [§2.5](../../readme.md#25-seguridad).
