# HU-6-BE-01 — Endpoints de jugadores y recarga idempotente

## Código
`HU-6-BE-01` — vinculado con **HU-6: El operador gestiona jugadores y recarga su saldo**.

## Título
Endpoints de jugadores y recarga idempotente del wallet

## Descripción
Implementar en `nova-application` + `nova-web-api`:

- `GET /api/v1/operator/players` — búsqueda paginada de jugadores por email (parcial, case-insensitive) con su saldo.
- `POST /api/v1/operator/players/{playerId}/wallet/recharge` — recarga del saldo virtual. Operación con efecto económico: **idempotente** (cabecera `Idempotency-Key`, reutilizando el mecanismo de `HU-1-BE-02`) y transaccional. Inserta un `wallet_transactions` de tipo `RECHARGE` con `performed_by_user_id` = operador.

## Criterios de aceptación
- **AC1**: `GET /operator/players?email=ana` devuelve la página de jugadores cuyo email contiene "ana", con su saldo.
- **AC2**: `recharge` de importe positivo incrementa el saldo y registra el movimiento `RECHARGE` con el operador.
- **AC3**: Importe ≤ 0 → `422`; jugador inexistente → `404`.
- **AC4**: Misma `Idempotency-Key` reenviada → el saldo solo se incrementa una vez.
- **AC5**: Solo rol `OPERATOR`; otros roles → `403`.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-application`, `nova-web-api`, `wallet`, `idempotencia`

## Comentarios
- Reutiliza el mecanismo de idempotencia y la tabla `idempotency_keys` introducidos en `HU-1-BE-02`.
- **Dependencias directas:** `HU-4-BE-01` (externa, auth/roles) · `HU-1-BE-02` (externa, mecanismo de idempotencia que reutiliza la recarga).

## Enlaces y referencias
- Historia: [HU-6](../../stories/HU-6.md).
- API: catálogo [4.2](../../readme.md#42-catálogo-de-endpoints).
- Modelo: [3.2.3 wallets, 3.2.4 wallet_transactions, 3.2.11 idempotency_keys](../../readme.md#32-descripción-de-entidades-principales).
