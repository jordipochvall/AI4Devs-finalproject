# HU-38-QA-01 — Test de las contraseñas semilla configurables

## Código
`HU-38-QA-01` — vinculado con **HU-38: Las contraseñas de las cuentas semilla del demo se configuran por entorno**.

## Título
Test que verifica el uso de las variables de entorno y el *fallback* de desarrollo

## Descripción
Añadido `SeedDataLoaderTest` (`JdbcTemplate` simulado, capturando el hash BCrypt pasado al `INSERT INTO users` de la cuenta admin): (a) con una contraseña vacía (simula una variable de entorno presente-pero-vacía), el hash capturado coincide con la contraseña de desarrollo (`admin123`), no con una cadena vacía; (b) con una contraseña personalizada, el hash capturado coincide con ella y no con la de desarrollo.

## Criterios de aceptación
- **AC1**: Test que confirma el *fallback* a las contraseñas de desarrollo cuando no hay variables definidas (o están vacías). ✅ `blankOverrideFallsBackToTheDevDefault_notAnEmptyPassword`.
- **AC2**: Test que confirma que, con variables definidas, las cuentas semilla quedan creadas con esas contraseñas. ✅ `insertsUsersWithTheConfiguredPasswordsNotTheDevDefaults`.
- **AC3**: Verificación manual/documentada de que `scripts/smoke-test.sh` sigue pasando con la contraseña configurada del jugador semilla. ✅ Documentado en `deploy/README.md`; no ejecutable de extremo a extremo todavía (HU-35 bloqueada, sin VPS real donde correr el script contra un despliegue).

## Prioridad
Should Have

## Estimación
1 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `backend`, `seed`, `seguridad`

## Comentarios
- **Dependencias directas:** `HU-38-BE-01`.

## Enlaces y referencias
- Historia: [HU-38](../../stories/HU-38.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
