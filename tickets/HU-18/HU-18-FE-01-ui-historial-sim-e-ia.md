# HU-18-FE-01 — UI de historial de simulaciones e hilos de IA

## Código
`HU-18-FE-01` — vinculado con **HU-18: El matemático revisa el historial de simulaciones e IA**.

## Título
Vista de historial de simulaciones con acceso al hilo de IA de cada una

## Descripción
Añadir al backoffice matemático una vista paginada de simulaciones anteriores (estado, RTP, fecha; filtro por juego/config) y, al seleccionar una, mostrar su hilo de preguntas/respuestas de IA.

## Criterios de aceptación
- **AC1**: La vista lista las simulaciones anteriores con paginación y filtro por juego/config.
- **AC2**: Seleccionar una simulación muestra su hilo de Q&A de IA (con el modelo y la fecha).
- **AC3**: La UI funciona en **español e inglés** y solo es accesible con rol `MATH_ANALYST`.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `tanstack-query`, `math`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-18-BE-01`.

## Enlaces y referencias
- Historia: [HU-18](../../stories/HU-18.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Diseño y UX: [§1.3](../../readme.md#13-diseño-y-experiencia-de-usuario).
