# HU-27-QA-01 — Tests del modal de detalle de partida

## Código
`HU-27-QA-01` — vinculado con **HU-27: El operador consulta el detalle de una partida desde la auditoría**.

## Título
Tests del modal de detalle (render de importes/rejilla/líneas + cierre)

## Descripción
Cubrir con tests de componente (Vitest + Testing Library) el modal de detalle de partida: que renderiza los importes, la rejilla de símbolos y las líneas ganadoras a partir de un detalle conocido, y que el cierre invoca el callback. Mantener `tsc --noEmit` limpio y la paridad i18n es/en de las claves nuevas.

## Criterios de aceptación
- **AC1**: Un test renderiza el modal con un `RoundDetail` simulado y verifica que aparecen los importes (apuesta/premio), la rejilla (símbolos esperados) y la línea ganadora con su formato.
- **AC2**: Un test verifica que pulsar "Cerrar" (o Escape) invoca `onClose`.
- **AC3**: La suite de frontend queda en verde con `tsc --noEmit` limpio; las claves i18n nuevas existen en **es** y **en**.

## Prioridad
Could Have

## Estimación
1 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `frontend`, `vitest`, `testing-library`, `i18n`

## Comentarios
- El detalle del backend (`GET /operator/rounds/{roundId}`) ya está cubierto por los ITs de `HU-16` (`OperatorDashboardIT`); este ticket cubre **sólo** la nueva superficie de UI.
- **Dependencias directas:** `HU-27-FE-01`.

## Enlaces y referencias
- Historia: [HU-27](../../stories/HU-27.md).
- Ticket de implementación: [HU-27-FE-01](HU-27-FE-01-modal-detalle-partida.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
