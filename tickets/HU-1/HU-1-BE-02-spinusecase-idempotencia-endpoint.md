# HU-1-BE-02 — SpinUseCase, idempotencia y endpoint `POST /spin`

## Código
`HU-1-BE-02` — vinculado con **HU-1: El jugador realiza un giro**.

## Título
`SpinUseCase`, idempotencia y endpoint `POST /api/v1/player/games/{gameId}/spin`

## Descripción
Implementar el **caso de uso del spin** (`nova-application`) que orquesta: lectura del wallet con *optimistic locking*, débito de la apuesta, ejecución del `SpinKernel` (`HU-1-BE-01`) con un `RngEngine` sembrado por la `RngFactory` **a través de un `MaterializingSink`** que mapea la salida del kernel a los agregados de dominio (`Round`, `Money`, `SpinResult`), crédito del premio y persistencia atómica de `game_rounds` y `wallet_transactions`. Todo dentro de **una única transacción de BBDD** (rollback total ante cualquier fallo). El `MaterializingSink` es la contraparte de producción del `CountingSink` del simulador (ver readme [§2.1.7](../../readme.md#217-motor-de-juego-núcleo-data-oriented-y-doble-materialización)): mismo kernel, distinta materialización.

Implementa la **idempotencia** sobre la cabecera `Idempotency-Key` (tabla `idempotency_keys`, 3.2.11): un reintento con la misma key devuelve el resultado original; con la misma key y *payload* distinto responde `409`. Ticket **transversal**: la idempotencia la reutiliza la recarga de `HU-6-BE-01`.

## Criterios de aceptación
- **AC1**: `POST .../spin` con apuesta válida devuelve `200` con un `SpinResult` (incluye free spins si procede).
- **AC2**: Tras un spin con premio, `balance = pre - bet + win` y se insertan `wallet_transactions` BET y WIN con `game_round_id` del round.
- **AC3**: Si el motor falla a mitad, la transacción revierte: ni `wallets`, ni `wallet_transactions`, ni `game_rounds` se modifican.
- **AC4**: Apuesta fuera del rango `[min,max]`, **no múltiplo del nº de líneas** de la `config` activa (3.3.3), o superior al saldo → `422`.
- **AC5**: Petición sin `Idempotency-Key` válida (UUID) → `400`.
- **AC6**: Misma `Idempotency-Key` + mismo payload → mismo `SpinResult`, sin segundo round ni movimientos extra.
- **AC7**: Misma `Idempotency-Key` + payload distinto → `409`.
- **AC8**: Dos giros concurrentes del mismo jugador: el *optimistic lock* del `wallet` provoca conflicto; el caso de uso **reintenta** y, si persiste, responde `409` (modificación concurrente del saldo). Nunca se pierde ni se duplica un débito/crédito.

## Prioridad
Must Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-application`, `nova-web-api`, `idempotencia`, `transaccionalidad`, `transversal`

## Comentarios
- El diagrama de secuencia [2.1.6](../../readme.md#216-flujo-de-un-giro-diagrama-de-secuencia) ilustra la orquestación.
- **Dependencias directas:** `HU-1-BE-01` (intra, motor/`SpinKernel`) · `HU-1-DB-01` (intra, esquema/persistencia: `game_rounds`, `wallet_transactions`, `idempotency_keys`) · `HU-4-BE-01` (externa, auth/roles).

## Enlaces y referencias
- Historia: [HU-1](../../stories/HU-1.md).
- Flujo: [2.1.6](../../readme.md#216-flujo-de-un-giro-diagrama-de-secuencia).
- Idempotencia: [2.5.4](../../readme.md#25-seguridad).
- Modelo: [3.2.4, 3.2.8, 3.2.11](../../readme.md#32-descripción-de-entidades-principales).
- API: [4.4.3 Giro](../../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios).
