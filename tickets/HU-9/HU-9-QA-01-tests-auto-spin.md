# HU-9-QA-01 — Tests de auto-spin

## Código
`HU-9-QA-01` — vinculado con **HU-9: El jugador usa auto-spin con safeguards de juego responsable**.

## Título
Tests E2E del auto-spin y sus safeguards

## Descripción
- **E2E (Playwright)**: lanzar un auto-spin y verificar la parada automática por (a) número de giros completados, (b) umbral de saldo cruzado —con aparición del mensaje de pausa— y (c) parada manual con el botón "Detener".
- **Unit/componente**: la máquina de estados del auto-spin no lanza un nuevo giro si el saldo es inferior a la apuesta.

## Criterios de aceptación
- **AC1**: Verificada la parada por nº de giros.
- **AC2**: Verificada la parada por umbral de saldo con el mensaje de juego responsable.
- **AC3**: Verificada la parada manual.
- **AC4**: Verificado que no se ejecuta un giro sin saldo suficiente.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `playwright`, `auto-spin`, `juego-responsable`

## Comentarios
- **Dependencias directas:** `HU-9-FE-01` (intra).

## Enlaces y referencias
- Historia: [HU-9](../../stories/HU-9.md).
- Estrategia de tests: [2.6](../../readme.md#26-tests).
