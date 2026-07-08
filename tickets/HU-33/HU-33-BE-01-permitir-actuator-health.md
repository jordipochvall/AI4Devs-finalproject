# HU-33-BE-01 — Permitir `/actuator/health` sin autenticación

## Código
`HU-33-BE-01` — vinculado con **HU-33: Bug — `/actuator/health` exige autenticación y rompe el propio despliegue**.

## Título
Permitir el healthcheck sin token, manteniendo el resto de actuator protegido

## Descripción
Verificado al implementar: **`spring-boot-starter-actuator` no era ni siquiera una dependencia del proyecto** (única mención de "actuator" en todo el repo: la línea del *healthcheck* de `docker-compose.prod.yml`), así que `/actuator/health` no existía como *endpoint*. La corrección tiene tres partes: (1) añadir `spring-boot-starter-actuator` a `nova-web-api/pom.xml`; (2) en `application.yml`, fijar explícitamente `management.endpoints.web.exposure.include: health` (ya es el valor por defecto de Spring Boot) y `management.endpoint.health.show-details: never`, para no filtrar detalles internos aunque el *endpoint* sea público; (3) en `SecurityConfig.filterChain`, añadir `requestMatchers("/actuator/health/**").permitAll()` **antes** de `anyRequest().authenticated()`.

## Criterios de aceptación
- **AC1**: `GET /actuator/health` sin cabecera `Authorization` responde `200` con `"status":"UP"`.
- **AC2**: Cualquier otro sub-*endpoint* de actuator (no expuesto por defecto salvo `health`) sigue sin ser alcanzable/autenticado.
- **AC3**: `deploy/docker-compose.prod.yml` levanta el stack y el contenedor `api` alcanza el estado `healthy`.

## Prioridad
Must Have

## Estimación
2 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `security`, `bug`, `actuator`, `despliegue`

## Comentarios
- Verificar también que `deploy/deploy.sh` (que hace polling de `/actuator/health` dentro del propio contenedor) y `scripts/smoke-test.sh` (desde fuera) pasan tras el cambio.
- **Dependencias directas:** ninguna; es el ticket más independiente del bloque.

## Enlaces y referencias
- Historia: [HU-33](../../stories/HU-33.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
