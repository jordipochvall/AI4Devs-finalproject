# HU-32-BE-01 — Explainer local (offline) determinista como fallback

## Código
`HU-32-BE-01` — vinculado con **HU-32: "Ask the AI" funcional (con modo offline sin API key)**.

## Título
`Explainer` offline que redacta a partir de las métricas cuando no hay key

## Descripción
Implementar un `Explainer` (puerto `nova-domain/ai`) **local y determinista** que se active cuando **no** hay IA externa configurada, de modo que el endpoint `/explain` **responda con una explicación** (derivada de las métricas de la simulación: RTP y su IC, comparación con el target, hit-frequency, volatilidad, frecuencia de free-spins, descomposición por símbolo…) **en lugar de 503**. Cuando `anthropic.enabled=true`, sigue mandando el `AnthropicExplainerAdapter`; sus errores de proveedor (key inválida, rate limit) **degradan a 503** de forma controlada. Reutiliza el patrón `FakeExplainer` de los ITs como base.

## Criterios de aceptación
- **AC1**: Sin key (IA externa desactivada), `POST /math/simulations/{id}/explain` devuelve una explicación útil generada localmente (no 503).
- **AC2**: Con `anthropic.enabled=true` y key, la respuesta proviene de Claude (adaptador Anthropic).
- **AC3**: Un fallo del proveedor se traduce en 503 (`ExplainerUnavailable`) sin propagar 500 ni romper el panel.
- **AC4**: La explicación offline es determinista y se apoya en las métricas ya persistidas de la simulación.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `ia`, `hexagonal`, `resiliencia`

## Comentarios
- Selección de bean por `@ConditionalOnProperty`/wiring: Anthropic si `enabled=true`, offline en caso contrario.
- **Dependencias directas:** `HU-8` (puerto/endpoint/`ExplainBox`).

## Enlaces y referencias
- Historia: [HU-32](../../stories/HU-32.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
