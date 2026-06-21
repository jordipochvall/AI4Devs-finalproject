# HU-13-FE-01 — Interceptor de renovación silenciosa

## Código
`HU-13-FE-01` — vinculado con **HU-13: Sesión persistente con refresh tokens**.

## Título
Interceptor Axios de renovación silenciosa del *access token*

## Descripción
Añadir al cliente Axios la lógica de **renovación transparente**: ante un `401` por *access token* expirado, el interceptor llama a `/auth/refresh`, actualiza el token en el store de sesión y **reintenta** la petición original. Una única renovación concurrente (las peticiones en vuelo se encolan); si el refresh falla, limpia la sesión y redirige a `/login`.

## Criterios de aceptación
- **AC1**: Ante un `401` por expiración, el interceptor renueva y reintenta la petición sin intervención del usuario.
- **AC2**: Si el refresh falla (`401`), se limpia la sesión y se redirige a `/login`.
- **AC3**: Varias peticiones concurrentes que reciben `401` disparan **una sola** renovación (cola); el resto se reintenta tras renovar.
- **AC4**: La renovación es transparente: la operación en curso continúa sin error visible.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `axios`, `auth`, `fase-post-mvp`

## Comentarios
- Se integra sobre el interceptor de JWT existente sin duplicarlo.
- **Dependencias directas:** `HU-13-BE-01` (endpoint de refresh) · `HU-4-FE-01` (sesión/cliente, externa).

## Enlaces y referencias
- Historia: [HU-13](../../stories/HU-13.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Seguridad y sesión: [§2.5](../../readme.md#25-seguridad).
