# HU-36-BE-01 — Rechazar `JWT_SECRET` débil al arrancar

## Código
`HU-36-BE-01` — vinculado con **HU-36: La API rechaza arrancar con secretos de despliegue débiles o de ejemplo**.

## Título
Validación de longitud mínima de `JWT_SECRET` en el arranque

## Descripción
Añadir una validación (por ejemplo, en la construcción de `JwtService` o en un `@PostConstruct`/*validator* de configuración) que compruebe que `jwt.secret` tiene al menos 32 caracteres (mínimo razonable para HS256) y lance una excepción de arranque con un mensaje claro si no lo cumple. Debe fallar-rápido: la aplicación no debe llegar a aceptar tráfico con un secreto débil.

## Criterios de aceptación
- **AC1**: Con `JWT_SECRET` de menos de 32 caracteres, la aplicación no arranca y el log indica el requisito incumplido.
- **AC2**: Con un `JWT_SECRET` de 32+ caracteres, la aplicación arranca con normalidad.
- **AC3**: Los perfiles de test (que ya definen su propio `JWT_SECRET` de prueba) siguen arrancando sin cambios.

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
