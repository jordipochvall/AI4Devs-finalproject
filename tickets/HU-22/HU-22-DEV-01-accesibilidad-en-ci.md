# HU-22-DEV-01 — Integración de la auditoría de accesibilidad en CI

## Código
`HU-22-DEV-01` — vinculado con **HU-22: Accesibilidad WCAG 2.1 AA**.

## Título
Job/paso de CI que ejecuta la auditoría de accesibilidad y falla ante regresiones

## Descripción
Añadir al pipeline un paso que ejecute la auditoría automática (axe) sobre las vistas principales y **falle el build** si aparecen violaciones de nivel A/AA, para evitar regresiones de accesibilidad.

## Criterios de aceptación
- **AC1**: El pipeline ejecuta la auditoría de accesibilidad en cada integración.
- **AC2**: El build termina en rojo si se introducen violaciones A/AA.
- **AC3**: El job se cachea (Node/deps) para no penalizar el tiempo de CI.

## Prioridad
Should Have

## Estimación
1 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `ci`, `accesibilidad`, `wcag`

## Comentarios
- Se apoya en la suite de `HU-22-QA-01`.
- **Dependencias directas:** `HU-22-QA-01`.

## Enlaces y referencias
- Historia: [HU-22](../../stories/HU-22.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Pipeline CI: [§2.4](../../readme.md#24-infraestructura-y-despliegue).
