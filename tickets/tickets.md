# Índice de tickets de trabajo — NovaCasino Studio

Backlog de implementación del MVP: **39 tickets** repartidos en las 12 historias (`HU-1` … `HU-12`). Cada ticket es un fichero `HU-N-EQUIPO-NN-…md` en su carpeta `HU-N/`. La historia de origen de cada grupo está en [`../stories/`](../stories/) (índice: [`../stories/stories.md`](../stories/stories.md)).

**Convenciones:** código `HU-N-EQUIPO-NN` · equipos **BE** (Backend), **FE** (Frontend), **QA**, **DEV** (DevOps/Plataforma) · estimación en **Story Points** Fibonacci (1, 2, 3, 5, 8, 13).

> **Más allá del MVP:** el desglose en tickets del backlog de evolución (`HU-13` … `HU-26`, 47 tickets) está en [`tickets-2.md`](tickets-2.md), correspondiente a las historias de [`../stories/stories-2.md`](../stories/stories-2.md).

## HU-1 — El jugador realiza un giro (33 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-1-BE-01](HU-1/HU-1-BE-01-motor-de-juego-data-driven.md) | Motor de juego data-driven | Backend | 8 |
| [HU-1-BE-02](HU-1/HU-1-BE-02-spinusecase-idempotencia-endpoint.md) | SpinUseCase, idempotencia y endpoint `/spin` | Backend | 5 |
| [HU-1-DB-01](HU-1/HU-1-DB-01-esquema-y-migraciones.md) | Esquema y migraciones Flyway | DB | 5 |
| [HU-1-FE-01](HU-1/HU-1-FE-01-componente-slotgame-y-spin.md) | Componente `<SlotGame>` y spin | Frontend | 8 |
| [HU-1-QA-01](HU-1/HU-1-QA-01-tests-flujo-de-juego.md) | Suite de tests del flujo de juego | QA | 5 |
| [HU-1-DEV-01](HU-1/HU-1-DEV-01-docker-compose-y-pipeline-ci.md) | Docker Compose y pipeline CI | DevOps | 2 |

## HU-2 — El matemático valida un juego con el simulador (18 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-2-BE-01](HU-2/HU-2-BE-01-modulo-nova-simulator.md) | Módulo `nova-simulator` | Backend | 5 |
| [HU-2-BE-02](HU-2/HU-2-BE-02-api-simulaciones-asincrona.md) | API de simulaciones asíncrona | Backend | 3 |
| [HU-2-FE-01](HU-2/HU-2-FE-01-backoffice-matematico-y-dashboard.md) | Backoffice matemático y dashboard | Frontend | 5 |
| [HU-2-QA-01](HU-2/HU-2-QA-01-tests-del-simulador-y-convergencia-rtp.md) | Tests del simulador y convergencia del RTP | QA | 3 |
| [HU-2-DEV-01](HU-2/HU-2-DEV-01-job-perf-en-ci.md) | Job `perf` en CI | DevOps | 2 |

## HU-3 — El operador resuelve una reclamación con el replay (15 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-3-BE-01](HU-3/HU-3-BE-01-api-auditoria-con-filtros.md) | API de auditoría con filtros | Backend | 3 |
| [HU-3-BE-02](HU-3/HU-3-BE-02-endpoint-replay-determinista.md) | Endpoint de *replay* determinista | Backend | 3 |
| [HU-3-FE-01](HU-3/HU-3-FE-01-backoffice-operador-auditoria.md) | Backoffice operador: auditoría | Frontend | 3 |
| [HU-3-FE-02](HU-3/HU-3-FE-02-pantalla-de-replay.md) | Pantalla de Replay | Frontend | 3 |
| [HU-3-QA-01](HU-3/HU-3-QA-01-tests-auditoria-y-replay.md) | Tests de auditoría y replay | QA | 3 |

## HU-4 — Registro y autenticación (10 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-4-BE-01](HU-4/HU-4-BE-01-autenticacion-y-autorizacion.md) | Autenticación y autorización | Backend | 5 |
| [HU-4-FE-01](HU-4/HU-4-FE-01-pantallas-registro-y-login.md) | Pantallas de registro y login | Frontend | 3 |
| [HU-4-QA-01](HU-4/HU-4-QA-01-tests-de-autenticacion.md) | Tests de autenticación y autorización | QA | 2 |

## HU-5 — Lobby y saldo del jugador (6 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-5-BE-01](HU-5/HU-5-BE-01-endpoints-catalogo-y-wallet.md) | Endpoints de catálogo y wallet | Backend | 2 |
| [HU-5-FE-01](HU-5/HU-5-FE-01-lobby-y-saldo.md) | Lobby y saldo | Frontend | 2 |
| [HU-5-QA-01](HU-5/HU-5-QA-01-tests-lobby-y-saldo.md) | Tests de lobby y saldo | QA | 2 |

## HU-6 — Gestión y recarga de jugadores (8 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-6-BE-01](HU-6/HU-6-BE-01-endpoints-jugadores-y-recarga.md) | Endpoints de jugadores y recarga idempotente | Backend | 3 |
| [HU-6-FE-01](HU-6/HU-6-FE-01-backoffice-operador-recarga.md) | Backoffice operador: búsqueda y recarga | Frontend | 3 |
| [HU-6-QA-01](HU-6/HU-6-QA-01-tests-recarga.md) | Tests de gestión y recarga | QA | 2 |

## HU-7 — Edición y versionado de matemática (10 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-7-BE-01](HU-7/HU-7-BE-01-endpoints-edicion-y-versionado-config.md) | Endpoints de edición y versionado de `config` | Backend | 5 |
| [HU-7-FE-01](HU-7/HU-7-FE-01-editor-de-matematica.md) | Editor de matemática | Frontend | 3 |
| [HU-7-QA-01](HU-7/HU-7-QA-01-tests-versionado-e-invariantes.md) | Tests de versionado e invariantes | QA | 2 |

## HU-8 — Explicación con IA (9 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-8-BE-01](HU-8/HU-8-BE-01-adaptador-anthropic-y-endpoint-explain.md) | Adaptador Anthropic y endpoint `explain` | Backend | 5 |
| [HU-8-FE-01](HU-8/HU-8-FE-01-caja-pregunta-ia.md) | Caja de pregunta a la IA | Frontend | 2 |
| [HU-8-QA-01](HU-8/HU-8-QA-01-tests-explainability.md) | Tests de AI explainability | QA | 2 |

## HU-9 — Auto-spin con safeguards (5 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-9-FE-01](HU-9/HU-9-FE-01-auto-spin-con-safeguards.md) | Auto-spin con safeguards | Frontend | 3 |
| [HU-9-QA-01](HU-9/HU-9-QA-01-tests-auto-spin.md) | Tests de auto-spin | QA | 2 |

## HU-10 — Audio inmersivo (4 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-10-FE-01](HU-10/HU-10-FE-01-capa-de-audio.md) | Capa de audio inmersivo | Frontend | 3 |
| [HU-10-QA-01](HU-10/HU-10-QA-01-tests-audio.md) | Tests de la capa de audio | QA | 1 |

## HU-11 — Internacionalización ES/EN (7 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-11-FE-01](HU-11/HU-11-FE-01-i18next-bundles-y-conmutacion.md) | i18next: bundles y conmutación de idioma | Frontend | 3 |
| [HU-11-BE-01](HU-11/HU-11-BE-01-mensajes-i18n-en-la-api.md) | Mensajes i18n en la API | Backend | 2 |
| [HU-11-QA-01](HU-11/HU-11-QA-01-tests-i18n.md) | Tests de internacionalización | QA | 2 |

## HU-12 — Juego responsable y sello DGOJ (4 SP)

| Código | Título | Equipo | SP |
|---|---|---|---|
| [HU-12-FE-01](HU-12/HU-12-FE-01-sello-dgoj-y-juego-responsable.md) | Sello DGOJ y mensajes de juego responsable | Frontend | 3 |
| [HU-12-QA-01](HU-12/HU-12-QA-01-tests-compliance-ui.md) | Tests de la capa de cumplimiento | QA | 1 |

## Resumen

**Total: 40 tickets · 129 SP.**

| Por historia | SP | | Por equipo | Tickets | SP |
|---|---|---|---|---|---|
| HU-1 | 33 | | Backend (BE) | 12 | 49 |
| HU-2 | 18 | | Frontend (FE) | 13 | 44 |
| HU-3 | 15 | | QA | 12 | 27 |
| HU-4 | 10 | | DB | 1 | 5 |
| HU-5 | 6 | | DevOps (DEV) | 2 | 4 |
| HU-6 | 8 | | **Total** | **40** | **129** |
| HU-7 | 10 | | | | |
| HU-8 | 9 | | | | |
| HU-9 | 5 | | | | |
| HU-10 | 4 | | | | |
| HU-11 | 7 | | | | |
| HU-12 | 4 | | | | |

> **Nota sobre el trabajo de base de datos:** el ticket de BBDD es **`HU-1-DB-01` — Esquema y migraciones Flyway** (`V1` esquema/índices, `V2` triggers de inmutabilidad, `V3` seed), **fundación de datos** que presupone todo ticket de persistencia. La infra que lo ejecuta (contenedor Postgres + arranque) es de `HU-1-DEV-01`; la persistencia transaccional se construye en `HU-1-BE-02`, y el versionado/índices específicos del editor en `HU-7-BE-01`. Los demás tickets de persistencia (`HU-5-BE-01`, `HU-6-BE-01`, `HU-2-BE-02`, `HU-3-BE-01/02`, `HU-8-BE-01`) **asumen** el esquema como fundación, igual que la infra.

---

## Árboles de dependencias por historia

Un sub-diagrama por historia. Aristas `A → B` = "**A depende de B**" (directas, reducción transitiva). Los nodos **externos** (tickets de otra HU) se marcan con borde discontinuo; representan entradas que deben existir antes pero se desarrollan en su propia historia. Las dependencias entre historias están en [`../stories/stories.md`](../stories/stories.md).

> El ticket fundacional **`HU-1-DEV-01`** (infra/CI) es raíz de casi todo el backend; aparece como entrada externa en otras historias.

### HU-1 — El jugador realiza un giro
```mermaid
flowchart TD
    DEV01["HU-1-DEV-01 · infra/CI (raíz)"]
    DB01["HU-1-DB-01 · esquema + migraciones"]
    BE01["HU-1-BE-01 · motor (SpinKernel)"]
    BE02["HU-1-BE-02 · spin + idempotencia"]
    FE01["HU-1-FE-01 · &lt;SlotGame&gt;"]
    QA01["HU-1-QA-01 · tests"]
    x4BE["HU-4-BE-01 · auth (ext)"]
    x5BE["HU-5-BE-01 · catálogo/config (ext)"]
    x4FE["HU-4-FE-01 · sesión (ext)"]
    DB01 --> DEV01
    BE01 --> DEV01
    BE02 --> BE01
    BE02 --> DB01
    BE02 --> x4BE
    FE01 --> BE02
    FE01 --> x5BE
    FE01 --> x4FE
    QA01 --> FE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x4BE,x5BE,x4FE ext
```

### HU-4 — Registro y autenticación
```mermaid
flowchart TD
    BE01["HU-4-BE-01 · auth (JWT, register, login)"]
    FE01["HU-4-FE-01 · pantallas registro/login"]
    QA01["HU-4-QA-01 · tests auth"]
    xDEV["HU-1-DEV-01 · infra/CI (ext)"]
    BE01 --> xDEV
    FE01 --> BE01
    QA01 --> BE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class xDEV ext
```

### HU-5 — Lobby y saldo
```mermaid
flowchart TD
    BE01["HU-5-BE-01 · catálogo + wallet"]
    FE01["HU-5-FE-01 · lobby"]
    QA01["HU-5-QA-01 · tests"]
    x4BE["HU-4-BE-01 · auth (ext)"]
    x4FE["HU-4-FE-01 · sesión (ext)"]
    BE01 --> x4BE
    FE01 --> BE01
    FE01 --> x4FE
    QA01 --> FE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x4BE,x4FE ext
```

### HU-6 — Gestión y recarga de jugadores
```mermaid
flowchart TD
    BE01["HU-6-BE-01 · jugadores + recarga"]
    FE01["HU-6-FE-01 · backoffice recarga"]
    QA01["HU-6-QA-01 · tests"]
    x4BE["HU-4-BE-01 · auth (ext)"]
    x1BE2["HU-1-BE-02 · idempotencia (ext)"]
    BE01 --> x4BE
    BE01 --> x1BE2
    FE01 --> BE01
    QA01 --> FE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x4BE,x1BE2 ext
```

### HU-7 — Edición y versionado de matemática
```mermaid
flowchart TD
    BE01["HU-7-BE-01 · endpoints config"]
    FE01["HU-7-FE-01 · editor"]
    QA01["HU-7-QA-01 · tests"]
    x4BE["HU-4-BE-01 · auth (ext)"]
    BE01 --> x4BE
    FE01 --> BE01
    QA01 --> BE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x4BE ext
```

### HU-2 — Simulador
```mermaid
flowchart TD
    BE01["HU-2-BE-01 · nova-simulator"]
    BE02["HU-2-BE-02 · API simulaciones"]
    FE01["HU-2-FE-01 · dashboard"]
    QA01["HU-2-QA-01 · tests"]
    DEV01["HU-2-DEV-01 · job perf"]
    x1BE1["HU-1-BE-01 · SpinKernel (ext)"]
    x4BE["HU-4-BE-01 · auth (ext)"]
    x7BE["HU-7-BE-01 · config a simular (ext)"]
    x1DEV["HU-1-DEV-01 · CI (ext)"]
    BE01 --> x1BE1
    BE02 --> BE01
    BE02 --> x4BE
    BE02 --> x7BE
    FE01 --> BE02
    QA01 --> BE02
    DEV01 --> QA01
    DEV01 --> x1DEV
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x1BE1,x4BE,x7BE,x1DEV ext
```

### HU-3 — Replay + auditoría
```mermaid
flowchart TD
    BE01["HU-3-BE-01 · auditoría"]
    BE02["HU-3-BE-02 · replay"]
    FE01["HU-3-FE-01 · backoffice auditoría"]
    FE02["HU-3-FE-02 · pantalla replay"]
    QA01["HU-3-QA-01 · tests"]
    x4BE["HU-4-BE-01 · auth (ext)"]
    x1BE2["HU-1-BE-02 · genera rounds (ext)"]
    x1FE["HU-1-FE-01 · &lt;SlotGame&gt; (ext)"]
    BE01 --> x4BE
    BE01 --> x1BE2
    BE02 --> x4BE
    BE02 --> x1BE2
    FE01 --> BE01
    FE02 --> BE02
    FE02 --> x1FE
    QA01 --> BE01
    QA01 --> BE02
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x4BE,x1BE2,x1FE ext
```

### HU-8 — IA explainability
```mermaid
flowchart TD
    BE01["HU-8-BE-01 · adaptador Anthropic"]
    FE01["HU-8-FE-01 · caja pregunta IA"]
    QA01["HU-8-QA-01 · tests"]
    x4BE["HU-4-BE-01 · auth (ext)"]
    x2BE2["HU-2-BE-02 · simulación COMPLETED (ext)"]
    BE01 --> x4BE
    BE01 --> x2BE2
    FE01 --> BE01
    QA01 --> BE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x4BE,x2BE2 ext
```

### HU-9 — Auto-spin
```mermaid
flowchart TD
    FE01["HU-9-FE-01 · auto-spin"]
    QA01["HU-9-QA-01 · tests"]
    x1FE["HU-1-FE-01 · &lt;SlotGame&gt; (ext)"]
    FE01 --> x1FE
    QA01 --> FE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x1FE ext
```

### HU-10 — Audio inmersivo
```mermaid
flowchart TD
    FE01["HU-10-FE-01 · capa de audio"]
    QA01["HU-10-QA-01 · tests"]
    x1FE["HU-1-FE-01 · &lt;SlotGame&gt; (ext)"]
    FE01 --> x1FE
    QA01 --> FE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x1FE ext
```

### HU-11 — Internacionalización ES/EN
```mermaid
flowchart TD
    FE01["HU-11-FE-01 · i18next"]
    BE01["HU-11-BE-01 · mensajes API i18n"]
    QA01["HU-11-QA-01 · tests"]
    xDEV["HU-1-DEV-01 · esqueleto (ext)"]
    FE01 --> xDEV
    BE01 --> xDEV
    QA01 --> FE01
    QA01 --> BE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class xDEV ext
```

### HU-12 — Juego responsable y sello DGOJ
```mermaid
flowchart TD
    FE01["HU-12-FE-01 · sello DGOJ / juego responsable"]
    QA01["HU-12-QA-01 · tests"]
    x4FE["HU-4-FE-01 · shell UI (ext)"]
    x11FE["HU-11-FE-01 · i18n textos (ext)"]
    FE01 --> x4FE
    FE01 --> x11FE
    QA01 --> FE01
    classDef ext stroke-dasharray:5 5,fill:#fdf6e3,color:#073642
    class x4FE,x11FE ext
```
