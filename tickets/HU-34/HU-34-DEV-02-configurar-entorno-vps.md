# HU-34-DEV-02 — Configurar el Environment de GitHub apuntando al VPS

## Código
`HU-34-DEV-02` — vinculado con **HU-34: Versionar y activar el pipeline de CI/CD para poder desplegar al VPS**.

## Título
Configurar el *Environment* de despliegue (secretos + aprobación) para el VPS del demo

## Descripción
Configurar en GitHub el *Environment* que `cd.yml` usa para desplegar (`staging`/`production`, según se decida) con sus secretos: `*_DB_URL`, `*_DB_USER`, `*_DB_PASSWORD`, `*_JWT_SECRET`, `*_ANTHROPIC_API_KEY`, `*_BASE_URL`, apuntando a la base de datos y al VPS reales del demo. Añadir *required reviewers* en el *Environment* de destino final, tal y como ya prevé `cd.yml` (comentario: "configure required reviewers in repo settings → Environments"). Documentar en `deploy/README.md` cómo se llega desde el *runner* de GitHub Actions hasta el VPS (SSH, *self-hosted runner* u otro mecanismo elegido).

## Criterios de aceptación
- **AC1**: El *Environment* de destino existe en GitHub con todos los secretos que `cd.yml` referencia.
- **AC2**: El *Environment* de producción tiene *required reviewers* configurados (aprobación manual antes de desplegar).
- **AC3**: Un despliegue de prueba desde `cd.yml` llega a ejecutar `deploy.sh` contra el VPS y corre `scripts/smoke-test.sh`.
- **AC4**: `deploy/README.md` documenta cómo se conecta el pipeline con el VPS concreto.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `ci-cd`, `secretos`, `despliegue`

## Comentarios
- Requiere que **HU-33** esté resuelta antes de un intento real de despliegue (si no, el propio `deploy.sh`/*smoke test* fallarían por el healthcheck). ✅ HU-33 ya implementada.
- **Bloqueado**: aún no hay un VPS ni dominio reales sobre los que configurar el *Environment* (confirmado con el equipo). Sin esto no hay a qué apuntar `*_DB_URL`/`*_BASE_URL` ni sobre qué host desplegar. Se retoma en cuanto se disponga de esos datos.
- **Dependencias directas:** `HU-34-DEV-01` (✅), `HU-33` (✅ healthcheck).

## Estado
⏳ **Bloqueado** — pendiente de disponer de un VPS/dominio real.

## Enlaces y referencias
- Historia: [HU-34](../../stories/HU-34.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
