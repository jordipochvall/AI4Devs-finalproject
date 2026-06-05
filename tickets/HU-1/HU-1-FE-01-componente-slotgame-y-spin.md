# HU-1-FE-01 — Componente `<SlotGame>` y spin

## Código
`HU-1-FE-01` — vinculado con **HU-1: El jugador realiza un giro**.

## Título
Componente `<SlotGame>` data-driven (rejilla, animación, free spins) e integración del spin

## Descripción
Implementar el componente React `<SlotGame config={...} />` que renderiza **cualquier juego** interpretando su `config` (apartado 3.3): rejilla `grid.cols × grid.rows`, símbolos desde `frontend/public/assets/<theme>/`, animación de giro, resaltado de paylines y cinemática de free spins. Incluye el botón **Spin** y el selector de apuesta. La invocación va con cabecera `Idempotency-Key` (UUID en cliente) y el saldo se actualiza tras cada giro.

El `config` del juego lo obtiene del endpoint de detalle (`HU-5-BE-01`); la navegación desde el lobby (`HU-5-FE-01`) es solo enrutado (no una dependencia de construcción). El componente se reutiliza en la pantalla de Replay (`HU-3-FE-02`).

## Criterios de aceptación
- **AC1**: Dado un `config` 5x3, renderiza la rejilla con los símbolos de `view` en sus posiciones.
- **AC2**: Al pulsar **Spin**, invoca `POST .../spin` con `betCents`, `currency` e `Idempotency-Key`; anima el giro, resalta `winningPaylines` y actualiza el saldo.
- **AC3**: Si `freeSpins.triggered`, reproduce la cinemática y anima cada `rounds[i]` secuencialmente.
- **AC4**: Ante `422` "saldo insuficiente", muestra un mensaje sin alterar la rejilla.
- **AC5**: El componente es agnóstico al juego: solo cambian el `config` y los assets.
- **AC6**: Expone un *prop* `mode` que permite el modo "replay" (sin botón de spin) para reutilización por HU-3.

## Prioridad
Must Have

## Estimación
8 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `motor-ui`, `data-driven`, `reuso-componente`

## Comentarios
- El audio (música/SFX/voz) lo añade `HU-10-FE-01`; el auto-spin lo añade `HU-9-FE-01`. Este ticket entrega el giro manual.
- **Dependencias directas:** `HU-1-BE-02` (intra, endpoint del spin) · `HU-5-BE-01` (externa, endpoint de detalle/`config`) · `HU-4-FE-01` (externa, sesión autenticada). *No* depende de `HU-5-FE-01` (el enlace lobby→juego es enrutado).

## Enlaces y referencias
- Historia: [HU-1](../../stories/HU-1.md).
- Esquema del config: [3.3](../../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
- API spin: [4.4.3](../../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios).
