# HU-3-FE-02 — Pantalla de Replay

## Código
`HU-3-FE-02` — vinculado con **HU-3: El operador resuelve una reclamación con el replay**.

## Título
Pantalla de *Replay* que reutiliza `<SlotGame>` con los datos del giro auditado

## Descripción
Implementar la pantalla `/operator/replay/{roundId}` que reproduce visualmente la partida auditada. Consume `GET /api/v1/operator/rounds/{roundId}/replay` y **reutiliza el componente `<SlotGame>`** (`HU-1-FE-01`) configurado en modo "replay":

- El componente recibe el `config` (la versión exacta `gameConfigId`) y el `result` registrado, en lugar de invocar al backend.
- Reproduce la animación del giro mostrando la `view` final y resaltando las `winningPaylines` exactas.
- Si el round tiene `freeSpins.rounds`, los reproduce uno a uno con la misma secuencia que el jugador vio.
- Cabecera con metadatos: `roundId`, `gameId`, jugador, fecha, apuesta, premio.
- Botón **"Reproducir de nuevo"** que reinicia la animación; el resultado es **idéntico** en cada reproducción (visualmente verificable).

## Criterios de aceptación
- **AC1**: La pantalla carga correctamente el `replay` de un `roundId` válido y muestra los metadatos.
- **AC2**: La animación reproduce la rejilla final (`view`) y las paylines ganadoras tal como las recibió el jugador originalmente.
- **AC3**: Para un round con free spins, los giros gratis se animan secuencialmente con sus respectivos `view` y premios.
- **AC4**: Reproducir varias veces el mismo `roundId` da **siempre la misma secuencia visual** (verificable mediante comparación de capturas en `HU-3-QA-01`).
- **AC5**: Si el `roundId` no existe, se muestra un mensaje "Partida no encontrada" (404 del backend).
- **AC6**: El componente `<SlotGame>` se reutiliza sin duplicar lógica; un *prop* `mode="replay"` desactiva el botón de spin y la barra de apuesta.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `replay`, `determinismo`, `dgoj`, `reuso-componente`

## Comentarios
- Es la "killer feature" visible del producto desde el lado operador.
- **Dependencias directas:** `HU-3-BE-02` (intra, endpoint replay) · `HU-1-FE-01` (externa, `<SlotGame>` en modo replay).

## Enlaces y referencias
- Historia: [HU-3](../../readme.md#5-historias-de-usuario).
- Funcionalidad B4 (replay): [1.2 Características](../../readme.md#12-características-y-funcionalidades-principales).
- API: [4.4.5 Replay](../../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios).
- RNG y replay determinista: [2.5.3](../../readme.md#25-seguridad).
