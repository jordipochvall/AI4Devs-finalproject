# Índice de tickets de trabajo (bloque 4 · demo público en VPS) — NovaCasino Studio

Cuarto bloque de implementación, en continuidad con el MVP de [`tickets.md`](tickets.md), el post-MVP de [`tickets-2.md`](tickets-2.md) y el cierre de huecos de [`tickets-3.md`](tickets-3.md): **16 tickets** en **8 historias** (`HU-33` … `HU-40`). Las historias de origen están en [`../stories/stories-4.md`](../stories/stories-4.md).

**Convenciones:** código `HU-N-EQUIPO-NN` · equipos **BE** (Backend), **FE** (Frontend), **QA**, **DEV** (DevOps/Plataforma) · estimación en **Story Points** Fibonacci (1, 2, 3, 5, 8, 13).

> **Estado: 2/8 historias implementadas.** HU-33 y HU-36 implementadas y verificadas en vivo (rebuild del contenedor `api` de desarrollo + `curl` + un `docker run` puntual). HU-34 parcial (`.github/workflows` versionado; el `Environment` de GitHub queda bloqueado sin un VPS/dominio real). HU-35 bloqueada por el mismo motivo. Trazabilidad en [`../conversation.md`](../conversation.md).

## HU-33 — Bug: `/actuator/health` exige autenticación y rompe el propio despliegue (3 SP) — ✅ Implementada

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-33-BE-01](HU-33/HU-33-BE-01-permitir-actuator-health.md) | Permitir `/actuator/health` sin autenticación | Backend | 2 |
| [HU-33-QA-01](HU-33/HU-33-QA-01-tests-actuator-y-despliegue.md) | Tests del healthcheck y verificación del despliegue | QA | 1 |

> **Bloqueante nº 1.** Sin esto, `deploy/docker-compose.prod.yml` nunca marca `api` como *healthy* y ningún despliegue (manual o vía `cd.yml`) llega a completarse. **Causa raíz real, más profunda de lo esperado:** `spring-boot-starter-actuator` no era ni siquiera una dependencia del proyecto (el *endpoint* no existía en absoluto), además de que `SecurityConfig` lo habría bloqueado igualmente. Corrección: dependencia añadida + `management.endpoints.web.exposure.include=health` (+ `show-details: never`) + `permitAll` acotado a `/actuator/health/**`. Verificado: IT `ActuatorHealthIT` (2/2) y en vivo contra el contenedor `api` reconstruido (`200 {"status":"UP"}` sin token; `/actuator/env` y `/api/v1/player/games` siguen en `401`).

## HU-34 — Versionar y activar el pipeline de CI/CD para poder desplegar al VPS (3 SP) — 🟡 Parcial

| Código | Título | Equipo | SP | Estado |
|---|---|---|---|---|
| [HU-34-DEV-01](HU-34/HU-34-DEV-01-versionar-workflows.md) | Comprometer `.github/workflows` al repositorio | DevOps | 1 | ✅ |
| [HU-34-DEV-02](HU-34/HU-34-DEV-02-configurar-entorno-vps.md) | Configurar el Environment de GitHub apuntando al VPS | DevOps | 2 | ⏳ Bloqueado (sin VPS) |

> **Reutilización de diseño.** El pipeline ya está diseñado e implementado en disco desde `HU-24`; HU-34 sólo lo versiona y lo conecta con el VPS real. `ci.yml`/`cd.yml` ya están comprometidos (sin `push` todavía); `.github/modernize/` (artefactos de una extensión de VS Code, no del proyecto) se deja fuera del control de versiones a propósito.

## HU-35 — El demo público sólo se sirve por HTTPS y la API no queda expuesta directamente (4 SP) — ⏳ Bloqueada (falta VPS/dominio)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-35-DEV-01](HU-35/HU-35-DEV-01-tls-proxy.md) | TLS automático delante de `web` | DevOps | 3 |
| [HU-35-DEV-02](HU-35/HU-35-DEV-02-cerrar-puerto-api.md) | Dejar de publicar el puerto de la API al host | DevOps | 1 |

## HU-36 — La API rechaza arrancar con secretos de despliegue débiles o de ejemplo (3 SP) — ✅ Implementada

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-36-BE-01](HU-36/HU-36-BE-01-validar-jwt-secret.md) | Rechazar `JWT_SECRET` débil al arrancar | Backend | 2 |
| [HU-36-DEV-01](HU-36/HU-36-DEV-01-placeholder-env-example.md) | Placeholder no productivo en `.env.example` | DevOps | 1 |

> **Verificado con `docker run` puntual** de la imagen reconstruida: `JWT_SECRET=admin` → `IllegalStateException` en el arranque, el contenedor no llega a exponer el puerto; con el secreto real (64+ bytes) generado por `openssl rand -base64 48`, arranca con normalidad.

## HU-37 — Acotar la concurrencia de simulaciones para proteger el VPS del demo (5 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-37-BE-01](HU-37/HU-37-BE-01-executor-y-limite-concurrencia.md) | Executor acotado y límite de simulaciones concurrentes | Backend | 3 |
| [HU-37-QA-01](HU-37/HU-37-QA-01-tests-limite-concurrencia.md) | Test del límite de concurrencia de simulaciones | QA | 2 |

## HU-38 — Las contraseñas de las cuentas semilla del demo se configuran por entorno (3 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-38-BE-01](HU-38/HU-38-BE-01-contrasenas-semilla-por-entorno.md) | Externalizar las contraseñas semilla a variables de entorno | Backend | 2 |
| [HU-38-QA-01](HU-38/HU-38-QA-01-tests-contrasenas-semilla.md) | Test de las contraseñas semilla configurables | QA | 1 |

> **No se elimina el sembrado.** Las cuentas demo se mantienen (útiles para evaluadores); sólo dejan de tener contraseñas fijas en el código fuente.

## HU-39 — Un error de render no deja la pantalla en blanco (3 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-39-FE-01](HU-39/HU-39-FE-01-error-boundary.md) | `ErrorBoundary` global en el frontend | Frontend | 2 |
| [HU-39-QA-01](HU-39/HU-39-QA-01-tests-error-boundary.md) | Test del `ErrorBoundary` | QA | 1 |

## HU-40 — Endurecer la Content-Security-Policy del frontend (2 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-40-FE-01](HU-40/HU-40-FE-01-endurecer-csp.md) | Añadir `object-src`, `base-uri`, `frame-ancestors`, `form-action` e `img-src` a la CSP | Frontend | 1 |
| [HU-40-QA-01](HU-40/HU-40-QA-01-verificacion-csp.md) | Verificación de la CSP endurecida | QA | 1 |

## Resumen

| Historia | Tickets | SP |
|---|---|---|
| HU-33 | 2 | 3 |
| HU-34 | 2 | 3 |
| HU-35 | 2 | 4 |
| HU-36 | 2 | 3 |
| HU-37 | 2 | 5 |
| HU-38 | 2 | 3 |
| HU-39 | 2 | 3 |
| HU-40 | 2 | 2 |
| **Total** | **16** | **26** |
