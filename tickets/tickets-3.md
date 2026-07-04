# Índice de tickets de trabajo (bloque 3 · cierre de huecos) — NovaCasino Studio

Tercer bloque de implementación, en continuidad con el MVP de [`tickets.md`](tickets.md) y el post-MVP de [`tickets-2.md`](tickets-2.md): **16 tickets** en **6 historias** (`HU-27` … `HU-32`). Las historias de origen están en [`../stories/stories-3.md`](../stories/stories-3.md).

**Convenciones:** código `HU-N-EQUIPO-NN` · equipos **BE** (Backend), **FE** (Frontend), **QA**, **DEV** (DevOps/Plataforma), **DB** · estimación en **Story Points** Fibonacci (1, 2, 3, 5, 8, 13).

> **Estado: 6/6 historias implementadas.** HU-27..30 (frontend), **HU-31** (bug RTP: recalibración + V12 + guard) y **HU-32** ("ask the AI" con modo offline sin key + activación Anthropic + errores→503) implementadas y verificadas (`mvn` nova-web-api 38/38 + ITs de explain en verde). Trazabilidad en [`../conversation.md`](../conversation.md).

## HU-27 — Detalle de una partida desde la auditoría (3 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-27-FE-01](HU-27/HU-27-FE-01-modal-detalle-partida.md) | Modal de detalle de partida en la auditoría del operador | Frontend | 2 |
| [HU-27-QA-01](HU-27/HU-27-QA-01-tests-detalle-partida.md) | Tests del modal de detalle de partida | QA | 1 |

> **Reutilización de backend.** El endpoint `GET /operator/rounds/{roundId}` (y su DTO `RoundDetailDto`) se construyó en `HU-16-BE-01`; HU-27 no añade backend, sólo lo consume desde la UI.

## HU-28 — Sistema de diseño y tipografía coherentes (9 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-28-DEV-01](HU-28/HU-28-DEV-01-fuentes-autoalojadas.md) | Empaquetar fuentes web auto-alojadas (CSP-safe) | DEV | 1 |
| [HU-28-FE-01](HU-28/HU-28-FE-01-base-diseno.md) | Base de diseño: tokens, reset y tipografía global | Frontend | 3 |
| [HU-28-FE-02](HU-28/HU-28-FE-02-componentes-tokenizar.md) | Componentes canónicos y tokenización del CSS de la app | Frontend | 3 |
| [HU-28-QA-01](HU-28/HU-28-QA-01-noregresion-contraste.md) | No-regresión del sistema de diseño y contraste AA | QA | 2 |

## HU-29 — Pantalla de juego responsive sin scroll (5 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-29-FE-01](HU-29/HU-29-FE-01-layout-responsive.md) | Layout responsive sin scroll y barra de acción estable | Frontend | 3 |
| [HU-29-QA-01](HU-29/HU-29-QA-01-responsive-spin-estable.md) | Tests de responsive y estabilidad del botón Spin | QA | 2 |

## HU-30 — Fondos temáticos y giro de rodillos natural (7 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-30-FE-01](HU-30/HU-30-FE-01-fondos-tematicos.md) | Fondos temáticos por juego | Frontend | 2 |
| [HU-30-FE-02](HU-30/HU-30-FE-02-giro-rodillos.md) | Giro de rodillos natural (reels + parada escalonada) | Frontend | 3 |
| [HU-30-QA-01](HU-30/HU-30-QA-01-tests-animacion.md) | Tests de tematización, giro de rodillos y reduce-motion | QA | 2 |

## HU-31 — Bug: RTP empírico fuera de rango en los juegos semilla (5 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-31-BE-01](HU-31/HU-31-BE-01-recalibrar-configs-semilla.md) | Recalibrar las configuraciones semilla a un RTP realista | Backend/Math | 3 |
| [HU-31-QA-01](HU-31/HU-31-QA-01-guard-rtp-en-banda.md) | Guard de regresión: RTP de las configs semilla en banda | QA | 2 |

> **Bug, no de motor.** El motor está validado (`EngineRtpPropertyTest`) y el frontend formatea bien; el ~3000% viene de configs semilla sin calibrar. Se corrige recalibrando la math + guard de regresión.

## HU-32 — "Ask the AI" funcional (con modo offline sin API key) (6 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-32-BE-01](HU-32/HU-32-BE-01-explainer-offline.md) | Explainer local (offline) determinista como fallback | Backend | 3 |
| [HU-32-DEV-01](HU-32/HU-32-DEV-01-activacion-anthropic.md) | Activación de Anthropic en el despliegue | DEV | 1 |
| [HU-32-QA-01](HU-32/HU-32-QA-01-tests-explain.md) | Tests del "ask the AI" (offline, Claude y degradación) | QA | 2 |

> **Funciona sin key.** El modo offline determinista hace útil la caja de preguntas out-of-the-box; el camino real a Claude se activa con `ANTHROPIC_ENABLED=true` + API key.

## Resumen

| Historia | Tickets | SP |
|---|---|---|
| HU-27 | 2 | 3 |
| HU-28 | 4 | 9 |
| HU-29 | 2 | 5 |
| HU-30 | 3 | 7 |
| HU-31 | 2 | 5 |
| HU-32 | 3 | 6 |
| **Total** | **16** | **35** |
