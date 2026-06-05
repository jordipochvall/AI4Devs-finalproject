# HU-8-FE-01 — Caja de pregunta a la IA

## Código
`HU-8-FE-01` — vinculado con **HU-8: El matemático interpreta resultados con IA**.

## Título
Caja de pregunta a la IA en el dashboard de simulación

## Descripción
Añadir al dashboard de simulación (`frontend/src/math/`) una **caja de pregunta en lenguaje natural** que invoca `POST /math/simulations/{id}/explain` sobre la simulación completada y muestra la respuesta de Claude. Si la IA no está disponible (`503`), la caja se muestra deshabilitada con un mensaje informativo, sin romper el resto del dashboard.

## Criterios de aceptación
- **AC1**: La caja solo está activa cuando la simulación está `COMPLETED`.
- **AC2**: Al enviar una pregunta, se muestra la respuesta de la IA y la pregunta queda en el hilo visible.
- **AC3**: Si el backend responde `503`, la caja se deshabilita con el mensaje "IA no disponible" y el dashboard sigue funcionando.
- **AC4**: La UI funciona en **español e inglés**.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `ia`, `dashboard`, `i18n`

## Comentarios
- Complementa el dashboard de `HU-2-FE-01`.
- **Dependencias directas:** `HU-8-BE-01` (intra).

## Enlaces y referencias
- Historia: [HU-8](../../stories/HU-8.md).
- Funcionalidad C4: [1.2](../../readme.md#12-características-y-funcionalidades-principales).
- Wireframe (backoffice matemático): [1.3](../../readme.md#13-diseño-y-experiencia-de-usuario).
