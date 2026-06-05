# HU-2-DEV-01 — Job `perf` en CI

## Código
`HU-2-DEV-01` — vinculado con **HU-2: El matemático valida un juego con el simulador**.

## Título
Job `perf` en CI (10M giros, presupuesto <10 min, fallo si se regresan métricas)

## Descripción
Añadir al pipeline de GitHub Actions (`.github/workflows/ci.yml`) un **job `perf`** dedicado, separado del `build-test` para no penalizar cada commit:

- Se ejecuta sobre la misma matriz de JDK 21 y arranca el módulo `nova-simulator`.
- Lanza el test marcado con `@Tag("perf")` (`HU-2-QA-01`), que ejecuta `SimulationRunner.run(10_000_000)` y verifica el presupuesto **< 10 min**.
- **Falla el build** si se supera el tiempo o si las métricas agregadas (RTP, hit frequency, volatilidad) se desvían más allá del umbral configurado respecto a las métricas-baseline registradas en el repo (regresión funcional, no solo de tiempo).
- Publica el `SimulationResult` y los tiempos como artefactos para análisis histórico.

Ticket *companion* de `HU-2-QA-01`: este se ocupa del cableado de CI, aquel del contenido de los tests.

## Criterios de aceptación
- **AC1**: El *job* `perf` aparece en el pipeline y depende del job `build-test`.
- **AC2**: Con un runner estándar de GitHub Actions, el test `@Tag("perf")` finaliza en < 10 min en condiciones normales.
- **AC3**: Si el tiempo se excede o las métricas se desvían, el job termina en rojo y bloquea el merge a `main`.
- **AC4**: Los artefactos `SimulationResult.json` y `duration_ms.txt` quedan publicados en el run del workflow.
- **AC5**: El job se cachea (Maven + JDK) para evitar tiempos de setup innecesarios.

## Prioridad
Must Have

## Estimación
2 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `github-actions`, `ci`, `perf`, `regresion`

## Comentarios
- Si el runner gratuito de GitHub no diera consistentemente <10 min, se considera mover este job a un runner *self-hosted* (post-MVP).
- Las métricas-baseline (RTP empírico de referencia por juego semilla, con su error estándar) se versionan en `e2e/baselines/` o equivalente.
- **Dependencias directas:** `HU-2-QA-01` (intra, el test `@Tag("perf")`) · `HU-1-DEV-01` (externa, esqueleto del CI).

## Enlaces y referencias
- Historia: [HU-2](../../readme.md#5-historias-de-usuario).
- Pipeline CI: [2.4.2 Proceso de despliegue](../../readme.md#24-infraestructura-y-despliegue).
- Tests (job perf): [2.6 Tests](../../readme.md#26-tests).
