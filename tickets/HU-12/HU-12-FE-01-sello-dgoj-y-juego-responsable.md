# HU-12-FE-01 — Sello DGOJ y mensajes de juego responsable

## Código
`HU-12-FE-01` — vinculado con **HU-12: El jugador percibe mensajes de juego responsable y el sello DGOJ**.

## Título
Capa de UI de cumplimiento: sello DGOJ, aviso +18, enlace de juego responsable y aviso por umbral

## Descripción
Implementar en el *layout* del cliente del jugador la capa de cumplimiento: **banner permanente** con el sello DGOJ y el aviso "+18" presente en todas las pantallas (login, lobby, juego), **enlace a "Juego responsable"** con su contenido informativo, y un **aviso contextual** cuando las pérdidas acumuladas en la sesión superan un umbral, invitando a hacer una pausa. Todos los textos vía i18n (HU-11).

## Criterios de aceptación
- **AC1**: El sello DGOJ y el aviso "+18" son visibles de forma permanente en login, lobby y pantalla de juego.
- **AC2**: El enlace "Juego responsable" abre la información correspondiente.
- **AC3**: Al superar el umbral de pérdida de sesión, se muestra el mensaje de pausa de juego responsable.
- **AC4**: La pantalla de login muestra el aviso de mayoría de edad y el mensaje de juego responsable.
- **AC5**: Todos los textos están internacionalizados (ES/EN).

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `dgoj`, `juego-responsable`, `compliance`, `i18n`

## Comentarios
- El umbral de pérdida de sesión es de cliente; los límites persistentes y la autoexclusión son **post-MVP** (1.5-D7).
- Se apoya en `HU-11-FE-01` (i18n) y aparece también en el login de `HU-4-FE-01`.
- **Dependencias directas:** `HU-4-FE-01` (externa, shell de UI autenticado) · `HU-11-FE-01` (externa, i18n de los textos del banner).

## Enlaces y referencias
- Historia: [HU-12](../../stories/HU-12.md).
- Funcionalidad A10: [1.2](../../readme.md#12-características-y-funcionalidades-principales).
- Cumplimiento DGOJ: [2.5.5](../../readme.md#25-seguridad).
