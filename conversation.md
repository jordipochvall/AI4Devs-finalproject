# Conversation log — NovaCasino Studio

Este fichero recoge el histórico de prompts (preguntas del usuario y respuestas del asistente) utilizados durante el desarrollo del proyecto **NovaCasino Studio**, organizados de forma cronológica y numerados para facilitar su referencia desde el [`readme.md`](./readme.md) y el [`prompts.md`](./prompts.md).

Convenciones:

- La numeración es global y creciente: se mantendrá a lo largo de **todas las fases** del proyecto (PM, arquitectura, backend, frontend, etc.).
- "Pregunta del usuario" recoge el mensaje literal o un fiel resumen estructurado cuando el usuario respondió a un cuestionario interactivo.
- "Respuesta del asistente" recoge el resumen ejecutivo de la respuesta (no se reproduce la conversación íntegra para mantener la legibilidad).

---

## Fase 0 · Rol Product Manager — PRD inicial (puntos 0 y 1 del readme)

### Prompt 1 — Encargo inicial: actuar como PM experto en gambling

**Pregunta del usuario:**

> Quiero que actues como un product manager experto con años de experiencia en el sector del gambling. Tu misión va a consistir en diseñar una plataforma de juegos de slots que sea atractiva para el jugador. La principal restricción es que todas las fases del desarrollo desde esta especificación hasta el lanzamiento debe ser ejecutable con la ayuda de un asistente de IA como tu con un tiempo de entre 30 y 40 horas.
>
> Te voy a dar una lista de características básicas que este producto debe tener sin prejuicio que tu añadas las que ya tengas con tu expertise. Es más, valoraré que hagas aportaciones más allá de estas iniciales. En caso de dudas prefiero que me preguntes y planifiquemos conjuntamente a que tomes decisiones unilaterales. Por otro lado quiero que las decisiones que escribirás en el PRD sean todas bien razonadas.
>
> - El jugador verá 2 pantallas: lobby con las carátulas de los juegos disponibles y pantalla de juego.
> - Un primer backoffice para operadores: configuración de juegos (monedas, apuestas, etc.) y análisis de partidas para resolver conflictos.
> - Un segundo backoffice para el equipo de matemáticas: configuración de RTP/volatilidad y simulador capaz de lanzar hasta 10M de partidas en menos de 10 minutos.
> - Fuera de scope: pagos/cobros y jackpots progresivos.
> - Se debe ser compliant con la regulación de la DGOJ (España); para esta primera versión no es necesario generar informes oficiales pero sí no tomar decisiones que lo bloqueen a futuro.
>
> Ahora nos centramos en los puntos 0 y 1 del `readme.md`. Cuando se apruebe el plan, rellenar esos puntos. Adicionalmente, abrir un `conversation.md` con todos los prompts numerados.

**Respuesta del asistente:**

Tras leer el repositorio (solo `readme.md` y `prompts.md` como plantillas) se confirma que el proyecto arranca de cero. Se identifican varias decisiones de producto y técnicas críticas para el PRD y se inicia una serie de rondas de preguntas estructuradas para acordarlas con el usuario antes de redactar nada.

---

### Prompt 2 — Ronda 1 de decisiones técnicas y de alcance

**Pregunta del asistente al usuario (4 preguntas en formato cuestionario):**

1. ¿Qué stack técnico se usa, sabiendo que el simulador debe entregar 10M partidas en <10 min y todo en 30-40 h?
2. ¿Qué tipo de slots y cuántos juegos en el MVP?
3. ¿Cómo se gestiona el saldo del jugador si los pagos están fuera de scope?
4. ¿Qué nivel de cumplimiento DGOJ se implementa en este MVP (vs. solo "tener en cuenta")?

**Respuesta del usuario:**

1. **Java + Spring Boot** para backend, **TypeScript** para frontend.
2. **3 video slots jugables**: dos 5x3 y uno 3x3.
3. **Saldo virtual ("fun money") con recarga manual desde el backoffice operador**.
4. Implementar (multi-selección): registro auditable inmutable de cada giro · mensajes de juego responsable + sello DGOJ + verificación >18 en login · RNG certificable (separación motor/aleatorio + seeds reproducibles + documentación matemática).

---

### Prompt 3 — Ronda 2: bonus, tema visual, despliegue y ficha del proyecto

**Pregunta del asistente al usuario (4 preguntas):**

1. ¿Qué features de bonus para los slots 5x3?
2. ¿Qué tema visual/estética para los 3 juegos?
3. ¿Cómo se entrega el proyecto para ejecutarlo en local?
4. ¿Qué datos uso para la "Ficha del proyecto" (punto 0 del readme)?

**Respuesta del usuario:**

1. **Wild + Scatter + Free Spins** (en los 5x3; el 3x3 queda clásico, solo líneas).
2. **3 temáticas distintas**: Egipcio, Frutas clásico, Espacial.
3. **Docker Compose** (todo en uno: backend + frontend + Postgres).
4. **Nombre completo: Jordi Poch** · **Repo: el actual de GitHub** · **Nombre del proyecto: a definir conjuntamente** (el asistente propondrá 3 alternativas).

---

### Prompt 4 — Ronda 3: nombre comercial de la plataforma

**Pregunta del asistente al usuario:** elegir entre tres nombres con justificación de marca: *SpinForge*, *IberSlots*, *NovaCasino Studio*.

**Respuesta del usuario:** **NovaCasino Studio** (posicionamiento B2B "game studio", refuerza el diferencial del backoffice matemático y del motor data-driven).

---

### Prompt 5 — Feedback al primer plan: añadir i18n, audio y diferenciación

**Pregunta del usuario:**

> Me gustaría añadir soporte multiidioma y música que ayude a hacer una experiencia más envolvente para el usuario. Por otro lado, quiero que me propongas ideas innovadoras para hacer un proyecto más diferenciado.

**Respuesta del asistente:** se acepta el feedback y se preparan tres preguntas para fijar el alcance de i18n, alcance de audio y selección de features innovadoras (de un catálogo de 4 propuestas razonadas: replay determinista, comparador A/B en simulador, AI explainability con Claude, math-as-code versionado). Se incorporan adicionalmente, por defecto, dos recomendaciones del PM: auto-spin con safeguards (juego responsable) y accesibilidad WCAG 2.1 AA básica.

---

### Prompt 6 — Ronda 4: idiomas, audio y features diferenciales

**Pregunta del asistente al usuario (3 preguntas):**

1. ¿Qué idiomas debe soportar el MVP?
2. ¿Qué alcance de audio para hacer la experiencia inmersiva?
3. ¿Qué features innovadoras incorporamos para diferenciar NovaCasino Studio? (multi-selección)

**Respuesta del usuario:**

1. **Español + Inglés**.
2. **Música ambiente por temática + SFX comunes + voz de locutor en eventos especiales** (Big Win, Free Spins) en ES/EN.
3. **Replay determinista de partidas en backoffice operador** + **AI-powered explainability del simulador con Claude**.

---

### Prompt 7 — Feedback al plan final: ajustar scope de accesibilidad

**Pregunta del usuario:**

> Quita "Accesibilidad WCAG 2.1 AA básica", queda fuera de la primera versión. OK al resto.

**Respuesta del asistente:** se elimina la accesibilidad WCAG del MVP, se desplaza a "fuera de scope v1", se recalcula la estimación de horas (de ~52 h a ~50.5 h) y se actualizan las palancas de recorte. Se repite la presentación del plan para aprobación.

---

### Prompt 8 — Aprobación del plan y ejecución

**Pregunta del usuario:** aprobación del plan presentado.

**Respuesta del asistente:** ejecución de las dos acciones aprobadas en el plan:

1. **Edición de `readme.md`** rellenando los puntos 0 (Ficha del proyecto) y 1 (Descripción general del producto = PRD): objetivo, características y funcionalidades por perfil (jugador, operador, matemático, plataforma), wireframes ASCII de las pantallas core e instrucciones de instalación con Docker Compose. Las secciones 2-7 quedan intactas como plantilla para fases siguientes.
2. **Creación de `conversation.md`** (este fichero) con la numeración global de los prompts de la fase 0.

Decisiones consolidadas que entran en el PRD:

| Tema | Decisión |
|---|---|
| Stack | Java 21 + Spring Boot · TypeScript + React + Vite · PostgreSQL · Docker Compose |
| Juegos | 3 video slots (Egipcio 5x3, Frutas 3x3, Espacial 5x3); 5x3 con Wild + Scatter + Free Spins |
| Saldo | Virtual con recarga manual desde backoffice operador |
| DGOJ implementado | Auditoría inmutable · UI de juego responsable + sello + >18 · RNG certificable |
| Idiomas | ES + EN |
| Audio | Música por temática + SFX + voz locutor en eventos especiales |
| Features diferenciales | Replay determinista + AI explainability con Claude |
| Recomendación PM por defecto | Auto-spin con safeguards |
| Fuera de scope v1 | Pagos, jackpots progresivos, informes RFJ, WCAG completa, autoexclusión y límites de pérdida |
| Riesgo abierto | Estimación ~50.5 h frente al techo de 40 h; documentadas las palancas de recorte |

---

## Fase 1 · Rol Arquitecto Senior — Arquitectura del sistema (punto 2 del readme)

### Prompt 9 — Cambio de rol a arquitecto senior y encargo del punto 2

**Pregunta del usuario:**

> A partir de ahora quiero que actues como un arquitecto senior con experiencia en patrones de diseño de software y especialmente de aquellos aplicados a la industria del gambling. Ya no eres product manager. Ahora vamos a generar el punto 2 del `readme.md`. La parte correspondiente a la parte de arquitectura del sistema. Igual que antes consúltame cualquier duda que puedas tener y justifícame las decisiones que vayas tomando. No dudes en usar gráficos allí donde sea posible para ayudar a clarificar los conceptos. Si quedan muy grandes trocéalos con sentido. Sigue actualizando el fichero `conversation.md`.

**Respuesta del asistente:** se asume el nuevo rol y se identifican las decisiones arquitectónicas críticas que condicionan todo el punto 2. Se preparan dos rondas de preguntas para cerrar: estilo macro, patrón interno, comunicación cliente-servidor, organización del simulador, persistencia, testing y frontend stack.

---

### Prompt 10 — Ronda 1 de decisiones arquitectónicas

**Pregunta del asistente al usuario (4 preguntas):**

1. ¿Qué estilo de arquitectura macro para el backend?
2. ¿Qué patrón interno aplico en el backend?
3. ¿Cómo modelo la comunicación cliente-servidor para la pantalla del juego?
4. ¿Cómo organizo el simulador y la persistencia para alcanzar 10M/<10 min?

**Respuesta del usuario:**

1. **Monolito modular Maven multi-módulo**.
2. **Arquitectura hexagonal (ports & adapters) + DDD ligero**.
3. **REST puro request/response por cada giro**.
4. **Mismo proceso, módulo Maven separado, ForkJoinPool, sin BBDD durante la run**.

---

### Prompt 11 — Ronda 2: persistencia, testing y frontend stack

**Pregunta del asistente al usuario (3 preguntas):**

1. ¿Persistencia y estrategia de auditoría inmutable?
2. ¿Qué estrategia de testing dejo recogida en la arquitectura?
3. ¿Frontend state management y librerías auxiliares?

**Respuesta del usuario:**

1. *Comentario abierto*: "Considera el uso de bases de datos no relacionales (como MongoDB) para mejorar la performance" → se transforma en una nueva ronda de preguntas (Prompt 12).
2. **Pirámide clásica: unit del motor + integration con Testcontainers + 1 E2E feliz con Playwright**.
3. **React + Zustand + TanStack Query + i18next**.

---

### Prompt 12 — Ronda 3: análisis MongoDB vs PostgreSQL e integridad anti-tampering

**Pregunta del asistente al usuario (2 preguntas):**

1. Sobre la persistencia con MongoDB en el mix, ¿qué modelo prefieres? (Polyglot Postgres+Mongo / Mongo todo en uno / Postgres todo en uno)
2. Si vamos polyglot, ¿añado hash-chain en `game_round`?

**Respuesta del usuario:**

1. **"Si MongoDB no da mejor performance entonces quizás no es necesario meterlo"**. El asistente analiza honestamente los números (volumen MVP esperado: miles de spins/día, no 100k/seg) y concluye que MongoDB no aporta perf suficiente para justificar la complejidad operacional. Se cierra la decisión: **PostgreSQL único + JSONB para configs + particionado mensual de `game_round` + hash-chain implementado como trigger Postgres con `pgcrypto`**.
2. **Sí, hash-chain** (mantenido aunque se implementa en Postgres en lugar de Mongo).

---

### Prompt 13 — Ejecución del punto 2

**Pregunta del usuario:** aprobación tácita tras cerrar todas las decisiones arquitectónicas.

**Respuesta del asistente:** redacción del punto 2 completo del `readme.md` con seis subsecciones:

- **2.1 Diagrama de arquitectura** — tres diagramas C4 (contexto, contenedores, componentes del backend) en Mermaid + tabla de patrones aplicados (Hexagonal, DDD, Strategy, Builder, Command+Event, Repository, Adapter, Map-Reduce, Hash-chain) + análisis de beneficios y sacrificios.
- **2.2 Descripción de componentes principales** — desglose de los 6 módulos Maven del backend (`nova-domain`, `nova-application`, `nova-infrastructure`, `nova-simulator`, `nova-web-api`, `nova-common`), del frontend (React + Zustand + TanStack Query + i18next + Howler.js) y de los servicios externos (Anthropic).
- **2.3 Descripción de alto nivel del proyecto y estructura de ficheros** — árbol completo del repo con explicación de la regla de dependencia hexagonal y la convención de carpetas verticales en frontend.
- **2.4 Infraestructura y despliegue** — diagrama Mermaid de la topología Docker Compose (3 contenedores: `web` nginx, `api` Spring Boot, `postgres`) + diagrama del pipeline CI/CD (GitHub Actions) con la nota de que cloud queda fuera de MVP.
- **2.5 Seguridad** — cuatro bloques: autenticación (JWT + BCrypt + verificación >18), integridad de auditoría (diagrama Mermaid del hash-chain + trigger Postgres), RNG criptográficamente fuerte (SecureRandom + seeds reproducibles + replay determinista), defensa en profundidad (CORS, rate limiting con Bucket4j, validación, CSP) y específicas de gambling/DGOJ (server-side game logic, trazabilidad total, sello DGOJ, juego responsable).
- **2.6 Tests** — diagrama de pirámide (unit denso + property-based con jqwik + ArchUnit + integration con Testcontainers + 1 E2E con Playwright) + ejemplos representativos + tabla de cobertura objetivo por módulo.

Decisiones consolidadas que entran en el punto 2 del PRD:

| Tema | Decisión |
|---|---|
| Estilo macro | Monolito modular Maven multi-módulo |
| Patrón interno | Hexagonal + DDD ligero |
| Comunicación cliente-juego | REST puro |
| Simulador | In-process, módulo Maven separado, ForkJoinPool, sin BBDD durante la run |
| Persistencia | PostgreSQL 16 con JSONB + particionado mensual de `game_round` |
| Inmutabilidad auditoría | Hash-chain SHA-256 vía trigger Postgres + `pgcrypto` + trigger anti-UPDATE/DELETE |
| RNG | `SecureRandom` aislado en adapter; puerto en dominio; seeds registradas para replay |
| Frontend stack | React 18 + TS + Vite + Zustand + TanStack Query + i18next + Howler.js |
| Testing | JUnit + AssertJ + jqwik (property-based) + ArchUnit + Testcontainers + Playwright (1 E2E) |
| Despliegue | Docker Compose (`web`, `api`, `postgres`); cloud explícitamente fuera de MVP |
| CI | GitHub Actions: `mvn verify` + `pnpm test/build` + cobertura Jacoco |

---

### Prompt 14 — Correcciones tras revisión del usuario al punto 2

**Pregunta del usuario:**

> He revisado el punto 2 del `readme.md`. Veo varias cosas a corregir:
> - El diagrama 2.1.1 no se ve bien, da error.
> - El diagrama 2.1.2 tiene una presentación mejorable y no entiendo por qué has puesto la versión 16 de PostgreSQL en vez de la 18 (también en punto 2.2.3).
> - El diagrama 2.1.3 tiene la letra demasiado pequeña, no es fácilmente legible. Divídelo en 3 diferentes, una para cada actor del sistema.
> - ¿Por qué motivo haces la *hash chain* del punto 2.5.2?
> - Quiero que los tests de integración del punto 2.6 estén en un directorio separado de test llamado `it`, es decir `src/it` en vez de `src/test`.

**Respuesta del asistente:** se justifica honestamente la decisión del hash-chain (en MVP single-node sin firma externa no aporta seguridad real adicional sobre el trigger anti-UPDATE/DELETE; lo metí como diferencial de posicionamiento y preparación para fases futuras con firma externa) y se hace una ronda de 2 preguntas finales para cerrar:

- **Hash-chain:** **quitarlo del MVP**, dejar nota explícita en 2.5.2 difiriéndolo a fases con firma externa.
- **Tests de integración:** confirmado uso de `maven-failsafe-plugin` con `src/it/java` y `src/it/resources` por módulo (registrados con `build-helper-maven-plugin`).

A continuación se aplican las cinco correcciones al `readme.md`:

1. **Diagrama 2.1.1**: simplificada la sintaxis Mermaid (sustituido el cylinder `[(...)]` por rectángulo en Anthropic, eliminado el guion largo `—` problemático y renombrado el actor `Math` a `Mathematician` para evitar colisiones con palabras reservadas en algún parser).
2. **Diagrama 2.1.2**: dirección cambiada a `LR`, añadido el container `web` (nginx) explícitamente diferenciado del SPA en el navegador, añadido el actor `Usuario` y actualizado a **PostgreSQL 18**.
3. **Diagrama 2.1.3**: dividido en tres diagramas independientes — **2.1.3.1 Flujo del Jugador**, **2.1.3.2 Flujo del Operador**, **2.1.3.3 Flujo del Matemático** —, cada uno mostrando solo los componentes implicados en sus casos de uso. Letra legible.
4. **Hash-chain quitado**:
   - Eliminada la fila "Hash-chain" de la tabla 2.1.4 de patrones aplicados.
   - Reformulada la viñeta de "Auditabilidad" en 2.1.5 (ahora la cubre el trigger anti-UPDATE/DELETE).
   - Eliminado `HashChain` del listado de clases de dominio en 2.2.1.
   - Renombrada la migración Flyway `V3__hashchain_trigger.sql` por `V2__immutable_audit_trigger.sql` (sin hash-chain) en la estructura del 2.3.
   - 2.5.2 renombrada de "Integridad de la auditoría — hash-chain" a "Inmutabilidad de la auditoría", sustituyendo el diagrama del hash-chain por la explicación del trigger BBDD anti-UPDATE/DELETE y una nota explícita que difiere el hash-chain a fases con firma externa.
   - Eliminada la mención a "hashes" en la fila de logs de 2.5.4 y la mención a "hash válido encadenado" del ejemplo de integration tests en 2.6.
5. **Tests en `src/it`**:
   - Actualizada la estructura de ficheros del 2.3: cada módulo Maven muestra ahora explícitamente `src/test/java` (unit, Surefire) y, en `nova-infrastructure` y `nova-web-api`, también `src/it/java` y `src/it/resources` (Failsafe).
   - Añadida al final de 2.6 una tabla "Convención de carpetas y plugins Maven" con la separación Surefire/Failsafe, naming `*Test` vs `*IT`, registro vía `build-helper-maven-plugin` y nota sobre `mvn test` (solo unit) vs `mvn verify` (ambos).

Adicional: actualizado **PostgreSQL 16 → 18** en 2.2.3 y en el diagrama de Docker Compose 2.4.1 (incluida la imagen `postgres:18-alpine` en la tabla de servicios).

---

## Fase 2 · Rol Arquitecto Senior — Modelo de datos (punto 3 del readme)

### Prompt 15 — Encargo del punto 3

**Pregunta del usuario:**

> Vamos a hacer el apartado 3 del `readme.md` ahora. Ahora toca el modelo de datos. Teniendo presente lo definido en la descripción general del producto y de la arquitectura vamos a diseñar el modelo de datos teniendo presente lo ya escrito en el apartado 3 como requerimiento (aunque quítalo para la versión definitiva).

**Respuesta del asistente:** se asumen como guía los textos previos del apartado 3 (que se eliminan en la versión final) y se prepara una ronda de preguntas de modelado de datos para cerrar las decisiones que más impacto tienen en el schema antes de pintarlo.

---

### Prompt 16 — Ronda de decisiones de modelado

**Pregunta del asistente al usuario (3 preguntas):**

1. ¿Qué estrategia de PK uso para las entidades? (BIGSERIAL / UUID v7 sólo en `game_round` / UUID v7 en todo)
2. ¿Multi-tenancy en el modelo desde el día 0? (single-tenant puro / tabla `operators` con FK en todo)
3. ¿Cómo modelo los Free Spins en `game_round`? (rows independientes con `triggering_round_id` / `free_spin_session` aparte / un row con array JSONB)

**Respuesta del usuario:**

1. **`BIGSERIAL` para todo** — secuencial, óptimo para B-tree y particionado de `game_round`.
2. **Sí, multi-tenant desde el día 0** — tabla `operators` + FK `operator_id` en `users`, `games`, `wallets` y `game_round`. Aunque MVP es single-operador, se prepara la arquitectura para evitar migración masiva en fases posteriores.
3. **Cada free spin es un row independiente con `is_free_spin=true` y `triggering_round_id`** — granularidad máxima de auditoría y replay individual de cada spin, agrupable por sesión de free spins.

**Respuesta del asistente:** redacción del punto 3 completo del `readme.md` con la siguiente estructura:

- **Cabecera del punto 3** — tabla con las 8 decisiones transversales del modelo (PK BIGSERIAL, multi-tenant, dinero en BIGINT céntimos, JSONB+GIN, particionado mensual de `game_round`, inmutabilidad por trigger, optimistic locking en `wallets`, naming snake_case en inglés y plural).
- **3.1 Diagrama del modelo de datos** — dividido en dos diagramas Mermaid `erDiagram` para mantener la legibilidad:
  - **3.1.1 Núcleo: operadores, cuentas y catálogo de juegos** — `operators`, `users`, `wallets`, `games`, `game_configs`.
  - **3.1.2 Superficie operacional: partidas, transacciones y simulaciones** — `game_round`, `wallet_transactions`, `simulation_runs`.
- **3.2 Descripción de entidades principales** — nueve subapartados (3.2.1 a 3.2.9) detallando cada tabla con tabla de columnas (tipo, restricciones, notas), constraints, índices, triggers, particionado y relaciones. El último (3.2.9) recoge los tipos enumerados (`role_enum`, `theme_enum`, `tx_type_enum`, `sim_status_enum`) que se crean en `V1__schema.sql`.

**Decisiones consolidadas que entran en el punto 3:**

| Tema | Decisión |
|---|---|
| BBDD | PostgreSQL 18, esquema único `novacasino`, migraciones Flyway |
| PKs | `BIGSERIAL` en todas las entidades |
| Multi-tenant | Tabla `operators` + FK `operator_id` en `users`, `games`, `wallets`, `game_round`, `simulation_runs` |
| Dinero | `BIGINT` en céntimos + `CHAR(3)` currency |
| Configs y resultados de juego | `JSONB` + índices GIN |
| `game_round` | PK compuesta `(id, created_at)`, particionada mensualmente con `pg_partman`, inmutable por trigger anti-UPDATE/DELETE |
| Free spins | Cada spin es un row con `is_free_spin=TRUE` y `triggering_round_id` self-FK al spin disparador |
| Concurrencia wallet | Optimistic locking con columna `version` |
| Versionado matemático | `game_configs` con UNIQUE `(game_id, version)`; `games.active_config_id` apunta a la versión publicada (FK DEFERRABLE para resolver el ciclo en INSERT) |
| Auditoría de simulaciones | `simulation_runs` solo guarda agregados; los 10M de spins individuales no se persisten |
| AI explainability | `simulation_runs.ai_explanation_log` (JSONB) registra el histórico de Q&A con Claude para trazabilidad |
| Verificación >18 | CHECK en `users.birth_date` |
| Tipos | Cuatro `ENUM` nativos: `role_enum`, `theme_enum`, `tx_type_enum`, `sim_status_enum` |

> Nota: varias de estas decisiones se revisaron y corrigieron en el Prompt 17.

---

### Prompt 17 — Revisión crítica DBA del modelo de datos y correcciones

**Pregunta del usuario:**

> Quiero que revises con ojos críticos el modelo de datos como si fueras un DBA experto en bases de datos relacionales en busca de posibles fallos de diseño.
>
> [tras la revisión] Vamos a resolver los puntos que has encontrado. Ve ofreciéndome opciones para cada problema.

**Respuesta del asistente:** revisión crítica que identificó **2 fallos críticos** (el schema no se podía crear tal cual), **3 importantes** y varios de criterio:

- **C1 (crítico)** — FK a tabla particionada: con `game_round` particionada y PK compuesta `(id, created_at)`, las FK `wallet_transactions.game_round_id` y el self-FK `triggering_round_id` no podían referenciar solo `id`.
- **C2 (crítico)** — `CHECK` de edad con `AGE()`/`NOW()`: expresión no inmutable, se re-evalúa de forma inconsistente en un `restore`. Anti-patrón.
- **I1** — varias FK sin índice (locks y seq scans).
- **I2** — justificación técnicamente falsa de por qué `wallet_transactions` no llevaba trigger (un `ROLLBACK` de `INSERT` no dispara triggers de `DELETE`).
- **I3** — inconsistencia de naming `game_round` (singular) vs convención plural.
- Criterio: enums nativos rígidos, `ai_explanation_log` JSONB con riesgo de *row bloat*, `DEFERRABLE` innecesario, falta de auditoría de publicación de matemática, inmutabilidad asimétrica de `game_configs`, login multi-tenant ambiguo, `password_hash VARCHAR(60)` sin margen.

Se resolvió en dos rondas de preguntas con opciones. **Decisiones tomadas:**

| Problema | Decisión |
|---|---|
| C1 — particionado | **Quitar el particionado en MVP**; `game_rounds` con PK simple `id`. Particionado documentado como evolución futura. |
| C2 — verificación de edad | **Quitar el `CHECK`**; la mayoría de edad se valida solo en la capa de aplicación. |
| I2 — ledger inmutable | **Añadir trigger anti-UPDATE/DELETE** a `wallet_transactions`, igual que `game_rounds`. |
| Enums | **Migrar a `VARCHAR` + `CHECK IN (...)`**; se elimina la dependencia `hypersistence-utils`. |
| AI Q&A log | **Nueva tabla hija `simulation_explanations`** (sustituye al JSONB `ai_explanation_log`). |
| Publicación de matemática | **Nueva tabla `game_config_publications`** con historial completo (quién publicó qué versión y cuándo). |
| Inmutabilidad de `game_configs` | **Añadir trigger anti-UPDATE/DELETE**. |

**Correcciones triviales aplicadas sin trade-off:** índices explícitos en todas las FK huérfanas (I1); naming unificado a plural (`game_rounds`, `simulation_runs`); eliminación del `DEFERRABLE` innecesario en `games.active_config_id`; `password_hash` ampliado a `VARCHAR(255)`; `UNIQUE (game_round_id, type)` en `wallet_transactions`; nota sobre la resolución del tenant en el login multi-tenant.

**Resultado:** el punto 3 se reescribió por completo. El modelo pasa de 8 tablas + tipos enum a **10 tablas** (`operators`, `users`, `wallets`, `wallet_transactions`, `games`, `game_configs`, `game_config_publications`, `game_rounds`, `simulation_runs`, `simulation_explanations`). Las cuatro tablas histórico-regulatorias (`game_rounds`, `wallet_transactions`, `game_configs`, `game_config_publications`) comparten la función `fn_forbid_update_delete` y un trigger de inmutabilidad cada una (migración `V2__immutability_triggers.sql`). Se alinearon además dos referencias en el punto 2 (se elimina la mención a `pg_partman` en 2.2.3 y se actualiza el nombre de la migración en 2.3 y 2.5.2).

---

## Fase 3 · Rol Documentalista — Revisión de coherencia (puntos 0-3 del readme)

### Prompt 18 — Revisión documental de coherencia y lagunas

**Pregunta del usuario:**

> Antes de pasar a los siguientes apartados quiero que actúes como un documentalista experto y revises si el contenido generado hasta ahora es coherente o si detectas algunas lagunas para que un desarrollador, un product manager o una IA puedan hacerse una idea precisa de lo que se pretende. [tras la revisión] Resuelve tanto las incoherencias como las lagunas.

**Respuesta del asistente:** revisión documental de los puntos 0-3 que detectó **5 incoherencias** y **6 lagunas** (más varias menores). Tras presentarlas, se resolvieron todas.

**Incoherencias corregidas:**

| # | Incoherencia | Corrección |
|---|---|---|
| INC-1 | El punto 2.5.1 aún describía el `CHECK` de edad eliminado en el Prompt 17 | Reescrito: la verificación ≥18 vive en la capa de aplicación; se explica por qué no hay `CHECK`. |
| INC-2 | `game_round` (singular) residual en los puntos 1 y 2 | Unificado a `game_rounds` en 1.2-D3, 2.1.5, 2.5.5, 2.6 y 3.2.1. |
| INC-3 | "Verificación de edad en login" vs "en registro" | Unificado a **en el registro** en 1.1, A1 y el Flujo 1. |
| INC-4 | `apps/web` (2.2.2) no existía en la estructura de ficheros (2.3) | Cambiado a `frontend/`. |
| INC-5 | Cardinalidad `USERS ||--\|\| WALLETS` (uno-a-uno obligatorio) pese a que operador/matemático no tienen wallet | Corregida a `USERS ||--o| WALLETS` (cero o uno). |

**Lagunas resueltas:**

| # | Laguna | Resolución |
|---|---|---|
| LAG-A | El esquema del JSON de configuración de juego (pilar del motor *data-driven*) no estaba especificado | Nuevo apartado **3.3** con estructura campo a campo, ejemplo completo de un juego 5x3 y reglas de validación. |
| LAG-B | El contrato del puerto `RngEngine` no permitía el *replay* (solo `nextInt`/`getSeed`) | 2.5.3 reescrito: se separa `SecureRandom` (genera el seed) de un PRNG determinista reproducible (genera la secuencia), y se añade el puerto `RngFactory` con `create()` y `createWithSeed(long)`. |
| LAG-C | El estado de una sesión de free spins en curso no estaba modelado | Decisión documentada (A6 y nota en 3.2.8): la ronda completa de free spins se computa y persiste **atómicamente** junto al spin disparador; no hay estado de sesión intermedio. |
| LAG-D | "Perfil de jugador" del simulador mencionado pero no definido ni modelado | Eliminada la mención en C2 (el simulador se configura solo con nº de spins y apuesta fija). |
| LAG-E | Idempotencia de las operaciones de juego no abordada | Añadida fila "Idempotencia" en 2.5.4: `POST .../spin` con cabecera `Idempotency-Key` y deduplicación en backend. |
| LAG-F | Ausencia de glosario de términos de gambling | Añadido un **glosario** al inicio del punto 1 (RTP, volatilidad, reel, payline, paytable, wild, scatter, free spins, RNG, seed, GGR, DGOJ, RFJ…). |

**Lagunas menores:** se aclara que el MVP solo soporta `EUR` (decisión transversal 3), que el umbral de desviación de RTP de C5 es un parámetro de aplicación, y que `nova-common` es la excepción a la regla de dependencia hexagonal.

---

## Fase 4 · Rol Experto en APIs — Especificación de la API (punto 4 del readme)

### Prompt 19 — Encargo del punto 4 y diseño de la API

**Pregunta del usuario:**

> Quiero ahora que te comportes como un experto en diseño de APIs y microservicios y definas la API del backend necesaria para la aplicación.

**Respuesta del asistente:** se asume el rol y se hace una ronda de 2 preguntas de diseño:

1. **Versionado** → **Sí, prefijo `/api/v1/...`** (implica alinear las referencias del punto 2).
2. **Endpoints a detallar en OpenAPI** → el usuario pide **detallarlos todos**, marcando como **prioritarios** el spin, la simulación, el replay y el login/registro de jugador.

A continuación se ejecuta:

- **Alineación del versionado** en los puntos 1-2: `/api/player/*`, `/api/operator/*`, `/api/math/*`, `/api/auth/login`, `POST /api/player/spin` y la URL local de la API pasan a `/api/v1/...`.
- **Ajuste de coherencia** en 3.2.8: el campo `result` (JSONB) pasa de `{reels, winning_paylines, scatter_count}` a `{view, winningPaylines, scatterCount, multiplier}` en camelCase, para no colisionar con `reels` (las *strips* de configuración del apartado 3.3) y alinearse con la API.
- **Redacción del punto 4 completo** con cuatro subapartados:
  - **4.1 Principios de diseño** — REST, versionado `/api/v1`, OpenAPI 3.1 + springdoc, JWT por rol, errores RFC 9457 *Problem Details*, paginación envuelta, idempotencia (`Idempotency-Key`), operaciones largas asíncronas (simulación = `202` + *polling*, sin WebSockets), *microservice-readiness* y tabla de códigos HTTP.
  - **4.2 Catálogo de endpoints** — ~26 endpoints en 4 tablas (Auth, Player, Operator, Math), con marca ★ de prioridad.
  - **4.3 Ficha de cada endpoint** — todos los endpoints detallados (acceso, parámetros, cuerpo, respuestas).
  - **4.4 OpenAPI 3.1 + ejemplos** — especificación formal en YAML de los 5 endpoints prioritarios (registro, login, spin, lanzar simulación, replay) con ejemplos request/response JSON.
- Se reutiliza el vocabulario del apartado 3.3 (`grid`, `symbols`, `reels`, `paylines`, `paytable`, `bonus`) en los contratos de la API, según lo acordado.

**Decisiones consolidadas que entran en el punto 4:**

| Tema | Decisión |
|---|---|
| Estilo / base | REST JSON, base `/api/v1`, contrato OpenAPI 3.1 autogenerado con springdoc |
| Auth | JWT Bearer; autorización por rol; `/auth/register` y `/auth/login` públicos |
| Errores | RFC 9457 *Problem Details* (`application/problem+json`), i18n ES/EN |
| Paginación | `page`/`size` + envoltorio `{content, page, size, totalElements, totalPages}` |
| Idempotencia | Cabecera `Idempotency-Key` en `spin` y `recharge` |
| Operación larga | Lanzar simulación → `202 Accepted` + *polling* sobre `GET .../simulations/{id}` |
| Endpoints prioritarios | registro, login, spin, lanzar simulación, replay (detallados en OpenAPI con ejemplos) |

---

## Fase 5 · Rol Auditor Externo — Revisión integral del readme (puntos 0-4)

### Prompt 20 — Auditoría del documento y corrección de hallazgos

**Pregunta del usuario:**

> Puedes hacer una revisión del presente documento como si fueras un auditor externo experto en proyectos de desarrollo de software? Analiza el documento `readme.md` desde diferentes puntos de vista y busca formas de mejora a cualquier nivel.

**Respuesta del asistente:** auditoría integral de los puntos 0-4 que identificó **2 hallazgos de severidad alta**, **6 medios**, **7 bajos** y **3 mejoras de valor añadido**. Tras presentar el informe, el usuario aprobó las correcciones (con una matización en H2). Cambios aplicados:

| Ref | Hallazgo | Resolución |
|---|---|---|
| H1 | Idempotencia prometida sin soporte en el modelo de datos | Nueva tabla `idempotency_keys` (entidad 11ª): diagrama 3.1.2, apartado 3.2.11, relación con `users`. |
| H2 | Riesgo de alcance frente a las 30-40 h | El MVP se acota a **16 endpoints** (5 ★ + soporte imprescindible); el catálogo 4.2 marca cada endpoint como ★ MVP / MVP / post-MVP. |
| H3 | `games.theme` seguía como `theme_enum` | Migrado a `VARCHAR(20)` + `CHECK IN (...)`, coherente con la decisión transversal 7. |
| H4 | "`nova-simulator` ejecuta `SpinUseCase`" (impreciso) | Corregido: el simulador reutiliza el motor de dominio (`Game`, `Reels`, `Paytable`, `RngEngine`), sin wallet ni BBDD. |
| H5 | Dimensión de rejilla duplicada (`games` y `config`) | Eliminados `grid_rows`/`grid_cols` de `games`; la rejilla vive solo en `game_configs.config.grid`. |
| H6 | Cambios de configuración comercial sin auditar | Documentada la decisión de no auditarlos en MVP (nota en 3.2.5 y en 1.5-D5). |
| H7 | Transaccionalidad del `spin` no explícita | Explicada en la nota "Atomicidad del spin" (3.2.8) y en el nuevo diagrama de secuencia 2.1.6. |
| H8 | Test de rendimiento del simulador fuera de CI | Pasa a ejecutarse en CI en un *job* `perf` dedicado (pipeline 2.4.2 y apartado 2.6). |
| H9 | Cardinalidad `users`–`wallets` ("1→1") | Corregida a "1 → 0..1" también en el texto de relaciones de 3.2.2. |
| H10 | La nota de `config` omitía `grid` | Añadido `grid` a la lista de campos en 3.2.6. |
| H11 | Textos-guía de plantilla inconsistentes | Eliminado el texto-plantilla del punto 1 y des-*blockquoteada* la nota de 1.4. |
| H12 | Índice de un solo nivel | Añadido segundo nivel (subapartados 1.1-1.5, 2.1-2.6, 3.1-3.3, 4.1-4.4). |
| H13 | Capitalización de títulos inconsistente | Normalizados los encabezados de los puntos 2, 3, 5, 6 y 7 a *sentence case*. |
| H14 | "Spring Boot 3" impreciso | Fijada la versión **Spring Boot 3.4** en todas las referencias. |
| H15 | E2E Playwright dentro de `src/it/java` | Movido a un proyecto `e2e/` propio (Playwright en TS), ejecutado en un *job* `e2e` de CI. |
| M1 | Falta diagrama de secuencia del `spin` | Añadido apartado 2.1.6 con un `sequenceDiagram` que muestra idempotencia y transaccionalidad. |
| M2 | Variables de entorno dispersas | Añadida tabla consolidada de variables de entorno en 1.4. |
| M3 | "post-MVP" disperso por el documento | Consolidado en el nuevo apartado **1.5 Supuestos y decisiones diferidas** (5 supuestos + 11 decisiones diferidas). |

**Matización del usuario en H2:** en lugar de implementar solo los 5 endpoints ★ (que dejarían el producto no demostrable end-to-end) o los 26 completos, se acota el MVP a **5 ★ + ~11 endpoints de soporte imprescindibles = 16**, marcando el resto como post-MVP en el catálogo.



