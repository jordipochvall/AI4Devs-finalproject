# HU-19-FE-01 — UI de configuración de límites y autoexclusión

## Código
`HU-19-FE-01` — vinculado con **HU-19: Límites de pérdida y autoexclusión impuestos en servidor**.

## Título
Pantalla de juego responsable: límites y autoexclusión

## Descripción
Añadir a la superficie del jugador una pantalla para configurar límites (pérdida/depósito/tiempo) y solicitar la autoexclusión, consumiendo los endpoints **Fase 2**. Refleja el periodo de enfriamiento al relajar un límite y muestra el estado vigente. Si el servidor rechaza un giro por límite/autoexclusión, presenta el mensaje de pausa de juego responsable.

## Criterios de aceptación
- **AC1**: El jugador puede fijar/endurecer límites y autoexcluirse desde la UI.
- **AC2**: Relajar un límite muestra que el cambio queda diferido por el enfriamiento.
- **AC3**: Ante el rechazo del servidor por límite/autoexclusión, se muestra el mensaje de pausa sin romper la UI.
- **AC4**: La UI funciona en **español e inglés**.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `juego-responsable`, `dgoj`, `i18n`

## Comentarios
- Se integra con la capa de juego responsable del MVP (HU-12) y el manejo de errores del giro (HU-1-FE-01).
- **Dependencias directas:** `HU-19-BE-01`.

## Enlaces y referencias
- Historia: [HU-19](../../stories/HU-19.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Juego responsable: [HU-12](../../stories/HU-12.md).
