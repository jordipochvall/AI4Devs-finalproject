# HU-18-BE-01 — Endpoints de historial de simulaciones y de IA

## Código
`HU-18-BE-01` — vinculado con **HU-18: El matemático revisa el historial de simulaciones e IA**.

## Título
`GET /math/simulations` y `GET /math/simulations/{id}/explanations` (+ *prompt caching*)

## Descripción
Exponer el historial paginado de simulaciones del operador (estado, RTP empírico, fecha; filtrable por juego/config) y el hilo de preguntas/respuestas de IA de una simulación. Como ampliación opcional, habilitar *prompt caching* en el adaptador Anthropic para abaratar la IA (decisión diferida D10).

## Criterios de aceptación
- **AC1**: `GET /math/simulations` devuelve una página con estado, RTP empírico y fecha, filtrable por juego/config.
- **AC2**: `GET /math/simulations/{id}/explanations` devuelve las Q&A con su `model` y fecha.
- **AC3**: **Aislamiento**: solo simulaciones/explicaciones del operador del token.
- **AC4**: Solo rol `MATH_ANALYST`; otros roles → `403`.

## Prioridad
Could Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `math`, `paginacion`, `ia`, `fase-post-mvp`

## Comentarios
- Endpoints **post-MVP** del catálogo (§4.2). Solo lectura sobre `simulation_runs`/`simulation_explanations`.
- **Dependencias directas:** `HU-2-BE-02` (simulaciones) · `HU-8-BE-01` (explicaciones IA).

## Enlaces y referencias
- Historia: [HU-18](../../stories/HU-18.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Catálogo: [§4.2](../../readme.md#42-catálogo-de-endpoints) · Decisión diferida [§1.5 D10](../../readme.md#15-supuestos-y-decisiones-diferidas).
