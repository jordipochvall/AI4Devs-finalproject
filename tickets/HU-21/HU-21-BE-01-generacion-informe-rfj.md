# HU-21-BE-01 — Generación del informe regulatorio RFJ + *gating* por integridad

## Código
`HU-21-BE-01` — vinculado con **HU-21: Generación de informes regulatorios DGOJ (RFJ)**.

## Título
`POST /operator/reports/rfj` con agregados del periodo y bloqueo ante integridad rota

## Descripción
Endpoint **Fase 2** que genera el informe regulatorio (formato RFJ) de un periodo a partir de `game_rounds`, referenciando el estado de integridad (HU-20). Si la verificación de integridad del periodo falla, la generación se **bloquea** para no reportar datos manipulados.

## Criterios de aceptación
- **AC1**: `POST /operator/reports/rfj` genera el documento con los agregados regulatorios del periodo.
- **AC2**: El informe referencia el estado de integridad del periodo.
- **AC3**: Si la integridad del periodo falla, la generación se bloquea e indica la inconsistencia.
- **AC4**: Solo perfiles autorizados (`OPERATOR`); otros roles → `403`.

## Prioridad
Could Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `dgoj`, `informes`, `fase-2`

## Comentarios
- Endpoint **Fase 2** (nuevo; ver catálogo §4.2). El formato exacto se ajusta a la norma vigente.
- **Dependencias directas:** `HU-20-BE-01` (integridad verificable; y, transitivamente, `HU-3`).

## Enlaces y referencias
- Historia: [HU-21](../../stories/HU-21.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Decisión diferida: [§1.5 D6](../../readme.md#15-supuestos-y-decisiones-diferidas) · Modelo [§3.2.8 `game_rounds`](../../readme.md#32-descripción-de-entidades-principales).
