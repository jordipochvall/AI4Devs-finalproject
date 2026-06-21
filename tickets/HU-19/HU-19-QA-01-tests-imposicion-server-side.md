# HU-19-QA-01 — Tests de imposición *server-side* de límites y autoexclusión

## Código
`HU-19-QA-01` — vinculado con **HU-19: Límites de pérdida y autoexclusión impuestos en servidor**.

## Título
Tests de que el servidor bloquea el giro al cruzar límites o durante autoexclusión

## Descripción
Verificar que la imposición es efectiva en servidor: forzar el cruce de un límite de pérdida y comprobar que el siguiente giro se rechaza **sin** descontar saldo ni registrar partida; comprobar el bloqueo durante una autoexclusión vigente y el enfriamiento al relajar. Integración (Failsafe + Testcontainers).

## Criterios de aceptación
- **AC1**: IT — con el límite de pérdida alcanzado, `POST .../spin` se rechaza y no hay cambios en `wallets` ni `game_rounds`.
- **AC2**: IT — durante una autoexclusión vigente, el giro se bloquea.
- **AC3**: IT — endurecer aplica al instante; aumentar un límite no surte efecto hasta pasado el enfriamiento.
- **AC4**: El bypass desde el cliente no evita el bloqueo (la verificación es server-side).

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `dgoj`, `juego-responsable`

## Comentarios
- **Dependencias directas:** `HU-19-BE-01`, `HU-19-FE-01`.

## Enlaces y referencias
- Historia: [HU-19](../../stories/HU-19.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
