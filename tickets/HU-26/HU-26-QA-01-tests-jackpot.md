# HU-26-QA-01 — Tests de determinismo y convergencia del jackpot

## Código
`HU-26-QA-01` — vinculado con **HU-26: Jackpots progresivos**.

## Título
Tests del jackpot: determinismo, contribución al *pool* y convergencia del RTP

## Descripción
Verificar que la concesión del jackpot es determinista (mismo seed/config → misma concesión, idéntica en motor, simulador y replay), que la contribución al *pool* es correcta y que el simulador refleja la contribución del jackpot al RTP empírico. Incluye extender el *golden-master* del motor.

## Criterios de aceptación
- **AC1**: Unit/golden — con el mismo seed y config, la concesión del jackpot es reproducible bit a bit.
- **AC2**: Unit — la fracción de cada apuesta que va al *pool* y el reinicio tras concederse son correctos.
- **AC3**: Property/simulador — el RTP empírico refleja la contribución del jackpot dentro del intervalo de confianza.

## Prioridad
Could Have

## Estimación
3 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `golden-master`, `jqwik`, `jackpot`, `determinismo`

## Comentarios
- **Dependencias directas:** `HU-26-BE-01`, `HU-26-FE-01`.

## Enlaces y referencias
- Historia: [HU-26](../../stories/HU-26.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests (golden-master, property-based): [§2.6](../../readme.md#26-tests).
