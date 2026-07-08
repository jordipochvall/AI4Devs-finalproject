# HU-36-DEV-01 — Placeholder no productivo en `.env.example`

## Código
`HU-36-DEV-01` — vinculado con **HU-36: La API rechaza arrancar con secretos de despliegue débiles o de ejemplo**.

## Título
Sustituir `JWT_SECRET=admin` por un placeholder evidente y documentar cómo generar uno fuerte

## Descripción
En `.env.example`, cambiar `JWT_SECRET=admin` por un valor que sea obviamente un *placeholder* no productivo (p. ej. `JWT_SECRET=change-me-please-32-chars-min...`) y añadir un comentario con el comando para generar uno fuerte (`openssl rand -base64 48`). Así, si alguien copia `.env.example` a `.env` sin cambiarlo, además de ser evidente a simple vista, la validación de `HU-36-BE-01` lo rechazaría igualmente si fuese demasiado corto.

## Criterios de aceptación
- **AC1**: `.env.example` ya no contiene `JWT_SECRET=admin`. ✅ (`JWT_SECRET=change-me-generate-a-real-32-byte-secret`)
- **AC2**: El nuevo valor de ejemplo es, por sí mismo, evidentemente no-productivo. ✅
- **AC3**: Hay un comentario documentando cómo generar un secreto fuerte real. ✅ (ya existía `openssl rand -base64 48`; se añadió la mención de que la app rechaza arrancar si es más corto, HU-36).

## Comentario adicional
El `.env` local de desarrollo (no versionado) también tenía literalmente `JWT_SECRET=admin` — se ha sustituido por un secreto real generado con `openssl rand -base64 48` para que el contenedor `api` de desarrollo siga arrancando tras `HU-36-BE-01`.

## Prioridad
Should Have

## Estimación
1 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `documentación`, `seguridad`

## Comentarios
- **Dependencias directas:** ninguna; complementa `HU-36-BE-01`.

## Enlaces y referencias
- Historia: [HU-36](../../stories/HU-36.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
