# HU-40-QA-01 — Verificación de la CSP endurecida

## Código
`HU-40-QA-01` — vinculado con **HU-40: Endurecer la Content-Security-Policy del frontend**.

## Título
Test/verificación de que la cabecera CSP incluye las nuevas directivas y nada se rompe

## Descripción
Añadir una verificación (test de configuración de `nginx.conf` si el proyecto tiene *tooling* para ello, o una comprobación manual documentada) de que la cabecera `Content-Security-Policy` devuelta incluye las nuevas directivas, y una pasada manual de regresión por las pantallas principales de la aplicación confirmando que no aparecen recursos bloqueados por CSP en la consola del navegador.

## Criterios de aceptación
- **AC1**: Verificación de que la cabecera CSP servida contiene `object-src 'none'`, `base-uri 'self'`, `frame-ancestors 'none'`, `form-action 'self'` e `img-src`.
- **AC2**: Pasada manual de regresión (lobby, juego, paneles) sin errores de CSP en consola.

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
