# Orden de implementación del MVP — NovaCasino Studio

40 tickets · 129 SP. El orden sigue **estrictamente el grafo de dependencias por historia** de
[`stories/stories.md`](stories/stories.md) (orden topológico): se completa una historia entera
—sus tickets en orden **BE → FE → QA**— antes de empezar la siguiente.

**Orden topológico de historias:**
`(fundaciones)` → HU-11 → HU-4 → HU-5 → HU-6 → HU-7 → HU-12 → HU-1 → HU-2 → HU-3 → HU-9 → HU-10 → HU-8

> **Nota sobre las fundaciones.** `HU-1-DEV-01` (infra/CI) y `HU-1-DB-01` (esquema) son tickets de la
> historia HU-1 pero se tratan como **fundación transversal**: los presupone todo el backlog, así que
> van primero, antes incluso de HU-11. El resto de HU-1 (motor, spin, UI) se hace en su posición
> topológica.

> **Trade-off conocido.** Este orden estricto sitúa **`HU-1-BE-01` (el motor, 8 SP y de mayor riesgo
> técnico)** relativamente tarde (tras HU-5/6/7/12). Un orden alternativo "de-risk" lo adelantaría justo
> tras HU-5 (única dependencia real del motor). Se mantiene el orden por historia salvo decisión en contra.

---

## ✅ Fundaciones *(7 SP · completado)*

| # | Ticket | Equipo | SP | Estado |
|---|--------|--------|----|--------|
| 1 | [HU-1-DEV-01](tickets/HU-1/HU-1-DEV-01-docker-compose-y-pipeline-ci.md) — Docker Compose + pipeline CI | DEV | 2 | ✅ |
| 2 | [HU-1-DB-01](tickets/HU-1/HU-1-DB-01-esquema-y-migraciones.md) — Esquema Flyway (schema + triggers + seed) | DB | 5 | ✅ |

## ✅ HU-11 — Internacionalización ES/EN *(7 SP · completado)*

| Ticket | Equipo | SP | Estado |
|--------|--------|----|--------|
| [HU-11-BE-01](tickets/HU-11/HU-11-BE-01-mensajes-i18n-en-la-api.md) — Mensajes i18n en la API | BE | 2 | ✅ |
| [HU-11-FE-01](tickets/HU-11/HU-11-FE-01-i18next-bundles-y-conmutacion.md) — i18next: bundles y conmutación | FE | 3 | ✅ |
| [HU-11-QA-01](tickets/HU-11/HU-11-QA-01-tests-i18n.md) — Tests de i18n | QA | 2 | ✅ |

## ✅ HU-4 — Registro y autenticación *(10 SP · completado)*

| Ticket | Equipo | SP | Estado |
|--------|--------|----|--------|
| [HU-4-BE-01](tickets/HU-4/HU-4-BE-01-autenticacion-y-autorizacion.md) — Auth: JWT, register, login, roles | BE | 5 | ✅ |
| [HU-4-FE-01](tickets/HU-4/HU-4-FE-01-pantallas-registro-y-login.md) — Pantallas de registro y login | FE | 3 | ✅ |
| [HU-4-QA-01](tickets/HU-4/HU-4-QA-01-tests-de-autenticacion.md) — Tests de autenticación | QA | 2 | ✅ |

---

## ⬜ HU-5 — Lobby y saldo del jugador *(6 SP · siguiente)*

> Depende de HU-4. Es prerequisito de HU-1.

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-5-BE-01](tickets/HU-5/HU-5-BE-01-endpoints-catalogo-y-wallet.md) — Endpoints de catálogo y wallet | BE | 2 |
| [HU-5-FE-01](tickets/HU-5/HU-5-FE-01-lobby-y-saldo.md) — Lobby y saldo | FE | 2 |
| [HU-5-QA-01](tickets/HU-5/HU-5-QA-01-tests-lobby-y-saldo.md) — Tests de lobby y saldo | QA | 2 |

## ⬜ HU-6 — Gestión y recarga de jugadores *(8 SP)*

> Depende de HU-4.

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-6-BE-01](tickets/HU-6/HU-6-BE-01-endpoints-jugadores-y-recarga.md) — Endpoints de jugadores y recarga idempotente | BE | 3 |
| [HU-6-FE-01](tickets/HU-6/HU-6-FE-01-backoffice-operador-recarga.md) — Backoffice operador: búsqueda y recarga | FE | 3 |
| [HU-6-QA-01](tickets/HU-6/HU-6-QA-01-tests-recarga.md) — Tests de gestión y recarga | QA | 2 |

## ⬜ HU-7 — Edición y versionado de matemática *(10 SP)*

> Depende de HU-4. Es prerequisito de HU-2 (el simulador necesita `game_configs`).

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-7-BE-01](tickets/HU-7/HU-7-BE-01-endpoints-edicion-y-versionado-config.md) — Endpoints edición/versionado de `config` | BE | 5 |
| [HU-7-FE-01](tickets/HU-7/HU-7-FE-01-editor-de-matematica.md) — Editor de matemática | FE | 3 |
| [HU-7-QA-01](tickets/HU-7/HU-7-QA-01-tests-versionado-e-invariantes.md) — Tests de versionado e invariantes | QA | 2 |

## ⬜ HU-12 — Juego responsable y sello DGOJ *(4 SP)*

> Depende de HU-4.

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-12-FE-01](tickets/HU-12/HU-12-FE-01-sello-dgoj-y-juego-responsable.md) — Sello DGOJ y mensajes de juego responsable | FE | 3 |
| [HU-12-QA-01](tickets/HU-12/HU-12-QA-01-tests-compliance-ui.md) — Tests de la capa de cumplimiento | QA | 1 |

## ⬜ HU-1 — El jugador realiza un giro *(26 SP restantes)*

> Depende de HU-5. Núcleo del producto y de mayor riesgo (motor data-driven). Sus fundaciones
> (`HU-1-DEV-01`, `HU-1-DB-01`) ya están hechas.

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-1-BE-01](tickets/HU-1/HU-1-BE-01-motor-de-juego-data-driven.md) — `SpinKernel` + `GameCompiler` + `RoundSink` | BE | 8 |
| [HU-1-BE-02](tickets/HU-1/HU-1-BE-02-spinusecase-idempotencia-endpoint.md) — `SpinUseCase`, idempotencia y endpoint `/spin` | BE | 5 |
| [HU-1-FE-01](tickets/HU-1/HU-1-FE-01-componente-slotgame-y-spin.md) — Componente `<SlotGame>` y spin | FE | 8 |
| [HU-1-QA-01](tickets/HU-1/HU-1-QA-01-tests-flujo-de-juego.md) — Suite de tests del flujo de juego | QA | 5 |

## ⬜ HU-2 — El matemático valida un juego con el simulador *(18 SP)*

> Depende de HU-1 (motor) y HU-7 (configs).

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-2-BE-01](tickets/HU-2/HU-2-BE-01-modulo-nova-simulator.md) — Módulo `nova-simulator` | BE | 5 |
| [HU-2-BE-02](tickets/HU-2/HU-2-BE-02-api-simulaciones-asincrona.md) — API de simulaciones asíncrona | BE | 3 |
| [HU-2-FE-01](tickets/HU-2/HU-2-FE-01-backoffice-matematico-y-dashboard.md) — Backoffice matemático y dashboard | FE | 5 |
| [HU-2-QA-01](tickets/HU-2/HU-2-QA-01-tests-del-simulador-y-convergencia-rtp.md) — Tests del simulador y convergencia | QA | 3 |
| [HU-2-DEV-01](tickets/HU-2/HU-2-DEV-01-job-perf-en-ci.md) — Job `perf` en CI (10M spins <10 min) | DEV | 2 |

## ⬜ HU-3 — Auditoría y replay *(15 SP)*

> Depende de HU-1.

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-3-BE-01](tickets/HU-3/HU-3-BE-01-api-auditoria-con-filtros.md) — API de auditoría con filtros | BE | 3 |
| [HU-3-BE-02](tickets/HU-3/HU-3-BE-02-endpoint-replay-determinista.md) — Endpoint de replay determinista | BE | 3 |
| [HU-3-FE-01](tickets/HU-3/HU-3-FE-01-backoffice-operador-auditoria.md) — Backoffice operador: auditoría | FE | 3 |
| [HU-3-FE-02](tickets/HU-3/HU-3-FE-02-pantalla-de-replay.md) — Pantalla de Replay | FE | 3 |
| [HU-3-QA-01](tickets/HU-3/HU-3-QA-01-tests-auditoria-y-replay.md) — Tests de auditoría y replay | QA | 3 |

## ⬜ HU-9 — Auto-spin con safeguards *(5 SP)*

> Depende de HU-1.

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-9-FE-01](tickets/HU-9/HU-9-FE-01-auto-spin-con-safeguards.md) — Auto-spin con safeguards | FE | 3 |
| [HU-9-QA-01](tickets/HU-9/HU-9-QA-01-tests-auto-spin.md) — Tests de auto-spin | QA | 2 |

## ⬜ HU-10 — Audio inmersivo *(4 SP)*

> Depende de HU-1.

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-10-FE-01](tickets/HU-10/HU-10-FE-01-capa-de-audio.md) — Capa de audio inmersivo (Howler.js) | FE | 3 |
| [HU-10-QA-01](tickets/HU-10/HU-10-QA-01-tests-audio.md) — Tests de la capa de audio | QA | 1 |

## ⬜ HU-8 — Explicación con IA *(9 SP)*

> Depende de HU-2.

| Ticket | Equipo | SP |
|--------|--------|----|
| [HU-8-BE-01](tickets/HU-8/HU-8-BE-01-adaptador-anthropic-y-endpoint-explain.md) — Adaptador Anthropic y endpoint `explain` | BE | 5 |
| [HU-8-FE-01](tickets/HU-8/HU-8-FE-01-caja-pregunta-ia.md) — Caja de pregunta a la IA | FE | 2 |
| [HU-8-QA-01](tickets/HU-8/HU-8-QA-01-tests-explainability.md) — Tests de AI explainability | QA | 2 |

---

## Progreso

| Historia | SP | Acumulado | Estado |
|----------|----|-----------|--------|
| Fundaciones | 7 | 7 | ✅ |
| HU-11 | 7 | 14 | ✅ |
| HU-4 | 10 | 24 | ✅ |
| HU-5 | 6 | 30 | ⬜ siguiente |
| HU-6 | 8 | 38 | ⬜ |
| HU-7 | 10 | 48 | ⬜ |
| HU-12 | 4 | 52 | ⬜ |
| HU-1 (resto) | 26 | 78 | ⬜ |
| HU-2 | 18 | 96 | ⬜ |
| HU-3 | 15 | 111 | ⬜ |
| HU-9 | 5 | 116 | ⬜ |
| HU-10 | 4 | 120 | ⬜ |
| HU-8 | 9 | 129 | ⬜ |
