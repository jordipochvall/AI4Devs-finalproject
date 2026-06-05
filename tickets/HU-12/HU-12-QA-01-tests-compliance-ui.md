# HU-12-QA-01 — Tests de la capa de cumplimiento

## Código
`HU-12-QA-01` — vinculado con **HU-12: El jugador percibe mensajes de juego responsable y el sello DGOJ**.

## Título
Tests de presencia del sello DGOJ y disparo de avisos de juego responsable

## Descripción
- **E2E/UI (Playwright)**: verificar que el sello DGOJ y el aviso "+18" están presentes en login, lobby y pantalla de juego; que el enlace "Juego responsable" es accesible; y que al superar el umbral de pérdida de sesión aparece el mensaje de pausa.
- **Test de componente**: el banner se renderiza en el *layout* común de todas las pantallas del jugador.

## Criterios de aceptación
- **AC1**: Verificada la presencia del sello/aviso en las tres pantallas del jugador.
- **AC2**: Verificado el acceso a la información de juego responsable.
- **AC3**: Verificado el disparo del aviso por umbral de pérdida.

## Prioridad
Should Have

## Estimación
1 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `playwright`, `dgoj`, `compliance`

## Comentarios
- **Dependencias directas:** `HU-12-FE-01` (intra).

## Enlaces y referencias
- Historia: [HU-12](../../stories/HU-12.md).
- Cumplimiento DGOJ: [2.5.5](../../readme.md#25-seguridad).
