# HU-4 — Registro y autenticación de usuarios

> **Como** persona usuaria de NovaCasino Studio (jugador, operador o matemático),
> **quiero** registrarme y autenticarme con email y contraseña,
> **para** acceder de forma segura a la superficie que me corresponde según mi rol.

**Contexto y valor.** Historia **transversal** a los tres perfiles: es la puerta de entrada a toda la plataforma y el prerequisito de HU-1, HU-2 y HU-3. Incluye la verificación de mayoría de edad (≥18) exigida por la DGOJ, realizada en la capa de aplicación.

**Prioridad:** Must Have · **Estimación:** M · **Endpoints:** `POST /auth/register` ★, `POST /auth/login` ★

## Criterios de aceptación (BDD)

```gherkin
# language: es
Característica: Registro y autenticación

  Escenario: Registro de un jugador válido
    Dado que no tengo cuenta
    Cuando me registro con un email no usado, una contraseña válida y una fecha de nacimiento de hace 25 años
    Entonces mi cuenta se crea con rol "PLAYER"
    Y recibo un token de sesión que me deja entrar directamente

  Escenario: Registro rechazado por menor de edad
    Cuando intento registrarme con una fecha de nacimiento de hace 16 años
    Entonces el registro se rechaza indicando que no se permite a menores de 18 años
    Y no se crea ninguna cuenta

  Escenario: Registro rechazado por email duplicado
    Dado que ya existe una cuenta con el email "ana@example.com"
    Cuando intento registrarme con ese mismo email
    Entonces el registro se rechaza indicando que el email ya está en uso

  Escenario: Login correcto
    Dado que tengo una cuenta activa
    Cuando inicio sesión con mis credenciales correctas
    Entonces recibo un token de sesión válido y los datos de mi usuario y rol

  Escenario: Login con credenciales inválidas
    Cuando inicio sesión con una contraseña incorrecta
    Entonces el acceso se deniega con un error de credenciales inválidas

  Escenario: Acceso a un recurso sin sesión
    Cuando solicito un recurso protegido sin token
    Entonces el acceso se deniega solicitando autenticación

  Escenario: Acceso con rol no autorizado
    Dado que estoy autenticado con rol "PLAYER"
    Cuando intento acceder a una superficie de operador
    Entonces el acceso se deniega por falta de permisos
```

## Verificación INVEST

| Criterio | Cumplimiento |
|---|---|
| **I**ndependiente | Se construye y prueba de forma autónoma; no depende de ninguna otra HU (al contrario, las demás dependen de ella). |
| **N**egociable | El detalle de la política de contraseñas o el TTL del token es ajustable sin alterar el objetivo. |
| **V**aliosa | Sin autenticación no hay acceso a ninguna funcionalidad; habilita a los tres perfiles. |
| **E**stimable | Alcance acotado a dos endpoints y el filtro de seguridad; el equipo puede tallarla. |
| **S**mall | Cabe holgadamente en un sprint. |
| **T**estable | Cada escenario es verificable con tests de integración (códigos 201/401/403/409/422). |

**Fuera de alcance:** renovación de token (`/auth/refresh`) y recuperación de contraseña, ambas **post-MVP**.

---

## Dependencias

Depende directamente de: **[HU-11](HU-11.md)** (i18n: las pantallas de auth y los mensajes de error de la API van en ES/EN).

> Solo se listan dependencias **directas** (reducción transitiva); las indirectas se alcanzan a través de ellas. Árbol completo en [stories.md](stories.md).

---

- Historia relacionada con: [§5. Historias de usuario](../readme.md#5-historias-de-usuario) y la seguridad descrita en [§2.5.1](../readme.md#25-seguridad).
- Endpoints: [§4.4.1 Registro y §4.4.2 Login](../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios).
- Tickets de trabajo: [`tickets/HU-4/`](../tickets/HU-4/) (BE-01 auth, FE-01 pantallas registro/login, QA-01 tests).
