# HU-24-DEV-02 — Pipeline de entrega continua (staging → producción)

## Código
`HU-24-DEV-02` — vinculado con **HU-24: Despliegue cloud con entrega continua**.

## Título
Pipeline de CD con promoción a *staging* y a producción con aprobación y rollback

## Descripción
Pipeline de **CD** que, tras la CI, construye y publica imágenes versionadas, despliega a *staging* automáticamente (con migraciones y *health checks*) y promociona a producción **previa aprobación**, sin downtime perceptible y con *rollback* disponible.

## Criterios de aceptación
- **AC1**: Al integrar en la rama principal se construyen y publican imágenes versionadas de API y web.
- **AC2**: *Staging* se despliega automáticamente con migraciones aplicadas y comprobación de salud.
- **AC3**: La promoción a producción requiere aprobación y permite *rollback*.

## Prioridad
Should Have

## Estimación
5 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `ci`, `cd`, `cloud`, `fase-post-mvp`

## Comentarios
- **Dependencias directas:** `HU-24-DEV-01` (IaC y secretos del entorno).

## Enlaces y referencias
- Historia: [HU-24](../../stories/HU-24.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Infra y despliegue: [§2.4](../../readme.md#24-infraestructura-y-despliegue).
