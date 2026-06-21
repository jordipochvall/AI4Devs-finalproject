# HU-19-BE-01 — Endpoints de límites/autoexclusión + verificación *server-side* en el spin

## Código
`HU-19-BE-01` — vinculado con **HU-19: Límites de pérdida y autoexclusión impuestos en servidor**.

## Título
`POST /player/limits`, `POST /player/self-exclusion` y *gate* del giro en el servidor

## Descripción
Endpoints **Fase 2** para fijar límites de juego responsable (pérdida/depósito/tiempo) y autoexclusión, y la **verificación previa al giro** en `SpinUseCase`: si un límite está alcanzado o hay autoexclusión vigente, el giro se rechaza **sin** descontar saldo ni registrar partida. Endurecer un límite aplica de inmediato; relajarlo respeta un periodo de enfriamiento.

## Criterios de aceptación
- **AC1**: `POST /player/limits` y `POST /player/self-exclusion` registran la configuración y son aplicables de inmediato.
- **AC2**: Con un límite alcanzado o autoexclusión vigente, `POST .../spin` → rechazo (4xx) **sin** efecto en saldo ni en `game_rounds`.
- **AC3**: Endurecer un límite aplica al instante; aumentarlo queda diferido por el enfriamiento.
- **AC4**: La verificación es **server-side**: no depende del cliente.
- **AC5**: Solo rol `PLAYER` gestiona sus propios límites; otros roles → `403`.

## Prioridad
Should Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-application`, `nova-web-api`, `dgoj`, `juego-responsable`, `fase-2`

## Comentarios
- Endpoints **Fase 2** (nuevos, no contemplados en el contrato del MVP; ver catálogo §4.2).
- **Dependencias directas:** `HU-19-DB-01` (esquema) · `HU-1-BE-02` (el giro donde se impone, externa).

## Enlaces y referencias
- Historia: [HU-19](../../stories/HU-19.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Catálogo (Fase 2): [§4.2](../../readme.md#42-catálogo-de-endpoints) · Decisión diferida [§1.5 D7](../../readme.md#15-supuestos-y-decisiones-diferidas).
