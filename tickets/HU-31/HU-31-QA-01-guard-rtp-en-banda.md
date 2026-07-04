# HU-31-QA-01 — Guard de regresión: RTP de las configs semilla en banda

## Código
`HU-31-QA-01` — vinculado con **HU-31: Bug — RTP empírico fuera de rango en los juegos semilla**.

## Título
Test que simula cada config semilla y falla si el RTP se sale de banda

## Descripción
Añadir un test (estilo `SimulatorRtpPropertyTest`, pero sobre las **configs semilla reales** compiladas) que ejecute una simulación por juego y asserte que el **RTP empírico** queda dentro de una banda del `rtp_target` declarado. Sirve de guard de regresión para que no vuelva a colarse una config descalibrada. Verificación adicional en la UI del simulador (los tres juegos muestran RTP creíble).

## Criterios de aceptación
- **AC1**: Un test simula cada config semilla y verifica `|rtp − target| ≤ banda` (con giros suficientes para un error estándar bajo).
- **AC2**: El test **falla** si se reintroduce una config con RTP fuera de banda (regresión).
- **AC3**: Revisión manual en el panel del matemático: los tres juegos muestran RTP dentro de rango.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `backend`, `rtp`, `regresión`, `simulador`

## Comentarios
- Reutiliza `SimulationRunner`/`GameCompiler` y el patrón de los tests de propiedad existentes.
- **Dependencias directas:** `HU-31-BE-01`.

## Enlaces y referencias
- Historia: [HU-31](../../stories/HU-31.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
