# HU-38-BE-01 — Externalizar las contraseñas semilla a variables de entorno

## Código
`HU-38-BE-01` — vinculado con **HU-38: Las contraseñas de las cuentas semilla del demo se configuran por entorno**.

## Título
`SeedDataLoader` lee las contraseñas semilla de variables de entorno (con *default* de desarrollo)

## Descripción
`SeedDataLoader` recibe las cuatro contraseñas por constructor (`@Value("${seed.password.admin:admin123}")`, etc.), mapeadas en `application.yml` a `SEED_ADMIN_PASSWORD`/`SEED_OPERATOR_PASSWORD`/`SEED_MATH_PASSWORD`/`SEED_PLAYER_PASSWORD` (el *bridge* `seed.password.admin: ${SEED_ADMIN_PASSWORD:admin123}` es necesario porque el *binding* relajado de Spring no habría mapeado ese nombre de variable automáticamente). Añadido un guard `orDefault(value, default)` en el propio constructor: una variable de entorno **presente pero vacía** (p. ej. una línea de `.env` sin rellenar) cuenta como "definida" para la resolución de *placeholders* de Spring y NO caería al *default* — el guard evita sembrar una cuenta con contraseña vacía en ese caso. Wired en `docker-compose.yml` y `deploy/docker-compose.prod.yml`; documentado en `.env.example` y `deploy/README.md`. `scripts/smoke-test.sh` ahora lee `SEED_PLAYER_PASSWORD` (con el mismo *default*) en vez de tener `player123` fijo.

## Criterios de aceptación
- **AC1**: Sin las variables `SEED_*_PASSWORD` definidas, el sembrado usa las contraseñas de desarrollo actuales (comportamiento sin cambios para `docker-compose.yml`/tests). ✅ `SeedDataLoaderTest`.
- **AC2**: Con las variables definidas, cada cuenta semilla se crea con la contraseña de su variable. ✅ `SeedDataLoaderTest.insertsUsersWithTheConfiguredPasswordsNotTheDevDefaults`.
- **AC3**: `.env.example`/`deploy/README.md` documentan las nuevas variables y su uso en el despliegue del VPS. ✅
- **AC4**: `scripts/smoke-test.sh` recibe la contraseña del jugador semilla configurada (no la de desarrollo, si se ha cambiado) para poder loguearse en el VPS. ✅ (lee `SEED_PLAYER_PASSWORD`, mismo *default*).

**Límite de verificación en vivo:** la BBDD de desarrollo persistente ya tiene usuarios sembrados de sesiones anteriores, así que `SeedDataLoader` se salta el sembrado (`Seed users already present`) — no se puede observar el efecto real de las nuevas variables contra una BBDD real sin vaciarla (fuera de alcance: no se debe hacer `docker compose down -v`). Verificado en su lugar: (a) el contenedor `api` de desarrollo arranca con normalidad tras el cambio (la inyección de constructor funciona), (b) el test unitario con `JdbcTemplate` simulado cubre la lógica real de resolución de contraseñas end-to-end dentro de `run()`.

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
