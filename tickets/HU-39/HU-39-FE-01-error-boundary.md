# HU-39-FE-01 — `ErrorBoundary` global en el frontend

## Código
`HU-39-FE-01` — vinculado con **HU-39: Un error de render no deja la pantalla en blanco**.

## Título
Añadir un `ErrorBoundary` en la raíz de la aplicación con mensaje y acción de recuperación

## Descripción
Crear un componente `ErrorBoundary` (clase React con `componentDidCatch`/`getDerivedStateFromError`, ya que los *hooks* no cubren errores de render) y envolver con él el árbol raíz de la aplicación (`App.tsx`/`main.tsx` o el `Router`). Al capturar un error, mostrar una pantalla con un mensaje amigable (localizado, i18next) y una acción de recuperación (recargar la página / volver al inicio), y registrar el error en consola para diagnóstico.

## Criterios de aceptación
- **AC1**: Un error de render en cualquier componente hijo es capturado por el `ErrorBoundary` y no deja la pantalla en blanco.
- **AC2**: Se muestra un mensaje localizado con una acción de recuperación funcional.
- **AC3**: El error capturado se registra (consola u otro canal ya existente) para poder diagnosticarlo.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `resiliencia`, `ux`

## Comentarios
- **Dependencias directas:** ninguna.

## Enlaces y referencias
- Historia: [HU-39](../../stories/HU-39.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
