# HU-37-BE-01 — Executor acotado y límite de simulaciones concurrentes

## Código
`HU-37-BE-01` — vinculado con **HU-37: Acotar la concurrencia de simulaciones para proteger el VPS del demo**.

## Título
`ThreadPoolTaskExecutor` propio para `@Async` + límite de simulaciones en curso

## Descripción
Añadido `EngineConfig.simulationTaskExecutor(...)`: un `ThreadPoolTaskExecutor` nombrado (`core=2/max=4/queue=10`, configurable por `app.simulation.executor.*`) referenciado explícitamente desde `SimulationExecutor.run` vía `@Async("simulationTaskExecutor")`. Añadido `SimulationLaunchPort.countRunning()` (implementado en `SimulationLaunchAdapter` con `SimulationRunJpaRepository.countByStatus`); `SimulationUseCase.launch` rechaza con `TooManySimulationsException` (nueva, → HTTP 429 vía `GlobalExceptionHandler`) si `countRunning() >= maxConcurrentSimulations`, propiedad `app.simulation.max-concurrent` (por defecto 3) inyectada en `UseCaseConfig`.

**Incidente al desplegar (detectado en vivo, no por los tests unitarios):** el primer nombre elegido para el bean del executor (`"simulationExecutor"`) colisionaba con el bean **autodetectado** del `@Component SimulationExecutor` (Spring deriva el nombre de bean por defecto del nombre de la clase). El contenedor `api` de desarrollo, al reconstruirse, **no llegaba a arrancar**: `BeanDefinitionOverrideException`. Los tests unitarios no lo detectaron porque construyen los objetos directamente, sin contexto de Spring. Renombrado a `"simulationTaskExecutor"`; verificado reconstruyendo el contenedor y confirmando que arranca.

## Criterios de aceptación
- **AC1**: `SimulationExecutor` usa un `ThreadPoolTaskExecutor` acotado y nombrado, no el executor por defecto. ✅
- **AC2**: Con menos simulaciones `RUNNING` que el límite configurado, una nueva simulación válida se acepta y ejecuta con normalidad. ✅ Verificado en vivo: lanzada una simulación de 1000 giros contra el contenedor `api` reconstruido → `202 RUNNING` → `COMPLETED` en ~10ms con métricas coherentes.
- **AC3**: Al alcanzar el límite, una nueva simulación se rechaza con un error explícito (no se acepta silenciosamente ni se cuelga). ✅ Cubierto por test unitario (`SimulationUseCaseTest.launch_atConcurrencyLimit_throwsAndSkipsConfigLookup`).
- **AC4**: El límite es configurable (propiedad de aplicación), no un valor fijo en código. ✅ `app.simulation.max-concurrent` (`SIMULATION_MAX_CONCURRENT`).

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
