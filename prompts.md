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

**Prompt 1:**

**Prompt 2:**

**Prompt 3:**

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
