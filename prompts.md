> Detalla en esta sección los prompts principales utilizados durante la creación del proyecto, que justifiquen el uso de asistentes de código en todas las fases del ciclo de vida del desarrollo. Esperamos un máximo de 3 por sección, principalmente los de creación inicial o  los de corrección o adición de funcionalidades que consideres más relevantes.
Puedes añadir adicionalmente la conversación completa como link o archivo adjunto si así lo consideras


## Índice

1. [Descripción general del producto](#1-descripción-general-del-producto)
2. [Arquitectura del sistema](#2-arquitectura-del-sistema)
3. [Modelo de datos](#3-modelo-de-datos)
4. [Especificación de la API](#4-especificación-de-la-api)
5. [Historias de usuario](#5-historias-de-usuario)
6. [Tickets de trabajo](#6-tickets-de-trabajo)
7. [Pull requests](#7-pull-requests)

---

## 1. Descripción general del producto

**Prompt 1:** (Encargo inicial — define el producto, los perfiles de usuario y las restricciones)

> Quiero que actues como un product manager experto con años de experiencia en el sector del gambling. Tu misión va a consistir en diseñar una plataforma de juegos de slots que sea atractiva para el jugador. La principal restricción es que todas las fases del desarrollo desde esta especificación hasta el lanzamiento debe ser ejecutable con la ayuda de un asistente de IA como tu con un tiempo de entre 30 y 40 horas.
>
> Te voy a dar una lista de características básicas que este producto debe tener sin prejuicio que tu añadas las que ya tengas con tu expertise. Es más, valoraré que hagas aportaciones más allá de estas iniciales. En caso de dudas prefiero que me preguntes y planifiquemos conjuntamente a que tomes decisiones unilaterales. Por otro lado quiero que las decisiones que escribirás en el PRD sean todas bien razonadas.
>
> - El jugador verá 2 pantallas: la del lobby donde se ofrecerán los juegos disponibles con su carátula. Al clickar el juego se pasará a la pantalla del juego.
> - Deberá existir un primer backoffice enfocado a los operadores para configurar los juegos (monedas disponibles, apuestas disponibles, etc) y para analizar las partidas jugadas y resolver posibles conflictos con los jugadores.
> - Un segundo backoffice enfocado al equipo de matemáticas que generará las matemáticas de cada juego. Debe incluir todo lo necesario para que un matemático pueda analizar y configurar distintos perfiles de RTP y volatilidad. Es imprescindible que esta parte incluya un simulador que permita lanzar hasta 10 millones de partidas en menos de 10 minutos.
> - Queda fuera del ámbito de este proyecto la gestión de pagos y cobros (se dejaría para fases posteriores) y el uso de jackpots progresivos.
> - Para esta primera fase deberemos ser compliants con la regulación española de la Dirección General de Ordenación del Juego (DGOJ). No hace falta generar informes para ellos pero sí tenerlo en cuenta para evitar tomar decisiones que nos lo puedan impedir en un futuro.
>
> Este proyecto va a tener varias fases. Ahora nos vamos a centrar en los puntos 0 y 1 del fichero `readme.md`. Cuando me des el OK al plan deberás rellenar los campos de los puntos 0 y 1. El punto 1 es el PRD propiamente dicho. Por otro lado quiero que todos los prompts con sus preguntas y respuestas se escriban en un nuevo fichero `conversation.md`. Numera cada uno de los prompts para que después sea más fácil referenciarlos.

---

**Prompt 2:** (Feedback que añade el diferencial del producto — i18n, audio inmersivo y features innovadoras)

> Me gustaría añadir soporte multiidioma y música que ayude a hacer una experiencia más envolvente para el usuario. Por otro lado, quiero que me propongas ideas innovadoras para hacer un proyecto más diferenciado.

Este prompt provocó la incorporación al PRD de: i18n ES/EN, audio inmersivo (música ambiente por temática + SFX + voz de locutor en eventos especiales) y, tras una ronda de propuestas razonadas con coste-beneficio, las dos features diferenciales seleccionadas: **Replay determinista de partidas** en el backoffice operador y **AI-powered explainability del simulador con Claude**.

---

**Prompt 3:** (Ajuste final de scope — saca explícitamente WCAG del MVP)

> Quita "Accesibilidad WCAG 2.1 AA básica", queda fuera de la primera versión. Ok al resto.

Este prompt cerró el alcance del MVP: la accesibilidad WCAG queda documentada como *fuera de scope v1* y la estimación de horas se recalcula a ~50.5 h, con palancas de recorte explícitas para volver al rango 30-40 h si fuera necesario.

---

## 2. Arquitectura del Sistema

### **2.1. Diagrama de arquitectura:**

**Prompt 1:** (Encargo inicial del rol arquitecto — define el alcance del punto 2 y el estilo de trabajo)

> A partir de ahora quiero que actues como un arquitecto senior con experiencia en patrones de diseño de software y especialmente de aquellos aplicados a la industria del gambling. Ya no eres product manager. Ahora vamos a generar el punto 2 del `readme.md`. La parte correspondiente a la parte de arquitectura del sistema. Igual que antes consúltame cualquier duda que puedas tener y justifícame las decisiones que vayas tomando. No dudes en usar gráficos allí donde sea posible para ayudar a clarificar los conceptos. Si quedan muy grandes trocéalos con sentido. Sigue actualizando el fichero `conversation.md`.

Este prompt provocó dos rondas de preguntas estructuradas que cerraron las decisiones macro: **monolito modular Maven multi-módulo**, **arquitectura hexagonal + DDD ligero**, **REST puro** para la pantalla del juego y **simulador in-process con `ForkJoinPool` sin persistencia durante la run**.

---

**Prompt 2:** (Cuestionamiento del stack de persistencia — fuerza un análisis crítico y honesto en lugar de seguir el camino por defecto)

> Considera el uso de bases de datos no relacionales (como MongoDB) para mejorar la performance.
>
> [tras la propuesta polyglot del asistente]
>
> Si el MongoDB no da mejor performance entonces quizás no es necesario meterlo.

Este prompt fue clave porque obligó a hacer un análisis cuantitativo real (volumen MVP esperado: miles de spins/día, no decenas de miles/segundo) y descartar MongoDB por **complejidad operacional sin retorno** en este contexto. Cerró la decisión: **PostgreSQL único + JSONB para configuraciones + particionado mensual de `game_round`**, manteniendo ACID estricto en el wallet (requisito DGOJ).

---

**Prompt 3:** (Revisión crítica del usuario al primer borrador — afina diagramas, versión de stack, alcance y convención de tests)

> He revisado el punto 2 del `readme.md`. Veo varias cosas a corregir:
> - El diagrama 2.1.1 no se ve bien, da error.
> - El diagrama 2.1.2 tiene una presentación mejorable y no entiendo por qué has puesto la versión 16 de PostgreSQL en vez de la 18 (también en punto 2.2.3).
> - El diagrama 2.1.3 tiene la letra demasiado pequeña, no es fácilmente legible. Divídelo en 3 diferentes, una para cada actor del sistema.
> - ¿Por qué motivo haces la *hash chain* del punto 2.5.2?
> - Quiero que los tests de integración del punto 2.6 estén en un directorio separado de test llamado `it`, es decir `src/it` en vez de `src/test`.

Este prompt cerró cinco correcciones que mejoraron sustancialmente la calidad del punto 2: arreglo de Mermaid en el diagrama de contexto, mejora de presentación y actualización a **PostgreSQL 18**, **división del C4 nivel 3 en tres diagramas (uno por actor)** para legibilidad, **eliminación del *hash-chain* del MVP** (tras justificar que en single-node sin firma externa no aporta seguridad real adicional sobre el trigger anti-UPDATE/DELETE) y adopción de la convención **`src/it/java` con `maven-failsafe-plugin`** para los tests de integración, separados de los unit en `src/test/java`.

### **2.2. Descripción de componentes principales:**

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

### **2.3. Descripción de alto nivel del proyecto y estructura de ficheros**

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

### **2.4. Infraestructura y despliegue**

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

### **2.5. Seguridad**

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

### **2.6. Tests**

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

---

### 3. Modelo de Datos

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

---

### 4. Especificación de la API

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

---

### 5. Historias de Usuario

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

---

### 6. Tickets de Trabajo

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

---

### 7. Pull Requests

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**
