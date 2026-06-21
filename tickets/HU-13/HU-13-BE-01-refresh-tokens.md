# HU-13-BE-01 — Refresh tokens: endpoint `/auth/refresh`, rotación y revocación

## Código
`HU-13-BE-01` — vinculado con **HU-13: Sesión persistente con refresh tokens**.

## Título
Endpoint `POST /api/v1/auth/refresh` con almacenamiento, rotación y revocación de refresh tokens

## Descripción
Emitir, junto al *access token* de vida corta (1 h), un **refresh token** de vida más larga persistido (tabla `refresh_tokens` o equivalente, con `user_id`, hash del token, expiración y estado). El endpoint `POST /auth/refresh` valida el refresh token, emite un nuevo *access token* y **rota** el refresh (invalida el anterior y entrega uno nuevo). El cierre de sesión revoca el refresh. Materializa la decisión diferida D2 del MVP.

## Criterios de aceptación
- **AC1**: Con un *access token* expirado y un refresh válido, `refresh` devuelve `200` con un nuevo *access token* (y un refresh rotado).
- **AC2**: Un refresh inválido, caducado o revocado → `401`.
- **AC3**: El cierre de sesión (`logout`) revoca el refresh: deja de servir para renovar.
- **AC4**: **Rotación**: cada uso emite un refresh nuevo e invalida el anterior (detección de reutilización).
- **AC5**: Los refresh se persisten **hasheados** (nunca en claro) con su expiración.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `auth`, `jwt`, `seguridad`, `fase-post-mvp`

## Comentarios
- Endpoint **post-MVP** del catálogo (§4.2); en el MVP se re-autentica al expirar el token.
- **Dependencias directas:** `HU-4-BE-01` (auth/JWT/roles existente).

## Enlaces y referencias
- Historia: [HU-13](../../stories/HU-13.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Catálogo de endpoints: [§4.2](../../readme.md#42-catálogo-de-endpoints) (`/auth/refresh`, *post-MVP*).
- Seguridad y TTL: [§2.5](../../readme.md#25-seguridad) · Decisión diferida [§1.5 D2](../../readme.md#15-supuestos-y-decisiones-diferidas).
