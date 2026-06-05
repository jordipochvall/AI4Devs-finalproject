# HU-5-FE-01 — Lobby y saldo

## Código
`HU-5-FE-01` — vinculado con **HU-5: El jugador accede al lobby y consulta su saldo**.

## Título
Lobby con catálogo de juegos y saldo del jugador

## Descripción
Implementar el **lobby** del jugador (`frontend/src/player/`): rejilla responsiva con las carátulas, nombre y temática de los juegos activos (`GET /player/games`), el saldo virtual en la cabecera (`GET /player/wallet`) y la navegación a la pantalla de juego al hacer click en una carátula (que cargará el `config` vía `GET /player/games/{id}` y montará el `<SlotGame>` de `HU-1-FE-01`).

Se separó del antiguo ticket de "registro/login/lobby": la auth UI es ahora `HU-4-FE-01`.

## Criterios de aceptación
- **AC1**: El lobby muestra las carátulas de los juegos activos en una rejilla responsiva.
- **AC2**: El saldo es visible en la cabecera con dos decimales y divisa.
- **AC3**: Al hacer click en una carátula, navega a `/play/{gameId}` y se carga su `config`.
- **AC4**: Un juego inactivo no aparece en el lobby.
- **AC5**: La UI funciona en **español e inglés**.

## Prioridad
Must Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `tanstack-query`, `lobby`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-5-BE-01` (intra, endpoints) · `HU-4-FE-01` (externa, sesión). El lobby **navega** a `/play/{id}` (`HU-1-FE-01`) por enrutado, pero **no** la requiere construida (se rompe así el ciclo lobby↔juego).

## Enlaces y referencias
- Historia: [HU-5](../../stories/HU-5.md).
- Funcionalidad A2: [1.2](../../readme.md#12-características-y-funcionalidades-principales).
- Flujo 1: [1.3](../../readme.md#13-diseño-y-experiencia-de-usuario).
