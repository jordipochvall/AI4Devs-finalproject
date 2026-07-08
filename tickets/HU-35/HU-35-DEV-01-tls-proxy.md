# HU-35-DEV-01 — TLS automático delante de `web`

## Código
`HU-35-DEV-01` — vinculado con **HU-35: El demo público sólo se sirve por HTTPS y la API no queda expuesta directamente**.

## Título
Añadir terminación TLS (HTTPS) con certificado automático delante del frontend

## Descripción
Añadir un proxy con TLS automático (por ejemplo, **Caddy** por su renovación de certificados sin configuración adicional, o **nginx + certbot**) delante del servicio `web` en `deploy/docker-compose.prod.yml`, usando el dominio/subdominio asignado al VPS. Redirigir todo el tráfico `:80` a `:443`. `web` puede seguir sirviendo por HTTP puertas adentro de la red de Compose; el proxy es quien termina TLS de cara a internet.

## Criterios de aceptación
- **AC1**: `https://<dominio-del-vps>` sirve la aplicación con un certificado válido.
- **AC2**: `http://<dominio-del-vps>` redirige a `https://`.
- **AC3**: El certificado se renueva automáticamente (o el mecanismo de renovación queda documentado y programado).

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `tls`, `despliegue`, `seguridad`

## Comentarios
- Requiere tener un dominio/subdominio apuntando al VPS antes de poder emitir el certificado.
- **Dependencias directas:** `HU-33` (el stack debe poder arrancar/estar sano antes de exponerlo).

## Enlaces y referencias
- Historia: [HU-35](../../stories/HU-35.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
