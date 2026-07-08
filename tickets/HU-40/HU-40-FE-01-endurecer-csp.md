# HU-40-FE-01 — Endurecer la CSP en `nginx.conf`

## Código
`HU-40-FE-01` — vinculado con **HU-40: Endurecer la Content-Security-Policy del frontend**.

## Título
Añadir `object-src`, `base-uri`, `frame-ancestors`, `form-action` e `img-src` a la CSP

## Descripción
En `frontend/nginx.conf`, ampliar la cabecera `Content-Security-Policy` actual (`default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';`) con: `object-src 'none'`, `base-uri 'self'`, `frame-ancestors 'none'`, `form-action 'self'` e `img-src 'self' data:` (para cubrir el favicon/recursos embebidos en `data:`). Verificar manualmente que ninguna pantalla (lobby, juego, paneles de operador/matemático) queda con recursos bloqueados tras el cambio.

## Criterios de aceptación
- **AC1**: La cabecera `Content-Security-Policy` de cualquier respuesta incluye las nuevas directivas.
- **AC2**: Navegación manual por las pantallas principales sin errores de CSP en la consola del navegador.

## Prioridad
Could Have

## Estimación
1 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `seguridad`, `csp`

## Comentarios
- **Dependencias directas:** ninguna.

## Enlaces y referencias
- Historia: [HU-40](../../stories/HU-40.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
