# HU-4-FE-01 — Pantallas de registro y login

## Código
`HU-4-FE-01` — vinculado con **HU-4: Registro y autenticación de usuarios**.

## Título
Pantallas de registro y login + gestión de sesión

## Descripción
Implementar en la SPA las pantallas de **registro** (email, password, `birthDate`, `locale`) y **login** (email, password), el almacenamiento del token en el store de sesión (Zustand) y el **interceptor Axios** que añade `Authorization: Bearer <token>` a las peticiones. Tras autenticarse, el usuario es redirigido a la superficie de su rol (jugador → lobby, operador → `/operator`, matemático → `/math`).

## Criterios de aceptación
- **AC1**: El registro valida en cliente el formato de email y la longitud mínima de password (8), y muestra el error de servidor en `409` (email duplicado) y `422` (menor de edad).
- **AC2**: Tras registro/login correcto, el token se persiste y el usuario es redirigido según su rol.
- **AC3**: El interceptor adjunta el JWT en todas las llamadas a la API y, ante `401`, redirige a `/login`.
- **AC4**: Las pantallas funcionan en **español e inglés**.
- **AC5**: La pantalla de login muestra el aviso de mayoría de edad y el mensaje de juego responsable (apoya a HU-12).

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `zustand`, `auth`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-4-BE-01` (intra, endpoints de auth).
- Se separó del antiguo ticket de "registro/login/lobby": el lobby es ahora `HU-5-FE-01`.

## Enlaces y referencias
- Historia: [HU-4](../../stories/HU-4.md).
- Funcionalidad A1: [1.2](../../readme.md#12-características-y-funcionalidades-principales).
- API: [4.4.1 Registro, 4.4.2 Login](../../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios).
