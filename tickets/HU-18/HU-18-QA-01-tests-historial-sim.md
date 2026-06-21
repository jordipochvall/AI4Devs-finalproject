# HU-18-QA-01 — Tests de historial de simulaciones e IA

## Código
`HU-18-QA-01` — vinculado con **HU-18: El matemático revisa el historial de simulaciones e IA**.

## Título
Tests de los listados de simulaciones y de explicaciones IA

## Descripción
Verificar que los historiales devuelven páginas correctas, filtran por juego/config y respetan el aislamiento por operador. Integración (Failsafe + Testcontainers); la IA se prueba con el `FakeExplainerAdapter` de HU-8-QA-01 cuando aplique.

## Criterios de aceptación
- **AC1**: IT — `GET /math/simulations` devuelve la página esperada con el filtro aplicado.
- **AC2**: IT — `GET .../explanations` devuelve el hilo de Q&A con su `model`.
- **AC3**: IT — aislamiento por operador y `403` para roles no matemáticos.

## Prioridad
Could Have

## Estimación
1 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `math`, `ia`

## Comentarios
- **Dependencias directas:** `HU-18-BE-01`, `HU-18-FE-01`.

## Enlaces y referencias
- Historia: [HU-18](../../stories/HU-18.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
