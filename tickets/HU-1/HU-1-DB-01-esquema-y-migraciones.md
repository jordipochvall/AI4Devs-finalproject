# HU-1-DB-01 — Esquema y migraciones Flyway

## Código
`HU-1-DB-01` — vinculado con **HU-1: El jugador realiza un giro** (ticket fundacional de datos, transversal a todo el backlog).

## Título
Esquema de base de datos y migraciones Flyway (DDL + triggers de inmutabilidad + seed)

## Descripción
Crear el **esquema completo** de PostgreSQL 18 y sus migraciones Flyway, base sobre la que persiste toda la plataforma (modelo detallado en el [§3 del readme](../../readme.md#3-modelo-de-datos)). Es la **fundación de datos**: cualquier ticket que lea o escriba en BBDD la presupone, igual que la infraestructura.

Migraciones (en `nova-web-api/src/main/resources/db/migration/`):
- **`V1__schema.sql`** — las **11 tablas** (`operators`, `users`, `wallets`, `wallet_transactions`, `games`, `game_configs`, `game_config_publications`, `game_rounds`, `simulation_runs`, `simulation_explanations`, `idempotency_keys`) con sus tipos, `CHECK` (estados como `VARCHAR + CHECK IN`, dinero `BIGINT` céntimos, edad **sin** `CHECK`), claves primarias `BIGSERIAL`, FK con sus **índices explícitos**, `UNIQUE` (p. ej. `(operator_id, email)`, `(game_id, version)`, `(user_id, endpoint, idem_key)`) y el **índice GIN** sobre `game_configs.config`.
- **`V2__immutability_triggers.sql`** — función compartida `fn_forbid_update_delete()` y **4 triggers** `BEFORE UPDATE OR DELETE` sobre las tablas histórico-regulatorias (`game_rounds`, `wallet_transactions`, `game_configs`, `game_config_publications`).
- **`V3__seed.sql`** — datos semilla: operador `novacasino-default`; 3 juegos (Egipcio 5x3, Frutas 3x3, Espacial 5x3) con su `config` y `rtp_target`; usuarios `OPERATOR`, `MATH_ANALYST` y 3 `PLAYER` con 1.000 € de saldo virtual.

## Criterios de aceptación
- **AC1**: Con un volumen vacío, `docker compose up` aplica `V1`/`V2`/`V3` sin error y el esquema `novacasino` queda creado con las 11 tablas.
- **AC2**: Las **constraints** se cumplen: dinero en `BIGINT`, estados restringidos por `CHECK IN (...)`, `balance_cents >= 0`, `UNIQUE` declarados.
- **AC3**: Toda columna FK tiene **índice** (o queda cubierta por el prefijo de un índice/constraint existente); existe el **GIN** sobre `game_configs.config`.
- **AC4**: Cualquier `UPDATE` o `DELETE` sobre `game_rounds`, `wallet_transactions`, `game_configs` o `game_config_publications` **falla** con la excepción de `fn_forbid_update_delete`.
- **AC5**: El **seed** crea los 3 juegos y los usuarios semilla con los saldos esperados; los logins semilla funcionan.
- **AC6**: **Idempotencia de Flyway**: re-arrancar el contenedor no re-aplica migraciones ni duplica datos; añadir una `V4__*.sql` aplica solo la nueva.

## Prioridad
Must Have

## Estimación
5 SP

## Equipo responsable
DB

## Etiquetas
`bbdd`, `postgresql`, `flyway`, `ddl`, `triggers`, `inmutabilidad`, `seed`, `fundacion-datos`

## Comentarios
- **Dependencias directas:** `HU-1-DEV-01` (externa, contenedor Postgres + arranque de la app que dispara Flyway). Es **fundación de datos**: todos los tickets de persistencia (`HU-1-BE-02`, `HU-5-BE-01`, `HU-6-BE-01`, `HU-7-BE-01`, `HU-2-BE-02`, `HU-3-BE-01/02`, `HU-8-BE-01`) la presuponen — no se re-cablea en cada uno (mismo tratamiento que la infra).
- El particionado de `game_rounds` queda **fuera de v1** (ver nota del §3); PK simple.
- Extraído de `HU-1-DEV-01` para tener un ticket de BBDD explícito.

## Enlaces y referencias
- Historia: [HU-1](../../stories/HU-1.md).
- **Modelo de datos: [§3](../../readme.md#3-modelo-de-datos)** (entidades 3.2.x; triggers 3.2.12).
- Inmutabilidad: [§2.5.2](../../readme.md#25-seguridad).
- Esquema del `config`: [§3.3](../../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
- Infra que la ejecuta: `HU-1-DEV-01`.
