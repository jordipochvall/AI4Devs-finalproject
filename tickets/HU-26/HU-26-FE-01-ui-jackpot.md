# HU-26-FE-01 — UI del jackpot (*pool* en vivo y celebración)

## Código
`HU-26-FE-01` — vinculado con **HU-26: Jackpots progresivos**.

## Título
Visualización del *pool* del jackpot y cinemática de concesión en `<SlotGame>`

## Descripción
Mostrar en la pantalla de juego el valor actual del *pool* del jackpot (cuando el juego lo tiene) y, al concederse, una **cinemática de celebración**. Se integra sobre `<SlotGame>` sin acoplar la lógica de juego, usando el resultado del giro que ya incluye la concesión.

## Criterios de aceptación
- **AC1**: En un juego con jackpot, la pantalla muestra el valor actual del *pool*, formateado por `locale`.
- **AC2**: Cuando un giro concede el jackpot, se reproduce la cinemática de celebración con el importe ganado.
- **AC3**: En juegos sin jackpot, la UI no muestra el *pool* ni cambia el comportamiento.
- **AC4**: La UI funciona en **español e inglés**.

## Prioridad
Could Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `motor-ui`, `jackpot`, `i18n`

## Comentarios
- Reutiliza el componente `<SlotGame>` (HU-1-FE-01) y, opcionalmente, la capa de audio (HU-10).
- **Dependencias directas:** `HU-26-BE-01`.

## Enlaces y referencias
- Historia: [HU-26](../../stories/HU-26.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Componente de juego: [HU-1](../../stories/HU-1.md).
