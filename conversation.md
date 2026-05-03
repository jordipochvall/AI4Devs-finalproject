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


