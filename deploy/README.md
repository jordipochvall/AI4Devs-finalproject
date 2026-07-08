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

**Cuentas semilla (HU-38).** El sembrado de cuentas demo (`admin@nova.test`, `operator@nova.test`,
`math@nova.test`, `player{1,2,3}@nova.test`) se mantiene siempre — útil para que un evaluador entre
sin pedir alta — pero sus contraseñas se pueden fijar por entorno: `SEED_ADMIN_PASSWORD`,
`SEED_OPERATOR_PASSWORD`, `SEED_MATH_PASSWORD`, `SEED_PLAYER_PASSWORD`. En blanco/sin definir, caen
en los valores de desarrollo (`admin123`, etc.). Si se fija `SEED_PLAYER_PASSWORD` en el despliegue,
pasar el mismo valor a `scripts/smoke-test.sh` (variable de entorno del mismo nombre) para que el
*smoke test* siga pudiendo loguearse.

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

## Demo pública gratuita (Vercel + Render + Neon)

Alternativa al pipeline anterior (que asume un VPS propio, todavía no disponible — ver HU-34/HU-35)
para publicar una demo online gratuita de este TFM, desplegada directamente desde GitHub sin
necesidad de gestionar servidores:

- **Vercel** — solo el frontend (`frontend/`, build estático de Vite). `frontend/vercel.json` hace de
  *reverse proxy*: reescribe `/api/*` hacia el backend de Render en el servidor (el navegador solo ve
  un origen), igual que hoy hace `nginx.conf` con el servicio `api` en Docker Compose — así no hace
  falta configurar CORS en el backend.
- **Render** (plan Free, *Web Service* de Docker) — el backend (`backend/Dockerfile`), como
  contenedor persistente real. Se eligió sobre el propio soporte de Docker de Vercel porque ahí el
  backend correría como *Vercel Function* (escala a cero, límites de duración por invocación) y este
  proyecto lanza simulaciones en un hilo en segundo plano que sigue corriendo después de responder al
  HTTP (HU-37, `SimulationExecutor`) — un patrón que no encaja con ese modelo serverless.
- **Neon** (Free tier) — Postgres gestionado. Flyway aplica el esquema y `SeedDataLoader` siembra las
  cuentas demo (`admin@nova.test`, etc.) en el primer arranque; no hace falta ningún paso manual de
  BBDD. Se usa el connection string **directo** de Neon (sin el sufijo `-pooler`): el pooler de Neon es
  PgBouncer en modo *transaction*, incompatible con los *advisory locks* de sesión que usa Flyway para
  coordinar migraciones. Con un único contenedor y el pool de Hikari ya acotado
  (`maximum-pool-size: 10`), el endpoint directo no tiene problema de límite de conexiones.

### Orden de despliegue

1. **Neon** — crear proyecto gratis, copiar el connection string directo
   (`postgresql://usuario:password@ep-xxx.<region>.aws.neon.tech/novacasino?sslmode=require`).
2. **Render** — *New Web Service* → conectar el repo de GitHub → Root Directory `backend`, entorno
   Docker, plan Free, *Health Check Path* `/actuator/health`, *Auto-Deploy* sobre la rama elegida.
   Variables de entorno: `SPRING_DATASOURCE_URL`/`_USERNAME`/`_PASSWORD` (de Neon), `JWT_SECRET`
   (`openssl rand -base64 48`), opcionalmente `SEED_ADMIN_PASSWORD`/`SEED_OPERATOR_PASSWORD`/
   `SEED_MATH_PASSWORD`/`SEED_PLAYER_PASSWORD`. Anotar la URL asignada (`https://<nombre>.onrender.com`).
3. Sustituir el placeholder `REPLACE_WITH_RENDER_URL` en `frontend/vercel.json` por esa URL real
   (commit + push).
4. **Vercel** — importar el mismo repo → Root Directory `frontend` (detecta Vite/`vercel.json`
   automáticamente), plan Hobby, *Auto-Deploy* sobre la misma rama.

### Limitaciones conocidas (aceptables para una demo de TFM)

- Render Free hace *spin down* tras 15 min sin tráfico; la primera petición tras inactividad tarda
  unos segundos en despertar el contenedor y arrancar el contexto de Spring.
- Neon Free también escala a cero; el primer acceso a la BBDD tras inactividad añade una latencia
  adicional pequeña antes de que Render termine de arrancar.
- 512 MB de RAM en Render — de ahí el `-XX:MaxRAMPercentage=75.0` en `backend/Dockerfile`.
