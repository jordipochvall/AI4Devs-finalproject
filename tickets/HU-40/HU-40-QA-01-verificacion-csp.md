# HU-40-QA-01 — Verificación de la CSP endurecida

## Código
`HU-40-QA-01` — vinculado con **HU-40: Endurecer la Content-Security-Policy del frontend**.

## Título
Test/verificación de que la cabecera CSP incluye las nuevas directivas y nada se rompe

## Descripción
Verificado en vivo contra el contenedor `web` de desarrollo reconstruido: `curl -D- http://localhost:5173/` devuelve la cabecera `Content-Security-Policy` con las cinco directivas nuevas, e `index.html` (con el favicon `data:`) se sirve `200`.

## Criterios de aceptación
- **AC1**: Verificación de que la cabecera CSP servida contiene `object-src 'none'`, `base-uri 'self'`, `frame-ancestors 'none'`, `form-action 'self'` e `img-src`. ✅
- **AC2**: Pasada manual de regresión (lobby, juego, paneles) sin errores de CSP en consola. ⏳ No ejecutable desde este entorno (sin navegador); pendiente de una pasada manual en un navegador real antes de dar el bloque por cerrado.

## Prioridad
Could Have

## Estimación
1 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `frontend`, `seguridad`, `csp`

## Comentarios
- **Dependencias directas:** `HU-40-FE-01`.

## Enlaces y referencias
- Historia: [HU-40](../../stories/HU-40.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
