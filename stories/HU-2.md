# HU-2 — El matemático valida un juego con el simulador

> **Como** analista matemático,
> **quiero** lanzar una simulación masiva de una versión de juego y consultar sus métricas,
> **para** validar que su RTP y volatilidad cumplen el objetivo antes de publicarla.

**Contexto y valor.** Es el diferencial B2B de la plataforma: validar la matemática sobre el **mismo motor de producción**, con la garantía de que lo simulado es lo que se juega.

**Prioridad:** Must Have · **Estimación:** L · **Endpoint:** `POST /math/configs/{configId}/simulations` ★

## Criterios de aceptación (BDD)

```gherkin
# language: es
Característica: Simulación masiva de un juego

  Antecedentes:
    Dado un analista matemático autenticado
    Y una versión de configuración del juego "Espacial" con un RTP objetivo declarado de 96,00 %

  Escenario: Lanzar una simulación
    Cuando lanzo una simulación de 10.000.000 de giros con apuesta fija
    Entonces la simulación se acepta y queda en estado "RUNNING"
    Y recibo un identificador para consultar su progreso

  Escenario: La simulación cumple el objetivo de rendimiento
    Dado que he lanzado una simulación de 10.000.000 de giros
    Cuando la simulación termina
    Entonces ha tardado menos de 10 minutos
    Y su estado es "COMPLETED"

  Escenario: Consultar las métricas del resultado
    Dado que una simulación ha terminado
    Cuando consulto su resultado
    Entonces obtengo el RTP empírico con su intervalo de confianza, la curva de convergencia,
      la volatilidad, la hit frequency, la distribución de premios,
      el RTP de base game y de free spins y la descomposición por símbolo/feature

  Escenario: Validación frente al objetivo declarado
    Dado una configuración con RTP objetivo declarado de 96,00 %
    Cuando se simulan 10.000.000 de giros
    Y la diferencia entre el RTP empírico y el objetivo supera el umbral, fuera del intervalo de confianza
    Entonces el sistema marca la versión como desviada del objetivo

  Escenario: Número de giros fuera de rango
    Cuando intento lanzar una simulación de más de 10.000.000 de giros
    Entonces la petición se rechaza con un error de validación
```

## Verificación INVEST

| Criterio | Cumplimiento |
|---|---|
| **I**ndependiente | No depende de HU-1 ni HU-3; reutiliza el motor de dominio sin BBDD ni wallet. |
| **N**egociable | El conjunto exacto de métricas mostradas es negociable; el objetivo (validar RTP/volatilidad) no. |
| **V**aliosa | Habilita el ciclo de diseño matemático, núcleo del posicionamiento *game studio*. |
| **E**stimable | El motor ya está definido; el simulador es un envoltorio *map-reduce* acotado. |
| **S**mall | Cabe en un sprint; el dashboard de métricas avanzado puede separarse si crece. |
| **T**estable | El objetivo de 10M/<10 min y la convergencia del RTP son verificables automáticamente (job `perf`). |

**Fuera de alcance:** la *AI explainability* (pregunta a Claude) y la publicación a producción son historias independientes.

---

## Dependencias

Depende directamente de: **[HU-1](HU-1.md)** (el `SpinKernel`) y **[HU-7](HU-7.md)** (la versión de `config` a simular).

> Solo se listan dependencias **directas** (reducción transitiva); las indirectas se alcanzan a través de ellas. Árbol completo en [stories.md](stories.md).

---

- Historia en el readme: [§5. Historias de usuario](../readme.md#5-historias-de-usuario)
- Tickets de trabajo asociados: [`tickets/HU-2/`](../tickets/HU-2/)
- Endpoint prioritario: [§4.4.4 Lanzar simulación](../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios)
- Nota técnica: el simulador ejecuta el **mismo `SpinKernel`** que producción, vía un `CountingSink` cero-alloc, lo que garantiza "lo simulado = lo jugado" — ver [§2.1.7](../readme.md#217-motor-de-juego-núcleo-data-oriented-y-doble-materialización).
