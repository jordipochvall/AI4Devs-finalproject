# HU-33-QA-01 — Tests del healthcheck y verificación del despliegue

## Código
`HU-33-QA-01` — vinculado con **HU-33: Bug — `/actuator/health` exige autenticación y rompe el propio despliegue**.

## Título
Test de integración del healthcheck público + verificación manual del compose de producción

## Descripción
Añadido `ActuatorHealthIT` (extiende `AbstractIntegrationTest`) con dos casos: `GET /actuator/health` sin token → `200`/`"status":"UP"`; `GET /api/v1/player/games` sin token → sigue `401` (regresión: el `permitAll` no se ha ampliado por error a rutas de negocio). Ejecutado contra la base de datos de desarrollo ya levantada (`-Dit.jdbcUrl=jdbc:postgresql://localhost:5432/novacasino`, ya que Testcontainers no puede abrir el socket de Docker Desktop vía *named pipe* en este entorno — mismo *workaround* ya documentado en `AbstractIntegrationTest`): **2/2 en verde**. Verificación manual pendiente: levantar `deploy/docker-compose.prod.yml` y confirmar que `api` llega a `healthy` y que `scripts/smoke-test.sh` pasa su primer chequeo.

## Criterios de aceptación
- **AC1**: Test IT: `GET /actuator/health` sin token → `200`.
- **AC2**: Test IT de regresión: un *endpoint* de negocio protegido sin token sigue devolviendo `401`.
- **AC3**: Verificación manual documentada: `docker compose -f deploy/docker-compose.prod.yml up` deja `api` en `healthy` y `scripts/smoke-test.sh` pasa el chequeo de salud.

## Prioridad
Must Have

## Estimación
1 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `backend`, `seguridad`, `despliegue`

## Comentarios
- **Dependencias directas:** `HU-33-BE-01`.

## Enlaces y referencias
- Historia: [HU-33](../../stories/HU-33.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
