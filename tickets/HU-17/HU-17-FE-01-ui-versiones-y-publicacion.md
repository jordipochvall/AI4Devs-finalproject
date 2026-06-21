# HU-17-FE-01 — UI de versiones y publicación en el editor de matemática

## Código
`HU-17-FE-01` — vinculado con **HU-17: El matemático publica y versiona la matemática activa**.

## Título
Listado de versiones y acción de publicación en el backoffice matemático

## Descripción
Añadir al editor de matemática (HU-7-FE-01) un panel de **versiones** del juego (con la activa destacada) y un botón **Publicar** que activa la versión seleccionada (`POST .../publish`), con confirmación y aviso si su RTP empírico no ha convergido aún a su objetivo.

## Criterios de aceptación
- **AC1**: El panel lista las versiones con su número, `rtp_target` y cuál está activa.
- **AC2**: Publicar una versión la activa y refresca el estado (la nueva queda marcada como activa).
- **AC3**: Intentar publicar la versión ya activa muestra el conflicto (`409`) sin romper la UI.
- **AC4**: La UI funciona en **español e inglés**.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `math`, `versionado`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-17-BE-01`.

## Enlaces y referencias
- Historia: [HU-17](../../stories/HU-17.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Diseño y UX (backoffice matemático): [§1.3](../../readme.md#13-diseño-y-experiencia-de-usuario).
