# HU-15-FE-01 — Backoffice operador: edición de la configuración comercial

## Código
`HU-15-FE-01` — vinculado con **HU-15: El operador gestiona la configuración comercial de los juegos**.

## Título
Pantalla de edición comercial de juegos en el backoffice del operador

## Descripción
Añadir a la superficie del operador una vista de juegos con su configuración comercial y un formulario de edición (apuestas, monedas, `active`). Al guardar, llama a `PUT /operator/games/{id}` y muestra los errores de validación (`422`) en línea.

## Criterios de aceptación
- **AC1**: La vista lista los juegos del operador con su configuración comercial editable.
- **AC2**: Al guardar un cambio inválido, los errores `422` se muestran sobre el campo correspondiente.
- **AC3**: Solo accesible con rol `OPERATOR`.
- **AC4**: La UI funciona en **español e inglés**.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `operator`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-15-BE-01` · `HU-6-FE-01` (superficie de operador, externa).

## Enlaces y referencias
- Historia: [HU-15](../../stories/HU-15.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Diseño y UX (backoffice operador): [§1.3](../../readme.md#13-diseño-y-experiencia-de-usuario).
