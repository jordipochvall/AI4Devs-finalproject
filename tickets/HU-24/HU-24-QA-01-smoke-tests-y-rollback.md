# HU-24-QA-01 — *Smoke tests* post-deploy y ensayo de rollback

## Código
`HU-24-QA-01` — vinculado con **HU-24: Despliegue cloud con entrega continua**.

## Título
Verificación post-despliegue y prueba de reversión

## Descripción
Definir *smoke tests* que se ejecutan tras cada despliegue (a *staging* y a producción) verificando los flujos críticos (login, lobby, giro, simulación), y ensayar un *rollback* a la versión anterior comprobando que el servicio se restablece.

## Criterios de aceptación
- **AC1**: Tras un despliegue, los *smoke tests* verifican login, lobby/giro y simulación; si fallan, el despliegue se marca como no saludable.
- **AC2**: Un *rollback* ensayado restablece la versión anterior y el servicio vuelve a estar sano.
- **AC3**: Los resultados de los *smoke tests* quedan disponibles en el run del pipeline.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `cd`, `smoke-tests`, `cloud`

## Comentarios
- **Dependencias directas:** `HU-24-DEV-02`.

## Enlaces y referencias
- Historia: [HU-24](../../stories/HU-24.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
