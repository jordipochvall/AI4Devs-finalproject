# HU-2-BE-01 — Módulo `nova-simulator`

## Código
`HU-2-BE-01` — vinculado con **HU-2: El matemático valida un juego con el simulador**.

## Título
Módulo `nova-simulator` (`SimulationRunner` + `ForkJoinPool` + `CountingSink` cero-alloc + `LongAdder`)

## Descripción
Crear el módulo Maven `nova-simulator` que ejecuta el **simulador masivo**. Ejecuta el **mismo `SpinKernel`** del dominio (`HU-1-BE-01`) sobre un `CompiledGame`, a través de un **`CountingSink`** que agrega métricas y **no copia ni retiene nada** (cero asignación por giro) — esa es la clave para 10M/<10 min. No pasa por `wallet`, `wallet_transactions` ni `game_rounds`: los giros se ejecutan en memoria y solo se persiste el resultado agregado en `simulation_runs` (ver 3.2.9). Ver el diseño en el readme [§2.1.7](../../readme.md#217-motor-de-juego-núcleo-data-oriented-y-doble-materialización).

Paraleliza los `numSpins` entre los núcleos con `ForkJoinPool`: el `CompiledGame` es inmutable y compartido (solo lectura); **cada worker tiene su propio `RngEngine`, sus buffers y su `CountingSink`**, y solo se comparten los acumuladores `LongAdder`/`DoubleAdder` (lock-free). Métricas agregadas que sirven de **herramientas para el matemático** (ver C3): RTP global **con error estándar** (acumula suma y suma de cuadrados de `win/bet` → `stdev/√N` → intervalo de confianza), **curva de convergencia** (muestreo de RTP a intervalos crecientes de giros), **descomposición** (RTP base game vs free spins, contribución por símbolo/feature), **estadísticas de reel strips** (frecuencia de símbolo, P(trigger), nº esperado de free spins con retrigger), hit frequency, volatilidad, max win, *longest dry streak*, histograma + percentiles de cola. El resultado es un `SimulationResult` serializable a JSONB. **La plataforma mide; no declara el RTP objetivo (eso es del matemático, `rtp_target`).**

## Criterios de aceptación
- **AC1**: `SimulationRunner.run(configId, numSpins, betCents)` devuelve un `SimulationResult` con todas las métricas listadas y el `duration_ms`.
- **AC1b**: El resultado incluye **error estándar del RTP** (vía suma y suma de cuadrados), **muestreo de la curva de convergencia**, **descomposición** (base/free spins, por símbolo/feature) y **estadísticas de reel strips** — persistidos en `simulation_runs` (`rtp_std_error`, `convergence_sample`, `rtp_breakdown`).
- **AC2**: El simulador **no abre transacciones** ni accede a `wallets` / `wallet_transactions` / `game_rounds`: solo lee la `game_configs` de partida (compilada a `CompiledGame`). **No calcula `rtp_target`** (lo declara el matemático).
- **AC3**: El bucle de simulación **no asigna por giro** (el `CountingSink` no copia ni retiene); verificado por el test de *allocation* (ver `HU-2-QA-01`).
- **AC4**: Los `LongAdder` agregan resultados de varios workers sin corrupción; el `CompiledGame` compartido no se muta. Verificado en test concurrente.
- **AC5**: `SimulationRunner.run(10_000_000, ...)` completa en **< 10 min** en el entorno reproducible del job `perf` (ver `HU-2-DEV-01`).
- **AC6**: La paralelización usa los cores disponibles sin OOM (p. ej. con `-Xmx512m`), gracias a la ausencia de asignación por giro.

## Prioridad
Must Have

## Estimación
5 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-simulator`, `motor`, `fork-join`, `concurrencia`, `performance`, `cero-alloc`

## Comentarios
- **"Lo simulado = lo jugado"**: el simulador ejecuta el *mismo* `SpinKernel` que producción; solo cambia el sink (`CountingSink` aquí, `MaterializingSink` en `HU-1-BE-02`). La fidelidad queda garantizada por construcción.
- Las métricas detalladas (histograma `prize_distribution`) se almacenan como `JSONB` en `simulation_runs`.

## Enlaces y referencias
- Historia: [HU-2](../../stories/HU-2.md).
- **Diseño del motor: [§2.1.7](../../readme.md#217-motor-de-juego-núcleo-data-oriented-y-doble-materialización)**.
- Arquitectura: [§2.2 Componentes](../../readme.md#22-descripción-de-componentes-principales), módulo `nova-simulator`.
- Modelo de datos: [§3.2.9 simulation_runs](../../readme.md#32-descripción-de-entidades-principales).
- Patrones: §2.1.4 (Map-Reduce + Núcleo data-oriented/Visitor).
- **Dependencias directas:** `HU-1-BE-01` (externa, `SpinKernel` + `CompiledGame` + `RoundSink`).
