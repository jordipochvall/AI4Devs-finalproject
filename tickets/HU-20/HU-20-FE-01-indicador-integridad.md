# HU-20-FE-01 — Indicador de integridad en la auditoría del operador

## Código
`HU-20-FE-01` — vinculado con **HU-20: Integridad *tamper-evident* de la auditoría**.

## Título
Verificación visual de integridad en la pantalla de auditoría

## Descripción
Añadir a la auditoría del operador (HU-3-FE-01) un control para verificar la integridad de un rango de partidas (consume `GET /operator/audit/integrity`) y mostrar el resultado (cadena intacta / ruptura detectada, con la primera partida afectada).

## Criterios de aceptación
- **AC1**: Desde la auditoría se puede lanzar la verificación de integridad de un rango.
- **AC2**: El resultado muestra "intacta" o, si falla, la primera partida cuya cadena se rompe.
- **AC3**: La UI funciona en **español e inglés** y solo es accesible con rol `OPERATOR`.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `integridad`, `dgoj`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-20-BE-01`.

## Enlaces y referencias
- Historia: [HU-20](../../stories/HU-20.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Auditoría (MVP): [HU-3](../../stories/HU-3.md).
