# HU-7-QA-01 — Tests de versionado e invariantes de matemática

## Código
`HU-7-QA-01` — vinculado con **HU-7: El matemático edita y versiona la matemática de un juego**.

## Título
Tests de versionado, validación de invariantes e inmutabilidad de versiones

## Descripción
- **Unit**: validación de invariantes del `config` (3.3.3) — símbolos referenciados existen, nº de reels = `grid.cols`, índices de payline en rango, `paytable` solo `REGULAR`, `triggerSymbol` `SCATTER`.
- **Integration (Failsafe + Testcontainers)**: `POST .../configs` válido crea versión N+1 persistiendo el `rtp_target` declarado (`201`); `config` inválido → `422` con `errors[]`; la versión previa permanece inalterada; UPDATE/DELETE manual sobre `game_configs` falla por el trigger de inmutabilidad; `403` por rol no matemático.

## Criterios de aceptación
- **AC1**: Cubiertas las invariantes de 3.3.3 con casos válidos e inválidos.
- **AC2**: Verificado que el `rtp_target` declarado se persiste tal cual (la plataforma no lo calcula ni lo modifica).
- **AC3**: Verificada la inmutabilidad: editar genera versión nueva, nunca modifica la anterior; el trigger bloquea UPDATE/DELETE.
- **AC4**: Cobertura de `nova-application` (casos de uso de matemática) ≥ 80 %.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `matematica`, `versionado`, `inmutabilidad`

## Comentarios
- **Dependencias directas:** `HU-7-BE-01` (intra).

## Enlaces y referencias
- Historia: [HU-7](../../stories/HU-7.md).
- Validación: [3.3.3](../../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
- Inmutabilidad: [2.5.2](../../readme.md#25-seguridad) y 3.2.12.
