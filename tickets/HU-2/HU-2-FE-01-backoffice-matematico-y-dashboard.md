# HU-2-FE-01 — Backoffice matemático y dashboard

## Código
`HU-2-FE-01` — vinculado con **HU-2: El matemático valida un juego con el simulador**.

## Título
Backoffice matemático: lanzar simulación, *polling* y dashboard de métricas

## Descripción
Implementar la superficie del matemático en la SPA (`frontend/src/math/`), accesible en `/math`:

- **Selector de juego y versión de `config`** (consume `GET /math/games` y `GET /math/configs/{id}`).
- **Editor mínimo del `config`** que muestra el objeto JSON (apartado 3.3) en un editor de texto con validación cliente del esquema; permite **declarar el `rtp_target`/`volatility_target`** (objetivo del matemático); al guardar, llama a `POST /math/games/{id}/configs` y muestra los `errors[]` devueltos por el backend si el `config` es inválido.
- **Panel de simulación**: campos `numSpins` (1 ≤ N ≤ 10 000 000, por defecto 10 000 000) y `betCents` + botón **Simular**. Al pulsar, llama a `POST .../simulations` y comienza el *polling* sobre `GET .../simulations/{id}` (TanStack Query, intervalo 5 s, *stop* al llegar a `COMPLETED`/`FAILED`).
- **Dashboard de resultados** (herramientas para el matemático, ver C3): RTP global **con su intervalo de confianza** y **comparación con el `rtp_target` declarado** (verde/ámbar/rojo según desviación vs umbral, considerando el IC); **curva de convergencia** (RTP vs nº de giros); **descomposición** RTP base game / free spins y **contribución por símbolo/feature**; **estadísticas de reel strips** (frecuencias, P(trigger), free spins esperados); volatilidad, hit frequency, max win, *longest dry streak*; **histograma** del `prizeDistribution` con percentiles de cola.

## Criterios de aceptación
- **AC1**: Solo los usuarios con rol `MATH_ANALYST` pueden acceder a `/math` (redirección a `/login` o `403` para otros roles).
- **AC2**: Al guardar un `config` inválido, los errores se muestran en línea (sobre el campo o como lista junto al editor).
- **AC3**: Tras lanzar una simulación de 10 M spins, la UI muestra el estado `RUNNING` con un indicador de progreso (basado en *polling*) hasta que pase a `COMPLETED`.
- **AC4**: Cuando llega `COMPLETED`, el dashboard pinta las métricas con formato (porcentajes con dos decimales, *currencies* en formato del `locale`), incluyendo el **IC del RTP**, la **curva de convergencia**, la **descomposición** y la **comparación con `rtp_target`**.
- **AC5**: La UI funciona en **español e inglés**.

## Prioridad
Must Have

## Estimación
5 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `tanstack-query`, `dashboard`, `polling`, `editor-json`, `i18n`

## Comentarios
- La feature **AI explainability** (caja de pregunta a Claude) se entrega aparte como complemento del dashboard si entra en sprint; está documentada en C4 y en el endpoint `POST .../simulations/{id}/explain`.
- Para el histograma, usar una librería ligera (p. ej. `recharts`).
- **Dependencias directas:** `HU-2-BE-02` (intra).

## Enlaces y referencias
- Historia: [HU-2](../../readme.md#5-historias-de-usuario).
- Funcionalidades del matemático: [1.2 Características](../../readme.md#12-características-y-funcionalidades-principales), bloque C.
- Wireframe: [1.3 Diseño y UX](../../readme.md#13-diseño-y-experiencia-de-usuario), bloque "Backoffice matemático".
- API: [4.4.4](../../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios), catálogo [4.2](../../readme.md#42-catálogo-de-endpoints).
