# HU-20-BE-01 — Cadena de integridad en la inserción + endpoint de verificación

## Código
`HU-20-BE-01` — vinculado con **HU-20: Integridad *tamper-evident* de la auditoría**.

## Título
Cálculo del hash encadenado al registrar el round y `GET /operator/audit/integrity`

## Descripción
Calcular el hash de integridad de cada `game_round` al insertarlo (encadenando el hash de la fila anterior) y exponer un endpoint **Fase 2** que verifica la consistencia de la cadena para un rango, identificando la primera fila rota si la hay. El cálculo en la inserción debe ser ligero para no degradar el giro.

## Criterios de aceptación
- **AC1**: Al registrar un round se calcula y persiste su hash encadenado.
- **AC2**: `GET /operator/audit/integrity` confirma una cadena intacta para un rango dado.
- **AC3**: Si una fila se altera ilícitamente, la verificación falla e identifica la primera ruptura.
- **AC4**: El cálculo no degrada el rendimiento del giro de forma perceptible.
- **AC5**: Solo rol `OPERATOR`; otros roles → `403`.

## Prioridad
Could Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `integridad`, `dgoj`, `fase-2`

## Comentarios
- Endpoint **Fase 2** (nuevo; ver catálogo §4.2). La firma con clave externa de custodia queda como ampliación (D3).
- **Dependencias directas:** `HU-20-DB-01` (columna de hash) · `HU-3-BE-01` (auditoría/rounds, externa).

## Enlaces y referencias
- Historia: [HU-20](../../stories/HU-20.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Replay guardar-y-renderizar: [§2.5.3](../../readme.md#253-rng-criptográficamente-fuerte-y-replay-determinista).
