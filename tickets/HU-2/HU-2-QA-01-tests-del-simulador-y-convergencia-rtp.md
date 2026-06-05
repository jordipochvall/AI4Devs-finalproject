# HU-2-QA-01 — Tests del simulador y convergencia del RTP

## Código
`HU-2-QA-01` — vinculado con **HU-2: El matemático valida un juego con el simulador**.

## Título
Tests del simulador: correctitud agregada, convergencia, error estándar, suite del job `perf`

## Descripción
Conjunto de tests del módulo `nova-simulator` y del flujo de simulación end-to-end:

- **Unit (JUnit)** sobre `MetricsAccumulator`: la suma agregada de varios hilos coincide con la suma directa para datasets pequeños y deterministas; el **error estándar** calculado (de suma y suma de cuadrados) coincide con el de referencia.
- **Property-based (jqwik)**: para 100 **configs de prueba con RTP conocido por construcción** (fixtures sintéticos), el RTP empírico tras 1 M de spins converge a ese RTP **dentro del intervalo de confianza** (no un umbral fijo: la tolerancia escala con el error estándar, lo que evita *flakiness* en alta volatilidad).
- **Test de convergencia específico**: con la `config` semilla de "Espacial", 10 M spins → el RTP empírico cae dentro del IC alrededor de su valor esperado; además se valida que la `convergence_sample` es monótonamente más estable.
- **Integration (Failsafe + Testcontainers)** sobre `POST /api/v1/math/configs/{id}/simulations`: respuesta `202`, estado `RUNNING → COMPLETED`, métricas pobladas (incluidos `rtp_std_error`, `convergence_sample`, `rtp_breakdown`).
- **Test del job `perf`** (marcado `@Tag("perf")`): `SimulationRunner.run(10_000_000)` finaliza en < 10 min. **Se ejecuta en CI** en el job `perf` dedicado (ver `HU-2-DEV-01`).

## Criterios de aceptación
- **AC1**: La cobertura mínima de `nova-simulator` es ≥ 80 %.
- **AC2**: El test de convergencia pasa usando el **intervalo de confianza** (no un umbral fijo), de modo que es estable también en configuraciones de alta volatilidad.
- **AC3**: El test del job `perf` ejecutado en el runner de CI estándar finaliza en menos del SLA y falla si lo supera.
- **AC4**: La concurrencia del `MetricsAccumulator` está verificada con un test que lanza N hilos sumando aleatoriamente y compara con la suma secuencial (incluido el cálculo de varianza/error estándar).
- **AC5**: Una regresión que rompa la convergencia del RTP (p. ej. un bug que descuente premios mal) hace fallar al menos un test.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `jqwik`, `junit`, `performance`, `testcontainers`, `convergencia`

## Comentarios
- El umbral de convergencia (`±0.5 %` por defecto) es un parámetro de la app (ver C5 en 1.2 y el ticket); puede ajustarse en función de `numSpins`.
- **Dependencias directas:** `HU-2-BE-02` (intra) — arrastra `HU-2-BE-01`. (El job `perf` `HU-2-DEV-01` depende de este ticket, no al revés.)

## Enlaces y referencias
- Historia: [HU-2](../../readme.md#5-historias-de-usuario).
- Estrategia de tests: [2.6 Tests](../../readme.md#26-tests).
- Convención de carpetas (`src/it`, Failsafe, `@Tag("perf")`): [2.6](../../readme.md#26-tests).
- Pipeline CI: [2.4.2](../../readme.md#24-infraestructura-y-despliegue).
