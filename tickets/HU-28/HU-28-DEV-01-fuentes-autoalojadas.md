# HU-28-DEV-01 — Empaquetar fuentes web auto-alojadas (CSP-safe)

## Código
`HU-28-DEV-01` — vinculado con **HU-28: Sistema de diseño y tipografía coherentes**.

## Título
Fuentes Cinzel + Inter auto-alojadas en `/fonts` con `@font-face`

## Descripción
Bundlear las fuentes **Cinzel** (display) e **Inter** (cuerpo), ambas OFL y subset latino, como `.woff2` en `frontend/public/fonts/` (servidas same-origin en `/fonts/*`), y declararlas en `shared/theme/fonts.css` con `font-display: swap`. Añadir `preload` de las variantes principales en `index.html`. Motivo: la CSP de nginx (`default-src 'self'`) bloquea Google Fonts por CDN.

## Criterios de aceptación
- **AC1**: Las fuentes se sirven desde `/fonts/*.woff2` (mismo origen) y cargan sin violar la CSP.
- **AC2**: `fonts.css` declara las familias con sus pesos y `font-display: swap`.
- **AC3**: `index.html` precarga las variantes principales (p. ej. Inter 400 y Cinzel 700).

## Prioridad
Should Have

## Estimación
1 SP

## Equipo responsable
DevOps / Plataforma

## Etiquetas
`frontend`, `fonts`, `csp`, `performance`

## Comentarios
- Licencia OFL: redistribución permitida auto-alojando los `.woff2`.
- **Dependencias directas:** ninguna (habilita `HU-28-FE-01`).

## Enlaces y referencias
- Historia: [HU-28](../../stories/HU-28.md).
- Especificación: [readme §5.2](../../readme.md#52-tipografía-fuentes-auto-alojadas).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
