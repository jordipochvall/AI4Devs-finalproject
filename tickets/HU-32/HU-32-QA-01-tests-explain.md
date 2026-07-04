# HU-32-QA-01 — Tests del "ask the AI" (offline, Claude y degradación)

## Código
`HU-32-QA-01` — vinculado con **HU-32: "Ask the AI" funcional (con modo offline sin API key)**.

## Título
Cobertura de modo offline, modo Anthropic (fake/IT) y errores → 503

## Descripción
Verificar los tres caminos del explainer: (1) **offline** sin key devuelve una explicación derivada de las métricas; (2) **Anthropic** con key (mediante fake/IT como `ExplainFakeIT`) responde vía el adaptador; (3) un **fallo del proveedor** degrada a 503 sin romper el panel. Incluir una comprobación de frontend de que el `ExplainBox` muestra la respuesta (o el aviso de indisponibilidad) según el caso.

## Criterios de aceptación
- **AC1**: Test de modo offline: el endpoint responde 200 con explicación no vacía sin IA externa.
- **AC2**: Test de modo Anthropic (fake): la respuesta proviene del adaptador cuando `enabled=true`.
- **AC3**: Test de error del proveedor: se traduce en 503 (`ExplainerUnavailable`).
- **AC4**: `ExplainBox` renderiza la respuesta en modo offline/Claude y el aviso en 503; suite de frontend en verde.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `ia`, `it`, `vitest`, `resiliencia`

## Comentarios
- Reutiliza `ExplainFakeIT`/`FakeExplainerConfig` como base de los ITs.
- **Dependencias directas:** `HU-32-BE-01`, `HU-32-DEV-01`.

## Enlaces y referencias
- Historia: [HU-32](../../stories/HU-32.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
