# HU-1-DEV-01 — Docker Compose y pipeline CI

## Código
`HU-1-DEV-01` — vinculado con **HU-1: El jugador realiza un giro**.

## Título
Stack Docker Compose (web/api/postgres), variables de entorno y esqueleto del pipeline CI

## Descripción
Crear la infraestructura local de desarrollo y el pipeline base de CI, base sobre la que se ejecutarán las tres HU:

- **`docker-compose.yml`** con tres servicios: `web` (nginx 1.27-alpine que sirve la SPA y hace `proxy_pass /api/` a `api:8080`), `api` (multi-stage Maven sobre `eclipse-temurin:21-jre-alpine`, Spring Boot 3.4) y `postgres` (`postgres:18-alpine`, healthcheck `pg_isready`, volumen `pgdata`).
- **`.env.example`** con todas las variables de entorno listadas en la tabla del apartado 1.4 (`POSTGRES_*`, `JWT_SECRET`, `JWT_TTL_SECONDS`, `ANTHROPIC_API_KEY`, `ANTHROPIC_MODEL`).
- **Multi-stage `Dockerfile`** en `backend/` y `frontend/`.
- **GitHub Actions** `.github/workflows/ci.yml` con el *job* `build-test`: setup JDK 21 + Node 20, cache de Maven y pnpm, `mvn -B verify` y `pnpm install && pnpm test && pnpm build`, publicación de cobertura Jacoco como artefacto. Los *jobs* `perf` y `e2e` se añaden en `HU-2-DEV-01` y `HU-1-QA-01` respectivamente.

> El **esquema y las migraciones Flyway** (su autoría y contenido) son de **`HU-1-DB-01`**; este ticket solo provee el contenedor Postgres y el arranque que las dispara.

Ticket **transversal**: HU-2 y HU-3 lo consumen.

## Criterios de aceptación
- **AC1**: `docker compose up --build` levanta los tres servicios desde un repo limpio; el healthcheck de `postgres` pasa antes de que arranque `api`.
- **AC2**: La SPA es accesible en `http://localhost:5173/` y las peticiones `/api/v1/*` llegan correctamente al backend.
- **AC3**: Al arrancar `api`, las migraciones Flyway de `HU-1-DB-01` se ejecutan automáticamente (este ticket garantiza el contenedor y el orden de arranque; el contenido de las migraciones es de `HU-1-DB-01`).
- **AC4**: `docker compose down -v` elimina el volumen y permite repetir el arranque limpio.
- **AC5**: El *job* `build-test` de GitHub Actions pasa en *pull request* y publica el reporte Jacoco como artefacto.

## Prioridad
Must Have

## Estimación
2 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `docker`, `docker-compose`, `github-actions`, `ci`, `transversal`

## Comentarios
- **Dependencias directas:** ninguna — es el **ticket fundacional** (infra/CI). Casi todos los demás tickets dependen de él directa o transitivamente; a nivel de *historia* se trata como fundación (ver nota en [stories.md](../../stories/stories.md)).
- HTTPS en local **no** se cubre; queda asumido a través de reverse proxy en producción (ver 2.5.4).
- El despliegue cloud público queda como **post-MVP** (ver 1.5, D9).

## Enlaces y referencias
- Historia: [HU-1](../../readme.md#5-historias-de-usuario).
- Infraestructura: [2.4 Infraestructura y despliegue](../../readme.md#24-infraestructura-y-despliegue).
- Variables de entorno: [1.4 Instrucciones de instalación](../../readme.md#14-instrucciones-de-instalación).
- Estructura del repo: [2.3](../../readme.md#23-descripción-de-alto-nivel-del-proyecto-y-estructura-de-ficheros).
