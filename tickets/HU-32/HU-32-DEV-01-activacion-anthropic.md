# HU-32-DEV-01 — Activación de Anthropic en el despliegue

## Código
`HU-32-DEV-01` — vinculado con **HU-32: "Ask the AI" funcional (con modo offline sin API key)**.

## Título
`ANTHROPIC_ENABLED` en `docker-compose` + `.env.example` + modelo válido

## Descripción
Cablear la activación del camino real a Claude: `docker-compose.yml` pasa la variable **`ANTHROPIC_ENABLED`** al contenedor `api` (hoy sólo pasa `ANTHROPIC_API_KEY`/`ANTHROPIC_MODEL`, por lo que la IA queda desactivada); documentar en `.env.example` las tres variables (`ANTHROPIC_ENABLED`, `ANTHROPIC_API_KEY`, `ANTHROPIC_MODEL`) y fijar un **id de modelo válido** (verificar con la referencia de la API de Claude). Sin key, el sistema opera en modo offline (HU-32-BE-01).

## Criterios de aceptación
- **AC1**: `docker-compose` propaga `ANTHROPIC_ENABLED` (por defecto `false`) al servicio `api`.
- **AC2**: `.env.example` documenta las tres variables con comentarios y un modelo válido por defecto.
- **AC3**: Con `ANTHROPIC_ENABLED=true` + key en `.env` local, la app arranca y el adaptador Anthropic queda activo; sin ellas, arranca en modo offline.

## Prioridad
Should Have

## Estimación
1 SP

## Equipo responsable
DevOps / Plataforma

## Etiquetas
`devops`, `configuración`, `ia`, `docker`

## Comentarios
- La API key **no** se commitea (sólo en `.env` local).
- **Dependencias directas:** `HU-32-BE-01`.

## Enlaces y referencias
- Historia: [HU-32](../../stories/HU-32.md).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
