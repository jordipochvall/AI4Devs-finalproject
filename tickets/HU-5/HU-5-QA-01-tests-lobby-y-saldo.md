# HU-5-QA-01 — Tests de lobby y saldo

## Código
`HU-5-QA-01` — vinculado con **HU-5: El jugador accede al lobby y consulta su saldo**.

## Título
Tests de catálogo, detalle de juego y saldo

## Descripción
Tests del flujo de navegación del jugador:

- **Integration (Failsafe + Testcontainers)**: `GET /player/games` devuelve solo activos; `GET /player/games/{id}` devuelve el `config` correcto y `404` para inexistente/inactivo; `GET /player/wallet` devuelve el saldo del jugador del token y no el de otros; autorización por rol (`403` para no-`PLAYER`).
- **E2E (Playwright)**: login → el lobby muestra los 3 juegos semilla y el saldo → click en una carátula → se abre la pantalla de juego con su rejilla.

## Criterios de aceptación
- **AC1**: Cubiertos los casos `200`/`404`/`403` de los tres endpoints.
- **AC2**: Verificado que un juego inactivo no se lista ni es accesible.
- **AC3**: El E2E confirma que el lobby carga el catálogo y el saldo y navega al juego.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `playwright`, `lobby`

## Comentarios
- **Dependencias directas:** `HU-5-FE-01` (intra) — arrastra `HU-5-BE-01`.

## Enlaces y referencias
- Historia: [HU-5](../../stories/HU-5.md).
- Estrategia de tests: [2.6](../../readme.md#26-tests).
