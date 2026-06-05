# HU-1-QA-01 — Tests del flujo de juego

## Código
`HU-1-QA-01` — vinculado con **HU-1: El jugador realiza un giro**.

## Título
Suite de tests del flujo de juego (unit del motor + property-based + IT del spin + E2E)

## Descripción
Pirámide de tests del **flujo del spin** (alineada con 2.6), centrada en el motor y el caso de uso del giro (la autenticación se prueba en `HU-4-QA-01`):

- **Unit (JUnit 5 + AssertJ)** en `nova-domain/src/test`: determinismo del motor (mismo seed → mismo `Round`), evaluación de paylines, sustituciones de wild y trigger de free spins.
- **Golden-master del `SpinKernel`**: un **corpus congelado** de fixtures `(seed, config) → result` (versionado en el repo) que el motor debe reproducir **bit a bit**; si el output deriva, **el build falla**. Es el *tripwire* que hace **consciente** cualquier ruptura del determinismo/comportamiento del motor — ante un fallo, el equipo decide explícitamente entre revertir (accidental) o re-baselinar el corpus (cambio intencionado, preferiblemente modelado como nueva versión de `config`). No persigue retrocompatibilidad: los replays históricos no dependen de esto (guardar-y-renderizar, ver 2.5.3 y `HU-3-BE-02`).
- **Property-based (jqwik)**: para 100 `config` **de prueba con RTP conocido por construcción** (fixtures sintéticos, no juegos reales), el RTP empírico tras 1M de spins converge a ese RTP **dentro del intervalo de confianza** (tolerancia que escala con el error estándar, no un umbral fijo). Verifica el *motor*, no el RTP objetivo de juegos reales (que lo declara el matemático).
- **ArchUnit**: ninguna clase de `nova-domain.*` importa `org.springframework.*` ni `jakarta.persistence.*`.
- **Integration (Failsafe + Testcontainers)** en `nova-web-api/src/it`: `POST .../spin` con JWT verifica respuesta, nueva fila en `game_rounds`, `wallet_transactions` BET+WIN coherentes, fallo de UPDATE/DELETE por el trigger y la idempotencia (misma key ⇒ mismo resultado; key+payload distinto ⇒ `409`).
- **E2E (Playwright en `e2e/`)**: *happy path* entrar al juego → spin → balance actualizado.

## Criterios de aceptación
- **AC1**: Cobertura `nova-domain` ≥ 90 %; `nova-application` ≥ 80 % (Jacoco).
- **AC2**: Los tests property-based ejecutan y pasan.
- **AC3**: La regla ArchUnit falla el build si se acopla Spring/JPA en `nova-domain`.
- **AC4**: El E2E del spin se ejecuta en el *job* `e2e` de CI.
- **AC5**: Una regresión del determinismo del motor hace fallar al menos un test (unit y/o golden-master).
- **AC6**: El **corpus golden-master** existe, está versionado y se ejecuta en CI (`build-test`); reproducir el motor sobre cada fixture da el `result` esperado bit a bit, y un cambio que altere la salida hace fallar el build.

## Prioridad
Must Have

## Estimación
5 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `junit`, `jqwik`, `golden-master`, `determinismo`, `archunit`, `testcontainers`, `playwright`

## Comentarios
- El job `perf` (10M spins) es de `HU-2`. Los tests de auth son de `HU-4-QA-01`.
- **Dependencias directas:** `HU-1-FE-01` (intra) — que arrastra transitivamente `HU-1-BE-02` → `HU-1-BE-01` → `HU-1-DEV-01`.

## Enlaces y referencias
- Historia: [HU-1](../../stories/HU-1.md).
- Estrategia de tests: [2.6](../../readme.md#26-tests).
