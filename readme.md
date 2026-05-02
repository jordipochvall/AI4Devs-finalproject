## Índice

0. [Ficha del proyecto](#0-ficha-del-proyecto)
1. [Descripción general del producto](#1-descripción-general-del-producto)
2. [Arquitectura del sistema](#2-arquitectura-del-sistema)
3. [Modelo de datos](#3-modelo-de-datos)
4. [Especificación de la API](#4-especificación-de-la-api)
5. [Historias de usuario](#5-historias-de-usuario)
6. [Tickets de trabajo](#6-tickets-de-trabajo)
7. [Pull requests](#7-pull-requests)

---

## 0. Ficha del proyecto

### **0.1. Tu nombre completo:**

Jordi Poch

### **0.2. Nombre del proyecto:**

**NovaCasino Studio**

### **0.3. Descripción breve del proyecto:**

NovaCasino Studio es una plataforma B2B de slots online compuesta por un cliente de juego para el jugador final (lobby + pantalla de juego), un backoffice operativo para el operador (configuración de juegos, gestión de jugadores y auditoría de partidas) y un backoffice matemático (edición de matemáticas y simulador masivo de hasta 10M de partidas en menos de 10 minutos). Está alineada con la regulación de la DGOJ (Dirección General de Ordenación del Juego, España) desde el primer día y se apoya en un motor de juego *data-driven* y en capacidades de IA (Claude) para asistir al equipo matemático en el análisis de resultados.

El alcance de esta primera versión cubre 3 video slots jugables (dos 5x3 con bonus completo y un 3x3 clásico), saldo virtual ("fun money") gestionado por el operador, registro auditable inmutable de cada giro y *replay* visual determinista de partidas. Quedan **fuera de esta versión** la integración con pasarelas de pago/cobro, los jackpots progresivos y la generación de informes oficiales RFJ para la DGOJ.

### **0.4. URL del proyecto:**

Pendiente de despliegue público. Esta primera fase contempla únicamente ejecución local mediante Docker Compose (ver sección [1.4](#14-instrucciones-de-instalación)). El despliegue en cloud se valorará en fases posteriores.

> Puede ser pública o privada, en cuyo caso deberás compartir los accesos de manera segura. Puedes enviarlos a [alvaro@lidr.co](mailto:alvaro@lidr.co) usando algún servicio como [onetimesecret](https://onetimesecret.com/).

### 0.5. URL o archivo comprimido del repositorio

Repositorio público de GitHub: `https://github.com/<owner>/AI4Devs-finalproject` (sustituir `<owner>` por el usuario/organización propietaria del fork).

> Puedes tenerlo alojado en público o en privado, en cuyo caso deberás compartir los accesos de manera segura. Puedes enviarlos a [alvaro@lidr.co](mailto:alvaro@lidr.co) usando algún servicio como [onetimesecret](https://onetimesecret.com/). También puedes compartir por correo un archivo zip con el contenido


---

## 1. Descripción general del producto

> Describe en detalle los siguientes aspectos del producto:

### **1.1. Objetivo:**

**Propósito.** NovaCasino Studio es una plataforma de slots online concebida como un *game studio* B2B: cubre toda la cadena de valor desde la creación matemática del juego hasta su consumo por el jugador final, con foco en el rigor matemático, la trazabilidad regulatoria y el time-to-market.

**Problema que resuelve.** Los estudios pequeños y medianos que diseñan juegos de slots se enfrentan a tres dolores recurrentes:

1. **Coste y lentitud del ciclo matemático**: simular millones de partidas para validar RTP, volatilidad y *hit frequency* requiere herramientas pesadas y poco interactivas. El equipo matemático pierde días iterando.
2. **Trazabilidad para regulación y soporte**: cuando un jugador reclama un giro, los operadores rara vez pueden reproducir la partida exacta. Las certificaciones (DGOJ, MGA, UKGC) exigen registros auditables y RNG demostrablemente justo.
3. **Acoplamiento entre matemática, motor y presentación**: cada juego nuevo se desarrolla casi desde cero, lo que multiplica el coste por título.

**Valor diferencial.** NovaCasino Studio aborda estos tres dolores con cuatro pilares:

- **Motor de juego *data-driven***. Un único *Slot Engine* en Java parametrizado por JSON. Añadir un juego nuevo es una operación de configuración, no de programación.
- **Simulador masivo de alto rendimiento**: 10 millones de partidas en menos de 10 minutos sobre el mismo motor de producción (no un modelo paralelo), garantizando que lo simulado es lo que se juega.
- **Auditoría inmutable + *replay* determinista**: cada giro se registra con su *seed* RNG, apuesta, balance pre/post y resultado. El operador puede reproducir visualmente la partida exacta para resolver conflictos en segundos.
- **AI-powered explainability**: el matemático pregunta en lenguaje natural sobre los resultados de una simulación ("¿por qué la volatilidad sube en el slot espacial?") y un asistente basado en Claude (Anthropic) interpreta los datos y responde.

**A quién sirve.** Tres perfiles principales, cada uno con su superficie:

| Perfil | Necesidad principal | Superficie en NovaCasino |
|---|---|---|
| **Jugador final** | Experiencia de juego entretenida, fluida y de confianza | Lobby web + pantalla de juego con audio inmersivo, i18n ES/EN, mensajes de juego responsable |
| **Operador** | Configurar juegos, gestionar jugadores y resolver incidencias | Backoffice operativo: configuración (monedas, apuestas), gestión de jugadores y saldo virtual, auditoría con *replay* |
| **Equipo matemático** | Diseñar y validar la matemática de cada juego | Backoffice matemático: editor de paytables/reels, simulador masivo, dashboards de métricas y asistente IA |

**Cumplimiento DGOJ desde el día 0.** Aunque esta versión no genera informes RFJ ni gestiona pagos reales, todas las decisiones arquitectónicas se han tomado para no bloquear una futura certificación: registro inmutable, *seeds* reproducibles, separación motor/RNG, verificación de mayoría de edad en login y sello DGOJ visible.

---

### **1.2. Características y funcionalidades principales:**

#### A. Cliente del jugador (web)

| # | Funcionalidad | Descripción |
|---|---|---|
| A1 | **Login con verificación de edad ≥18** | Registro/login simple por email + password con declaración explícita de mayoría de edad. Cumplimiento DGOJ. |
| A2 | **Lobby de juegos** | Muestra el catálogo con la carátula de cada juego (3 juegos en MVP). Click → pantalla del juego. |
| A3 | **Pantalla de juego (Slot Game)** | Único componente React `<SlotGame>` data-driven. Renderiza cualquier juego según JSON: rejilla, símbolos, líneas de pago, animaciones de giro y de premio. |
| A4 | **Apuesta configurable** | Selector de moneda y cuantía de apuesta dentro del rango definido por el operador para cada juego. |
| A5 | **Auto-spin con *safeguards*** | Permite N giros automáticos. Se detiene si el saldo cae por debajo de un umbral o tras un número máximo de giros, mostrando un mensaje de pausa (alineado con el espíritu DGOJ). |
| A6 | **Free Spins (5x3)** | Trigger por *Scatter* (≥3). Otorga N giros gratis con mecánica adicional (multiplicadores o reels especiales según cada juego). |
| A7 | **Wallet virtual persistente** | Saldo en *fun money* almacenado por jugador. Recargado por el operador desde su backoffice. Histórico de movimientos. |
| A8 | **Audio inmersivo** | Música ambiente por temática (egipcia, frutas clásico, espacial), SFX comunes (spin, win, big win, free spin trigger) y voz de locutor en eventos especiales. Mute toggle persistente. |
| A9 | **Internacionalización ES/EN** | Toda la UI (incluida la voz del locutor) disponible en castellano e inglés con cambio en caliente. |
| A10 | **Mensajes de juego responsable y sello DGOJ** | Banner permanente con el sello DGOJ, enlace a "juego responsable" y avisos contextuales. |

#### B. Backoffice operativo (operador)

| # | Funcionalidad | Descripción |
|---|---|---|
| B1 | **Gestión de jugadores** | Listado, búsqueda, alta/baja, recarga manual del saldo virtual. |
| B2 | **Configuración de juegos** | Para cada juego: monedas habilitadas, apuestas mínima/máxima, escalones de apuesta, estado activo/inactivo. |
| B3 | **Auditoría de partidas** | Listado paginado y filtrable de todos los giros: jugador, juego, *seed*, apuesta, balance pre/post, resultado, timestamp. |
| B4 | **Replay visual determinista** | A partir de cualquier giro auditado, el operador reproduce la animación exacta del giro en una pantalla idéntica a la del jugador. *Killer feature* para resolver disputas y demostrar transparencia ante la DGOJ. |
| B5 | **Dashboard de actividad** | Métricas básicas en tiempo real: jugadores activos, GGR (saldo apostado − saldo ganado), juegos más jugados. |

#### C. Backoffice matemático

| # | Funcionalidad | Descripción |
|---|---|---|
| C1 | **Editor de matemáticas** | Edición de la configuración JSON de cada juego: símbolos y sus pesos por reel, paytable (combinaciones y multiplicadores), líneas de pago, reglas de bonus (free spins, wilds, scatters). |
| C2 | **Simulador masivo** | Ejecuta hasta **10M de partidas en <10 min** sobre el mismo motor que producción. Configurable: nº de spins, apuesta fija, perfil de jugador. |
| C3 | **Dashboard de métricas de simulación** | RTP empírico (global, base game, free spins), *hit frequency*, volatilidad (desv. estándar), distribución de premios (histograma), max win, frecuencia de trigger de free spins, racha más larga sin premio. |
| C4 | **AI-powered explainability** | Caja de texto donde el matemático pregunta en lenguaje natural ("¿por qué la volatilidad de Espacial es 12.4 cuando esperábamos 10?"). El backend envía las métricas a Claude (Anthropic API) y devuelve una explicación interpretable. |
| C5 | **Validación previa al despliegue** | El sistema avisa si el RTP empírico se desvía más de un umbral del RTP teórico esperado o si hay otros indicadores anómalos. |

#### D. Plataforma y compliance (transversal)

| # | Funcionalidad | Descripción |
|---|---|---|
| D1 | **Motor de juego *data-driven*** | Único en Java, parametrizado por JSON. 3 juegos = 3 ficheros de configuración + 3 packs de assets. |
| D2 | **RNG certificable** | RNG criptográficamente fuerte (`SecureRandom`), aislado en módulo propio, con *seeds* reproducibles y documentación matemática para futura certificación. |
| D3 | **Registro auditable inmutable** | Tabla `game_round` con todos los datos del giro. Inserción *append-only*, sin update/delete por contrato y por *trigger* en BBDD. |
| D4 | **Roles y permisos** | `PLAYER`, `OPERATOR`, `MATH_ANALYST`. Cada rol accede solo a su superficie. JWT en headers. |
| D5 | **Despliegue local con Docker Compose** | Un comando arranca toda la stack. |

#### E. Alcance fuera de esta versión

- Pasarelas de pago/cobro (saldo virtual ≠ saldo real).
- Jackpots progresivos.
- Generación automática de informes RFJ para la DGOJ (la arquitectura los soporta a futuro).
- Accesibilidad WCAG 2.1 AA completa.
- Límites de pérdida y autoexclusión completos (preparados pero no implementados).
- Despliegue cloud público.

---

### **1.3. Diseño y experiencia de usuario:**

En esta primera fase no se entregan mockups gráficos ni vídeo (se generarán en la siguiente fase del proyecto, junto con los assets temáticos). A continuación se describen los **flujos de usuario principales** y se incluyen *wireframes* ASCII de las pantallas core para fijar la intención de diseño.

#### Flujo 1 — Jugador: del lobby al free spin

1. Jugador entra en `/`. Si no está autenticado, se le redirige a `/login` con declaración de edad ≥18.
2. Tras login, aterriza en el lobby con las carátulas de los 3 juegos y su saldo virtual visible.
3. Click en una carátula → pantalla de juego con la rejilla, controles de apuesta, botón *Spin* y botón *Auto-spin*.
4. Pulsa *Spin*: animación de giro, evaluación de líneas, animación de premio si lo hay, actualización de balance.
5. Si caen 3+ scatters: cinemática de "¡Free Spins!" (con voz de locutor) y entra en modo bonus.
6. Al salir del juego, el saldo y el historial quedan persistidos para la próxima sesión.

#### Flujo 2 — Operador: resolver una reclamación

1. Operador entra en `/operator` con su rol. Va a "Auditoría".
2. Filtra por jugador, juego y rango de fechas. Localiza el giro reclamado.
3. Pulsa "Replay". Se abre una pantalla idéntica a la del jugador y se ejecuta el giro exacto (mismo *seed*, mismas reels, mismo resultado).
4. Comparte el enlace de *replay* con el jugador como prueba.

#### Flujo 3 — Matemático: ajustar el RTP de un juego

1. Matemático entra en `/math`. Selecciona el juego "Espacial".
2. Edita el peso de un símbolo en la pestaña "Reels". Guarda.
3. Lanza una simulación de 10M de spins. Espera <10 min.
4. Revisa el dashboard: el RTP cae al 95.2 %, la volatilidad sube. Pregunta a la IA: "¿por qué sube la volatilidad?". La IA explica el cambio en la distribución de premios.
5. Itera ajustes hasta alcanzar el RTP/volatilidad objetivo.

#### Wireframes ASCII

```
LOBBY
┌────────────────────────────────────────────────────────────┐
│ NovaCasino Studio              [ES|EN]  Saldo: 1.000 €  ▼ │
├────────────────────────────────────────────────────────────┤
│                                                            │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐             │
│   │ EGIPCIO  │    │ FRUTAS   │    │ ESPACIAL │             │
│   │  (5x3)   │    │  (3x3)   │    │  (5x3)   │             │
│   └──────────┘    └──────────┘    └──────────┘             │
│                                                            │
│  Sello DGOJ │ Juego responsable │ +18                      │
└────────────────────────────────────────────────────────────┘

PANTALLA DE JUEGO
┌────────────────────────────────────────────────────────────┐
│ ← Lobby           ESPACIAL          Saldo: 985 €  🔊       │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌───┬───┬───┬───┬───┐                                     │
│  │ ★ │ ☄ │ ☄ │ 🪐│ A │                                     │
│  ├───┼───┼───┼───┼───┤                                     │
│  │ K │ ★ │ A │ ☄ │ ★ │   GANANCIA: 7,50 €                  │
│  ├───┼───┼───┼───┼───┤                                     │
│  │ ☄ │ K │ 🪐│ A │ K │                                     │
│  └───┴───┴───┴───┴───┘                                     │
│                                                            │
│  Apuesta: [- 1,00 € +]   [SPIN]   [AUTO-SPIN]              │
└────────────────────────────────────────────────────────────┘

BACKOFFICE OPERADOR — AUDITORÍA + REPLAY
┌────────────────────────────────────────────────────────────┐
│ /operator                                  Operador: Ana ▼ │
├────────────────────────────────────────────────────────────┤
│  Filtros: Jugador [   ] Juego [▼] Fecha [   ]  [Buscar]    │
│                                                            │
│  Round │ Jugador │ Juego  │ Apuesta │ Premio │ [Replay]    │
│  ──────┼─────────┼────────┼─────────┼────────┼─────────    │
│  R-201 │ user42  │ Egip.  │ 1,00 €  │ 4,50 € │ [▶ Replay]  │
│  R-202 │ user07  │ Esp.   │ 0,50 €  │ 0,00 € │ [▶ Replay]  │
└────────────────────────────────────────────────────────────┘

BACKOFFICE MATEMÁTICO — SIMULADOR + IA
┌────────────────────────────────────────────────────────────┐
│ /math                                  Matemático: Luis ▼  │
├────────────────────────────────────────────────────────────┤
│  Juego: ESPACIAL ▼     [Editar matemática]                 │
│                                                            │
│  Spins: [10.000.000]    Apuesta: [1,00 €]   [SIMULAR]      │
│                                                            │
│  Resultados:                                               │
│   • RTP global:  95,21 %       • Volatilidad: 12,4         │
│   • Hit freq:    24,7 %        • Max win:    1.250x        │
│   • RTP base game: 76,8 %      • RTP free spins: 18,4 %    │
│                                                            │
│  Pregunta a la IA:                                         │
│   ┌──────────────────────────────────────────────┐         │
│   │ ¿Por qué la volatilidad sube respecto a la   │         │
│   │ versión anterior?                            │         │
│   └──────────────────────────────────────────────┘         │
│   [Preguntar]                                              │
└────────────────────────────────────────────────────────────┘
```

---

### **1.4. Instrucciones de instalación:**

> Las instrucciones definitivas se publicarán en la fase de implementación. A continuación se documenta el procedimiento previsto, alineado con la arquitectura aprobada en este PRD.

#### Requisitos previos

- Docker Desktop 4.x o Docker Engine + Docker Compose v2
- Git
- Una API key de Anthropic (variable `ANTHROPIC_API_KEY`) para la funcionalidad de *AI explainability* en el backoffice matemático. Sin ella, el resto de la plataforma funciona; solo se desactiva esa feature.

#### Pasos de instalación local

```bash
# 1. Clonar el repositorio
git clone https://github.com/<owner>/AI4Devs-finalproject.git
cd AI4Devs-finalproject

# 2. Copiar la plantilla de variables de entorno y editarla
cp .env.example .env
# Editar .env y poner ANTHROPIC_API_KEY=sk-ant-...

# 3. Levantar todos los servicios (backend + frontend + Postgres)
docker compose up --build

# 4. (Primera vez) Las migraciones Flyway y las semillas se ejecutan
#    automáticamente al arrancar el backend.
```

#### URLs locales

| Superficie | URL |
|---|---|
| Lobby del jugador | `http://localhost:5173/` |
| Login del jugador | `http://localhost:5173/login` |
| Backoffice operador | `http://localhost:5173/operator` |
| Backoffice matemático | `http://localhost:5173/math` |
| API REST | `http://localhost:8080/api` |
| OpenAPI / Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Base de datos PostgreSQL | `localhost:5432` (`novacasino` / `novacasino`) |

#### Datos de semilla (auto-creados al primer arranque)

- 3 juegos preconfigurados: Egipcio (5x3), Frutas (3x3), Espacial (5x3).
- 1 usuario `OPERATOR` (`operator@nova.test` / `operator123`).
- 1 usuario `MATH_ANALYST` (`math@nova.test` / `math123`).
- 3 usuarios `PLAYER` (`player1@nova.test`, `player2@nova.test`, `player3@nova.test` / `player123`) cada uno con 1.000 € de saldo virtual.

#### Parar y resetear

```bash
docker compose down              # Para los servicios
docker compose down -v           # Para y borra los volúmenes (resetea la BBDD)
```

---

## 2. Arquitectura del Sistema

### **2.1. Diagrama de arquitectura:**
> Usa el formato que consideres más adecuado para representar los componentes principales de la aplicación y las tecnologías utilizadas. Explica si sigue algún patrón predefinido, justifica por qué se ha elegido esta arquitectura, y destaca los beneficios principales que aportan al proyecto y justifican su uso, así como sacrificios o déficits que implica.


### **2.2. Descripción de componentes principales:**

> Describe los componentes más importantes, incluyendo la tecnología utilizada

### **2.3. Descripción de alto nivel del proyecto y estructura de ficheros**

> Representa la estructura del proyecto y explica brevemente el propósito de las carpetas principales, así como si obedece a algún patrón o arquitectura específica.

### **2.4. Infraestructura y despliegue**

> Detalla la infraestructura del proyecto, incluyendo un diagrama en el formato que creas conveniente, y explica el proceso de despliegue que se sigue

### **2.5. Seguridad**

> Enumera y describe las prácticas de seguridad principales que se han implementado en el proyecto, añadiendo ejemplos si procede

### **2.6. Tests**

> Describe brevemente algunos de los tests realizados

---

## 3. Modelo de Datos

### **3.1. Diagrama del modelo de datos:**

> Recomendamos usar mermaid para el modelo de datos, y utilizar todos los parámetros que permite la sintaxis para dar el máximo detalle, por ejemplo las claves primarias y foráneas.


### **3.2. Descripción de entidades principales:**

> Recuerda incluir el máximo detalle de cada entidad, como el nombre y tipo de cada atributo, descripción breve si procede, claves primarias y foráneas, relaciones y tipo de relación, restricciones (unique, not null…), etc.

---

## 4. Especificación de la API

> Si tu backend se comunica a través de API, describe los endpoints principales (máximo 3) en formato OpenAPI. Opcionalmente puedes añadir un ejemplo de petición y de respuesta para mayor claridad

---

## 5. Historias de Usuario

> Documenta 3 de las historias de usuario principales utilizadas durante el desarrollo, teniendo en cuenta las buenas prácticas de producto al respecto.

**Historia de Usuario 1**

**Historia de Usuario 2**

**Historia de Usuario 3**

---

## 6. Tickets de Trabajo

> Documenta 3 de los tickets de trabajo principales del desarrollo, uno de backend, uno de frontend, y uno de bases de datos. Da todo el detalle requerido para desarrollar la tarea de inicio a fin teniendo en cuenta las buenas prácticas al respecto. 

**Ticket 1**

**Ticket 2**

**Ticket 3**

---

## 7. Pull Requests

> Documenta 3 de las Pull Requests realizadas durante la ejecución del proyecto

**Pull Request 1**

**Pull Request 2**

**Pull Request 3**

