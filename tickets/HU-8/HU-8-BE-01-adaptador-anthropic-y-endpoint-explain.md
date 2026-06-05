# HU-8-BE-01 — Adaptador Anthropic y endpoint `explain`

## Código
`HU-8-BE-01` — vinculado con **HU-8: El matemático interpreta resultados con IA**.

## Título
Adaptador Anthropic (Claude) + endpoint `POST /math/simulations/{id}/explain` + persistencia

## Descripción
Implementar el puerto `Explainer` en `nova-domain` y su adaptador `AnthropicExplainerAdapter` en `nova-infrastructure` (cliente de la API de Anthropic, modelo configurable vía `ANTHROPIC_MODEL`). El endpoint `POST /api/v1/math/simulations/{simulationId}/explain` toma una pregunta en lenguaje natural, compone un *prompt* con las métricas de la simulación (`simulation_runs`), llama a Claude y persiste la Q&A en `simulation_explanations` (3.2.10). El adaptador se activa con `@ConditionalOnProperty(anthropic.enabled)`: sin `ANTHROPIC_API_KEY`, el endpoint responde `503` y el resto de la plataforma funciona.

## Criterios de aceptación
- **AC1**: Con una simulación `COMPLETED` y la IA habilitada, `explain` devuelve `200` con `answer`, `model` y `askedAt`, y persiste la fila en `simulation_explanations`.
- **AC2**: Simulación en estado distinto de `COMPLETED` → `422`.
- **AC3**: Simulación inexistente → `404`.
- **AC4**: Sin `ANTHROPIC_API_KEY` configurada, `explain` → `503` y el resto de endpoints siguen operativos.
- **AC5**: Solo rol `MATH_ANALYST`; otros roles → `403`.
- **AC6**: El `model` usado queda registrado para trazabilidad.

## Prioridad
Should Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-domain`, `nova-infrastructure`, `nova-web-api`, `ia`, `anthropic`

## Comentarios
- El *prompt caching* para reducir coste se difiere (post-MVP, ver 1.5-D10).
- Para tests, se provee un `FakeExplainerAdapter` determinista (ver `HU-8-QA-01`).
- **Dependencias directas:** `HU-4-BE-01` (externa, auth/roles) · `HU-2-BE-02` (externa, simulaciones `COMPLETED`).

## Enlaces y referencias
- Historia: [HU-8](../../stories/HU-8.md).
- Servicios externos: [2.2.4](../../readme.md#22-descripción-de-componentes-principales).
- Modelo: [3.2.10 simulation_explanations](../../readme.md#32-descripción-de-entidades-principales).
- API: catálogo [4.2](../../readme.md#42-catálogo-de-endpoints).
