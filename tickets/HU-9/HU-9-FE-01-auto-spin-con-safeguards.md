# HU-9-FE-01 — Auto-spin con safeguards

## Código
`HU-9-FE-01` — vinculado con **HU-9: El jugador usa auto-spin con safeguards de juego responsable**.

## Título
Auto-spin con safeguards de juego responsable

## Descripción
Añadir al componente `<SlotGame>` (`HU-1-FE-01`) el modo **auto-spin**: el cliente repite el `spin` automáticamente N veces y aplica límites de parada (nº de giros, umbral de saldo, y opcionalmente pérdida máxima de sesión). Al cruzar un límite o agotarse el saldo, se detiene y muestra un **mensaje de pausa de juego responsable**. Incluye botón de parada manual.

## Criterios de aceptación
- **AC1**: Activar auto-spin de N giros ejecuta giros secuenciales actualizando saldo y rejilla en cada uno.
- **AC2**: Se detiene automáticamente al completar los N giros.
- **AC3**: Se detiene y muestra el mensaje de pausa si el saldo baja del umbral configurado.
- **AC4**: El botón "Detener" para el auto-spin tras el giro en curso.
- **AC5**: Si el saldo es inferior a la apuesta, el auto-spin no ejecuta más giros.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `auto-spin`, `juego-responsable`, `dgoj`

## Comentarios
- No añade endpoint: reutiliza `POST .../spin` (`HU-1-BE-02`) con su `Idempotency-Key`.
- **Dependencias directas:** `HU-1-FE-01` (externa, `<SlotGame>`).

## Enlaces y referencias
- Historia: [HU-9](../../stories/HU-9.md).
- Funcionalidad A5: [1.2](../../readme.md#12-características-y-funcionalidades-principales).
