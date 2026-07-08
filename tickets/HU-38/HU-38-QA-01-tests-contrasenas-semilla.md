# HU-38-QA-01 — Test de las contraseñas semilla configurables

## Código
`HU-38-QA-01` — vinculado con **HU-38: Las contraseñas de las cuentas semilla del demo se configuran por entorno**.

## Título
Test que verifica el uso de las variables de entorno y el *fallback* de desarrollo

## Descripción
Añadir un test sobre `SeedDataLoader` (o su lógica de resolución de contraseñas) que verifique: (a) sin las variables `SEED_*_PASSWORD` definidas, se usan las contraseñas de desarrollo actuales; (b) con las variables definidas, se usan esos valores al crear cada cuenta semilla (comprobable vía el `PasswordEncoder`/`matches` sobre el hash guardado).

## Criterios de aceptación
- **AC1**: Test que confirma el *fallback* a las contraseñas de desarrollo cuando no hay variables definidas.
- **AC2**: Test que confirma que, con variables definidas, las cuentas semilla quedan creadas con esas contraseñas.
- **AC3**: Verificación manual/documentada de que `scripts/smoke-test.sh` sigue pasando con la contraseña configurada del jugador semilla.

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
