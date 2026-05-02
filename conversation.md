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
