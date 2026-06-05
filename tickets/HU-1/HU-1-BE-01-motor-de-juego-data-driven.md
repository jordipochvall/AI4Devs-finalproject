# HU-1-BE-01 — Motor de juego data-driven

## Código
`HU-1-BE-01` — vinculado con **HU-1: El jugador realiza un giro**.

## Título
Motor de juego data-driven: `SpinKernel` + `GameCompiler`/`CompiledGame` + `RoundSink`

## Descripción
Implementar el **motor de juego** en `nova-domain` (Java 21 puro, sin Spring/JPA) con el diseño de **núcleo data-oriented + doble materialización** descrito en el readme [§2.1.7](../../readme.md#217-motor-de-juego-núcleo-data-oriented-y-doble-materialización). Es el corazón compartido por la ruta de producción (`HU-1-BE-02`) y por el simulador (`HU-2-BE-01`), por lo que su rendimiento y su determinismo son críticos.

Piezas a entregar:
- **`GameCompiler` → `CompiledGame`**: compila una vez el `config` JSON (apartado 3.3) a estructuras primitivas (símbolos a IDs `int` densos, reels `int[][]`, paytable `long[]`, paylines `int[][]`, bonus a primitivos). Inmutable; **cacheado por `configId`**.
- **`SpinKernel`**: dado `(CompiledGame, RngEngine, RoundSink)` resuelve el giro y la cascada de free spins (con *retrigger*) usando **solo primitivos**, **aritmética entera** y **cero asignaciones por giro** (buffers reutilizables). Empuja cada giro/free spin al `RoundSink` (no devuelve objetos ricos).
- **`RoundSink`** (puerto, patrón *Visitor*): `onSpin(window, winningLines, winCents, isFreeSpin, multiplier, …)` con buffers primitivos. (Las implementaciones `CountingSink` y `MaterializingSink` viven en `HU-2-BE-01` y `HU-1-BE-02` respectivamente.)
- Aplica **Strategy** para las features de bonus (Wild/Scatter/FreeSpins) y **Builder/Compiler** para `CompiledGame`.

## Criterios de aceptación
- **AC1**: Dado un `config` válido (apartado 3.3), `GameCompiler` produce un `CompiledGame` con símbolos como IDs `int`, reels/paylines/paytable en arrays primitivos.
- **AC2**: `SpinKernel` resuelve un giro y emite al `RoundSink` la ventana de símbolos, las paylines ganadoras, el premio (`long` céntimos), el contador de scatters y el multiplicador. El **premio total** = `Σ_líneas (multiplicador × lineBet)` + `Σ_scatter (multiplicador × betCents)`, con `lineBet = betCents / paylines.length` (ver readme 3.3.3): el premio de línea se calcula sobre la **apuesta por línea** y el de scatter sobre la **apuesta total**.
- **AC3**: **Determinismo** — con el mismo `seed` y `CompiledGame`, dos ejecuciones emiten exactamente la misma secuencia de giros (verificable bit a bit).
- **AC4**: **Cero asignación por giro** — un test de *allocation* confirma que `SpinKernel.spin(...)` no asigna en el *steady state* (buffers reutilizados).
- **AC5**: **Aritmética entera** — el cálculo de premios usa `long`/`int`; no hay `double` en el camino de decisión del resultado.
- **AC6**: Los `WILD` sustituyen a los `REGULAR` declarados en `bonus.wild.substitutes` (nunca a `SCATTER`); con ≥ `minTriggerCount` scatters se emite la ronda completa de free spins (`bet=0`, multiplicador, *retrigger*). El premio de scatter (campo opcional `scatterPays`, 3.3.1) es **independiente** del disparo de free spins: un recuento de scatter puede pagar premio sin disparar free spins, y viceversa.
- **AC6b** (semántica de evaluación, ver 3.3.3): cada payline se evalúa izq→der desde la columna 0 y paga **el mejor combo** posible (el `WILD` maximiza; una línea de solo `WILD` paga el `REGULAR` de mayor valor, una vez por línea). Los rodillos son **circulares** (la ventana hace *wrap* módulo `len`), con una `nextInt(len)` por reel en orden de columna. El `retrigger` de free spins es **ilimitado** y el `multiplier` aplica a **todos** los premios de la ronda.
- **AC7**: El `CompiledGame` se cachea por `configId` y se reutiliza entre giros y entre vías (producción/simulador).
- **AC8**: Cero dependencias de Spring/JPA en `nova-domain` (verificable con ArchUnit, ver `HU-1-QA-01`).

## Prioridad
Must Have

## Estimación
8 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-domain`, `motor`, `data-oriented`, `performance`, `determinismo`, `gambling`

## Comentarios
- Es el ticket de **mayor riesgo técnico** del proyecto: rendimiento (habilita 10M/<10 min) y determinismo (habilita replay y "lo simulado = lo jugado").
- El motor **no calcula el RTP teórico** de los juegos reales: ese objetivo (`rtp_target`) lo **declara el matemático** (`HU-7`). El RTP empírico lo mide el simulador (`HU-2`) sobre este mismo kernel. (El cálculo cerrado exacto de free spins con retrigger es matemática no trivial y queda en el criterio del matemático.)
- El detalle de diseño (kernel + sinks + reglas de determinismo) está en el readme §2.1.7.
- **Dependencias directas:** `HU-1-DEV-01` (intra, esqueleto Maven/infra). Es la base del motor; casi todo el backend depende de este ticket directa o transitivamente.

## Enlaces y referencias
- Historia: [HU-1](../../stories/HU-1.md).
- **Diseño del motor: [§2.1.7](../../readme.md#217-motor-de-juego-núcleo-data-oriented-y-doble-materialización)**.
- Patrones: [§2.1.4](../../readme.md#21-diagrama-de-arquitectura).
- Componentes (`nova-domain`): [§2.2](../../readme.md#22-descripción-de-componentes-principales).
- Esquema del config: [§3.3](../../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
