# HU-8-QA-01 — Tests de AI explainability

## Código
`HU-8-QA-01` — vinculado con **HU-8: El matemático interpreta resultados con IA**.

## Título
Tests de explainability con adaptador *fake* determinista

## Descripción
- **Integration (Failsafe + Testcontainers)** con un `FakeExplainerAdapter` determinista (sin llamadas reales a Anthropic): `explain` sobre simulación `COMPLETED` → `200` con respuesta y registro en `simulation_explanations`; estado distinto de `COMPLETED` → `422`; simulación inexistente → `404`; rol no matemático → `403`.
- **Test de degradación**: con el adaptador real deshabilitado (`anthropic.enabled=false`), `explain` → `503` y el resto de endpoints del backoffice matemático siguen respondiendo.

## Criterios de aceptación
- **AC1**: Cubiertos `200`/`422`/`404`/`403`/`503`.
- **AC2**: Los tests no realizan llamadas de red reales a Anthropic (usan el *fake*).
- **AC3**: Verificado que la Q&A queda persistida con el `model` correcto.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `ia`, `anthropic`

## Comentarios
- El `FakeExplainerAdapter` lo provee `HU-8-BE-01`.
- **Dependencias directas:** `HU-8-BE-01` (intra).

## Enlaces y referencias
- Historia: [HU-8](../../stories/HU-8.md).
- Estrategia de tests: [2.6](../../readme.md#26-tests).
