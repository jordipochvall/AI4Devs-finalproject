# HU-3-QA-01 — Tests de auditoría y reproducibilidad del replay

## Código
`HU-3-QA-01` — vinculado con **HU-3: El operador resuelve una reclamación con el replay**.

## Título
Tests de auditoría (filtros, paginación) y de reproducibilidad del *replay* (mismo `roundId` → mismo resultado)

## Descripción
Suite de tests del flujo de auditoría y replay del operador:

- **Integration (Failsafe + Testcontainers)** sobre `GET /operator/rounds`: con un dataset semilla generado por `HU-1-BE-02`, verifica filtros por `playerId`, `gameId`, `from`/`to`, paginación, orden por `created_at DESC` y autorización por rol (`PLAYER` → `403`).
- **Reproducibilidad del replay**: para 50 rounds auditados, invocar dos veces `GET /operator/rounds/{id}/replay` devuelve respuestas **byte-a-byte idénticas** (render del mismo registro inmutable). El replay **no recalcula**: la fidelidad la da el registro, no el motor.
- **Resiliencia al drift del motor**: el endpoint de replay devuelve `200` con el `result` registrado **aunque el `SpinKernel` haya evolucionado** (no hay gate de recomputación que pueda romperse). La verificación del determinismo del motor **no vive aquí**: es el *golden-master* de `HU-1-QA-01`.
- **Negativo**: `GET /operator/rounds/{id}/replay` para un `roundId` inexistente devuelve `404`.
- **Integration de inmutabilidad** (cross-cutting con HU-1-QA-01 pero se valida explícitamente aquí): un UPDATE o DELETE manual sobre `game_rounds` falla por el trigger `fn_forbid_update_delete`.

## Criterios de aceptación
- **AC1**: La cobertura mínima de `nova-application` (cases de uso `AuditQueryUseCase`, `ReplayRoundUseCase`) es ≥ 80 %.
- **AC2**: El test de reproducibilidad confirma que la misma petición devuelve siempre la misma respuesta (render del registro).
- **AC3**: El test de resiliencia confirma que el replay sigue devolviendo `200` con el registro inmutable aunque el motor cambie (no hay gate de recomputación). La estabilidad de `seed + config → result` se verifica en el *golden-master* de `HU-1-QA-01`.
- **AC4**: Los tests pasan en CI (job `build-test`).

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `junit`, `testcontainers`, `replay`, `determinismo`, `auditoria`, `dgoj`

## Comentarios
- Estos tests son la red de seguridad del valor diferencial "replay fiel" del producto y de la promesa de inmutabilidad para la DGOJ: el replay muestra el registro inmutable y resiste la evolución del motor. La verificación del determinismo del motor en sí es el *golden-master* de `HU-1-QA-01`.
- **Dependencias directas:** `HU-3-BE-01`, `HU-3-BE-02` (intra) — ambos arrastran `HU-1-BE-02`.

## Enlaces y referencias
- Historia: [HU-3](../../readme.md#5-historias-de-usuario).
- Estrategia de tests: [2.6 Tests](../../readme.md#26-tests).
- RNG y replay: [2.5.3](../../readme.md#25-seguridad).
- Inmutabilidad: [2.5.2](../../readme.md#25-seguridad) y 3.2.12.
