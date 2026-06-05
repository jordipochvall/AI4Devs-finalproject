# HU-4-BE-01 — Autenticación y autorización

## Código
`HU-4-BE-01` — vinculado con **HU-4: Registro y autenticación de usuarios**.

## Título
Autenticación y autorización (JWT, register, login, roles)

## Descripción
Implementar el sistema de autenticación **JWT Bearer (HS256)** y la autorización por rol (`PLAYER`, `OPERATOR`, `MATH_ANALYST`) sobre la API. Incluye los endpoints públicos `POST /api/v1/auth/register` y `POST /api/v1/auth/login`, el `SecurityFilterChain` de Spring Security 6, el hashing BCrypt (cost 12) y la **validación de mayoría de edad (≥18)** en el caso de uso de registro a partir de `birth_date` (en la capa de aplicación, no en BBDD; ver 3.2.2).

Ticket **transversal**: lo consumen todas las demás HU (filtro JWT + tabla `users`).

## Criterios de aceptación
- **AC1**: `register` con `birthDate` de un menor de 18 → `422` (RFC 9457).
- **AC2**: `register` válido crea la fila en `users` con `password_hash` BCrypt y devuelve `201` con un `AuthResponse` (JWT + usuario).
- **AC3**: `login` con credenciales correctas → `200` con JWT válido durante `JWT_TTL_SECONDS`.
- **AC4**: Endpoint `/operator/*` con JWT de rol `PLAYER` → `403`.
- **AC5**: Endpoint protegido sin `Authorization` → `401`.
- **AC6**: La password nunca se devuelve, loguea ni almacena en claro.

## Prioridad
Must Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `nova-infrastructure`, `seguridad`, `dgoj`, `transversal`

## Comentarios
- **Dependencias directas:** `HU-1-DEV-01` (externa, esqueleto Spring/infra). Ticket transversal: lo consumen casi todos los demás tickets.
- `JWT_SECRET` y `JWT_TTL_SECONDS` se inyectan por entorno (ver tabla de variables en 1.4).
- `POST /auth/refresh` queda **post-MVP**.
- En MVP single-tenant el `operator_id` resuelve a `novacasino-default`.

## Enlaces y referencias
- Historia: [HU-4](../../stories/HU-4.md).
- Seguridad: [2.5.1](../../readme.md#25-seguridad).
- Modelo: [3.2.2 users](../../readme.md#32-descripción-de-entidades-principales).
- API: [4.4.1 Registro, 4.4.2 Login](../../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios).
