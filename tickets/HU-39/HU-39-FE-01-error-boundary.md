# HU-39-FE-01 — `ErrorBoundary` global en el frontend

## Código
`HU-39-FE-01` — vinculado con **HU-39: Un error de render no deja la pantalla en blanco**.

## Título
Añadir un `ErrorBoundary` en la raíz de la aplicación con mensaje y acción de recuperación

## Descripción
Añadido `frontend/src/shared/errors/ErrorBoundary.tsx` (clase React con `getDerivedStateFromError`/`componentDidCatch`) envolviendo `<App />` en `main.tsx`. Al capturar un error muestra una pantalla (`role="alert"`) con título/mensaje localizados (nuevas claves `errorBoundary.*` en `shared.json` es/en) y un botón que reinicia el estado y navega a `/`. El error se registra con `console.error`.

## Criterios de aceptación
- **AC1**: Un error de render en cualquier componente hijo es capturado por el `ErrorBoundary` y no deja la pantalla en blanco. ✅
- **AC2**: Se muestra un mensaje localizado con una acción de recuperación funcional. ✅
- **AC3**: El error capturado se registra (consola u otro canal ya existente) para poder diagnosticarlo. ✅

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
