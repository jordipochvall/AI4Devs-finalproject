# HU-17-QA-01 — Tests de publicación y activación

## Código
`HU-17-QA-01` — vinculado con **HU-17: El matemático publica y versiona la matemática activa**.

## Título
Tests de versionado, publicación y activación efectiva

## Descripción
Verificar que publicar una versión mueve `active_config_id`, registra la publicación inmutable y que el jugador recibe la nueva `config` en su siguiente giro; y que publicar la versión activa devuelve `409`. Integración (Failsafe + Testcontainers).

## Criterios de aceptación
- **AC1**: IT — `publish` mueve `active_config_id` y crea la fila en `game_config_publications`.
- **AC2**: IT — tras publicar, el detalle del juego del jugador sirve la nueva versión.
- **AC3**: IT — publicar la versión ya activa → `409`; rol no matemático → `403`.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `math`, `versionado`

## Comentarios
- **Dependencias directas:** `HU-17-BE-01`, `HU-17-FE-01`.

## Enlaces y referencias
- Historia: [HU-17](../../stories/HU-17.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
