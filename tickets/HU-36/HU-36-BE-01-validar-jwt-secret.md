# HU-36-BE-01 — Rechazar `JWT_SECRET` débil al arrancar

## Código
`HU-36-BE-01` — vinculado con **HU-36: La API rechaza arrancar con secretos de despliegue débiles o de ejemplo**.

## Título
Validación de longitud mínima de `JWT_SECRET` en el arranque

## Descripción
Añadido `@PostConstruct validateSecret()` en `JwtService` que comprueba que `app.jwt.secret` tiene al menos 32 bytes (`MIN_SECRET_BYTES`) y lanza `IllegalStateException` con un mensaje claro si no lo cumple. Al ser `@PostConstruct`, el fallo ocurre durante la creación del bean (arranque de Spring), antes de que Tomcat acepte tráfico. Se ha simplificado `signingKey()` retirando el `WARN` que hacía la misma comprobación en cada petición (ya redundante: el arranque garantiza que el secreto es válido durante toda la vida de la app).

## Criterios de aceptación
- **AC1**: Con `JWT_SECRET` de menos de 32 bytes, la aplicación no arranca y el log indica el requisito incumplido. ✅ Verificado con `docker run` puntual de la imagen con `JWT_SECRET=admin`: `IllegalStateException: app.jwt.secret (JWT_SECRET) must be at least 32 bytes long, got 5...` y el contexto de Spring falla a arrancar.
- **AC2**: Con un `JWT_SECRET` de 32+ caracteres, la aplicación arranca con normalidad. ✅ Verificado reconstruyendo el contenedor `api` de desarrollo con un secreto fuerte nuevo en `.env` (el `admin` anterior fue sustituido): `/actuator/health` → `200`, login de un usuario semilla → token emitido.
- **AC3**: Los perfiles de test (que ya definen su propio `JWT_SECRET` de prueba) siguen arrancando sin cambios. ✅ `ci-test-secret-at-least-32-bytes-long` (unit) e `integration-test-secret-at-least-32-bytes-long` (IT) ya cumplían el mínimo; suite completa de unit tests y las ITs ya existentes (menos las afectadas por drift de datos de la BBDD persistente, ajeno a este cambio) siguen en verde.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `seguridad`, `jwt`, `configuración`

## Comentarios
- Revisar el `JWT_SECRET` usado en `ci.yml`/tests de integración para asegurar que ya cumple la longitud mínima (o ajustarlo si no).
- **Dependencias directas:** ninguna.

## Enlaces y referencias
- Historia: [HU-36](../../stories/HU-36.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
