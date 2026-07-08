# HU-38-BE-01 — Externalizar las contraseñas semilla a variables de entorno

## Código
`HU-38-BE-01` — vinculado con **HU-38: Las contraseñas de las cuentas semilla del demo se configuran por entorno**.

## Título
`SeedDataLoader` lee las contraseñas semilla de variables de entorno (con *default* de desarrollo)

## Descripción
En `SeedDataLoader` (`backend/nova-web-api/src/main/java/com/novacasino/api/seed/SeedDataLoader.java`), sustituir las contraseñas literales (`admin123`, `operator123`, `math123`, `player123`) por lectura de propiedades de configuración (`seed.password.admin`, `seed.password.operator`, `seed.password.math`, `seed.password.player`, mapeadas a `SEED_ADMIN_PASSWORD`, etc.), con los valores actuales como *default* en `application.yml` para no romper el `docker-compose.yml` de desarrollo ni los tests/ITs existentes. Documentar en `.env.example` y `deploy/README.md` que el despliegue del VPS debe fijar sus propios valores.

## Criterios de aceptación
- **AC1**: Sin las variables `SEED_*_PASSWORD` definidas, el sembrado usa las contraseñas de desarrollo actuales (comportamiento sin cambios para `docker-compose.yml`/tests).
- **AC2**: Con las variables definidas, cada cuenta semilla se crea con la contraseña de su variable.
- **AC3**: `.env.example`/`deploy/README.md` documentan las nuevas variables y su uso en el despliegue del VPS.
- **AC4**: `scripts/smoke-test.sh` recibe la contraseña del jugador semilla configurada (no la de desarrollo, si se ha cambiado) para poder loguearse en el VPS.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `seguridad`, `seed`, `configuración`

## Comentarios
- No se cambia qué cuentas/roles se siembran, sólo el origen de sus contraseñas.
- **Dependencias directas:** ninguna.

## Enlaces y referencias
- Historia: [HU-38](../../stories/HU-38.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
