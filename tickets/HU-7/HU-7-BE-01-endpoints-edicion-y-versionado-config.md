# HU-7-BE-01 — Endpoints de edición y versionado de matemática

## Código
`HU-7-BE-01` — vinculado con **HU-7: El matemático edita y versiona la matemática de un juego**.

## Título
Endpoints de edición/versionado de `config` + validación + RTP objetivo declarado

## Descripción
Implementar en `nova-application` + `nova-web-api`:

- `GET /api/v1/math/games` — juegos con su versión de `config` activa.
- `GET /api/v1/math/configs/{configId}` — detalle de una versión (`config` completo del apartado 3.3 + métricas teóricas).
- `POST /api/v1/math/games/{gameId}/configs` — crea una **nueva versión** de matemática: valida el `config` contra el JSON Schema y las invariantes de negocio (3.3.3) y persiste una fila nueva en `game_configs` (inmutable; nunca modifica versiones previas). El **`rtp_target`/`volatility_target` los aporta el matemático** en el cuerpo de la petición (su objetivo de diseño); la plataforma **no** los calcula.

## Criterios de aceptación
- **AC1**: `POST .../configs` con `config` válido crea la versión N+1 persistiendo el `rtp_target` declarado por el matemático y devuelve `201`. La plataforma no deriva el RTP.
- **AC2**: La versión anterior permanece inalterada (verificable: su `config` y `version` no cambian).
- **AC3**: Un `config` inválido (símbolo inexistente, payline fuera de rango, `triggerSymbol` no `SCATTER`, etc.) → `422` con `errors[]` por campo.
- **AC4**: `GET .../configs/{id}` devuelve el `config` completo; inexistente → `404`.
- **AC5**: Solo rol `MATH_ANALYST`; otros roles → `403`.

## Prioridad
Must Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-application`, `nova-web-api`, `matematica`, `versionado`, `validacion`

## Comentarios
- La **publicación** (`POST .../publish`) queda **post-MVP**.
- **Dependencias directas:** `HU-4-BE-01` (externa, auth/roles). El RTP empírico que valida el objetivo declarado lo aporta el simulador (`HU-2`), no este ticket.

## Enlaces y referencias
- Historia: [HU-7](../../stories/HU-7.md).
- Esquema y validación: [3.3](../../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
- Modelo: [3.2.6 game_configs](../../readme.md#32-descripción-de-entidades-principales).
- API: catálogo [4.2](../../readme.md#42-catálogo-de-endpoints).
