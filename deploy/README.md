# Despliegue continuo (HU-24, decisión D9)

Entrega continua a cloud sobre la CI existente (`.github/workflows/ci.yml`). El pipeline de CD
(`.github/workflows/cd.yml`) construye y publica imágenes versionadas, despliega a *staging* con
comprobación de salud y promociona a producción con **aprobación manual** y **rollback** ensayado.

## Flujo

1. **build-images** — al integrar en `main`, construye `…-api` y `…-web` etiquetadas por `git sha` y
   `latest`, y las publica en el registro (`ghcr.io`).
2. **deploy-staging** — `deploy/deploy.sh staging <version>` levanta el stack
   (`deploy/docker-compose.prod.yml`); Flyway aplica migraciones al arrancar la API; espera *health*
   y ejecuta `scripts/smoke-test.sh`.
3. **deploy-production** — requiere aprobación (GitHub *Environment* `production` con *required
   reviewers*); despliega, hace *smoke test* y, si falla, ejecuta `deploy/rollback.sh`.

## Secretos (nunca en la imagen)

Inyectados desde el gestor de secretos / GitHub Environments:
`*_DB_URL`, `*_DB_USER`, `*_DB_PASSWORD`, `*_JWT_SECRET`, `*_ANTHROPIC_API_KEY`, `*_BASE_URL`
(prefijos `STAGING_`/`PROD_`).

## Artefactos

| Archivo | Rol |
|---|---|
| `deploy/docker-compose.prod.yml` | IaC del stack (postgres + api + web) con *health checks* |
| `deploy/deploy.sh` | Despliegue + puerta de salud por entorno |
| `deploy/rollback.sh` | Vuelta a la última versión buena |
| `scripts/smoke-test.sh` | *Smoke tests* post-deploy (salud, login semilla, guard de auth) |

## Fuera de alcance

Multi-región activo-activo, autoescalado avanzado y DR con RPO/RTO formalizados (anotado en la
historia). El proveedor cloud y la herramienta de IaC concretos son negociables; aquí se usa Docker
Compose como IaC reproducible.
