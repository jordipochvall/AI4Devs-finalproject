# HU-28-FE-01 — Base de diseño: tokens, reset y tipografía global

## Código
`HU-28-FE-01` — vinculado con **HU-28: Sistema de diseño y tipografía coherentes**.

## Título
`shared/theme/{tokens,base}.css` + wiring global en `main.tsx`

## Descripción
Crear la capa base del sistema de diseño: `tokens.css` (`:root` con color, tipografía, espaciado, radios, sombras y placeholders de skin por tema) y `base.css` (reset `box-sizing`, `body` con `--font-body`, `h1–h3` con `--font-display`, utilidad `.num` de cifras tabulares, `:focus-visible` con `--focus`). Importar en `main.tsx` en el orden `fonts → tokens → base → components → a11y`.

## Criterios de aceptación
- **AC1**: Existe `tokens.css` con las variables de marca (un único dorado), texto con contraste ≥ AA, y bloques `[data-theme]` para los tres juegos.
- **AC2**: `base.css` fija la tipografía del cuerpo y de los encabezados globalmente; ninguna pantalla queda en la fuente por defecto del navegador.
- **AC3**: Los estilos globales se importan una sola vez en `main.tsx` en el orden correcto.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `css`, `design-tokens`, `tipografía`

## Comentarios
- **Dependencias directas:** `HU-28-DEV-01` (fuentes).

## Enlaces y referencias
- Historia: [HU-28](../../stories/HU-28.md).
- Especificación: [readme §5.3](../../readme.md#53-tokens-de-diseño).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
