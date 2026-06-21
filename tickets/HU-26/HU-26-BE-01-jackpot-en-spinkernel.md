# HU-26-BE-01 — Jackpot en el `SpinKernel` + soporte simulador/replay

## Código
`HU-26-BE-01` — vinculado con **HU-26: Jackpots progresivos**.

## Título
Contribución y concesión determinista del jackpot en el motor, con fidelidad

## Descripción
Ampliar el `SpinKernel` para que cada apuesta aporte una fracción configurada al *pool* y para que la **concesión** del jackpot sea **determinista** (misma decisión con el mismo seed y config). Tras concederse, el ganador recibe el *pool*, queda registrado en su partida y el *pool* se reinicia a su valor semilla. El simulador y el replay deben reflejar el jackpot ("lo simulado = lo jugado").

## Criterios de aceptación
- **AC1**: Cada giro aporta la fracción configurada de la apuesta al *pool* del jackpot.
- **AC2**: La concesión es **reproducible**: igual en producción, simulador y replay para el mismo seed/config.
- **AC3**: Al concederse, el jugador recibe el *pool*, se registra la concesión y el *pool* se reinicia.
- **AC4**: El simulador mide la contribución del jackpot al RTP empírico.
- **AC5**: Aritmética entera y cero-alloc se mantienen en el camino caliente del kernel.

## Prioridad
Could Have

## Estimación
8 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-domain`, `nova-simulator`, `motor`, `jackpot`, `determinismo`

## Comentarios
- Es el ticket de **mayor riesgo** del bloque post-MVP: toca el motor sin romper su determinismo (golden-master) ni su rendimiento.
- **Dependencias directas:** `HU-26-DB-01` (*pool*) · `HU-1-BE-01` (`SpinKernel`, externa).

## Enlaces y referencias
- Historia: [HU-26](../../stories/HU-26.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Motor y determinismo: [§2.1.7](../../readme.md#217-motor-de-juego-núcleo-data-oriented-y-doble-materialización).
