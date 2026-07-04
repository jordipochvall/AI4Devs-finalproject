# HU-28-FE-02 — Componentes canónicos y tokenización del CSS de la app

## Código
`HU-28-FE-02` — vinculado con **HU-28: Sistema de diseño y tipografía coherentes**.

## Título
`components.css` + migración de literales a tokens en todas las superficies

## Descripción
Definir en `shared/theme/components.css` los componentes canónicos (`.btn-primary`, `.btn-secondary`, `.btn-link`, `.field`/`.input`, `.dialog`/`.dialog-backdrop`, `.card`, `.server-error`) conservando los nombres de clase actuales (sin tocar el JSX). Migrar el CSS existente de `player/`, `operator/`, `math/` y `shared/` para consumir tokens (`var(--…)`) en lugar de literales, unificando el dorado y subiendo el contraste de los grises tenues.

## Criterios de aceptación
- **AC1**: Botones, campos y diálogos se ven idénticos entre superficies (provienen del catálogo común).
- **AC2**: No quedan literales de color de marca antiguos (p. ej. dos dorados distintos) ni `font-family` locales que rompan la coherencia.
- **AC3**: Los textos secundarios/tenues cumplen contraste WCAG AA sobre el fondo oscuro.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `css`, `refactor`, `a11y`

## Comentarios
- Sin cambios de marcado salvo utilidades (`.num`).
- **Dependencias directas:** `HU-28-FE-01`.

## Enlaces y referencias
- Historia: [HU-28](../../stories/HU-28.md).
- Especificación: [readme §5.1](../../readme.md#51-stack-y-estructura), [§5.3](../../readme.md#53-tokens-de-diseño).
- Índice de tickets del bloque 3: [tickets-3.md](../tickets-3.md).
