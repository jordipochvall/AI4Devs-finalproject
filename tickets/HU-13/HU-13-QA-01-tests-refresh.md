# HU-13-QA-01 — Tests de refresh y revocación

## Código
`HU-13-QA-01` — vinculado con **HU-13: Sesión persistente con refresh tokens**.

## Título
Tests de renovación, rotación y revocación de la sesión

## Descripción
Suite que verifica el ciclo de vida del refresh token: renovación con refresh válido, rechazo de refresh inválido/caducado/revocado, rotación (el anterior deja de valer) y revocación en `logout`. Integración (Failsafe) en el backend y test de componente del interceptor en el frontend.

## Criterios de aceptación
- **AC1**: IT — `refresh` con token válido devuelve `200` y un nuevo *access token*.
- **AC2**: IT — `refresh` tras `logout` (o con token rotado) → `401`.
- **AC3**: Componente FE — un `401` por expiración dispara la renovación y el reintento; un fallo de refresh redirige a `/login`.

## Prioridad
Should Have

## Estimación
1 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `auth`, `fase-post-mvp`

## Comentarios
- **Dependencias directas:** `HU-13-BE-01`, `HU-13-FE-01`.

## Enlaces y referencias
- Historia: [HU-13](../../stories/HU-13.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
