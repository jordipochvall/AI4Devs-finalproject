# HU-35-DEV-02 — Dejar de publicar el puerto de la API al host

## Código
`HU-35-DEV-02` — vinculado con **HU-35: El demo público sólo se sirve por HTTPS y la API no queda expuesta directamente**.

## Título
Quitar `ports: ["8080:8080"]` de `api` en `docker-compose.prod.yml`

## Descripción
En `deploy/docker-compose.prod.yml`, eliminar el mapeo `ports: ["8080:8080"]` del servicio `api`: debe quedar accesible **sólo** a través de la red interna de Compose, ya que `web` (`nginx.conf`) ya hace de proxy inverso hacia `http://api:8080/api/`. El *healthcheck* de `api` (`wget http://localhost:8080/actuator/health`) se ejecuta **dentro** del propio contenedor, así que no depende del puerto publicado y sigue funcionando igual.

## Criterios de aceptación
- **AC1**: `docker-compose.prod.yml` ya no publica el puerto 8080 de `api` al host.
- **AC2**: Desde fuera del VPS, una conexión a `<ip>:8080` no se establece.
- **AC3**: El *healthcheck* de `api` y el proxy de `web` hacia `/api/` siguen funcionando sin cambios.

## Prioridad
Must Have

## Estimación
1 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `despliegue`, `seguridad`, `red`

## Comentarios
- Cambio de una línea, pero cierra una vía de acceso directo a Swagger/actuator/la API cruda desde internet.
- **Dependencias directas:** ninguna; independiente de `HU-35-DEV-01`.

## Enlaces y referencias
- Historia: [HU-35](../../stories/HU-35.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
