# HU-7-FE-01 — Editor de matemática

## Código
`HU-7-FE-01` — vinculado con **HU-7: El matemático edita y versiona la matemática de un juego**.

## Título
Editor de matemática en el backoffice matemático

## Descripción
Implementar en la superficie del matemático (`frontend/src/math/`) el **editor de la `config`**: selector de juego y versión (`GET /math/games`, `GET /math/configs/{id}`), editor del objeto JSON (apartado 3.3) con validación de esquema en cliente, y acción de **guardar** que llama a `POST /math/games/{id}/configs` y muestra los `errors[]` del backend si el `config` es inválido. Tras guardar, la nueva versión aparece como la última.

## Criterios de aceptación
- **AC1**: Se puede seleccionar un juego y ver su versión de `config` activa.
- **AC2**: El editor valida en cliente el esquema básico y resalta errores antes de enviar.
- **AC3**: Al guardar un `config` inválido, se muestran los errores devueltos por el backend (en línea o como lista).
- **AC4**: El editor permite **declarar** el `rtp_target`/`volatility_target` (objetivo del matemático) y, al guardar un `config` válido, se crea una versión nueva con ese objetivo declarado visible.
- **AC5**: Solo rol `MATH_ANALYST` accede; la UI funciona en **español e inglés**.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `editor-json`, `matematica`, `i18n`

## Comentarios
- El panel de simulación y el dashboard de métricas son de `HU-2-FE-01`; este ticket cubre solo la edición/versionado.
- **Dependencias directas:** `HU-7-BE-01` (intra).

## Enlaces y referencias
- Historia: [HU-7](../../stories/HU-7.md).
- Funcionalidad C1: [1.2](../../readme.md#12-características-y-funcionalidades-principales).
- Esquema del config: [3.3](../../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
