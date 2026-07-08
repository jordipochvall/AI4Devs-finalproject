# HU-37-BE-01 — Executor acotado y límite de simulaciones concurrentes

## Código
`HU-37-BE-01` — vinculado con **HU-37: Acotar la concurrencia de simulaciones para proteger el VPS del demo**.

## Título
`ThreadPoolTaskExecutor` propio para `@Async` + límite de simulaciones en curso

## Descripción
Configurar un `ThreadPoolTaskExecutor` (bean `@Configuration`, con nombre de *pool* propio) para la ejecución `@Async` de `SimulationExecutor`, con un tamaño acotado y razonable para los recursos de un VPS pequeño (en vez del `SimpleAsyncTaskExecutor` por defecto de Spring, que crea un hilo sin límite por tarea). Añadir a `SimulationUseCase.launch` (o al puerto que consulta el estado de simulaciones) una comprobación del número de simulaciones actualmente `RUNNING`: si se alcanza un límite configurable, rechazar la nueva petición con un error claro en vez de aceptarla sin control.

## Criterios de aceptación
- **AC1**: `SimulationExecutor` usa un `ThreadPoolTaskExecutor` acotado y nombrado, no el executor por defecto.
- **AC2**: Con menos simulaciones `RUNNING` que el límite configurado, una nueva simulación válida se acepta y ejecuta con normalidad.
- **AC3**: Al alcanzar el límite, una nueva simulación se rechaza con un error explícito (no se acepta silenciosamente ni se cuelga).
- **AC4**: El límite es configurable (propiedad de aplicación), no un valor fijo en código.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `concurrencia`, `rendimiento`, `despliegue`

## Comentarios
- El tamaño del *pool* y el límite de concurrencia dependen de las especificaciones del VPS elegido; dejar un valor por defecto conservador.
- **Dependencias directas:** ninguna.

## Enlaces y referencias
- Historia: [HU-37](../../stories/HU-37.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
