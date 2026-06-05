# HU-6-FE-01 — Backoffice operador: búsqueda y recarga

## Código
`HU-6-FE-01` — vinculado con **HU-6: El operador gestiona jugadores y recarga su saldo**.

## Título
Backoffice operador: búsqueda de jugadores y recarga de saldo

## Descripción
Implementar en la superficie del operador (`frontend/src/operator/`) la pantalla de **gestión de jugadores**: buscador por email (consume `GET /operator/players`), tabla paginada con el saldo de cada jugador y un diálogo de **recarga** que invoca `POST .../wallet/recharge` con una `Idempotency-Key` generada en cliente. Al confirmar, el saldo se refresca.

## Criterios de aceptación
- **AC1**: El buscador muestra los jugadores cuyo email coincide, paginados.
- **AC2**: El diálogo de recarga valida en cliente que el importe sea > 0 y muestra el error de servidor si lo hubiera (`422`).
- **AC3**: Tras una recarga correcta, el saldo del jugador se actualiza en la tabla.
- **AC4**: Un reintento por doble click no duplica la recarga (misma `Idempotency-Key`).
- **AC5**: La UI funciona en **español e inglés**.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `tanstack-query`, `operador`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-6-BE-01` (intra).

## Enlaces y referencias
- Historia: [HU-6](../../stories/HU-6.md).
- Funcionalidad B1: [1.2](../../readme.md#12-características-y-funcionalidades-principales).
