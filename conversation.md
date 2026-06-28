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

---

## Fase 6 · Rol Product Engineer — Historias de usuario (punto 5 del readme)

### Prompt 21 — Generación de las 3 historias de usuario del MVP

**Pregunta del usuario:**

> Ahora quiero que generes las 3 historias de usuario principales necesarias para el MVP. Quiero que actúes como un product engineer y apliques las buenas prácticas como seguir los criterios INVEST y el uso de BDD.

**Respuesta del asistente:** se redacta el punto 5 del `readme.md` con las tres historias de usuario principales — una por perfil, cada una asociada a uno de los endpoints prioritarios (★):

- **HU-1 — El jugador realiza un giro** (`POST .../spin`): escenarios de giro sin premio, giro con premio, activación de free spins, saldo insuficiente e idempotencia.
- **HU-2 — El matemático valida un juego con el simulador** (`POST .../simulations`): lanzar simulación, objetivo de rendimiento 10M/<10 min, consulta de métricas, convergencia del RTP y validación de parámetros.
- **HU-3 — El operador resuelve una reclamación con el replay** (`GET .../replay`): localizar la partida, replay determinista, reproducibilidad y partida inexistente.

Cada historia incluye narrativa estándar (*Como… quiero… para…*), prioridad/estimación, **criterios de aceptación en formato BDD (Gherkin en español)** y una tabla de **verificación INVEST** (Independiente, Negociable, Valiosa, Estimable, Small, Testable) más el alcance excluido.

---

## Fase 7 · Rol Experto Agile — Descomposición en tickets de trabajo

### Prompt 22 — Descomposición de las 3 HU en tickets de trabajo

**Pregunta del usuario:**

> Quiero que actúes como un experto en metodologías Agile y descompongas cada una de las historias de usuario en tickets de trabajo con sentido. Cada ticket de trabajo deberá incluir los siguientes apartados: Código, Título, Descripción, Criterios de Aceptación, Prioridad, Estimación, Equipo responsable, Etiquetas, Comentarios (opcional), Enlaces o referencias a documentación. Los tickets de trabajo quiero que los generes cada uno por separado en una carpeta que llamaremos `tickets`. Para cada historia de usuario se creará una subcarpeta dentro para sus tickets de trabajo. El nombre de cada fichero será el código y el título del fichero. El formato será .md. Si tienes dudas no asumas nada, prefiero que preguntes.

**Respuesta del asistente:** ronda de 4 preguntas de método para no asumir nada, con las siguientes decisiones:

| Aspecto | Decisión |
|---|---|
| Granularidad | Por capa técnica (~4-7 tickets por HU). |
| Código del ticket | `HU-N-EQUIPO-NN` (p. ej. `HU-1-BE-03`). |
| Estimación | Story Points en escala Fibonacci (1, 2, 3, 5, 8, 13). |
| Equipos | Backend (`BE`), Frontend (`FE`), QA, DevOps/Plataforma (`DEV`). |

Convenciones asumidas y explicitadas en el plan: carpeta `tickets/` en raíz; subcarpetas `tickets/HU-N/`; nombre `HU-N-EQUIPO-NN-slug-titulo.md`; prioridad MoSCoW; etiquetas combinadas (área técnica + módulo + dominio funcional); enlaces a apartados del readme.

A continuación se crearon **17 tickets** (~70 SP) distribuidos así:

- **HU-1 — El jugador realiza un giro (7 tickets, 37 SP):** BE-01 autenticación y JWT, BE-02 motor data-driven, BE-03 `SpinUseCase` + idempotencia + endpoint `/spin`, FE-01 registro/login/lobby, FE-02 `<SlotGame>` + integración spin, QA-01 suite de tests (unit + property-based + IT + E2E), DEV-01 Docker Compose + esqueleto CI.
- **HU-2 — El matemático valida con el simulador (5 tickets, 18 SP):** BE-01 módulo `nova-simulator` (`ForkJoinPool` + `LongAdder`), BE-02 API asíncrona (`202` + polling) + endpoints de edición de matemática, FE-01 backoffice math + dashboard con histograma, QA-01 tests de convergencia RTP + test de `@Tag("perf")`, DEV-01 job `perf` en CI.
- **HU-3 — El operador resuelve con replay (5 tickets, 15 SP):** BE-01 API de auditoría con filtros, BE-02 endpoint de replay determinista, FE-01 pantalla de auditoría, FE-02 pantalla de Replay (reutiliza `<SlotGame>`), QA-01 tests de auditoría + reproducibilidad del replay.

Los tickets transversales (`HU-1-BE-01` auth, `HU-1-BE-03` idempotencia, `HU-1-DEV-01` infraestructura, `HU-1-BE-02` motor de dominio) se documentan como dependencias en HU-2 y HU-3.

Cada fichero `.md` sigue la misma plantilla de 10 apartados acordada con el usuario.

### Prompt 23 — Extracción de las HU a ficheros propios

**Pregunta del usuario:**

> Una vez acabes, me gustaría que cogieras las historias que ya incluiste en el readme y cada una de ellas la colocaras en un fichero dentro en formato md. El nombre del fichero sería el código de la historia (como se referencia en sus tickets de trabajo).

**Respuesta del asistente:** se crean tres ficheros `HU-N.md`, uno por historia, en su respectiva subcarpeta `tickets/HU-N/`. Cada fichero contiene la historia completa tal como aparece en el `readme.md` §5 (narrativa, contexto/valor, prioridad/estimación, criterios de aceptación BDD en Gherkin, tabla INVEST y alcance excluido) **y añade un índice de los tickets de trabajo** asociados (tabla con código + título + equipo + SP, con enlaces relativos a cada `.md`) más una sección de enlaces al readme y al endpoint prioritario.

Ficheros creados:

- `tickets/HU-1/HU-1.md` — El jugador realiza un giro (7 tickets, 37 SP).
- `tickets/HU-2/HU-2.md` — El matemático valida un juego con el simulador (5 tickets, 18 SP).
- `tickets/HU-3/HU-3.md` — El operador resuelve una reclamación con el replay (5 tickets, 15 SP).

De este modo cada subcarpeta `tickets/HU-N/` se vuelve **navegable de forma autocontenida**: el fichero `HU-N.md` actúa como índice, y desde él se accede a cada ticket de trabajo.

> **Corrección posterior (Prompt 24):** el usuario aclaró que no quería las historias dentro de `tickets/`, sino en una carpeta dedicada `stories/`. Los ficheros `tickets/HU-N/HU-N.md` se eliminaron y se recrearon como `stories/HU-1.md`, `stories/HU-2.md`, `stories/HU-3.md`, con el código de la historia como nombre de fichero y un pie con enlaces al readme, a su carpeta de tickets y al endpoint prioritario.

---

## Fase 8 · Rol Product Engineer — Resto de historias del MVP (carpeta `stories/`)

### Prompt 25 — Generación del resto de historias del MVP

**Pregunta del usuario:**

> Quiero que generes el resto de historias para el MVP teniendo en cuenta los mismos criterios que se aplicaron para generar las 3 primeras historias. [...] pon las historias en `stories`, no en el README.md.

**Respuesta del asistente:** ronda de 1 pregunta para fijar el alcance de "el resto"; el usuario eligió **cobertura exhaustiva de cada endpoint MVP**. Las 3 HU existentes cubrían 5 endpoints (spin, simulations create/get, rounds, replay); quedaban **11 endpoints MVP** sin historia. Se agrupan en **5 historias nuevas** por objetivo de perfil, garantizando que los 16 endpoints MVP tengan dueño:

| HU | Título | Endpoints MVP cubiertos | Prioridad · Est. |
|---|---|---|---|
| HU-4 | Registro y autenticación de usuarios | `POST /auth/register`, `POST /auth/login` | Must · M |
| HU-5 | El jugador accede al lobby y consulta su saldo | `GET /player/games`, `GET /player/games/{id}`, `GET /player/wallet` | Must · S |
| HU-6 | El operador gestiona jugadores y recarga su saldo | `GET /operator/players`, `POST .../wallet/recharge` | Must · S |
| HU-7 | El matemático edita y versiona la matemática de un juego | `GET /math/games`, `GET /math/configs/{id}`, `POST /math/games/{id}/configs` | Must · M |
| HU-8 | El matemático interpreta resultados con IA | `POST /math/simulations/{id}/explain` | Should · M |

Cada historia se redactó en `stories/HU-N.md` con **el mismo formato** que HU-1/2/3: narrativa estándar (*Como… quiero… para…*), contexto/valor, prioridad/estimación, criterios de aceptación **BDD (Gherkin en español)**, tabla de **verificación INVEST**, alcance excluido y pie de enlaces. **No se modificó el README** (a petición expresa: el readme §5 mantiene las 3 historias-faro; el resto vive solo en `stories/`).

Con esto, los 16 endpoints MVP del catálogo 4.2 quedan cubiertos por las 8 historias (HU-1 a HU-8).

### Prompt 26 — Las 4 historias restantes del MVP (features transversales)

**Pregunta del usuario:**

> Añade las 4 historias restantes para el MVP. Asegúrate que haya coherencia en la nomenclatura y los códigos del README.md y los de las historias.

**Respuesta del asistente:** se completa el backlog del MVP con las features del bloque A que **no están ligadas a un endpoint** (son comportamiento de cliente o capas transversales) y que por eso no entraban en la cobertura "por endpoint" del Prompt 25:

| HU | Título | Feature de origen | Prioridad · Est. |
|---|---|---|---|
| HU-9 | El jugador usa auto-spin con safeguards de juego responsable | A5 | Should · S |
| HU-10 | El jugador disfruta de audio inmersivo | A8 | Should · M |
| HU-11 | El usuario utiliza la plataforma en su idioma (ES/EN) | A9 | Must · S |
| HU-12 | El jugador percibe mensajes de juego responsable y el sello DGOJ | A10 | Must · S |

Cada una en `stories/HU-N.md`, con el mismo formato (narrativa, BDD Gherkin en español, INVEST, fuera de alcance, pie de enlaces). HU-9 reutiliza el endpoint de `spin` sin añadir endpoint; HU-10/11/12 son capas de presentación/transversales sin endpoint propio.

**Coherencia de nomenclatura (parte del encargo):** se alinearon los códigos en el `readme.md` §5 — las cabeceras de las 3 historias-faro pasan a "Historia de Usuario HU-1/HU-2/HU-3 — …" y se añadió una nota que declara que (a) el backlog completo (`HU-1` a `HU-12`) vive en [`stories/`](stories/) y (b) los códigos `HU-N` son la nomenclatura común a historias, tickets ([`tickets/`](tickets/)) y readme. No se añadió contenido de las historias nuevas al readme (a petición expresa del usuario).

**Estado del backlog del MVP:** 12 historias (`HU-1`…`HU-12`) en `stories/`. Cobertura: los 16 endpoints MVP (HU-1…HU-8) + las 4 features transversales del bloque A (HU-9…HU-12). Pendiente: descomponer en tickets HU-4…HU-12 (solo HU-1, HU-2 y HU-3 tienen tickets en `tickets/`).

### Prompt 27 — Descomposición en tickets de HU-4…HU-12 + refactor de HU-1

**Pregunta del usuario:**

> Sí, generalos por favor. [generar los tickets de las 9 historias restantes con el mismo patrón Agile]

**Respuesta del asistente:** antes de generar, se detectó un **solapamiento estructural**: los tickets de HU-1, creados cuando esa historia aún absorbía autenticación y lobby, incluían `HU-1-BE-01` (auth) — que es el alcance de HU-4 — y `HU-1-FE-01` (registro/login + lobby) — que mezcla HU-4 (auth UI) y HU-5 (lobby). Se preguntó cómo resolverlo y el usuario eligió **refactorizar: cada trabajo en una sola HU**.

Acciones ejecutadas:

1. **Refactor de HU-1** (queda gameplay puro, renumerado): `HU-1-BE-01` motor (antes BE-02), `HU-1-BE-02` spin+idempotencia (antes BE-03), `HU-1-FE-01` `<SlotGame>` (antes FE-02), `HU-1-QA-01` tests del juego (sin auth), `HU-1-DEV-01` infra (sin cambios). Se eliminaron el antiguo ticket de auth y el de registro/login/lobby.
2. **Tickets nuevos** para HU-4…HU-12, mismo patrón Agile (10 apartados, código `HU-N-EQUIPO-NN`, SP Fibonacci, equipos BE/FE/QA/DEV):
   - HU-4 (auth): BE-01, FE-01, QA-01 — el BE-01 hereda el alcance del antiguo `HU-1-BE-01`.
   - HU-5 (lobby+saldo): BE-01, FE-01 (hereda el lobby del antiguo `HU-1-FE-01`), QA-01.
   - HU-6 (recarga): BE-01, FE-01, QA-01.
   - HU-7 (editor matemática): BE-01, FE-01, QA-01.
   - HU-8 (IA explainability): BE-01 (adaptador Anthropic + endpoint), FE-01, QA-01.
   - HU-9 (auto-spin): FE-01, QA-01 (sin endpoint, reutiliza `spin`).
   - HU-10 (audio): FE-01, QA-01.
   - HU-11 (i18n): FE-01 (i18next), BE-01 (mensajes API i18n), QA-01.
   - HU-12 (DGOJ/juego responsable): FE-01, QA-01.
3. **Referencias cruzadas** actualizadas en los tickets de HU-2 y HU-3 por el renumerado de HU-1 (motor BE-02→BE-01, spin BE-03→BE-02, auth→`HU-4-BE-01`, SlotGame FE-02→FE-01).
4. **Índices en `stories/`** HU-4…HU-12 actualizados: el pie "pendientes de descomposición" pasa a enlazar a su carpeta `tickets/HU-N/`.

**Estado final del backlog:** **39 tickets** repartidos en 12 historias — HU-1 (5), HU-2 (5), HU-3 (5), HU-4 (3), HU-5 (3), HU-6 (3), HU-7 (3), HU-8 (3), HU-9 (2), HU-10 (2), HU-11 (3), HU-12 (2). Cada trabajo vive en una sola HU; la nomenclatura `HU-N-EQUIPO-NN` es coherente entre `tickets/`, `stories/` y `readme.md`.

---

## Fase 8 · Rol Documentalista — Revisión de coherencia y vacíos (2ª pasada)

### Prompt 28 — Revisión documental de todo el proyecto y correcciones

**Pregunta del usuario:**

> Puedes actuar como un experto en documentación y detectar posibles incoherencias o vacíos en la documentación?

**Respuesta del asistente:** revisión de `readme.md`, `conversation.md`, `prompts.md`, `stories/` y `tickets/` que detectó 11 hallazgos (D1-D11). El usuario decidió sobre cada uno: D1 (prompts.md duplicado del log, deliberado), D2/D3 (§6 tickets y §7 PRs los rellena él / PRs más adelante), D5 (duplicación README↔stories deliberada), D7 (pleonasmo "Historia de Usuario HU-N", no importa), D11 (fechas, dejar) → **no tocar**. Acciones aplicadas:

- **D6** — leyenda de tallas de estimación (S/M/L) y aclaración "historias en tallas, tickets en Story Points" en README §5 y en `stories/stories.md`.
- **D8** — corregido el texto obsoleto "la mayor de **las tres**" (ya hay 12 historias) en README §5 y `stories/HU-1.md`.
- **D9** — corregidos **2 anclas rotas** recurrentes en 16 ficheros: `#44-especificación-openapi-y-ejemplos` → `…-31-y-ejemplos--endpoints-prioritarios` y `#23-estructura-de-alto-nivel-del-proyecto` → `#23-descripción-…-y-estructura-de-ficheros`.
- **D10** — creados los índices `stories/stories.md` (12 historias) y `tickets/tickets.md` (39 tickets, totales por HU y por equipo: BE 49, FE 44, QA 27, DEV 5 = 125 SP).
- **D4** — revisión de candidatos a equipo DB: no hay ticket DB puro; el trabajo de BBDD vive en `HU-1-DEV-01` (migraciones), `HU-7-BE-01`, `HU-3-BE-01`, `HU-1-BE-02`. Documentado en `tickets.md` como recomendación (extraer `HU-1-DB-01` si se quisiera). No ejecutado.

---

## Fase 9 · Rol Consultor Arquitecto Senior — Revisión y mejoras de diseño

### Prompt 29 — Revisión consultiva del proyecto (diagnóstico)

**Pregunta del usuario:**

> Eres un arquitecto senior… actúas como consultor para revisar este proyecto y dar tu opinión. No quiero críticas capciosas, quiero críticas sobre temas importantes que aporten valor.

**Respuesta del asistente:** revisión de alto nivel con 5 temas de peso: (1) presupuesto 30-40 h vs alcance (matizado luego: viable en amplitud con IA, ajustado en profundidad); (2) tensión "motor data-driven + DDD" vs 10M/<10 min; (3) el replay determinista depende de versionar el código del motor, no solo el config; (4) el simulador en la JVM de la API degrada la demo; (5) el RTP teórico de free spins con retrigger es matemática dura e infraespecificada. Más notas: convergencia de RTP flaky en alta volatilidad, "compliant DGOJ día 0" sobrevendido, terminología RNG. El usuario decidió abordar los puntos 2 y 3.

### Prompt 30 — Punto 2: motor de doble vía (núcleo data-oriented + cáscara DDD)

**Pregunta del usuario:** desarrollar el punto 2 — versión *light* para el simulador y objetos de dominio para las partidas normales.

**Respuesta del asistente:** se diseñó y documentó **un único `SpinKernel` + dos materializaciones vía sink/visitor** (decisión del usuario: sink/visitor con callback). `GameCompiler`→`CompiledGame` (primitivos, cacheado por `configId`); `CountingSink` (simulador, cero-alloc) y `MaterializingSink` (producción → `Round`). Fidelidad "lo simulado = lo jugado" garantizada por construcción (mismo kernel, distinta materialización). Documentado en README **§2.1.7** (nueva, con diagrama), §2.1.4 (patrón), §2.2.1 (módulos), §2.6 (tests cero-alloc y fidelidad), §3.3 (compilación del config); historias HU-1/HU-2 y tickets HU-1-BE-01, HU-2-BE-01, HU-1-BE-02.

### Prompt 31 — Punto 3: replay "guardar-y-renderizar" + golden-master

**Pregunta del usuario:** ideas para el punto 3 (determinismo del replay frente a la evolución del motor); criterio: simplicidad y que el motor evolucione sin guardar N versiones.

**Respuesta del asistente:** se adoptó **"guardar-y-renderizar"**: el replay renderiza el `result` inmutable de `game_rounds` (autoritativo), no recalcula; así el motor evoluciona sin mantener N versiones. La recomputación con `createWithSeed` baja a **verificación/forense** y a un **golden-master** en CI (corpus congelado `(seed,config)→result` que rompe el build ante drift) — tras debatir con el usuario que el golden-master da *conciencia consciente* de la ruptura (revertir vs re-baselinar), no retrocompatibilidad automática. Cambios: README §2.5.3 (reformulado + rationale), §1.2-B4, §4.4.5, §2.1.7 regla 2, §2.6 (golden-master), §1.5-D12 (versionado/registry/firma como post-MVP); tickets HU-3-BE-02 (elimina el gate runtime recompute→`500`), HU-1-QA-01 (corpus golden-master), HU-3-QA-01 (resiliencia al drift); historia HU-3.

### Prompt 32 — Puntos 4 y 5 de la consultoría

**Pregunta del usuario:**

> El punto 4 lo veo asumible para una MVP. Para el 5, ¿qué podemos hacer para que la matemática quede más especificada? ¿No es en realidad parte del trabajo del matemático? La idea es proporcionarle herramientas para ayudarle, no sustituirle.

**Respuesta del asistente:** punto 4 (simulador en la JVM de la API degrada la demo) **aceptado como tradeoff de MVP**, sin acción. Punto 5 reencuadrado según el principio del usuario: **el RTP/volatilidad teóricos son un input declarado por el matemático (su PAR sheet), no un output calculado por la plataforma**. El usuario eligió 3 herramientas (sin la convolución exacta del base game):

| Herramienta | Cambio |
|---|---|
| RTP objetivo declarado + validación vs empírico | `game_configs.rtp_theoretical`/`volatility_theoretical` → **`rtp_target`/`volatility_target`** (declarados por el matemático). C5 valida empírico-vs-objetivo. El motor/plataforma **no** los calcula. |
| Intervalo de confianza + curva de convergencia | `simulation_runs` gana `rtp_std_error` (de suma y suma de cuadrados) y `convergence_sample` (JSONB); el simulador los computa y el dashboard los muestra; el test de convergencia usa el IC (no umbral fijo) → no *flaky* en alta volatilidad. |
| Descomposición del RTP + estadísticas de reel strips | `simulation_runs.rtp_breakdown` (JSONB): RTP base/free spins, contribución por símbolo/feature, frecuencias de símbolo, P(trigger), free spins esperados — cifras mecánicas que alimentan el modelo del matemático. |

Cambios aplicados en README §1.2 (C3/C5), §3.1.1 (ER), §3.2.6, §3.2.9, §3.3.3, §2.6 y §5 HU-2; tickets HU-7-BE-01/QA-01/FE-01 (RTP declarado, no calculado), HU-2-BE-01/BE-02/FE-01/QA-01 y HU-2-DEV-01 (SE, convergencia, descomposición; baseline empírica), HU-1-BE-01 (el motor ya no expone `theoreticalRtp()`); historias HU-2 y HU-7. **Principio rector documentado: la plataforma mide y descompone; el matemático declara y decide.**

---

## Fase 10 · Rol Documentalista — Dependencias y árboles

### Prompt 33 — Dependencias entre historias y entre tickets + árboles

**Pregunta del usuario:**

> Revisa la coherencia y añade tanto a historias como a tickets sus relaciones de dependencia. Solo las **directas** (si HU-1→HU-4 y HU-4→HU-10, en HU-1 pongo solo HU-4; HU-10 va en HU-4). Genera el **árbol de dependencias entre todas las historias** en `stories.md` y, simétricamente, lo mismo para los tickets de una historia.

**Respuesta del asistente:** ronda de 3 preguntas de modelado → decisiones: **aristas reales + reducción transitiva**, **Mermaid `flowchart`**, **árbol de tickets por historia con dependencias externas anotadas**.

**Hallazgo de coherencia (corregido):** existía un **ciclo** `HU-1-FE-01` ↔ `HU-5-FE-01` (el SlotGame dependía del lobby y el lobby del SlotGame). Roto: `HU-1-FE-01` depende de `HU-5-BE-01` (endpoint de `config`), no del lobby; `HU-5-FE-01` solo **navega** a `/play/{id}` (enrutado, no dependencia de construcción).

Acciones:
- **12 historias**: nueva sección `## Dependencias` con sus dependencias directas (HU-11 raíz; HU-4→HU-11; HU-5/6/7/12→HU-4; HU-1→HU-5; HU-2→HU-1+HU-7; HU-3/9/10→HU-1; HU-8→HU-2). Nota: infra/CI (`HU-1-DEV-01`) se trata como **fundación** a nivel de historia para evitar ciclos.
- **`stories/stories.md`**: sección "Árbol de dependencias entre historias" con `flowchart TD` Mermaid + tabla de aristas + orden de construcción topológico.
- **39 tickets**: línea **Dependencias directas** normalizada (intra/externa, reducción transitiva). Casos: idempotencia `HU-6-BE-01`→`HU-1-BE-02`; `HU-2-BE-02`→`HU-7-BE-01` (config a simular); QA reducidas al ticket que arrastra la cadena.
- **`tickets/tickets.md`**: sección "Árboles de dependencias por historia" con 12 sub-diagramas Mermaid (nodos externos con borde discontinuo).

---

## Fase 11 · Rol Agile/Documentalista — Tickets principales del README §6

### Prompt 34 — Selección de los 3 tickets y relleno del §6

**Pregunta del usuario:**

> Selecciona los 3 tickets de trabajo más relevantes y cuéntame el criterio. Si tienes dudas consúltame. Una vez lo acordemos rellenaremos el apartado 6 del readme.md.

**Respuesta del asistente:** se expuso el criterio (cubrir las 3 capas que pide el §6: backend/frontend/BBDD · centralidad · riqueza como muestra · riesgo representativo) y se acordó vía 2 preguntas:

- **Ticket de BBDD:** crear **`HU-1-DB-01`** (esquema + migraciones Flyway), extraído de `HU-1-DEV-01` (no existía ticket DB puro).
- **Ticket de backend:** **`HU-1-BE-01`** (motor / `SpinKernel`).
- **Ticket de frontend:** **`HU-1-FE-01`** (`<SlotGame>`).

Acciones:
- **Nuevo ticket `HU-1-DB-01`** (equipo `DB`, 5 SP): `V1` esquema (11 tablas, constraints, índices, GIN), `V2` triggers de inmutabilidad, `V3` seed; idempotencia Flyway. Fundación de datos.
- **Re-scope `HU-1-DEV-01`** a Docker/CI (las migraciones pasan a `HU-1-DB-01`); **SP 3 → 2**.
- **`HU-1-BE-02`**: añadida la dependencia `HU-1-DB-01` (persistencia).
- **README §6** relleno: criterio de selección + los **3 tickets completos** (todos los apartados) con enlace a su fichero canónico.
- **`tickets/tickets.md`**: tabla de HU-1 (+`HU-1-DB-01`, `HU-1-DEV-01` 3→2), totales **40 tickets · 129 SP**, nuevo equipo **DB** (1/5) y DevOps 5→4, nota de BBDD actualizada (ya existe el ticket) y sub-árbol de HU-1 con el nodo `DB-01`.
- A nivel de **historia** no cambia el árbol (HU-1-DB-01 es un ticket; la fundación de datos, como la infra, la presuponen los tickets de persistencia sin re-cablearse uno a uno).

---

## Fase 12 · Rol Arquitecto — Revisión de errores de definición

### Prompt 35 — Detectar y corregir errores de definición del proyecto

**Pregunta del usuario:**

> Actuando como arquitecto, haz una revisión completa del proyecto para detectar posibles **errores de definición**. No búsquedas capciosas: problemas que se puedan derivar de cómo está definido el proyecto para su implementación.

**Respuesta del asistente:** revisión read-only de readme/stories/tickets → **5 hallazgos** (descartando lo ya documentado como asunción: resolución de *tenant* en login, alcance del `Idempotency-Key`). Decisiones acordadas con el usuario: corregir **F1–F5**; F1 con **líneas fijas (todas activas)** y, en F5, los premios de scatter **sí** se soportan como **opción** del config e **independientes** de los free spins (matiz del usuario: "puede haber premios scatter que no disparen free spins").

| # | Sev. | Hallazgo | Corrección |
|---|---|---|---|
| **F1** | Alta | El multiplicador de la `paytable` se aplica "sobre la apuesta por línea", pero la API de spin recibe un único `betCents` total y no hay regla que ligue apuesta total ↔ nº de líneas ↔ apuesta por línea → no se puede calcular premio/RTP. | Regla en **§3.3.3**: todas las paylines activas, `lineBet = betCents / paylines.length`, `betCents % paylines.length == 0`, `min/step_bet_cents` múltiplos del nº de líneas. Notas en §3.3.1, §3.2.3 (games), §4.4.3/§4.4.4; `lineBetCents` en `SpinResult`; tickets `HU-1-BE-01` (fórmula de win) y `HU-1-BE-02` (`422`). |
| **F2** | Media | El contrato de `POST .../configs` no incluía `rtpTarget`/`volatilityTarget` (columnas NOT NULL) y prometía `rtpTheoretical`/"métricas teóricas"/"RTP calculado" — restos del modelo viejo que contradicen "la plataforma no calcula el RTP". | §4.2/§4.3: body con `rtpTarget`/`volatilityTarget`; respuestas "objetivo declarado" en vez de "teórica/calculada". `stories/HU-7.md` y `HU-7-BE-01` alineados. |
| **F3** | Baja | `publish` es post-MVP, así que una versión de matemática nueva nunca llega al jugador; no estaba dicho. | Nota en **§4.2** y `HU-7.md`: en MVP se **crea y simula** (sobre un `configId`) pero la **activación es post-MVP**; el jugador juega la `config` semilla. |
| **F4** | Baja | No estaba definido cuándo se crea el `wallet` de un jugador auto-registrado. | §3.2.3 y §4.4.1: el `wallet` se crea **en el registro**, misma transacción, `balance_cents = 0`. |
| **F5** | Baja | Ambigüedad sobre si los `SCATTER` pagan premio. | Campo **opcional** `scatterPays` (premio *anywhere* por recuento, sobre `betCents`), **independiente** de `bonus.freeSpins`; añadido a §3.3.1/§3.3.2/§3.3.3 y a `HU-1-BE-01`. |

**Verificado:** `rtpTheoretical`/"métricas teóricas"/"RTP calculado" → 0 (salvo este propio log); las afirmaciones "la plataforma no calcula el RTP" (§3.3.3, §5/HU-1-BE-01) siguen intactas y ya no se contradicen con el contrato de creación de config.

---

## Fase 13 · Rol Developer senior (gambling) — Revisión de dudas/ambigüedades

### Prompt 36 — Lectura crítica con ojos de developer que aterriza en el proyecto

**Pregunta del usuario:**

> Actúa como un developer senior con experiencia en gambling que aterriza en el proyecto sin saber nada de él. Léete la documentación y señala dudas, ambigüedades o posibles errores.

**Respuesta del asistente:** lectura completa de readme (§0–§4), historias y tickets → **11 hallazgos** (sin repetir F1–F5). El usuario eligió **decidir uno a uno los de motor/negocio (1–4)** y que documentara el resto con defaults. Decisiones:

| # | Tema | Decisión | Dónde |
|---|---|---|---|
| **H1** | Evaluación de línea con WILD | **Mejor premio por línea**: izq→der desde col 0, el wild maximiza, una línea de solo wilds paga el `REGULAR` top, el wild no tiene entrada propia. | §3.3.3 · HU-1-BE-01 (AC6b) · §6 |
| **H2** | Free spins | `multiplier` aplica a **todos** los premios de la ronda (línea + `scatterPays`); **retrigger ilimitado** (sin tope; motor/simulador toleran cascadas largas). | §3.3.1 · §3.3.3 · HU-1-BE-01 |
| **H3** | Recuento > clave mayor | **Validar cobertura sin huecos**: la validación calcula el máx. de apariciones posible por símbolo y exige declarar todos los recuentos alcanzables en `paytable`/`scatterPays`/`award`. | §3.3.3 · HU-7-BE-01 (AC3) |
| **H4** | Rodillo / RNG | **Tiras circulares** (wrap módulo `len`); una `nextInt(len)` por reel en orden de columna `0→cols-1`; bonus consume después. | §3.3.1 (reels) · §2.1.7 (regla 2) |
| **H5** | `winCents`/balance | Top-level del `SpinResult` = **total de la ronda** (incluye free spins); `freeSpins.rounds[]` desglosa; `win_cents` por fila es por giro. Reconcilia `post = pre - bet + win`. | SpinResult · §3.2.8 |
| **H6** | Replay con free spins | El endpoint **reconstruye** `freeSpins.rounds[]` desde las filas hijas (`triggering_round_id`); una fila hija se renderiza aislada. | §4.4.5 |
| **H7** | Concurrencia wallet | *Optimistic lock*: el caso de uso **reintenta** y, si persiste, `409` (modificación concurrente del saldo). | §4.4.3 · §4.1 · HU-1-BE-02 (AC8) |
| **H8** | Simulación asíncrona | Sin reanudación; runs en `RUNNING` huérfanas se marcan `FAILED` al arrancar tras un *timeout*. | §3.2.9 |
| **H9** | Juego responsable | Auto-spin/límites son **solo cliente** en MVP (server-side diferido, D7) — explicitado. | §2.5.5 |
| **H10** | Idempotencia | `UNIQUE (user_id, endpoint, idem_key)` (incluye `endpoint`) para que el mismo UUID en spin/recharge no colisione. | §3.2.11 · HU-1-DB-01 |
| **H11** | `result` vs columnas | `result` guarda solo lo visual/mecánico; importes en columnas; API y replay fusionan ambos. | §3.2.8 |

Nota: la opción elegida en H2 ("sin tope") no fijaba el alcance del multiplicador; se aplicó el default del sector (multiplica todos los premios de la ronda) dejándolo explícito por si se quiere cambiar.

---

## Fase 14 · Implementación del MVP

### Prompt 37 — Orden de implementación + inicio del desarrollo

**Pregunta del usuario:**

> Vamos a empezar con la implementación de la MVP. Basándote en la documentación, puedes listar de forma ordenada los tickets a ir implementando.

**Respuesta del asistente:** Se generó el fichero [`implementation.md`](implementation.md) con los 40 tickets ordenados en 8 fases según el grafo de dependencias de historias y las dependencias entre tickets. El camino crítico identificado: `HU-1-DEV-01 → HU-1-DB-01 → Auth → Lobby → HU-1-BE-01 (motor) → Simulador`.

---

### Prompt 38 — Ticket #1: HU-1-DEV-01 — Docker Compose + pipeline CI

**Pregunta del usuario:** `si` (proceder con el ticket 1).

**Respuesta del asistente:** Implementación completa del esqueleto del proyecto:
- `docker-compose.yml` (3 servicios: postgres→api→web, healthcheck, volumen pgdata)
- `.env.example`, `.gitignore`
- `.github/workflows/ci.yml` (job `build-test`: JDK21 + Node20 + pnpm, `mvn verify`, `pnpm test --run + build`, artefacto Jacoco)
- **Backend Maven multi-módulo** (`nova-common`, `nova-domain`, `nova-application`, `nova-infrastructure`, `nova-simulator`, `nova-web-api`) con POMs, `NovaCasinoApplication.java`, `application.yml` y smoke test
- **Frontend Vite/React** esqueleto: `package.json`, `vite.config.ts`, `tsconfig`, `index.html`, `App.tsx` + test, `nginx.conf`
- **E2E** esqueleto: `package.json`, `playwright.config.ts`
- `backend/Dockerfile` (multi-stage maven+jre-alpine, usuario no-root) y `frontend/Dockerfile`

**Errores corregidos durante el levantamiento:**
1. `App.test.tsx`: `test`/`expect` no encontrados → vitest 2.x usa Vite 5.x (conflicto con Vite 6.x del proyecto) → actualizar `vitest` y `@vitest/coverage-v8` a `^3.0.0`.
2. `vite.config.ts`: import de `vitest/config` en vez de `vite` para tipar la sección `test`.
3. `tsconfig.app.json`: excluir ficheros `*.test.*` del build de producción.
4. Postgres 18 cambió el mount a `/var/lib/postgresql` (sin `/data`). Corregido en `docker-compose.yml` + `docker compose down -v`.
5. Variables de entorno: el usuario necesitaba `cp .env.example .env` (Docker Compose lee `.env`, no `.env.example`).

**Resultado:** Stack levantado con los 3 servicios healthy; API arranca en 7.9s; Flyway conectado (sin migraciones aún); log en JSON estructurado (logstash).

---

### Prompt 39 — Ticket #2: HU-1-DB-01 — Esquema Flyway + seed

**Pregunta del usuario:** Avanzar con el ticket 2.

**Respuesta del asistente:** Implementación de las 3 migraciones Flyway y el cargador de seed dinámico:
- **`V1__schema.sql`**: 11 tablas (operators, users, wallets, wallet_transactions, games, game_configs, game_config_publications, game_rounds, simulation_runs, simulation_explanations, idempotency_keys) con todos los constraints (CHECK, FK, UNIQUE) e índices explícitos (39 en total, incluido GIN sobre `game_configs.config` y parcial sobre `game_rounds.triggering_round_id`). Ciclo `games ↔ game_configs` resuelto con ALTER TABLE tras crear ambas tablas.
- **`V2__immutability_triggers.sql`**: función `fn_forbid_update_delete()` + 4 triggers BEFORE UPDATE OR DELETE sobre tablas histórico-regulatorias.
- **`V3__seed.sql`**: operador `novacasino-default` + 3 juegos (Egipcio 5x3, Frutas 3x3, Espacial 5x3) con `active_config_id = NULL`.
- **`SeedDataLoader.java`** (Spring `ApplicationRunner`): inserta 5 usuarios con BCrypt cost 12 (no se pueden hardcodear hashes en SQL), 3 wallets (1.000 €), 3 `game_configs` con JSONs completos y activa las configs.

**Verificación en BD:** 11 tablas ✅, 39 índices ✅, 4 triggers (8 entradas UPDATE+DELETE) ✅, 145 constraints ✅, datos semilla correctos ✅, Flyway V1/V2/V3 `success=true` ✅.

---

### Prompt 40 — Ticket #3: HU-11-BE-01 — i18n en la API

**Pregunta del usuario:** Avanzar.

**Respuesta del asistente:** Configuración de internacionalización backend:
- `application.yml`: `spring.mvc.problem-details.enabled: true` + `spring.messages.basename: i18n/messages`.
- `i18n/messages.properties` (ES, defecto) + `i18n/messages_en.properties` (EN): 28 claves cubriendo todos los grupos de error (validación, auth, spin, idempotencia, simulación, editor matemático).
- `I18nConfig.java`: `AcceptHeaderLocaleResolver` con defecto `es` y soportados `[es, en]`.
- `GlobalExceptionHandler.java`: extiende `ResponseEntityExceptionHandler`, devuelve Problem Details RFC 9457 con mensajes i18n para 422 (validación), 400 (JSON malformado) y 500 (genérico).
- `GlobalExceptionHandlerTest.java`: 4 tests — AC1 (EN), AC2 (ES/defecto), AC3 (todas las claves en ambos bundles), AC4 (estructura HTTP invariante). **5 tests, BUILD SUCCESS**.

---

### Prompt 41 — Ticket #4: HU-4-BE-01 — Auth backend (JWT, register, login)

**Pregunta del usuario:** `si`.

**Respuesta del asistente:** Implementación completa del sistema de autenticación:
- `UserRole.java` (enum en `nova-domain`)
- `OperatorEntity`, `UserEntity`, `WalletEntity` + 3 repositorios JPA en `nova-infrastructure`
- `JwtService` (HS256, SHA-256 del secreto → 256 bits, soporta secretos cortos en dev)
- `NovaUserDetails`, `UserDetailsServiceImpl`, `JwtAuthFilter` (OncePerRequestFilter, no lanza 401 directamente)
- `SecurityConfig`: `SecurityFilterChain` stateless, rutas por rol, 401/403 como Problem Details RFC 9457
- `AuthService` (registro: age check ≥18 DGOJ + BCrypt + wallet 0€; login: verify credentials)
- `AuthController`: `POST /auth/register` (201) y `POST /auth/login` (200)
- Excepciones: `AgeVerificationException` (422), `EmailAlreadyRegisteredException` (409), `InvalidCredentialsException` (401)
- `NovaCasinoApplication` actualizado con `@EntityScan` + `@EnableJpaRepositories` para paquetes `com.novacasino.*`
- `AuthServiceTest`: 6 tests unitarios. **11 tests totales, BUILD SUCCESS**.

**Bug corregido:** Los campos `updated_at` de `UserEntity`, `WalletEntity` y `OperatorEntity` necesitaban inicialización + `@PrePersist`/`@PreUpdate`, de lo contrario Hibernate enviaba `null` violando NOT NULL (error detectado al registrar el primer usuario real: `jordi.poch@gmail.com`).

---

### Prompt 42 — Ticket #5: HU-4-FE-01 — Pantallas de registro y login

**Pregunta del usuario:** `si`.

**Respuesta del asistente:** Implementación del frontend de autenticación:
- `i18n/es.json` + `i18n/en.json` + `i18n/i18n.ts` (i18next con bundles ES/EN, fallback ES)
- `axiosClient.ts`: instancia Axios con interceptor de JWT (adjunta `Authorization: Bearer`) e interceptor de respuesta (401 con token → limpiar sesión + redirigir a `/login`; 401 sin token → dejar pasar para que el componente lo maneje)
- `authStore.ts` (Zustand con `persist` en localStorage): `token`, `user`, `setAuth`, `clearAuth`, `isAuthenticated`, `roleHomeRoute`
- `authApi.ts`: `register` y `login` sobre axiosClient
- `ProtectedRoute.tsx`: sin token → `/login`; rol incorrecto → home del rol
- `LoginPage.tsx`: validación cliente email/password, errores servidor 401/422, mensajes +18 y juego responsable (AC5)
- `RegisterPage.tsx`: validación cliente email/password/birthDate, errores 409/422, selector de idioma
- `auth.css`: estilos temáticos (fondo oscuro, card centrada)
- `App.tsx`: BrowserRouter + QueryClientProvider + rutas por rol con placeholders para HU-5..8
- `main.tsx`: import i18n antes de montar la app
- `authStore.test.ts`: 7 tests (estado inicial, setAuth, clearAuth, roleHomeRoute ×3)

---

### Prompt 43 — Ticket #6: HU-11-FE-01 — i18next, namespaces y conmutación de idioma

**Pregunta del usuario:** Avanzar (con interrupciones para actualizar `conversation.md`).

**Respuesta del asistente:** Sobre la base i18n creada en HU-4-FE-01, se completó la infraestructura de internacionalización:
- **Reestructuración a namespaces** `locales/<lang>/<ns>.json` con 4 namespaces por superficie: `shared`, `player`, `operator`, `math` (× es/en = 8 ficheros). Eliminados los `es.json`/`en.json` planos antiguos.
- `i18n.ts` reescrito: `defaultNS: 'shared'`, detección de idioma inicial (localStorage `nova-lang` → idioma del navegador → `es`) y **persistencia** vía listener `languageChanged` (AC1, AC3).
- `LanguageSwitcher.tsx` + CSS: selector ES/EN con **conmutación en caliente** sin recarga (AC2), integrado en login, registro y barra superior de los placeholders.
- `axiosClient.ts`: `Accept-Language` ahora se toma de `i18n.language` (refleja la conmutación, AC5).
- Simplificación: eliminado el dropdown `locale` redundante del registro; ahora se registra con el idioma activo de la UI.
- `App.tsx`: placeholders traducidos por namespace + logout.
- `tsconfig.app.json`: `resolveJsonModule: true`.
- `i18n.test.ts`: 7 tests (namespaces, conmutación, persistencia, **paridad de claves es/en** para AC4).

**Verificación:** ejecutado en contenedor `node:20-alpine` efímero con `pnpm@9`: **14 tests verdes** (authStore 6 + i18n 7 + App 1) y `pnpm build` correcto (tsc + vite).

**Bug corregido en CI:** el workflow usaba `pnpm test --run` (pnpm interpretaba `--run` como opción suya); corregido a `pnpm test -- --run`.

---

### Prompt 44 — Ticket #7: HU-4-QA-01 — Tests de autenticación y autorización

**Pregunta del usuario:** `si` (proceder con el ticket 7; recordatorio de mantener `conversation.md` al día).

**Respuesta del asistente:** Suite de tests de auth:
- **Unit** (`AuthServiceTest`, +2 tests = 8): límite exacto de edad (AC3) — nacido hoy hace 18 años exactos **pasa**; un día menos (cumple 18 mañana) → `422`.
- **Integración** (`src/it/java`, Failsafe + Testcontainers):
  - `AbstractIntegrationTest`: base con Postgres 18 real (Flyway + seed aplicados) vía Testcontainers; helper `loginAndGetToken`. Añadida una **vía opcional de BBDD externa** (`-Dit.jdbcUrl=...`) para depurar donde la JVM no puede abrir el socket de Docker.
  - `AuthFlowIT` (6 tests, AC1/AC4): `register` 201 (+ verifica que la respuesta no expone password/hash), 422 (menor), 409 (email duplicado), 400 (email inválido); `login` 200 (usuario semilla) y 401 (password incorrecta).
  - `AuthorizationMatrixIT` (4 tests, AC2): matriz rol×superficie — sin token → 401; rol incorrecto → 403; rol correcto → supera seguridad (404 al no existir aún el controller).

**Verificación de entorno:** Testcontainers no logra abrir el named pipe de Docker Desktop desde la JVM Windows (limitación local; en CI Linux funciona nativamente). Se validaron los IT **contra el Postgres del docker-compose** con `-Dit.jdbcUrl=jdbc:postgresql://localhost:5432/novacasino`: **13 unit + 10 IT = 23 tests verdes, BUILD SUCCESS**. (Efecto colateral: la corrida insertó usuarios `it-<uuid>@test.com` en la BBDD de desarrollo; `docker compose down -v` la resetea.)

---

### Prompt 45 — Ticket #8: HU-11-QA-01 — Tests de i18n (cierre de Fase 1)

**Pregunta del usuario:** `si`.

**Respuesta del asistente:** Tests de internacionalización:
- **Backend unit** `MessagesI18nParityTest` (2 tests, AC3): paridad de claves entre `messages.properties` y `messages_en.properties`; ningún valor en blanco.
- **Backend IT** `I18nApiIT` (3 tests, AC2): `Accept-Language: en` → `detail` "Invalid credentials."; `es` → "Credenciales incorrectas."; sin cabecera → español (default).
- **E2E** `e2e/tests/i18n.spec.ts` (Playwright, AC1): conmutación ES→EN sin recarga + persistencia tras reload + ausencia de claves i18n sin resolver en pantalla.
- El frontend ya cubría paridad/conmutación/persistencia en `i18n.test.ts` (HU-11-FE-01).

**Bug real corregido:** el `GlobalExceptionHandler` y los entrypoints 401/403 de `SecurityConfig` usaban `request.getLocale()`, que **sin `Accept-Language` devuelve el locale del servidor** (aquí EN) en vez del `es` configurado. Migrado a `LocaleContextHolder.getLocale()` (handler, dentro del dispatch) y a `localeResolver.resolveLocale(request)` (entrypoints de seguridad, antes del DispatcherServlet) — ambos respetan el default `es` del `AcceptHeaderLocaleResolver`. Test unitario adaptado a `LocaleContextHolder`.

**Verificación:** **15 unit + 13 IT = 28 tests verdes, BUILD SUCCESS** (IT contra el Postgres de compose).

> **Fin de la Fase 1** (Auth + i18n): tickets #1–#8 completados (HU-1-DEV-01, HU-1-DB-01, HU-11-BE-01, HU-4-BE-01, HU-4-FE-01, HU-11-FE-01, HU-4-QA-01, HU-11-QA-01).

---

## Fase 15 · Implementación por historias (orden estricto)

### Prompt 46 — Revisión del orden de tickets

**Pregunta del usuario:** "¿No debería ir primero HU-11 y luego HU-4?" — revisando la implementación.

**Respuesta del asistente:** Reconocido: a nivel de historia el grafo es HU-11 → HU-4. En la Fase 1 el backend sí respetó el orden (HU-11-BE-01 antes de HU-4-BE-01), pero en frontend se invirtió (HU-4-FE-01 antes de HU-11-FE-01), con un pequeño retrabajo (la base i18n creada en HU-4-FE-01 se reestructuró a namespaces en HU-11-FE-01). El usuario eligió **revisar `implementation.md`**: se **reescribió a orden estricto por historia** (orden topológico de stories.md), con fundaciones primero y marcas ✅/⬜. Se anotó el trade-off de que el orden estricto retrasa el motor `HU-1-BE-01` (mayor riesgo), con la opción "de-risk" de adelantarlo tras HU-5.

### Prompt 47 — Ticket HU-5-BE-01 — Endpoints de catálogo y wallet

**Pregunta del usuario:** Seguir con HU-5.

**Respuesta del asistente:** Endpoints de lectura del jugador:
- Entidades JPA de lectura `GameEntity` y `GameConfigEntity` (config JSONB mapeado con `@JdbcTypeCode(SqlTypes.JSON)`), repos `GameJpaRepository` (`findByActiveTrueOrderByIdAsc`, `findByIdAndActiveTrue`) y `GameConfigJpaRepository`.
- DTOs `GameSummaryDto` (id, name, theme, coverImageUrl, **grid**), `GameDetailDto` (+ minBet/maxBet/betStep + **config** completo), `WalletDto`.
- `PlayerCatalogService` (parsea el config con Jackson; extrae `grid` para el lobby) + `PlayerController` con `GET /player/games`, `GET /player/games/{id}`, `GET /player/wallet`.
- `GameNotFoundException` → handler **404** (i18n) en `GlobalExceptionHandler`.
- Tests: `PlayerCatalogServiceTest` (3 unit) + `PlayerCatalogIT` (6 IT: catálogo sin matemática, detalle con config, 404, saldo del token, 403 operador, 401 sin token).

**Verificación:** **18 unit + 19 IT = 37 tests verdes, BUILD SUCCESS** (IT contra el Postgres de compose).

### Prompt 48 — Ticket HU-5-FE-01 — Lobby y saldo

**Pregunta del usuario:** Continuar con la historia.

**Respuesta del asistente:** Lobby del jugador (React + TanStack Query):
- `shared/format/money.ts` — `formatMoney(cents, currency, locale)` con `Intl.NumberFormat`.
- `player/api/playerApi.ts` — tipos (`GameSummary`, `GameDetail`, `Wallet`) y hooks `useGames`/`useWallet`/`useGame`.
- `player/lobby/LobbyPage.tsx` + `lobby.css` — cabecera con saldo formateado + `LanguageSwitcher` + logout; grid responsivo de carátulas (nombre, temática traducida, `grid`); click → `navigate('/play/{id}')`.
- i18n: claves de lobby (loading/error/empty/play) y nombres de temática (`theme.EGYPTIAN/FRUITS/SPACE`) en `player.json` es/en.
- `App.tsx`: ruta `/` → `LobbyPage`; alineada la ruta de juego a `/play/:gameId` (el ticket usa esa ruta; antes era placeholder `/game/:id`).
- Tests: `money.test.ts` (4) + `LobbyPage.test.tsx` (3, hooks mockeados). Aserciones tolerantes al separador de miles (el ICU de node-alpine no lo aplica; CI con ICU completo sí).

**Verificación:** **21 tests frontend verdes + build de producción OK** (en contenedor `node:20-alpine` con pnpm 9). Fix menor: `App.test.tsx` pasó de `await import()` dinámico (timeout 5s en el primer arranque) a import estático.

### Prompt 49 — Ticket HU-5-QA-01 — Tests de lobby y saldo (cierra HU-5)

**Pregunta del usuario:** `si`.

**Respuesta del asistente:** Completada la cobertura de HU-5:
- **AC2** (IT): test `inactiveGame_notListedNorAccessible` en `PlayerCatalogIT` — inserta un juego `active=false` vía `JdbcTemplate`, verifica que no aparece en `GET /player/games` ni es accesible por detalle (404), y lo borra en `finally`.
- **AC3** (E2E): `e2e/tests/lobby.spec.ts` — login con jugador semilla → lobby con 3 cartas + saldo → click en carátula → navega a `/play/{id}` (la rejilla del juego la entrega HU-1-FE-01).
- AC1 (200/404/403) ya cubierto por `PlayerCatalogIT` (HU-5-BE-01) y `LobbyPage.test.tsx` (HU-5-FE-01).

**Verificación:** **18 unit + 20 IT = 38 tests verdes, BUILD SUCCESS** (`PlayerCatalogIT` = 7 tests).

> **HU-5 completada.** Progreso: fundaciones + HU-11 + HU-4 + HU-5 (30/129 SP). Siguiente historia: **HU-6** (gestión y recarga de jugadores).

---

## Fase 16 · HU-6 — Gestión y recarga de jugadores

### Prompt 50 — Ticket HU-6-BE-01 (+ fix build Docker) — Endpoints de jugadores y recarga idempotente

**Contexto previo:** el usuario hizo `docker compose down -v` y `up --build` falló al enviar `frontend/node_modules` (106 MB, generado en host por las validaciones) al contexto de Docker, con un fichero bloqueado. **Fix:** añadidos `frontend/.dockerignore` y `backend/.dockerignore` (excluyen `node_modules`/`target`).

**Decisión de orden (consultada):** HU-6-BE-01 necesita el mecanismo de idempotencia que el ticket atribuye a HU-1-BE-02 (aún no hecho). El usuario eligió **construir un componente de idempotencia reutilizable ahora**; HU-1-BE-02 lo reutilizará.

**Implementación:**
- **Idempotencia reutilizable**: `IdempotencyKeyEntity` (+repo), `IdempotencyService.execute(userId, endpoint, key, payload, type, supplier)` — hash SHA-256 del payload, guarda/reproduce la respuesta, todo en una transacción; `IdempotencyConflictException` → 409 (misma clave, payload distinto).
- `nova-domain`: enum `WalletTxType`. `nova-infra`: `WalletTransactionEntity` (factory `recharge(...)`, append-only) + repo; `IdempotencyKeyEntity` + repo; `UserJpaRepository.findByOperatorIdAndRoleAndEmailContainingIgnoreCase` (búsqueda paginada).
- DTOs: `PlayerSummaryDto`, `RechargeRequest`, `PageResponse<T>` (wrapper §4.1).
- `OperatorPlayerService` (búsqueda paginada con saldo; recarga: valida importe>0, +saldo con optimistic lock, inserta `RECHARGE` con `performed_by_user_id`) + `OperatorController` (`GET /operator/players`, `POST /operator/players/{id}/wallet/recharge` envuelta en idempotencia).
- Handlers en `GlobalExceptionHandler`: 404 (PlayerNotFound), 422 (InvalidAmount), 409 (IdempotencyConflict) + clave i18n `error.recharge.invalidAmount.detail` (es/en).
- Tests: `OperatorPlayerServiceTest` (4 unit) + `OperatorRechargeIT` (9 IT: listado, recarga, idempotencia same-key-once, conflicto 409, 422, 404, 400 sin clave, 403 player, 401 sin token).

**Fix de aislamiento de tests:** `PlayerCatalogIT.wallet` asumía saldo exacto 100000 de player1, pero `OperatorRechargeIT` lo muta; se relajó a "es número ≥ 0 + divisa EUR".

**Verificación:** stack reconstruido OK (valida el fix de `.dockerignore`), seed aplicado (5 usuarios/3 juegos/3 configs). **22 unit + 29 IT = 51 tests verdes, BUILD SUCCESS** (IT contra el Postgres de compose).

### Prompt 51 — Tickets HU-6-FE-01 y HU-6-QA-01 (cierran HU-6)

**Pregunta del usuario:** Hacer los 2 tickets restantes de HU-6.

**HU-6-FE-01 (frontend operador):**
- `operator/api/operatorApi.ts` — `usePlayers(email, page)` y `useRecharge()` (mutación con cabecera `Idempotency-Key`; invalida el listado al completar).
- `operator/players/OperatorPlayersPage.tsx` — buscador por email, tabla paginada con saldo formateado, botón Recargar por fila; `LanguageSwitcher` + logout.
- `operator/players/RechargeDialog.tsx` — diálogo con validación cliente (importe>0); **Idempotency-Key generada una vez al abrir** (anti doble-click, AC4) + botón deshabilitado mientras está en curso; al confirmar refresca el saldo.
- i18n `operator.json` (es/en) con players/recharge; ruta `/operator` → `OperatorPlayersPage` en `App.tsx`.
- Test `OperatorPlayersPage.test.tsx` (3): tabla con saldo, validación importe>0 sin llamar al server, recarga válida invoca mutación con Idempotency-Key.

**HU-6-QA-01 (tests):** el grueso ya estaba en `OperatorRechargeIT`; añadidos: **AC3** `recharge_recordsLedgerRowWithOperator` (verifica vía JdbcTemplate la fila `RECHARGE` con `performed_by_user_id` correcto y `game_round_id` null) y **AC1** `listPlayers_isPaginated` (page/size/totalElements/totalPages).

**Verificación:** backend **22 unit + 31 IT verdes, BUILD SUCCESS** (`OperatorRechargeIT` = 11); frontend **24 tests + build OK** (`crypto.randomUUID` disponible en el entorno de test).

> **HU-6 completada.** Progreso: fundaciones + HU-11 + HU-4 + HU-5 + HU-6 = **38/129 SP** (~29%). Siguiente historia: **HU-7** (edición y versionado de matemática).

---

## Fase 17 · HU-7 — Edición y versionado de matemática

### Prompt 52 — Ticket HU-7-BE-01 — Endpoints de edición/versionado + validación

**Pregunta del usuario:** `si` (continuar con HU-7).

**Implementación:**
- `GameConfigEntity` ampliada para inserción (rtpTarget, volatilityTarget, createdByUserId, notes) con constructor; repos: `GameConfigJpaRepository.findTopByGameIdOrderByVersionDesc` (siguiente versión), `GameJpaRepository.findByOperatorIdOrderByIdAsc`.
- `ConfigValidator` + `ConfigValidationException` (con `FieldError`): valida invariantes §3.3.3 — símbolos existentes, dimensiones de reels/paylines, kind correcto (paytable=REGULAR, scatterPays/triggerSymbol=SCATTER) y **cobertura de pagos sin huecos** (recuentos contiguos). Acumula todos los errores.
- DTOs: `MathGameDto`, `ConfigDetailDto`, `CreateConfigRequest`, `ConfigCreatedDto`.
- `MathService` (listGames con versión activa; getConfig; createConfig: valida + rtpTarget∈[0,1] + versión N+1 + inserta; **no publica** —active_config_id intacto, F3) + `MathController` (`GET /math/games`, `GET /math/configs/{id}`, `POST /math/games/{id}/configs`).
- Handlers: `ConfigNotFoundException`→404; `ConfigValidationException`→**422 con `errors[]`** por campo.
- Tests: `ConfigValidatorTest` (6 unit: válido, símbolo inexistente, payline fuera de rango, triggerSymbol no SCATTER, hueco de pago, símbolo de reel inexistente) + `MathConfigIT` (7: games con versión activa, crear versión N+1 con rtpTarget, versión previa inalterada, detalle+404, 422 con errors[], rtp fuera de rango 422, 403 operador/401 sin token).

**Verificación:** **28 unit + 38 IT = 66 tests verdes, BUILD SUCCESS** (IT contra el Postgres de compose).

### Prompt 53 — Tickets HU-7-FE-01 y HU-7-QA-01 (cierran HU-7)

**Pregunta del usuario:** Hacer los 2 tickets restantes de HU-7.

**HU-7-QA-01 (completar sobre lo ya hecho):** añadidos a `MathConfigIT` — `createConfig_persistsDeclaredRtpUnchanged` (AC2: el `rtp_target` declarado se relee igual, 0.9123) y `gameConfigs_areImmutable` (AC3: UPDATE/DELETE sobre `game_configs` lanzan `DataAccessException` por el trigger). El resto de invariantes ya estaba en `ConfigValidatorTest` (6) y `MathConfigIT`.

**HU-7-FE-01 (editor de matemática):**
- `math/api/mathApi.ts` — `useMathGames`, `useConfig(id)` (enabled si id≠null), `useCreateConfig` (invalida games al crear).
- `math/editor/MathEditorPage.tsx` + `math.css` — selector de juego, editor JSON en `<textarea>` (poblado con la config activa vía useEffect), inputs de `rtpTarget`/`volatilityTarget`/`notes`, validación cliente (JSON.parse + rango RTP), botón guardar; muestra `errors[]` del backend (422) en lista y mensaje de éxito con la nueva versión.
- i18n namespace `math.editor.*` (es/en); ruta `/math` → `MathEditorPage`.
- Test `MathEditorPage.test.tsx` (3): carga config activa al seleccionar juego (AC1), JSON inválido → error cliente sin llamar al backend (AC2), config válido → invoca crear versión con rtp declarado (AC4). Referencias estables en el mock para evitar bucle de useEffect.

**Verificación:** backend **28 unit + 40 IT verdes** (`MathConfigIT` = 9); frontend **27 tests + build OK**.

> **HU-7 completada.** Progreso: + HU-7 = **48/129 SP** (~37%). Siguiente historia: **HU-12** (juego responsable y sello DGOJ).

---

## Fase 18 · Calidad de código — buenas prácticas (transversal)

### Prompt 54 — Retrofit de estilo: `final`, comentarios y todo en inglés

**Pregunta del usuario:** Revisar todo el código con buenas prácticas: usar `final` donde proceda (campos + parámetros + locales), comentar las funciones (Javadoc/JSDoc) y que **todos los comentarios estén en inglés** (había varios en castellano). Aplicarlo de aquí en adelante.

**Decisiones (consultadas):** retrofit **de una vez** y `final` en **campos + parámetros + locales**.

**Memoria:** guardada la convención en `code-style-final-and-comments.md` (feedback) + índice en MEMORY.md.

**Retrofit aplicado (todo el código):**
- **Backend (producción):** `final` en campos inyectados, parámetros y locales no reasignados; Javadoc de clase/método en inglés; traducidos todos los comentarios y mensajes de excepción a inglés. Cubre `nova-domain`, `nova-infrastructure` (7 entidades + 7 repos), `nova-web-api` (security, config, seed, auth, player, operator, math, idempotency, common, `GlobalExceptionHandler`). Nota: campos de entidades JPA no pueden ser `final` (los puebla Hibernate por reflexión); se documenta.
- **Frontend (producción):** comentarios y cabeceras JSDoc en inglés en `shared` (axiosClient, authStore, authApi, ProtectedRoute, i18n, LanguageSwitcher, money, main), `player`, `operator`, `math` y `App.tsx`.
- **Tests (Java + TS + e2e):** traducidos comentarios y descripciones `it(...)` a inglés; `final` en los unit Java. Se mantienen en español las **aserciones sobre la UI/mensajes i18n renderizados** (p. ej. `getByText('Iniciar sesión')`, `isEqualTo("Se ha producido un error...")`), que validan el valor real del bundle ES.

**Verificación:** **28 unit + 40 IT** verdes (BUILD SUCCESS) y **frontend 27 tests + build** OK. Grep final: sin comentarios en castellano en `.java`; el único acento restante es una aserción i18n legítima.

---

## Fase 19 · HU-12 — Juego responsable y sello DGOJ

### Prompt 55 — Tickets HU-12-FE-01 y HU-12-QA-01 (cierran HU-12)

**Pregunta del usuario:** Seguir con HU-12.

**Implementación (capa de cumplimiento, frontend):**
- `shared/compliance/sessionGuardStore.ts` — store Zustand que acumula la pérdida neta de sesión (`recordRound(bet,win)`, nunca < 0) + umbral `LOSS_THRESHOLD_CENTS` (500 €) e `isLossThresholdExceeded`. Lo alimentará el spin (HU-1/HU-9).
- `ComplianceBanner.tsx` — banner permanente: sello DGOJ + "+18" + enlace "Juego responsable" (abre diálogo) + aviso de pausa cuando se supera el umbral. `ResponsibleGamingDialog.tsx` + `compliance.css`.
- i18n `compliance.*` (es/en) en el namespace `shared`.
- Integrado en login, registro, lobby y la ruta de juego; se consolidó el aviso inline duplicado del login/registro en el banner.
- Tests: `sessionGuardStore.test.ts` (4: acumulación, no negativo, umbral) + `ComplianceBanner.test.tsx` (3: sello/+18/enlace AC1, abre diálogo AC2, aviso de pausa al superar umbral AC3) + `e2e/compliance.spec.ts` (sello/+18 + diálogo en login). AC3 completo en E2E depende del juego (HU-1/HU-9).

**Verificación:** **frontend 34 tests + build OK** (27 → 34: +7).

> **HU-12 completada.** Progreso: + HU-12 = **52/129 SP** (~40%). Siguiente y plato fuerte: **HU-1** (motor de juego) — el resto de HU-1: `HU-1-BE-01` (SpinKernel), `HU-1-BE-02` (spin+idempotencia), `HU-1-FE-01` (`<SlotGame>`), `HU-1-QA-01`.

---

## Fase 20 · HU-1 — El jugador realiza un giro (motor)

### Prompt 56 — Ticket HU-1-BE-01 (motor de juego data-oriented)

**Pregunta del usuario:** Empezar con HU-1 (el motor).

**Implementación (`nova-domain`, Java puro, sin Spring/JPA — §2.1.7):** núcleo *data-oriented* + doble materialización.
- **`GameConfigSpec`** (árbol de records JDK): representación neutral del `config` (§3.3). La frontera de parseo JSON→spec vive fuera de domain (lo hará `HU-1-BE-02`), así `nova-domain` no toca Jackson.
- **`GameCompiler` → `CompiledGame`**: compila el spec a primitivos (símbolos a IDs `int` densos, `reels`/`paylines` `int[][]`, paytable/scatter `long[][]`, free spins a primitivos) y **cachea por `configId`** (`ConcurrentHashMap`). Precalcula `bestRegularByCount` (para la línea de solo WILD). Inmutable.
- **`RngEngine`** (puerto): `nextInt(bound)`, orden de consumo fijo. **`RoundSink`** (puerto Visitor): `onSpin(window, …)` con buffers reutilizados.
- **`SpinKernel`**: resuelve giro base + cascada de free spins (retrigger ilimitado) con **solo primitivos**, **aritmética entera** y **cero asignaciones por giro** (3 buffers reutilizados: `window`, `winningLines`, `symbolCounts`). Ventana column-major (`col*rows+row`), rodillos circulares (wrap módulo `len`), una `nextInt` por reel en orden de columna. Premio = `mult×lineBet` por línea (mejor combo, WILD maximiza) + `mult×betCents` por scatter *anywhere*; multiplicador de free spins aplica a todos.

**Tests (11 verdes):** `GameCompilerTest` (2: compila a primitivos AC1, caché por configId AC7) + `SpinKernelTest` (9: premio sobre lineBet AC2, WILD extiende / línea full-WILD paga mejor regular AC6b, scatter *anywhere* independiente de free spins AC6, multiplicador de free spins, retrigger ilimitado, **determinismo bit a bit con misma semilla** AC3, guarda `betCents % paylines`, **cero-alloc en steady state** AC4 vía `ThreadMXBean.getThreadAllocatedBytes`). Aritmética entera AC5 garantizada por construcción (sin `double`).

> **Hallazgo:** el `config` "Egipcio" del readme (§3.3.2) es **ilustrativo, no balanceado**: su frecuencia de scatter + award hacen que el retrigger **diverja** (deriva positiva → cascada infinita). El motor debe tolerarlo (AC6b), pero como fixture de test provocaba OOM; el test de determinismo usa una variante acotada (`retrigger=false`). El balanceo (que el RTP/cascada converjan) es responsabilidad del **matemático** (§3.3.3, HU-7), no del motor.

> **HU-1-BE-01 completado.** Progreso **60/129 SP** (~47%). Siguiente: `HU-1-BE-02` (`SpinUseCase` + idempotencia + endpoint `POST /spin` + `MaterializingSink` + adaptador `SecureRandom` del `RngEngine`).

### Prompt 57 — Ticket HU-1-BE-02 (SpinUseCase, idempotencia y endpoint `POST /spin`)

**Pregunta del usuario:** Continuar con `HU-1-BE-02`.

**Implementación:**
- **Motor enriquecido:** el `RoundSink.onSpin` ahora emite el desglose por línea (buffers paralelos `winningLineSymbols/Counts/Wins`) para construir `winningPaylines[]` del `SpinResult` sin recalcular en la capa de materialización (fidelidad). `CompiledGame` precalcula `bestRegularSymbolByCount` y expone `freeSpinsAwardedFor(count)`.
- **RNG:** `RngFactory` (puerto en domain) + adaptador `JavaRandomRngFactory` (infra) sobre `java.util.Random` (LCG reproducible cross-platform → habilita replay). La semilla por ronda la genera `SecureRandom` en el servicio y se persiste en `game_rounds.rng_seed`.
- **Persistencia:** `GameRoundEntity` (+repo) append-only con `result` JSONB (`view/winningPaylines/scatterCount/multiplier`); free spins como filas hijas (`triggering_round_id`). Factorías `bet()`/`win()` en `WalletTransactionEntity` (BET negativo, WIN positivo).
- **`GameConfigMapper`** (web-api): frontera JSON→`GameConfigSpec` (domain no ve Jackson). **`MaterializingSink`**: contraparte de producción del `CountingSink`, copia cada giro a objetos de dominio. **`EngineConfig`**: `GameCompiler` como `@Bean` singleton (caché compartida).
- **`SpinService`:** carga wallet (optimistic lock), valida apuesta (rango/step/múltiplo de líneas → 422; saldo insuficiente → 422), corre `SpinKernel` vía `MaterializingSink`, débito+crédito, persiste rounds + `wallet_transactions` (BET + WIN si premio) atómicamente. **Idempotencia** vía `IdempotencyService` (bean separado y transaccional). **Reintento por optimistic lock** (3 intentos) → `ConcurrentSpinException` 409. Endpoint `POST /player/games/{gameId}/spin` con `Idempotency-Key` (UUID inválido/ausente → 400 vía nuevo handler `MethodArgumentTypeMismatchException`).

**Tests:** `SpinServiceTest` (5 unit con mocks: balance/ledger AC2, apuesta fuera de rango/step AC4, saldo insuficiente, reintento→409 AC8) + **`SpinIT` (6, contra Postgres real)**: giro resuelto + reconciliación de saldo AC1/AC2, replay idempotente AC6, key ausente/ inválida → 400 AC5, apuesta inválida → 422 AC4. Totales: **domain 11 + web-api unit 33 + IT (SpinIT 6)**.

> **Hallazgo (seguridad de demo):** los configs semilla Egipcio/Espacial traían `retrigger:true` con awards generosos → riesgo de **cascada divergente** que colgaría un giro real. El motor debe tolerar cascadas largas (AC6b), pero el config divergente es un fallo del *balanceo* (responsabilidad del matemático). Para que el MVP sea demoable sin cuelgues, dejé los seeds con `retrigger:false` (acotado), documentado en `SeedDataLoader`. *(Requiere re-seed de BBDD fresca para aplicar; la capacidad de retrigger ilimitado del motor queda intacta y testada.)*

> **HU-1-BE-02 completado.** Progreso **65/129 SP** (~50%). Siguiente: `HU-1-FE-01` (componente `<SlotGame>` data-driven + animación de giro + free spins).

### Prompt 58 — Ticket HU-1-FE-01 (componente `<SlotGame>` data-driven + spin)

**Pregunta del usuario:** Continuar con `HU-1-FE-01`.

**Implementación (frontend):**
- **`playerApi`**: tipos del config (`GameConfig`, `ConfigSymbol`) y del `SpinResult` (con `freeSpins.rounds` anidados); llamada `spin(gameId, betCents, currency, idemKey)` con cabecera `Idempotency-Key` (UUID de `crypto.randomUUID()`).
- **`<SlotGame>`** (`player/game/SlotGame.tsx`): componente **data-driven y agnóstico al juego** (AC5) — renderiza la rejilla `cols×rows` desde el `config`, símbolos desde `/assets/<theme>/` con fallback a texto, resalta las celdas de las `winningPaylines` y reproduce la **cinemática de free spins** (anima cada `rounds[i]` secuencialmente con token de cancelación). Selector de apuesta (±step en rango) y botón **Spin**. Prop `mode="replay"` oculta los controles para reutilización en HU-3 (AC6). `stepMs` para tests deterministas.
- **`GamePage`** (`/play/:gameId`): carga juego + wallet, gestiona apuesta (init = `minBet`), mutación de spin (TanStack Query) que actualiza saldo (invalidación de `['player','wallet']`) y alimenta `sessionGuardStore.recordRound` (integración HU-12). Error `422` → mensaje "saldo insuficiente / apuesta no válida" **sin alterar la rejilla** (AC4). Reemplaza el `Placeholder` de la ruta en `App.tsx`.
- i18n: claves `player:game.*` (es/en). CSS propio (`slotGame.css`, `gamePage.css`).

**Tests:** `SlotGame.test.tsx` (7: rejilla desde `view` AC1, click→`onSpinClick` AC2, resaltado de paylines, cinemática de free spins AC3, error sin alterar rejilla AC4, agnóstico 3x3/5x3 AC5, replay oculta controles AC6). **Frontend: 41 tests verdes (10 ficheros) + build (tsc + Vite) OK.**

> **HU-1-FE-01 completado.** Progreso **73/129 SP** (~57%). Con esto el jugador ya puede **jugar de principio a fin** (lobby → giro → saldo). Siguiente y cierre de HU-1: `HU-1-QA-01` (suite de tests del flujo de juego: golden-master del motor, integración del flujo completo, e2e).

### Prompt 59 — Ticket HU-1-QA-01 (suite de tests del flujo de juego) · cierra HU-1

**Pregunta del usuario:** Continuar con `HU-1-QA-01`.

**Implementación (pirámide de tests del flujo del spin):**
- **ArchUnit** (`DomainPurityArchTest`, 2): ninguna clase de `nova-domain` depende de `org.springframework..` ni `jakarta.persistence..` → falla el build si se acopla Spring/JPA (AC3, cierra también AC8 de HU-1-BE-01).
- **Property-based jqwik** (`EngineRtpPropertyTest`, 15 tries): configs sintéticos 3x3 de **RTP conocido por construcción** (`RTP = pA³·mA + pB³·mB`); el RTP empírico tras 300k giros converge dentro de tolerancia (≫ error estándar). Verifica el motor, no el RTP objetivo de juegos reales (AC2).
- **Golden-master** (`GoldenMasterTest` + corpus versionado `src/test/resources/golden/golden-master.properties`): 4 fixtures `(config, seed, bet)` reducidos a SHA-256 de la secuencia canónica de giros; el motor debe reproducirlos **bit a bit** o el build falla (tripwire del determinismo, AC5/AC6). Regenerable con `-Dgolden.generate=true`.
- **Integration** (`SpinIT` ampliado a 9, contra Postgres real): además del flujo y la idempotencia ya cubiertos, ahora valida que el giro **persiste `game_rounds` + movimiento BET** (vía JDBC), que `game_rounds` es **inmutable** (UPDATE rechazado por el trigger), y que **misma key + payload distinto → 409**.
- **E2E Playwright** (`e2e/tests/spin.spec.ts`): happy path login → abrir juego (Frutas, sin free spins) → Spin → la ronda resuelve sin error (job `e2e` de CI, AC4).

**Verificación:** **nova-domain 15** (kernel 9, compiler 2, ArchUnit 2, golden 1, jqwik 1) + **web-api unit 33** + **SpinIT 9** (Postgres real) — todo verde. *Cobertura Jacoco (AC1 ≥90%): el motor está cubierto de forma exhaustiva por construcción; el reporte se genera en el job `build-test` de CI.*

> **HU-1 COMPLETADA al 100%** (motor + spin + endpoint + UI + QA). Progreso **78/129 SP (~60%)**. El núcleo del producto está cerrado y probado de extremo a extremo. Siguiente historia (orden topológico): **HU-2 — el matemático valida un juego con el simulador** (módulo `nova-simulator` que reutiliza el mismo `SpinKernel` vía `CountingSink`).

---

## Fase 21 · HU-2 — El matemático valida un juego con el simulador

### Prompt 60 — Ticket HU-2-BE-01 (módulo `nova-simulator`)

**Pregunta del usuario:** Continuar con `HU-2-BE-01`.

**Implementación (`nova-simulator`, Java puro, Map-Reduce):**
- **`CountingSink`** (contraparte del `MaterializingSink`): `RoundSink` **round-aware** que agrega y **descarta** — no copia ni retiene los buffers del kernel (**cero-alloc por giro**, AC3). El kernel emite base + free spins seguidos en el mismo hilo, así que acumula por **ronda** (cierra en el siguiente giro base) → métricas a nivel de ronda. Uno por worker, todo primitivos + 2 arrays preasignados.
- **`SimulationAccumulator`**: combina los `CountingSink` de los workers con `LongAdder`/`DoubleAdder` (lock-free, AC4) + CAS para máximos.
- **`SimulationRunner`**: ejecuta el **mismo `SpinKernel`** sobre el `CompiledGame` (inmutable, compartido) repartiendo `numSpins` en un `ForkJoinPool` (try-with-resources); **cada worker con su kernel, RNG y sink**. **No abre transacciones ni toca wallet/rounds/DB** (AC2). Devuelve `SimulationResult` (serializable a JSONB).
- **Métricas (herramientas del matemático, C3):** RTP + **error estándar** (Σr, Σr²), RTP base vs free spins, **contribución por símbolo**, hit frequency, volatilidad, max win, *longest dry streak*, **histograma** de premios, **curva de convergencia** (muestreo geométrico single-thread, capado a 200k) y **frecuencias de símbolo por reel**. La plataforma **mide; no declara `rtp_target`** (AC2).

**Tests (`SimulationRunnerTest`, 4):** RTP conocido por construcción (`pA³·mA+pB³·mB`=.75) converge + todas las métricas/desglose/histograma/convergencia/reel-stats (AC1/AC1b); **agregación paralela correcta** (numSpins exacto, mismo seed→mismo resultado, AC4); contribución y trigger de free spins; validación de argumentos. **Verde.** El motor procesa 200k giros con métricas en ~0.5s → AC5 (10M<10min) trivial; el job `perf` dedicado es HU-2-DEV-01.

> **HU-2-BE-01 completado.** Progreso **83/129 SP (~64%)**. Siguiente: `HU-2-BE-02` (API asíncrona de simulaciones: `202 Accepted` + estado `RUNNING`→`COMPLETED` + polling, persistiendo en `simulation_runs`).

### Prompt 61 — Ticket HU-2-BE-02 (API de simulaciones asíncrona)

**Pregunta del usuario:** Continuar con `HU-2-BE-02`. *(Los endpoints `GET /math/games`, `GET /math/configs/{id}`, `POST /math/games/{gameId}/configs` ya existían de HU-7; este ticket añade solo la API de simulaciones.)*

**Implementación:**
- **Persistencia:** `SimulationRunEntity` (+repo) mapea `simulation_runs` (§3.2.9): métricas `NUMERIC` nullables, JSONB (`prize_distribution`, `convergence_sample`, `rtp_breakdown`), `status` RUNNING/COMPLETED/FAILED.
- **Async:** `@EnableAsync` + `SimulationRunner` como `@Bean` (usa el adaptador `JavaRandomRngFactory`). **`SimulationExecutor`** (`@Async`, bean separado para que el proxy aplique): compila el config, corre el `SimulationRunner` fuera del hilo de petición y persiste métricas → `COMPLETED` (o `FAILED` + `error_message`).
- **`SimulationService`** (no transaccional: la fila RUNNING se commitea antes de que el worker async la lea): valida `numSpins∈[1,10M]` (→422) y `betCents` múltiplo de paylines (→422), comprueba pertenencia del config al operador (→404), inserta RUNNING y dispara el executor → responde `202` con `simulationId`, `status`, `startedAt`, `pollUrl`. `getSimulation` devuelve estado + métricas (JSONB parseado a `JsonNode`).
- Endpoints en `MathController`: `POST /math/configs/{configId}/simulations` (202) y `GET /math/simulations/{id}` (polling). Excepciones nuevas mapeadas: `InvalidSimulationParamsException`→422, `SimulationNotFoundException`→404.

**Tests:** `SimulationServiceTest` (4 unit: rango de spins AC2, config inexistente 404, launch dispara executor + 202/pollUrl, lookup 404) + **`SimulationIT` (3, Postgres real): lanzar→RUNNING→polling→COMPLETED con métricas (AC1/AC3/AC4), numSpins fuera de rango→422 (AC2), rol PLAYER→403 (AC6)**. Totales backend: **domain 15 · simulator 4 · web-api unit 37 · IT (SpinIT 9 + SimulationIT 3 + previos)**.

> **HU-2-BE-02 completado.** Progreso **86/129 SP (~67%)**. Siguiente: `HU-2-FE-01` (backoffice matemático: lanzar simulación, ver convergencia del RTP y descomposición en un dashboard).

### Prompt 62 — Ticket HU-2-FE-01 (backoffice matemático: simulación + dashboard)

**Pregunta del usuario:** Continuar con `HU-2-FE-01`. *(El editor de config/selector ya venía de HU-7-FE-01; este ticket añade el panel de simulación + polling + dashboard.)*

**Implementación (frontend):**
- **`mathApi`**: tipos `SimulationAccepted`/`SimulationStatus` + `useLaunchSimulation` (POST) y **`useSimulation(id)`** con **polling de TanStack Query** (`refetchInterval` 5 s mientras `RUNNING`, *stop* al `COMPLETED`/`FAILED`).
- **`SimulationPanel`** (`math/sim/`): campos `numSpins` (def. 10M) + `betCents` + botón **Simular**; al lanzar arranca el polling. Indicador `RUNNING` con spinner (AC3). **Dashboard** al `COMPLETED`: RTP con **IC 95%** (`±1.96·stdError`) y **comparación con `rtp_target`** (verde si el objetivo cae dentro del IC / ámbar / rojo); rejilla de métricas (RTP base/free, hit freq, volatilidad, max win, trigger freq, racha seca, duración); **curva de convergencia** (SVG line dependency-free); **contribución por símbolo** y **histograma** (barras); **estadísticas de rodillos** (frecuencias derivadas del config en cliente). 422 → mensaje de parámetros inválidos sin romper la UI.
- Integrado bajo el editor en `MathEditorPage` cuando hay config activa. i18n `math:sim.*` (es/en). Gráficos con SVG/CSS para no añadir dependencia (recharts) y evitar reinstalar `node_modules`.

**Tests:** `SimulationPanel.test.tsx` (3: lanzar→`mutate`, indicador RUNNING AC3, dashboard con RTP%/IC/veredicto verde AC4) + mock de los nuevos hooks en `MathEditorPage.test`. **Frontend: 44 tests verdes (11 ficheros) + build OK.**

> **HU-2-FE-01 completado.** Progreso **91/129 SP (~71%)**. El matemático ya puede **editar, simular y validar** un juego de principio a fin. Siguiente y cierre de HU-2: `HU-2-QA-01` (tests del simulador y convergencia del RTP) + `HU-2-DEV-01` (job `perf` 10M<10min en CI).

### Prompt 63 — Ticket HU-2-QA-01 (tests del simulador y convergencia)

**Pregunta del usuario:** Continuar con `HU-2-QA-01`.

**Implementación (pirámide de tests del simulador):**
- **`SimulationAccumulatorTest` (AC4):** merge concurrente (8 hilos) del mismo conjunto de `CountingSink` == merge secuencial (sumas `LongAdder` exactas; `DoubleAdder` con tolerancia FP; máximos por CAS); + test del **error estándar** contra un cálculo de referencia (stdev/√n). Verifica la agregación sin corrupción y la varianza.
- **`SimulatorRtpPropertyTest` (jqwik, AC2/AC5):** a través del `SimulationRunner` completo, configs sintéticos de RTP conocido → el RTP empírico cae **dentro del IC** (tolerancia = `5·rtpStdError`, no umbral fijo → estable en alta volatilidad). Un bug de premios desviaría el RTP varios errores estándar y fallaría.
- **`SimulatorConvergenceTest` (AC2/AC5):** 1M giros sobre config de RTP conocido (0.75) → dentro del IC; además la `convergenceSample` se **estabiliza monótonamente** (desviación del tercio final < tercio inicial).
- **`SimulationPerfTest` (`@Tag("perf")`, AC3):** 10M giros de un 5x3 con wild + free spins acotadas → **3.5 s** (≪ SLA 10 min). Excluido del build normal vía propiedad `excludedGroups=perf`; el job dedicado lo corre con `-Dgroups=perf -DexcludedGroups=` (HU-2-DEV-01).
- **`SimulationIT`** reforzado: el polling a COMPLETED valida también `rtpStdError`, `convergenceSample` (array) y `rtpBreakdown` (objeto) poblados.

**Verificación:** **nova-simulator 8** (normal, perf excluido) + perf 1 (10M en 3.5 s) + **SimulationIT 3** (Postgres) — todo verde. *(Cobertura AC1 ≥80%: cubierta por construcción; reporte en CI.)*

> **HU-2-QA-01 completado.** Progreso **94/129 SP (~73%)**. Siguiente y cierre de HU-2: `HU-2-DEV-01` (añadir el job `perf` al pipeline CI que ejecuta el test de 10M giros con SLA).

### Prompt 64 — Ticket HU-2-DEV-01 (job `perf` en CI) · cierra HU-2

**Pregunta del usuario:** Continuar con `HU-2-DEV-01`.

**Implementación:**
- **Test `perf` reforzado** (`SimulationPerfTest`): además del presupuesto de tiempo (<10 min), ahora es un **guard de regresión funcional**. Para ser reproducible en cualquier runner (independiente del nº de cores), usa **paralelismo fijo (4) y semilla fija** → métricas bit-idénticas; compara RTP/hit/volatilidad contra una **baseline versionada** (`src/test/resources/perf/baseline.properties`, regenerable con `-Dperf.generate=true`). Escribe `SimulationResult.json` + `duration_ms.txt` en `target/perf/` como artefactos.
- **Job `perf` en `.github/workflows/ci.yml`**: corre tras `build-test` (`needs`), JDK 21 con caché Maven, ejecuta `mvn -pl nova-simulator -am test -Dgroups=perf -DexcludedGroups=` y publica los artefactos. Falla (bloquea merge) si se supera el SLA **o** si las métricas se desvían de la baseline.

**Verificación local:** 10M giros en **4.6 s**; segunda corrida reproduce las métricas exactas (RTP 7.764…, hit 0.564…, vol 21.62…) → la comparación con baseline pasa; artefactos escritos. *(RTP>1 es esperable: el config de perf es carga sintética, no un juego balanceado.)*

> **HU-2 COMPLETADA al 100%** (simulador + API async + dashboard + QA + job perf). Progreso **96/129 SP (~74%)**. El matemático tiene el flujo completo: editar → simular 10M → validar convergencia → dashboard, con guard de rendimiento/regresión en CI. Siguiente historia: **HU-3 — Auditoría y replay** (depende de HU-1; reutiliza `game_rounds` y el `<SlotGame mode="replay">`).

---

## Fase 22 · HU-3 — Auditoría y replay

### Prompt 65 — Ticket HU-3-BE-01 (API de auditoría con filtros)

**Pregunta del usuario:** Continuar con `HU-3-BE-01`. *(`GET /operator/players` ya existía de HU-6.)*

**Implementación:**
- **`GET /api/v1/operator/rounds`** (paginado): filtros opcionales combinables (AND) `playerId`, `gameId`, `from`, `to` (ISO-8601 vía `@DateTimeFormat`); orden por defecto `created_at DESC`. Devuelve `PageResponse<RoundSummaryDto>`.
- **`AuditService`** consulta `game_rounds` con **`JpaSpecificationExecutor` + Criteria** — solo añade predicados para los filtros presentes (evita el clásico *"could not determine data type of parameter"* de PostgreSQL con parámetros nulos en JPQL `IS NULL OR …`). `GameRoundJpaRepository` ahora extiende `JpaSpecificationExecutor`. El orden `created_at DESC` aprovecha los índices `idx_game_rounds_*_created` (§3.2.8).

**Tests:** `AuditServiceTest` (2 unit: orden por defecto vía `ArgumentCaptor`, mapeo entidad→DTO) + **`AuditIT` (3, Postgres real): listado paginado tras generar un giro (AC1/AC2), filtro por juego inexistente → página vacía (AC3), rol PLAYER → 403 (AC4)**. **web-api unit 39 + AuditIT 3** verdes.

> **Hallazgo técnico:** el filtro JPQL `(:param IS NULL OR col = :param)` falla en PostgreSQL para `timestamptz`/tipos no inferibles cuando el parámetro es null. Solución adoptada: **Specifications** (predicados condicionales), que además mantienen el uso de índices.

> **HU-3-BE-01 completado.** Progreso **99/129 SP (~77%)**. Siguiente: `HU-3-BE-02` (endpoint de replay determinista `GET /operator/rounds/{id}/replay` — reconstruye la ronda, incl. free spins hijos).

### Prompt 66 — Ticket HU-3-BE-02 (endpoint de replay determinista)

**Pregunta del usuario:** Continuar con `HU-3-BE-02`.

**Implementación:**
- **`GET /api/v1/operator/rounds/{roundId}/replay`** → `ReplayDto` (roundId, gameId, gameConfigId, **rngSeed** [metadato forense], `result` autoritativo, `config` de la versión exacta usada).
- **`ReplayService`** (guardar-y-renderizar, §2.5.3): **lee el registro inmutable y lo devuelve tal cual, sin recalcular** (sin gate por drift del motor, AC4). Reconstruye `result.freeSpins.rounds[]` desde las filas hijas (`triggering_round_id`, orden cronológico) para un giro disparador; un free spin hijo se renderiza aislado. `lineBetCents = betCents / nº paylines` (de la `config` exacta de `gameConfigId`, no la activa, AC2). Reutiliza `SpinResultDto`/`WinningPaylineDto`.
- Repo: `findByTriggeringRoundIdOrderByIdAsc`. Excepción `RoundNotFoundException` → 404 (añadida al handler).

**Tests:** **`ReplayIT` (3, Postgres real)**: giro → operador obtiene el registro inmutable con `result` + `config` de la versión exacta (AC1/AC2), round inexistente → 404, rol PLAYER → 403 (AC5). **web-api unit 39 + ReplayIT 3** verdes.

> **HU-3-BE-02 completado.** Progreso **102/129 SP (~79%)**. El backend de auditoría/replay está listo. Siguiente: `HU-3-FE-01` (pantalla de auditoría del operador: tabla filtrable + enlace al replay).

### Prompt 67 — Ticket HU-3-FE-01 (backoffice operador: auditoría)

**Pregunta del usuario:** Continuar con `HU-3-FE-01`.

**Implementación (frontend):**
- **`operatorApi`**: tipo `RoundSummary` + `useRounds(filters, page, size)` (params `playerId`/`gameId`/`from`/`to`/`page`/`size`).
- **`AuditPage`** (`/operator/audit`): **filtros en la URL** (`useSearchParams`, compartible/back-friendly, AC3) — autocompletado de jugador por email (`usePlayers`, máx. 20 sugerencias, AC2), filtro de juego (ID), rango de fechas (inputs `date` → bounds UTC de día para la API). **Tabla paginada** (Round/Jugador/Juego/Apuesta/Premio/Fecha) con importes formateados por `locale` (AC4) y botón **Replay** por fila → `/operator/replay/{id}`. Paginación con prev/next deshabilitados en extremos (AC5).
- Routing: `/operator/audit` → `AuditPage`; `/operator/replay/:roundId` → placeholder (HU-3-FE-02). Enlace cruzado jugadores↔auditoría. i18n `operator:audit.*` + `operator:replay.*` (es/en).

**Tests:** `AuditPage.test.tsx` (3: tabla con round + botón Replay AC6, importes formateados AC4, prev deshabilitado en página 1 AC5). **Frontend: 47 tests verdes (12 ficheros) + build OK.**

> **HU-3-FE-01 completado.** Progreso **105/129 SP (~81%)**. Siguiente: `HU-3-FE-02` (pantalla de Replay que reutiliza `<SlotGame mode="replay">` con el registro inmutable).

### Prompt 68 — Ticket HU-3-FE-02 (pantalla de Replay)

**Pregunta del usuario:** Continuar con `HU-3-FE-02`.

**Implementación (frontend):**
- **`operatorApi.useReplay(roundId)`**: query del registro inmutable (`retry:false` para que el 404 sea respuesta final); tipos reutilizados de `playerApi` (`GameConfig`, `SpinResult`).
- **`ReplayPage`** (`/operator/replay/:roundId`): cabecera con metadatos (round, juego, versión config, apuesta, premio; jugador/fecha pasados por `state` desde la auditoría ya que el endpoint de replay no los lleva) y **reutiliza `<SlotGame mode="replay">`** (sin botón de spin ni barra de apuesta, AC6) alimentado con el `config` de la versión exacta y el `result` registrado. **"Reproducir de nuevo"** remonta el componente (`key={nonce}`) → la **misma secuencia exacta** se anima cada vez (determinista, AC4). Mensaje "Partida no encontrada" en 404 (AC5).
- Routing real `/operator/replay/:roundId` → `ReplayPage` (eliminado el `Placeholder` ya sin uso de `App.tsx`). i18n `operator:replay.*` (es/en).

**Tests:** `ReplayPage.test.tsx` (3: metadatos + rejilla registrada vía `<SlotGame>` AC1/AC2, replay sin botón Girar AC6, 404 → "Partida no encontrada" AC5). **Frontend: 50 tests verdes (13 ficheros) + build OK.**

> **HU-3-FE-02 completado.** Progreso **108/129 SP (~84%)**. La "killer feature" del operador (replay determinista visible) está lista. Siguiente y cierre de HU-3: `HU-3-QA-01` (tests de auditoría y replay: golden del registro, e2e del flujo operador).

### Prompt 69 — Ticket HU-3-QA-01 (tests de auditoría y replay) · cierra HU-3

**Pregunta del usuario:** Continuar con `HU-3-QA-01`.

**Implementación:** `AuditReplayQaIT` (5 tests, Postgres real) — red de seguridad del "replay fiel" + inmutabilidad (DGOJ):
- **Filtro por jugador + orden `created_at DESC`**: resuelve el `playerId` vía `/operator/players`, verifica que todas las filas son del jugador y que el orden es descendente.
- **Filtro por rango de fechas**: ventana futura → 0 resultados; desde epoch → ≥1.
- **Reproducibilidad byte-a-byte (AC2)**: dos GET del mismo replay → respuesta idéntica.
- **Resiliencia al drift / render del registro (AC3)**: el `result` del replay (`view`, `winCents`) **coincide exactamente** con lo que devolvió el spin → es el registro, no un recálculo (el determinismo del motor lo cubre el golden-master de HU-1-QA-01).
- **Inmutabilidad append-only**: UPDATE **y** DELETE sobre `game_rounds` rechazados por el trigger.

*(Filtros/paginación/403 y 404 ya estaban cubiertos por AuditIT/ReplayIT de los tickets BE.)*

**Verificación:** **AuditReplayQaIT 5** verde (+ AuditIT 3 + ReplayIT 3 + web-api unit 39 previos). *(AC1 cobertura casos de uso ≥80%: cubierta por unit+IT; reporte en CI.)*

> **HU-3 COMPLETADA al 100%** (API auditoría + replay + UI auditoría + UI replay + QA). Progreso **111/129 SP (~86%)**. Cerradas las 3 historias núcleo del producto (motor/juego, simulador, auditoría/replay). Quedan las *should-have*: **HU-9** (auto-spin), **HU-10** (audio) y **HU-8** (IA explainability). Siguiente: `HU-9-FE-01` (auto-spin con safeguards).

---

## Fase 23 · HU-9 — Auto-spin con safeguards

### Prompt 70 — Ticket HU-9-FE-01 (auto-spin con safeguards)

**Pregunta del usuario:** Continuar con `HU-9-FE-01`.

**Implementación (frontend, sin endpoint nuevo — reutiliza `POST /spin`):**
- **`autoSpin.ts`**: función pura `nextAutoDecision(remaining, balance, bet, stopBelow)` → `continue`/`done`/`insufficient`/`threshold` (safeguards con prioridad sobre la finalización).
- **`<SlotGame>`** (modo play): controles de auto-spin — input nº de giros (def. 10), input "parar si saldo < (€)", botón **Auto** / botón **Detener (N)**; mensaje de **pausa de juego responsable** (`role="alert"`) cuando se detiene por safeguard. El botón Spin se deshabilita durante el auto.
- **`GamePage`**: orquesta el bucle con refs (la `onSuccess` de la mutación lee el estado actual); encadena giros usando el `balancePostCents` del propio resultado (no espera el refetch del wallet). Se detiene al completar N (AC2), al bajar del bet (AC5) o del umbral (AC3); **Detener** corta tras el giro en curso (AC4); un error también corta el batch.

**Tests:** `autoSpin.test.ts` (5: continue/done/insufficient/threshold + prioridad de safeguards) + `SlotGame.test` (3 nuevos: inicia auto con el conteo, botón Detener con restante, mensaje de pausa). **Frontend: 58 tests verdes (14 ficheros) + build OK.**

> **HU-9-FE-01 completado.** Progreso **114/129 SP (~88%)**. Siguiente: `HU-9-QA-01` (tests de auto-spin: simular el bucle completo y los safeguards).

### Prompt 71 — Ticket HU-9-QA-01 (tests de auto-spin) · cierra HU-9

**Pregunta del usuario:** Continuar con `HU-9-QA-01`.

**Implementación:**
- **`GamePage.test.tsx`** (4, componente): dirige el bucle de auto-spin con la mutación mockeada (`playerApi.spin`) de forma determinista — **para por nº de giros** (AC1), **para con pausa de juego responsable al cruzar el umbral de saldo** (AC2), **no gira si el saldo < apuesta** (AC4) y **parada manual con "Detener"** usando una promesa diferida para garantizar el corte sin encadenar otro giro (AC3).
- **`e2e/tests/autospin.spec.ts`** (Playwright, job e2e): parada automática por conteo y parada manual, con selectores por clase (agnóstico al idioma).
- (La lógica pura de safeguards ya estaba en `autoSpin.test.ts` de HU-9-FE-01.)

**Verificación:** **frontend 62 tests verdes (15 ficheros) + build OK** (GamePage 4 nuevos).

> **HU-9 COMPLETADA al 100%.** Progreso **116/129 SP (~90%)**. Siguiente historia: **HU-10 — Audio inmersivo** (capa Howler.js sobre `<SlotGame>`).

---

## Fase 24 · HU-10 — Audio inmersivo

### Prompt 72 — Ticket HU-10-FE-01 (capa de audio)

**Pregunta del usuario:** Continuar con `HU-10-FE-01`.

**Decisión técnica:** Howler.js no está instalado (node_modules vive en contenedor); implemento la capa con **`HTMLAudioElement` nativo** (sin dependencia nueva), igual que con los gráficos del dashboard. Toda reproducción es *best-effort* y va con guardas (try/catch + `play().catch`), así nunca bloquea la animación (AC5).

**Implementación (frontend):**
- **`audioStore`** (Zustand + persist `nova-audio`): toggle de silencio **persistente entre sesiones** (AC4).
- **`audio.ts`**: `AudioBus` (música en bucle por temática con `setMuted`, SFX/voz one-shot que respetan mute) + helper puro `classifyWin` (none/win/bigWin con umbral ×10) + helpers de URL de assets.
- **`useGameAudio`** (hook): música ambiente por temática (AC1), SFX `spin` al iniciar giro y `win`/`bigWin`/`freeSpin` según resultado (AC2), **voz de locutor en el idioma del usuario** en Big Win / Free Spins (AC3), honrando el mute.
- Integrado en `GamePage` (sin acoplar la lógica de juego) + botón 🔊/🔇 en la cabecera. i18n `player:game.mute/unmute`.

**Tests:** `audio.test.ts` (6: `classifyWin` + helpers de URL) + `audioStore.test.ts` (3: toggle + persistencia AC4). **Frontend: 69 tests verdes (17 ficheros) + build OK.**

> **HU-10-FE-01 completado.** Progreso **119/129 SP (~92%)**. Siguiente: `HU-10-QA-01` (tests de la capa de audio: mute, no bloqueo).

### Prompt 73 — Ticket HU-10-QA-01 (tests de la capa de audio) · cierra HU-10

**Pregunta del usuario:** Continuar con `HU-10-QA-01`.

**Implementación:** `useGameAudio.test.tsx` (7, componente) — mockea el `audioBus` (el "Howler" de la app) y verifica que **cada evento dispara la llamada de audio esperada** (AC1): música por temática al montar, `setMuted`+`stopMusic` al silenciar, SFX `spin` al iniciar giro, `win`/`bigWin`(+voz)/`freeSpin`(+voz) según resultado; y que la **voz respeta el locale** (`en`→`/assets/voice/en/…`, AC3). La **persistencia del mute** (AC2) la cubre `audioStore.test`.

**Verificación:** **frontend 76 tests verdes (18 ficheros)** (useGameAudio 7 nuevos).

> **HU-10 COMPLETADA al 100%.** Progreso **120/129 SP (~93%)**. Última historia pendiente: **HU-8 — IA explainability** (adaptador Anthropic + endpoint `/explain` + caja de pregunta en el dashboard).

---

## Fase 25 · HU-8 — IA explainability

### Prompt 74 — Ticket HU-8-BE-01 (adaptador Anthropic + endpoint `/explain`)

**Pregunta del usuario:** Continuar con `HU-8-BE-01`.

**Implementación:**
- **Puerto `Explainer`** + `Explanation(answer, model)` en `nova-domain` (el caso de uso depende de la abstracción, no del SDK).
- **`AnthropicExplainerAdapter`** (`@ConditionalOnProperty(anthropic.enabled)`): llama a la Messages API de Claude vía **`RestClient`** (spring-web, sin dependencia nueva del SDK; node_modules-style evitado). Modelo configurable (`ANTHROPIC_MODEL`). Sin `ANTHROPIC_ENABLED=true` → no hay bean.
- **Persistencia:** `SimulationExplanationEntity` (+repo) mapea `simulation_explanations` (§3.2.10).
- **`ExplainService`** (inyecta `Optional<Explainer>`): carga la simulación (scope operador → 404), exige `COMPLETED` (→422), exige explainer presente (→503), compone el prompt con las métricas medidas + la pregunta, llama a Claude, **persiste la Q&A** y devuelve `answer`/`model`/`askedAt`. La plataforma mide; la IA solo interpreta.
- Endpoint `POST /math/simulations/{id}/explain`. Excepciones `SimulationNotCompletedException`→422 y `ExplainerUnavailableException`→503 mapeadas. Props `anthropic.*` en `application.yml` (deshabilitado por defecto).

**Tests:** `ExplainServiceTest` (4 unit: 200 con explainer mock, 422 no-completada, 404 inexistente, 503 sin explainer) + **`ExplainIT` (3, Postgres, IA deshabilitada): simulación completada → explain → 503 (AC4), desconocida → 404 (AC3), rol PLAYER → 403 (AC5)**. **web-api unit 43 + ExplainIT 3** verdes.

> **HU-8-BE-01 completado.** Progreso **125/129 SP (~97%)**. Siguiente: `HU-8-FE-01` (caja de pregunta a la IA en el dashboard del matemático).

### Prompt 75 — Ticket HU-8-FE-01 (caja de pregunta a la IA)

**Pregunta del usuario:** Continuar con `HU-8-FE-01`.

**Implementación (frontend):**
- **`mathApi.useExplain()`**: mutación `POST /math/simulations/{id}/explain`.
- **`ExplainBox`** (`math/sim/`): textarea + botón "Preguntar"; mantiene un **hilo Q&A visible** (AC2); si el backend responde **503** se **deshabilita con el aviso "IA no disponible"** sin romper el dashboard (AC3). Integrado en el `Dashboard` de `SimulationPanel`, que solo se renderiza con la simulación `COMPLETED` (AC1). i18n `math:sim.explain.*` (es/en, AC4).

**Tests:** `ExplainBox.test.tsx` (2: respuesta en el hilo AC2, 503 → caja deshabilitada AC3) + stub de `useExplain` en `SimulationPanel.test`. **Frontend: 78 tests verdes (19 ficheros) + build OK.**

> **HU-8-FE-01 completado.** Progreso **127/129 SP (~98%)**. Último ticket del MVP: `HU-8-QA-01` (tests de AI explainability con `FakeExplainerAdapter`).

### Prompt 76 — Ticket HU-8-QA-01 (tests de AI explainability) · cierra HU-8 y el MVP

**Pregunta del usuario:** Continuar con `HU-8-QA-01`.

**Implementación:**
- **`FakeExplainerConfig`** (`@TestConfiguration` en `src/it`): `Explainer` determinista **sin llamadas de red** a Anthropic (AC2); solo activo cuando un test lo `@Import`a (no contamina los demás contextos IT, donde el explainer está ausente y `explain`→503).
- **`ExplainFakeIT`** (`@Import(FakeExplainerConfig)`, Postgres real): `explain` sobre simulación **COMPLETED → 200** con `answer`/`model`/`askedAt`, y la **Q&A queda persistida con el `model` correcto** verificado por JDBC (AC1 200, AC3).
- Cobertura conjunta de AC1 (`200`/`422`/`404`/`403`/`503`): 200 + persistencia (ExplainFakeIT), 422/404/503 (ExplainServiceTest), 503/404/403 (ExplainIT).

**Verificación:** **ExplainFakeIT 1** verde (+ web-api unit 43 + ExplainIT 3 previos).

> **🎉 HU-8 COMPLETADA — y con ella el MVP AL 100% (129/129 SP).** Las 12 historias y los 40 tickets están implementados, probados (unit + property + golden-master + integración contra Postgres + e2e Playwright + job perf) y documentados. Backend: `nova-domain`/`-simulator` 23 unit + web-api 43 unit + ITs (auth, i18n, catálogo, recarga, config, spin, simulación, auditoría, replay, explain). Frontend: 78 tests + build. El producto cubre las tres superficies (jugador, operador, matemático) de principio a fin.

---

## Fase 26 · Backlog de evolución (post-MVP)

### Prompt 77 — Crear el backlog post-MVP (`stories-2.md` + HU-13…HU-26)

**Pregunta del usuario:** Crear el resto de historias para completar la app más allá del MVP, y un documento `stories-2.md` con el mismo formato que `stories.md`.

**Entrega (solo documentación, sin código):**
- **`stories/stories-2.md`** — índice post-MVP con el mismo formato que `stories.md`: unidades de estimación, tabla de historias (perfil/prioridad/talla/endpoints/decisión diferida/fase), cobertura, **árbol de dependencias** (mermaid + tabla textual mostrando las aristas hacia el MVP) y orden de construcción sugerido.
- **14 historias `HU-13`…`HU-26`** (un fichero cada una, formato idéntico a las del MVP: rol *Como/quiero/para*, contexto y valor, prioridad/estimación/endpoint, **criterios de aceptación BDD (Gherkin)**, verificación **INVEST**, fuera de alcance, dependencias y enlaces):
  - **Completan los 11 endpoints *post-MVP*** (§4.2): HU-13 refresh tokens, HU-14 historial del jugador, HU-15 config comercial + auditoría, HU-16 dashboard del operador, HU-17 publicación/versionado de matemática, HU-18 historial de simulaciones e IA.
  - **Materializan las decisiones diferidas** (§1.5): HU-19 límites/autoexclusión *server-side* (D7), HU-20 integridad *tamper-evident* (D3/D12), HU-21 informes DGOJ (D6), HU-22 accesibilidad WCAG 2.1 AA (D8), HU-23 particionado/retención de `game_rounds` (D4), HU-24 despliegue cloud + CD (D9).
  - **Amplían el producto:** HU-25 multi-operador (activa el multi-tenancy del esquema, rol `ADMIN`), HU-26 jackpots progresivos (D11 parcial).
- Puntero cruzado añadido en `stories.md` → `stories-2.md`.
- **Explícitamente fuera de alcance** (no se crean como historia): pasarelas de pago con dinero real (la plataforma es de saldo virtual por diseño).

> Backlog de evolución documentado: **14 historias post-MVP** listas para desglose en tickets cuando se prioricen. Los `HU-N.md` referencian "tickets por desglosar" (este bloque es backlog, aún sin tickets).

### Prompt 78 — Revisión documental + cubrir lagunas (Fase 2 en §4.2, `tickets-2.md`) y arreglo del diagrama

**Pregunta del usuario:** Actuar como documentalista: detectar lagunas/incoherencias en las historias y `stories-2.md`; revisar el árbol de dependencias de `stories-2.md` (no se veía igual que el de `stories.md`); cubrir las lagunas con mis sugerencias; generar `tickets-2.md` con la misma estructura que `tickets.md`; y añadir los endpoints nuevos separados como **Fase 2**.

**Revisión documental — hallazgos y arreglos:**
- **Diagrama de dependencias:** `stories-2.md` usaba un `subgraph MVP[...]` (caja) → distinto de `stories.md`. Reescrito a `flowchart TD` **plano** con `classDef`, igual que `stories.md`; eliminado el nodo colgante `HU-5` (sin aristas).
- **HU-19:** ancla inventada `(§2.1.x)` → corregida (referencia a HU-9).
- **HU-25:** referencia ambigua `§1.4 D4` (choca con la numeración D de §1.5) → reescrita; **laguna real** cubierta con *Nota técnica*: el rol `ADMIN` no existe en el dominio `users.role` (CHECK), requiere migración Flyway.
- Verificado coherente: cobertura de los 11 endpoints post-MVP, tallas/prioridades/dependencias entre índice y ficheros, orden de construcción y mapeo a decisiones diferidas.

**Lagunas cubiertas:**
- **Endpoints Fase 2 en `readme.md` §4.2:** añadidas las filas de los endpoints **nuevos** (no contemplados en el contrato del MVP) con fase `Fase 2` — `/player/limits`, `/player/self-exclusion` (HU-19), `/operator/audit/integrity` (HU-20), `/operator/reports/rfj` (HU-21) y un **grupo Admin** nuevo `/admin/operators` (HU-25) —, más una nota que distingue `post-MVP` (ya en el contrato) de `Fase 2` (nuevos) y la 4ª viñeta en la leyenda de fases.
- **`tickets/tickets-2.md`:** índice con la misma estructura que `tickets.md` — **47 tickets · 136 SP** desglosando HU-13…HU-26 (tablas por historia, resumen por historia/equipo con nota de las 5 migraciones Flyway, y un árbol de dependencias mermaid por historia con nodos externos discontinuos). Códigos en texto plano (los ficheros individuales se crearán al planificar el sprint).
- **Trazabilidad cruzada:** punteros `tickets.md`↔`tickets-2.md` y `stories-2.md`→`tickets-2.md`; los 14 pies de historia ahora enlazan a `tickets-2.md`.

> Backlog de evolución completo y trazado: 14 historias (`stories-2.md`) ↔ 47 tickets (`tickets-2.md`) ↔ endpoints Fase 2 en el catálogo §4.2.

### Prompt 79 — Crear los ficheros de ticket del backlog post-MVP

**Pregunta del usuario:** "No veo los tickets de las nuevas historias de esta fase 2." (`tickets-2.md` solo tenía el índice; faltaban los ficheros individuales como los del MVP).

**Entrega:** **47 ficheros de ticket** en `tickets/HU-13/` … `tickets/HU-26/`, con el **mismo formato** que los del MVP (Código · Título · Descripción · Criterios de aceptación · Prioridad · Estimación · Equipo responsable · Etiquetas · Comentarios con dependencias directas · Enlaces y referencias). Cada uno enlaza a su historia (`stories-2.md`), al índice (`tickets-2.md`) y a las secciones del readme; los tickets de endpoints **Fase 2** (HU-19/20/21/25) lo indican explícitamente.

**Cierre de trazabilidad:** los 47 códigos de `tickets-2.md` ahora **enlazan** a sus ficheros (slugs derivados de los nombres reales en disco) y la nota del índice se actualizó (ya no son "texto plano"). Validado: 47 ficheros creados (recuento por carpeta correcto), 47 enlaces resueltos sin rotos.

> **Backlog post-MVP completo a nivel de especificación:** 14 historias + 47 tickets (136 SP) documentados y trazados, listos para priorizar e implementar por sprint. *(Incidencia menor durante el trabajo: un `Write` con la ruta mal tecleada creó `C:\Users\jordi\�pps\…`; se eliminó el directorio espurio y se recreó el fichero en la ruta correcta.)*




### Prompt 80 — Implementación HU-13 (Sesión persistente con refresh tokens) · primer ticket del backlog post-MVP

**Pregunta del usuario:** "sigamos donde lo dejamos" / "Puedes seguir donde lo dejaste?" → empezar a implementar el backlog post-MVP. Siguiendo el orden de construcción de `stories-2.md`, se arranca por **HU-13** ticket a ticket (BE → FE → QA), como en el MVP.

**HU-13-BE-01 — Refresh tokens (backend):**
- Migración Flyway **V4** `refresh_tokens` (tabla mutable: `token_hash` SHA-256 único, `expires_at`, `revoked`; sin trigger de inmutabilidad).
- `RefreshTokenEntity` + repo (infra). `RefreshTokenService` (web-api): `issue`/`consume` (rotación: valida y revoca) / `revoke`; el token opaco es aleatorio de 256 bits y solo se persiste su hash.
- `AuthResponse` ahora incluye `refreshToken`; login/register lo emiten (login pasa a `@Transactional` de escritura). `AuthController`: `POST /auth/refresh` (200) y `POST /auth/logout` (204). `InvalidRefreshTokenException` → 401 (i18n es/en `error.auth.invalidRefresh.detail`). Propiedad `app.jwt.refresh-ttl-seconds` (30 días).
- Tests: `AuthServiceTest` (constructor + flujos refresh/logout) y `RefreshTokenServiceTest` (hash/rotación/revocación). **52 tests unitarios de web-api verdes.**

**HU-13-FE-01 — Renovación silenciosa (frontend):**
- `authStore`: añadido `refreshToken` + acción `setTokens` (rota tokens sin tocar el user). `authApi`: campo `refreshToken` + `refresh`/`logout` con **axios crudo** (evita recursión del interceptor).
- Interceptor de respuesta de `axiosClient` con **renovación transparente y single-flight**: ante 401 con sesión activa renueva una sola vez (promesa compartida), reintenta la petición original con el nuevo token; si falla, limpia sesión y redirige a `/login`.
- Logout revoca el refresh server-side: helper `session.ts` `logoutSession()` cableado en las 5 pantallas con logout (AC3 de la historia).
- Tests: `authStore.test.ts` actualizado (+`setTokens`) y nuevo `axiosClient.test.ts` (AC1 renovar+reintentar, AC3 single-flight, AC2 fallo→logout). **79 tests de frontend verdes + `tsc --noEmit` limpio.**

**HU-13-QA-01 — Tests de extremo a extremo:**
- `AuthRefreshIT` contra Postgres real (vía DB externa por el npipe de Docker Desktop): renovación, rotación de un solo uso (token revocado → 401), logout revoca, token desconocido → 401, validación 400. **5/5 verde**; V4 aplica correctamente (4 migraciones).
- Tests del interceptor (frontend) 3/3 verde.

> **HU-13 completa (BE + FE + QA), primera historia del backlog post-MVP implementada y verificada.** Incidencias resueltas durante el trabajo: Docker Desktop estaba parado (se arrancó); el 5432 lo ocupaba la Postgres de otro proyecto (rol `novacasino` inexistente) → se usó una Postgres desechable en 5433 para el IT; Testcontainers no abre el socket npipe → modo DB externa; el adapter de axios en el test debía **rechazar** en 401 (no resolver) y los spies del mock requerían `vi.hoisted`.

### Prompt 81 — Implementación HU-17 (Publicación y versionado de matemática)

**Pregunta del usuario:** "sigue" → continuar el backlog post-MVP con HU-17 (cierra la activación de matemática que el MVP dejó diferida). Ticket a ticket (BE → FE → QA).

**HU-17-BE-01 — Endpoints de versiones y publicación:**
- `GameConfigPublicationEntity` (append-only, `published_at` fijado en el constructor) + repo. `GameEntity`: ahora mapea/expone `setActiveConfigId` y `updated_at` (puntero mutable). `GameConfigJpaRepository.findByGameIdOrderByVersionDesc`.
- `MathService.listConfigs(gameId, operatorId)` (versiones con flag de activa) y `publish(gameId, configId, operatorId, mathUserId)` (mueve `games.active_config_id`, registra publicación inmutable; 409 si ya es la activa; 404 si la config no es del juego). Helper `ownedGame` reutilizado por `createConfig`.
- `MathController`: `GET /math/games/{id}/configs` y `POST /math/games/{id}/publish`. Seguridad `/math/**` ya restringe a `MATH_ANALYST` (AC5). `ConfigAlreadyActiveException` → 409 (i18n es/en `error.math.configAlreadyActive.detail`). DTOs `ConfigVersionDto`, `PublishResultDto`, `PublishRequest`.
- Tests: nuevo `MathServiceTest` (5: list/active-flag, publish mueve puntero + registra, 409 ya-activa, 404 config de otro juego, 404 juego de otro operador). **57 tests unitarios de web-api verdes.**

**HU-17-FE-01 — UI de versiones y publicación:**
- `mathApi`: `useConfigVersions(gameId)` y `usePublishConfig()` (invalida `['math','games']` y `['math','versions',gameId]`). Componente `VersionsPanel` (tabla de versiones con la activa destacada y botón Publicar; deshabilitado en la activa; muestra el 409 sin romper la UI) integrado en `MathEditorPage`. i18n `math.versions.*` (es/en) + estilos.
- Tests: `VersionsPanel.test.tsx` (AC1 lista+activa, AC2 publish llama API con configId, AC3 activa deshabilitada); `MathEditorPage.test` actualizado con los nuevos hooks. **85 tests de frontend verdes + `tsc --noEmit` limpio.**

**HU-17-QA-01 — Tests de publicación (IT):**
- `MathPublishIT` contra Postgres real: AC2 publica (mueve puntero + `publishedAt`), AC1 lista marca la activa, **AC4** un giro de jugador persiste la ronda con `game_config_id` = versión recién publicada, AC3 republicar la activa → 409, AC5 jugador → 403. Cada test clona la config activa y crea una versión nueva para ser independiente. **3/3 verde.**

> **HU-17 completa (BE + FE + QA), 2ª historia del backlog post-MVP.** Cierra el ciclo edición→validación→**publicación**: lo simulado puede pasar a producción y los nuevos giros usan la versión activada. Incidencia resuelta: `published_at` con `insertable=false` volvía `null` en la respuesta → se fija en el constructor de la entidad.

### Prompt 82 — Implementación HU-15 (Configuración comercial de juegos por el operador + auditoría)

**Pregunta del usuario:** "si" → continuar el backlog post-MVP con HU-15 (decisiones diferidas D1/D5). Ticket a ticket (BE → FE → QA).

**HU-15-BE-01/BE-02 — Endpoints de config comercial + auditoría:**
- Migración Flyway **V5** `game_commercial_audits` (append-only: `before_value`/`after_value` JSONB, autor, fecha; trigger reutilizando `fn_forbid_update_delete()` de V2).
- Entidad dedicada `GameCommercialEntity` sobre la tabla `games` (aísla del `GameEntity` que usa el motor; mapea `allowed_currencies char(3)[]` con `@JdbcTypeCode(ARRAY)`) + `GameCommercialAuditEntity` + repos (con aislamiento por operador `findByIdAndOperatorId`).
- `OperatorGameService.listGames` y `updateGame` (valida coherencia: max≥min y min/step múltiplos del nº de paylines de la config activa → 422; registra auditoría antes/después). Endpoints `GET /operator/games` y `PUT /operator/games/{id}` (rol `OPERATOR` por seguridad). `InvalidCommercialConfigException` → 422 (i18n es/en). DTOs `OperatorGameDto`, `UpdateGameRequest`.
- Tests: `OperatorGameServiceTest` (5: list+paylines, update+audit, 422 múltiplo, 422 max<min, 404 otro operador). **62 tests unitarios de web-api verdes.**

**HU-15-FE-01 — Backoffice comercial:**
- `operatorApi`: `useOperatorGames` + `useUpdateGame`. Página `OperatorGamesPage` (tabla con edición inline por juego: apuestas, monedas, activo; muestra el 422 con el detalle del backend en línea). Ruta `/operator/games` + enlace desde la pantalla de jugadores. i18n `operator.games.*` (es/en) + estilos.
- Tests: `OperatorGamesPage.test` (AC1 lista editable, AC2 422 inline, guardado llama API). **88 tests de frontend verdes + `tsc --noEmit` limpio.**

**HU-15-QA-01 — IT:**
- `GameCommercialIT` contra Postgres real: AC1 persiste el cambio y lo devuelve, AC2 inserta auditoría con autor/antes/después (verificado por `JdbcTemplate`), AC3 min/step no múltiplo de 5 líneas → 422, AC4 jugador → 403 y juego inexistente → 404 (aislamiento). Para no romper otros ITs del contexto compartido, mantiene `min/step` en 100 y solo cambia `maxBet`/monedas. **4/4 verde**; V5 aplica (5 migraciones) y se valida el mapeo `char(3)[]` (lectura y escritura).

> **HU-15 completa (BE+BE+FE+QA), 3ª historia del backlog post-MVP.** Autonomía comercial del operador con trazabilidad regulatoria (D5), separada de la matemática. Decisión de diseño: entidad `GameCommercialEntity` separada sobre `games` para no arriesgar el `GameEntity` del motor con el mapeo del array de monedas.

### Prompt 83 — Implementación HU-14 (Historial del jugador: movimientos y partidas)

**Pregunta del usuario:** "si" → continuar el backlog post-MVP con HU-14 (transparencia/juego responsable; solo lectura de datos ya persistidos, D1). Ticket a ticket (BE → FE → QA).

**HU-14-BE-01 — Endpoints de historial:**
- Repos: `WalletTransactionJpaRepository.findByWalletIdOrderByCreatedAtDesc` y `GameRoundJpaRepository.findByPlayerIdOrderByCreatedAtDesc` (paginados). Añadido `getCreatedAt()` a `WalletTransactionEntity` (faltaba).
- `PlayerHistoryService` (resuelve el wallet del jugador para movimientos; rondas por `player_id`; sin wallet → página vacía). DTOs `WalletTransactionDto` y `PlayerRoundDto` (player-scoped, sin `playerId`). Endpoints `GET /player/wallet/transactions` y `GET /player/rounds` (rol `PLAYER`, envoltorio `PageResponse`, orden `created_at DESC`).
- Tests: `PlayerHistoryServiceTest` (3: mapea movimientos, sin wallet→vacío, mapea rondas aisladas por user). **65 tests unitarios de web-api verdes.**

**HU-14-FE-01 — Pantallas de historial:**
- `playerApi`: `useWalletTransactions`/`usePlayerRounds` (paginadas, `keepPreviousData`) + tipos `PageResponse`/`WalletTransaction`/`PlayerRound`. Página `PlayerHistoryPage` con dos pestañas (Movimientos/Partidas), importes formateados por `locale` y paginación con extremos deshabilitados. Ruta `/history` + enlace desde el lobby. i18n `player.history.*` (es/en) + CSS.
- Tests: `PlayerHistoryPage.test` (AC1 movimientos+formato, AC2 cambio de pestaña, AC3 paginación en extremos). **91 tests de frontend verdes + `tsc --noEmit` limpio.**

**HU-14-QA-01 — IT:**
- `PlayerHistoryIT` contra Postgres real: AC1/AC4 páginas con envoltorio estándar ordenadas `created_at DESC` (jugador que ha girado), AC2/AC3 **aislamiento** (un jugador recién registrado no ve ningún dato de player1), AC5 operador → 403. **3/3 verde.**

> **HU-14 completa (BE+FE+QA), 4ª historia del backlog post-MVP.** Transparencia para el jugador reutilizando datos ya persistidos por el MVP, con aislamiento estricto por jugador. Ajuste menor: aserción de formato de dinero del test hecha agnóstica al ICU de `node:20-alpine`.

### Prompt 84 — Implementación HU-16 (Dashboard de actividad del operador)

**Pregunta del usuario:** "si"/"sigue" → continuar el backlog post-MVP con HU-16. Ticket a ticket (BE → FE → QA).

**HU-16-BE-01 — Dashboard + detalle de partida:**
- Agregaciones en `GameRoundJpaRepository` (`countActivePlayers`, `ggrCents`, `countRounds`, `topGames` con proyección `TopGameProjection`), acotadas por operador y rango de fechas. `OperatorDashboardService` (defaults de fechas para no bindear timestamps null en JPQL; resuelve nombres de los top juegos vía `gameRepo.findAllById`) + detalle de ronda (`roundDetail`, 404 si es de otro operador). DTOs `DashboardDto`/`TopGameDto` y `RoundDetailDto`. Endpoints `GET /operator/dashboard` y `GET /operator/rounds/{id}`.
- Tests: `OperatorDashboardServiceTest` (3). **68 tests unitarios de web-api verdes.**

**HU-16-FE-01 — Dashboard UI:**
- `operatorApi.useDashboard(from?,to?)`. Página `OperatorDashboardPage` (KPIs jugadores activos/GGR/partidas, filtro de fechas, barras de juegos más jugados con CSS inline). Ruta `/operator/dashboard` + enlace desde Jugadores. i18n `operator.dashboard.*` (es/en).
- Tests: `OperatorDashboardPage.test` (KPIs, formato GGR, filtro). **94 tests de frontend verdes + `tsc` limpio.**

**HU-16-QA-01 — IT:**
- `OperatorDashboardIT` contra Postgres real: AC1 los agregados coinciden con un **SQL de referencia** (robusto al contexto compartido), AC2 filtro de fechas futuro → todo 0, detalle de ronda con importes+grid, 404 ronda inexistente, 403 jugador. **5/5 verde.** (Ajuste: el filtro de fechas exige offset ISO `Z`, como `AuditPage`.)

> **HU-16 completa (BE+FE+QA), 5ª historia del backlog post-MVP.**

### Prompt 85 — Implementación HU-18, HU-19 y HU-20 (backlog post-MVP, en lote)

**Pregunta del usuario:** "si" + "cuando acabes sigue con las 3 historias que vienen a continuación, no hace falta que preguntes" → implementar HU-18, HU-19 y HU-20 seguidas, ticket a ticket.

**HU-18 — Historial de simulaciones e IA (Could Have):**
- BE-01: `GameRoundJpaRepository`... (no) → `SimulationRunJpaRepository.search(operatorId, configId, gameId, pageable)` (filtro por config/juego) + `SimulationExplanationJpaRepository.findBySimulationRunIdOrderByAskedAtAsc`. `SimulationHistoryService` (aislado por operador) + DTO `SimulationSummaryDto`. Endpoints `GET /math/simulations` y `GET /math/simulations/{id}/explanations`. Unit `SimulationHistoryServiceTest` (3).
- FE-01: `mathApi.useSimulationHistory`/`useSimulationExplanations` + `SimulationHistoryPanel` (lista + hilo IA expandible) integrado en el editor. i18n `math.history.*`. Tests (2).
- QA-01: `SimulationHistoryIT` (datos sembrados por JdbcTemplate; IA deshabilitada) — lista, hilo, 403 jugador, 404 inexistente. 4/4.

**HU-19 — Límites y autoexclusión server-side (Should Have, L):**
- DB-01: migración **V6** `player_limits` + `self_exclusions` (índices por user_id).
- BE-01: `ResponsibleGamingService` (endurecer aplica ya; relajar diferido por enfriamiento configurable; gate `assertCanSpin` con autoexclusión vigente → 403 y pérdida ≥ límite → 422). Integrado en `SpinService.doSpin` **antes** de tocar saldo. Endpoints Fase 2 `POST /player/limits`, `POST /player/self-exclusion`. Excepciones + i18n. Unit `ResponsibleGamingServiceTest` (6) + `SpinServiceTest` actualizado.
- FE-01: `LimitsPage` (límite de pérdida diaria + autoexclusión) + `playerApi` mutaciones; ruta `/limits` + enlace en lobby; i18n. Test (3).
- QA-01: `ResponsibleGamingIT` (jugadores nuevos; el gate corre antes del saldo) — límite 0 bloquea sin crear ronda, autoexclusión → 403, relajar diferido, operador → 403. 4/4.

**HU-20 — Integridad tamper-evident (Could Have):**
- DB-01: migración **V7** — `pgcrypto`, columnas `prev_hash`/`row_hash` (VARCHAR(64)) y trigger `BEFORE INSERT` que encadena el hash SHA-256 por operador (lock por operador; preimagen `prev|id|player|game|bet|win|balancePost|rngSeed`).
- BE-01: `IntegrityService.verify` recalcula la cadena reproduciendo el digest del trigger y reporta la primera ruptura. Endpoint Fase 2 `GET /operator/audit/integrity`. Unit `IntegrityServiceTest` (3).
- FE-01: `IntegrityCheck` (botón verificar → íntegra/rota) en la cabecera de auditoría; `operatorApi.useIntegrity`; i18n. Test (2).
- QA-01: `IntegrityChainIT` — cadena íntegra OK; manipulación por SQL (desactivando el trigger de inmutabilidad) detectada y **revertida** para no romper el contexto; 403 jugador. 3/3.

> **HU-18, HU-19 y HU-20 completas (BE+FE+QA).** Backend **80 unit + 98 IT verdes** (sin regresiones por V6/V7 ni por el gate de juego responsable en el giro); frontend **101 verdes + tsc limpio**. Hallazgo resuelto en HU-20: `CHAR(64)` rellenaba el `prev_hash` génesis con espacios → se usó `VARCHAR(64)` para que el seed `''` cuadrara entre SQL y Java. Backlog post-MVP: 8/14 historias implementadas (HU-13,14,15,16,17,18,19,20); migraciones V4–V7.

### Prompt 86 — Implementación HU-21 a HU-26 (cierre del backlog post-MVP)

**Pregunta del usuario:** "sigue con las 6 historias" → implementar las 6 restantes.

**HU-25 — Gestión multi-operador (ADMIN):** V8 (rol `ADMIN` en CHECK), `AdminController` `GET/POST /admin/operators` + `PUT` activar/desactivar, `AdminService` (alta operador + usuario inicial, aislamiento). **Login pasa a email global** (multi-tenant) y bloquea operador inactivo (403); `UserDetailsServiceImpl` también global. Seed `admin@nova.test/admin123`. FE `AdminPage` + rol/ruta `/admin`. Tests: AdminServiceTest 4, AdminPage 3, AdminMultiOperatorIT 4/4.

**HU-21 — Informe RFJ:** `RfjReportService` (agrega `game_rounds` por mes + **gate de integridad** HU-20 → 422 si la cadena está rota, con `firstBrokenRoundId`). `POST /operator/reports/rfj`. FE `OperatorReportsPage`. Tests: RfjReportServiceTest 2, RfjReportIT 3/3, FE 2.

**HU-22 — Accesibilidad WCAG 2.1 AA:** CSS global `a11y.css` (`:focus-visible`, `prefers-reduced-motion`, `.sr-only`), `aria-busy` en el grid; el `SlotGame` ya tenía roles grid/gridcell/status/alert y aria-labels. Test `SlotGame.a11y.test` 2. DEV-01: los tests a11y corren en el job de frontend de CI (nota añadida).

**HU-26 — Jackpot progresivo:** V9 (`jackpot_pools` con `@Version`, `jackpot_grants` append-only). `JackpotService` (contribución entera, **concesión determinista por seed**, reset al semilla) integrado en `SpinService.doSpin` (antes de persistir; el premio se atribuye a la ronda base). Pool expuesto en `GameDetailDto.jackpotCents`; FE ticker en `GamePage`. Tests: JackpotServiceTest 3, JackpotIT 2/2 (award+reset, no-award+contribución), FE ticker. *Nota:* el kernel zero-alloc no se tocó (AC5); la contribución del jackpot al RTP en el simulador queda como integración analítica de seguimiento.

**HU-23 — Particionado/retención:** V10 índice **BRIN** sobre `game_rounds(created_at)` (rangos eficientes en tabla append-only) + `docs/partitioning-and-retention.md` con el runbook completo de conversión a particionado mensual + archivado preservando inmutabilidad/cadena. La conversión in-place no se automatiza (reconstrucción con FKs+triggers → migración operativa).

**HU-24 — CD cloud:** `.github/workflows/cd.yml` (build+push imágenes versionadas → staging con health → producción con aprobación + rollback; secretos desde el gestor), IaC `deploy/docker-compose.prod.yml`, `deploy/deploy.sh`/`rollback.sh`, `scripts/smoke-test.sh`, `deploy/README.md`.

> **Backlog post-MVP completo: 14/14 historias (HU-13…HU-26).** Validación final en BD limpia: **backend 89 unit + 107 IT verdes**, **frontend 109 verdes + `tsc` limpio**, sin regresiones. Migraciones añadidas en total: V4 (refresh), V5 (auditoría comercial), V6 (juego responsable), V7 (cadena integridad), V8 (rol ADMIN), V9 (jackpot), V10 (BRIN). Incidencia clave resuelta: el login/JWT era single-tenant (operador por defecto) → se hizo global por email para soportar multi-operador (HU-25).

### Prompt 87 — Consolidación documental del backlog post-MVP (marcar implementado)

**Pregunta del usuario:** "haz el punto 1" → consolidar la documentación marcando HU-13…HU-26 como implementadas y actualizar el readme (endpoints Fase 2/post-MVP y migraciones).

**Cambios:**
- **`stories/stories-2.md`:** callout de estado "backlog COMPLETO 14/14" con recuentos de tests y migraciones V4–V10; columna **Estado = ✅ Implementada** en la tabla de las 14 historias; nota de alcance de HU-23 (BRIN + runbook) y HU-26 (kernel intacto; jackpot-en-simulador analítico).
- **`tickets/tickets-2.md`:** la nota "aún no implementados" sustituida por "**IMPLEMENTADO 14/14**" con recuentos de tests, migraciones y excepciones de alcance (HU-23 runbook, HU-24 artefactos CD/IaC).
- **`readme.md`:** callout "✅ Estado de evolución" en §4.2 (los 11 *post-MVP* + 6 *Fase 2* implementados; la columna *Fase* pasa a significar **origen en el contrato**, no estado); corregidas las notas que quedaban como "no implementado/se difiere" — activación de matemática (HU-17), auditoría comercial (HU-15, V5), refresh tokens (HU-13, V4), imposición server-side de límites (HU-19, D7), tamper-evidence (HU-20, V7), multi-tenant operativo (HU-25); callout de estado bajo las decisiones diferidas (D1–D12 abordadas); enumeración de migraciones **V4–V10** y triggers de inmutabilidad ampliados (`game_commercial_audits`, `jackpot_grants`); árbol de proyecto actualizado.

> Documentación alineada con el código: las 26 historias (MVP + evolución) figuran como implementadas y trazadas. Sin cambios de código en este prompt (solo docs).

### Prompt 88 — Refactor a hexagonal estricto (opción B): fundación + primer vertical slice

**Pregunta del usuario:** "la B" → poblar `nova-application`/`nova-common` con la arquitectura hexagonal estricta (puertos en application, adaptadores en infra; application NO depende de infra ni web).

**Hallazgo:** los `pom.xml` ya estaban cableados para hexagonal estricto (application → domain+common+jakarta.transaction; infra → application). Los módulos solo estaban vacíos.

**Entregado (vertical slice HU-20 integridad, como patrón probado):**
- `nova-common`: `IntegrityReportDto` (DTO compartido puro).
- `nova-application`: puerto `AuditChainPort` + modelo `AuditRound` + caso de uso `VerifyIntegrityUseCase` (POJO, `jakarta.transaction.@Transactional`, sin Spring). Test `VerifyIntegrityUseCaseTest` (3) con el puerto mockeado.
- `nova-infrastructure`: adaptador `AuditChainJpaAdapter` (`@Component`) que implementa el puerto sobre `GameRoundJpaRepository`.
- `nova-web-api`: `UseCaseConfig` (`@Bean` que ensambla el caso de uso inyectando el puerto/adaptador); `OperatorController` y `RfjReportService` ahora usan `VerifyIntegrityUseCase`. Eliminados `IntegrityService`/DTO/test antiguos.
- **Guard ArchUnit** `ApplicationArchTest`: ninguna clase de `com.novacasino.application` depende de `infrastructure`, `api` (web) ni `org.springframework`.

**Verde:** módulos compilan; `nova-application` 4 tests (caso de uso 3 + ArchUnit 1); web-api 86 unit; ITs `IntegrityChainIT` 3/3 + `RfjReportIT` 3/3 confirman el wiring en runtime.

> Patrón hexagonal estricto establecido y validado de extremo a extremo, con ambos módulos ya poblados y la regla de capas enforced. Quedan ~18 casos de uso por migrar siguiendo el mismo patrón (esfuerzo mecánico grande; cada uno requiere sus puertos + modelos + adaptadores).

### Prompt 89 — Hexagonal estricto: migración de casos de uso (en curso)

Continuación de la opción B, slice a slice con verde en cada uno. Progreso:
- **HU-20 integridad** → `VerifyIntegrityUseCase` + `AuditChainPort`/`AuditRound` + adaptador `AuditChainJpaAdapter` + `IntegrityReportDto` en `nova-common`. (slice 1)
- **HU-13 refresh tokens** → `RefreshTokenUseCase` + `RefreshTokenStorePort`/`StoredRefreshToken` + adaptador `RefreshTokenStoreJpaAdapter`; `InvalidRefreshTokenException` movida a `nova-application`; `RefreshTokenService` antiguo eliminado; wiring `@Bean` con `@Value` del TTL en `UseCaseConfig`; `AuthService`/`AuthServiceTest`/`GlobalExceptionHandler` actualizados. (slice 2)

`nova-application`: 9 tests (ArchUnit 1 + 2 casos de uso). web-api 81 unit. ITs verificados por slice (integridad, RFJ, auth/refresh) en verde. ArchUnit garantiza que application no depende de infra/web/Spring.

**Pendientes (~16):** PlayerCatalog, PlayerHistory, ResponsibleGaming, Jackpot, Spin (núcleo, el mayor), OperatorPlayer/Recharge, Audit, Replay, OperatorGame, OperatorDashboard, RfjReport, Math, Simulation, SimulationHistory, Explain, Admin, Idempotency. Se migrarán dejando primero `PageResponse` y DTOs compartidos en `nova-common`.

### Prompt 90 — Hexagonal estricto: más slices (PlayerHistory, ResponsibleGaming) + común

Continuación de la opción B. Migrados a `nova-application` (puerto + caso de uso + adaptador `@Component` en infra + `@Bean` en `UseCaseConfig`):
- **HU-14 PlayerHistory** → `PlayerHistoryUseCase` + `PlayerHistoryPort` + `PlayerHistoryJpaAdapter`. `PageResponse`/`PageRequestDto`/`WalletTransactionDto`/`PlayerRoundDto` movidos a `nova-common`; mapeo Spring `Page→PageResponse` aislado en `infrastructure/support/Pages`; los controllers convierten `Pageable`→`PageRequestDto`.
- **HU-19 ResponsibleGaming** → `ResponsibleGamingUseCase` + `ResponsibleGamingPort`/`PlayerLimit` + `ResponsibleGamingJpaAdapter`. `LimitDto`/`SelfExclusionDto` → common; `LimitReachedException`/`SelfExcludedException` → application. `SpinService` y `PlayerController` usan el caso de uso; `GlobalExceptionHandler` actualizado.

Estado: `nova-application` 18 tests (ArchUnit + 5 casos de uso). web-api 72 unit. ITs por slice verdes (historial, auditoría, simulaciones, RG, spin). ArchUnit garantiza application sin deps de infra/web/Spring.

**Migrados hasta ahora (5):** VerifyIntegrity (HU-20), RefreshToken (HU-13), PlayerHistory (HU-14), ResponsibleGaming (HU-19). **Pendientes (~14):** PlayerCatalog, Jackpot, OperatorPlayer/Audit/Replay/OperatorGame/OperatorDashboard/RfjReport, Math/Simulation/SimulationHistory/Explain, Admin, Spin(+Idempotency).

### Prompt 91 — Hexagonal estricto: PlayerCatalog (HU-5) y Audit (HU-3)

- **HU-5 PlayerCatalog** → `PlayerCatalogUseCase` + `PlayerCatalogPort` + `PlayerCatalogJpaAdapter`. DTOs `GameSummaryDto`/`GameDetailDto`/`WalletDto` → `nova-common` (se añade `jackson-databind` a common por los `JsonNode`). `GameNotFoundException` → `nova-application` (`com.novacasino.application.exception`); imports actualizados en MathService/OperatorGameService/SpinService/handler.
- **HU-3 Audit** → `AuditUseCase` + `AuditRoundsPort` + `AuditRoundsJpaAdapter` (la `Specification`/Criteria vive en el adaptador). `RoundSummaryDto` → common.

Migrados (6): VerifyIntegrity, RefreshToken, PlayerHistory, ResponsibleGaming, PlayerCatalog, Audit. `nova-application` 23 tests, web-api 67 unit, verde.

### Prompt 92 — Hexagonal estricto: bloque Operador

Migrados a puertos+casos de uso+adaptadores: **OperatorGame (HU-15)** (validación comercial en el caso de uso; snapshot/auditoría JSON en el adaptador), **OperatorPlayer (HU-6)** (búsqueda + recarga idempotente), **OperatorDashboard (HU-16)** (KPIs + detalle de ronda), **RfjReport (HU-21)** (verifica integridad vía VerifyIntegrityUseCase + agrega). DTOs a nova-common: OperatorGameDto, PlayerSummaryDto, DashboardDto, RoundDetailDto, WinningPaylineDto, RfjReportDto. Excepciones a nova-application: GameNotFoundException, RoundNotFoundException (application.exception); InvalidCommercialConfigException, InvalidAmountException, PlayerNotFoundException, ReportIntegrityException (application.operator.exception).

Migrados (10): VerifyIntegrity, RefreshToken, PlayerHistory, ResponsibleGaming, PlayerCatalog, Audit, OperatorGame, OperatorPlayer, OperatorDashboard, RfjReport. Pendientes: Replay (con Spin), bloque Math (Math/Simulation/SimHistory/Explain), Admin, Spin+Jackpot+Idempotency. nova-application 34 tests, web-api 53 unit, verde.

### Prompt 93 — Hexagonal estricto: bloque Math (parcial)

Migrados: **MathConfig (HU-17)** (listar/ver/publicar/crear versión; reglas de propiedad y publicación en el caso de uso; `ConfigValidator` movido a `application.math.validation` y cableado como @Bean), **SimulationHistory (HU-18)** y **Explain (HU-8)** (comparten `SimulationQueryPort`; `Explainer` opcional inyectado en el @Bean). DTOs a nova-common: MathGameDto, ConfigVersionDto, PublishResultDto, ConfigCreatedDto, ConfigDetailDto, SimulationSummaryDto, ExplanationDto. Excepciones a application.math.exception: ConfigNotFound, ConfigAlreadyActive, SimulationNotFound, SimulationNotCompleted, ExplainerUnavailable; ConfigValidationException a application.math.validation.

Migrados (13 servicios): + MathConfig, SimulationHistory, Explain. Pendientes: SimulationService (lanzador async), Admin (HU-25), Spin core (HU-1)+Jackpot+Replay+Idempotency, y validación final + docs. nova-application 53 tests, web-api 35 unit, verde.

### Prompt 94 — Hexagonal estricto: SimulationService y Admin

- **SimulationService (HU-2)** → `SimulationUseCase` + `SimulationLaunchPort`. El adaptador (`SimulationLaunchAdapter`) vive en nova-web-api (no en infra) porque orquesta `SimulationExecutor` (async) y `GameConfigMapper`, colaboradores web; así nova-application no depende de ellos. DTOs SimulationAcceptedDto/SimulationStatusDto a common; InvalidSimulationParamsException a application.math.exception.
- **Admin (HU-25)** → `AdminUseCase` + `AdminPort` + `AdminJpaAdapter`; `PasswordHasherPort` con adaptador `PasswordHasherAdapter` en web (envuelve PasswordEncoder). OperatorDto a common; OperatorCodeExists/OperatorNotFound a application.admin.exception; EmailAlreadyRegisteredException a application.auth.exception (AuthService actualizado).

Verificado: 18 ITs verdes en áreas migradas (Math/Sim/Operator/Player) — el contexto Spring arranca con todos los nuevos beans/adaptadores.

Migrados (15 servicios). **Pendiente: el núcleo Spin (HU-1) + Jackpot (HU-26) + Replay (HU-3) + Idempotency** (lo más acoplado al motor y a la transacción idempotente), luego validación final + docs de arquitectura. nova-application 62 tests, web-api 27 unit, verde.

### Prompt 95 — Hexagonal estricto: Replay, decisión del núcleo de spin y validación final

- **Replay (HU-3)** → `ReplayUseCase` + `ReplayPort` + `ReplayJpaAdapter`. `SpinResultDto` (+`FreeSpinsDto`) y `ReplayDto` movidos a nova-common.
- **Decisión núcleo de spin**: `SpinService`, `IdempotencyService`, `JackpotService` y `MaterializingSink` se mantienen en `nova-web-api` (capa de composición). Razón: el reintento captura `org.springframework.dao.OptimisticLockingFailureException` y la frontera idempotente la gestiona Spring; llevar eso a `nova-application` rompería ArchUnit. El cálculo puro ya vive en `nova-domain`; el gate de juego responsable se invoca vía `ResponsibleGamingUseCase`.
- **Validación final**: backend unit completo verde (domain 15, application 62 incl. ArchUnit, simulator 8, web-api 27) + **107 ITs verdes** (Spin/Jackpot/Replay/Idempotency/Auth/Math/Operator/Player/Admin/i18n). Frontend no afectado (los movimientos de paquete Java no cambian las formas JSON).
- **Docs**: readme §2.2 actualizado (descripción real de nova-application con sus casos de uso + puertos, nova-common con jackson-databind, nota del núcleo transaccional en web) y §2.2.5 (regla ArchUnit de application).

**Refactor hexagonal estricto (opción B) COMPLETO**: 17 casos de uso migrados a `nova-application` (puertos + adaptadores `@Component` en infra/web + `@Bean` en `UseCaseConfig`); el núcleo transaccional del spin permanece en la capa de composición por su acoplamiento intrínseco a Spring-tx.

### Prompt 96 — Rate limiting con Bucket4j (cierre de incoherencia doc↔código)

La doc (§2.5.4) afirmaba rate limiting con Bucket4j sobre `/auth/login` y `/player/*/spin`, pero **no estaba implementado**. Implementado de verdad:
- Dependencia `com.bucket4j:bucket4j-core` (gestionada en el parent pom, `bucket4j.version=8.10.1`).
- `RateLimitFilter` (`OncePerRequestFilter`, token bucket en memoria single-node) registrado en la cadena de seguridad `addFilterAfter(JwtAuthFilter)`: limita `POST /api/v1/auth/login` (clave por IP) y `POST /api/v1/player/games/*/spin` (clave por usuario autenticado, o IP). Al agotar el cupo → `429` (RFC 9457, i18n) con `Retry-After` y `X-Rate-Limit-Remaining`.
- Config `app.rate-limit.*` en `application.yml` (`RateLimitProperties`, `@ConfigurationProperties`): login 10/min, spin 60/min por defecto, todo por variables de entorno; `enabled` desactivable.
- Tests: `RateLimitFilterTest` (5 unit) + `RateLimitIT` (429 end-to-end con cupo pequeño). En `AbstractIntegrationTest` el rate limiting se desactiva para no falsear los múltiples logins/spins desde localhost del contexto compartido.
- Doc §2.5.4 afinada para describir el filtro real (clave, 429, Retry-After, cupos configurables).

Verificado: unit completo verde (web-api 32) + ITs de auth/spin + RateLimitIT verdes.

### Prompt 97 — Cierre de incoherencias doc↔código (doc-down + limpieza)

Tras la auditoría, corregidas:
- **Endpoints implementados sin documentar**: añadidos `POST /auth/logout` y `PUT /admin/operators/{operatorId}` al catálogo §4.2 y a sus fichas §4.3 (nueva ficha §4.3.5 Admin).
- **Contratos desalineados en §4.3**: `refresh` documentaba "Auth — token válido" → es `Body — refreshToken`; quitados filtros inexistentes `type` (/player/wallet/transactions) y `gameId` (/player/rounds); `/math/simulations` documentaba `gameId,status` → real `configId,gameId`.
- **Paginación falsa**: `/math/games/{gameId}/configs` y `/math/simulations/{id}/explanations` se documentaban como paginados con `page,size`, pero devuelven `List` → corregidos catálogo §4.2 y fichas §4.3.
- **Fichas §4.3 que faltaban** (Fase 2): `/player/limits`, `/player/self-exclusion`, `/operator/audit/integrity`, `/operator/reports/rfj` + sección Admin.
- **Atribución de migraciones**: §2.2 atribuía "migraciones" a nova-infrastructure; aclarado que infra trae el **motor** Flyway pero los **scripts** `V*.sql` viven en `nova-web-api/src/main/resources/db/migration` (coherente con §6).
- **Limpieza**: eliminada la dependencia **no usada** `jakarta.validation-api` de `nova-common` (ningún DTO la importa); así la afirmación de §2.2.5 ("única dependencia de terceros: jackson-databind") es exacta.

Los 40 endpoints quedan documentados en catálogo §4.2 y ficha §4.3. Build unit verde (web-api 32) tras la limpieza del pom.

### Prompt 98 — Cierre de lagunas de QA (G1–G7)

Tras la auditoría de QA, implementadas todas las recomendaciones:
- **G1**: test de `ResponsibleGamingUseCase.promoteIfDue` (relajación diferida ya vencida → se promueve y persiste).
- **G2**: tests de `RateLimitFilter` para la clave **por usuario autenticado** del spin (independiente de IP) y para `X-Forwarded-For` en login.
- **G3 (correctness)**: `FilterRegistrationBean(setEnabled=false)` para `JwtAuthFilter` y `RateLimitFilter` → evita que Spring Boot los auto-registre como filtros de servlet y se ejecuten dos veces (doble consumo de tokens) en Tomcat real. Solo corren dentro de la cadena de seguridad.
- **G4**: ITs de ramas defensivas de adaptadores — juego activo sin config → 404 (`PlayerCatalogIT`), jugador sin wallet → 404 (`OperatorRechargeIT`), juego sin config → se omite la regla de múltiplo de líneas (`GameCommercialIT`, con `@Transactional` rollback porque el audit comercial es inmutable y tiene FK a `games`).
- **G5**: `RateLimitIT` end-to-end del spin (429 por usuario, capacidad 2) además del login; IPs aisladas por test.
- **G6**: `AuditIT` reforzado con filtros `playerId` (match/no-match) y ventana `from`/`to`.
- **G7**: happy path de `AdminUseCase.setActive`, test unitario de `OperatorInactiveException` en login, y aserción del cuerpo 429 localizado (en) en `RateLimitIT`.

Validado: unit verde (domain 15, application 64, simulator 8, web-api 35) + ITs nuevos/modificados verdes (RateLimit login+spin, PlayerCatalog, OperatorRecharge, GameCommercial, Audit) + regresión de auth/authorization/spin con el cambio de G3.

### Prompt 99 — Clean code: trocear métodos demasiado largos

Auditados con un analizador de llaves los métodos ≥35 líneas del backend y refactorizados los que lo merecían (comportamiento idéntico, solo extracción de helpers con nombre):
- **`SpinService.doSpin`** (94→49): orquestación plana sobre `activeGame`, `fundedWallet`, `runEngine`, `computeJackpot`/`settleJackpot` (+ record `JackpotOutcome`), `persistFreeSpins`, `recordLedger`.
- **`ConfigValidator.validate`** (144→16): dividido en `validateGrid`/`validateSymbols`/`validateReels`/`validatePaylines`/`validatePaytable`/`validateScatterPays`/`validateBonus`.
- **`GameConfigMapper.toSpec`** (62→13): `mapSymbols`/`mapReels`/`mapPaylines`/`mapPaytable`/`mapScatterPays`/`mapWild`/`mapFreeSpins`.
- **`GameCompiler.build`** (119→23): `buildSymbols`/`buildReels`/`buildPaylines`/`buildLineWins`/`buildScatterTables`/`buildFreeSpins` con records portadores (`Symbols`/`LineWins`/`ScatterTables`/`FreeSpins`).
- **`SecurityConfig.filterChain`** (43→22): handlers 401/403 extraídos a `writeUnauthorized`/`writeForbidden`/`writeProblem` (elimina duplicación).

Dejados intactos a propósito: `SpinKernel.evaluateLine` (núcleo del motor en caliente, cero-alloc + golden-master) y `SeedDataLoader.run` (seeding de arranque ya factorizado en helpers, secuencia plana sin anidamiento).

Validado: unit completo verde (domain 15 incl. golden-master que protege `build`, application 64, simulator 8, web-api 35) + 46 ITs sensibles (spin/jackpot/validación/motor/401-403/replay) verdes.
