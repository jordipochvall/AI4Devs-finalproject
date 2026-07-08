# HU-40-FE-01 — Endurecer la CSP en `nginx.conf`

## Código
`HU-40-FE-01` — vinculado con **HU-40: Endurecer la Content-Security-Policy del frontend**.

## Título
Añadir `object-src`, `base-uri`, `frame-ancestors`, `form-action` e `img-src` a la CSP

## Descripción
Ampliada la cabecera `Content-Security-Policy` de `frontend/nginx.conf` con: `img-src 'self' data:`, `object-src 'none'`, `base-uri 'self'`, `frame-ancestors 'none'`, `form-action 'self'`. **Hallazgo al implementar:** el favicon de `index.html` ya era un `data:image/svg+xml` embebido — sin `img-src` explícito, éste caía bajo `default-src 'self'` (que no permite `data:`), así que el favicon llevaba tiempo bloqueado silenciosamente por la CSP; esta historia lo corrige de paso.

## Criterios de aceptación
- **AC1**: La cabecera `Content-Security-Policy` de cualquier respuesta incluye las nuevas directivas. ✅ Verificado en vivo (contenedor `web` reconstruido): `curl -D- http://localhost:5173/` devuelve la cabecera completa con las 5 directivas nuevas.
- **AC2**: Navegación manual por las pantallas principales sin errores de CSP en la consola del navegador. ⏳ Verificado que `index.html` se sirve con `200` y el favicon `data:` está presente; falta una pasada manual completa por lobby/juego/paneles en un navegador real (no automatizable desde este entorno).

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
