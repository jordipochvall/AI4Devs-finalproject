# HU-8 — El matemático interpreta resultados con IA

> **Como** analista matemático,
> **quiero** preguntar en lenguaje natural sobre los resultados de una simulación,
> **para** entender el comportamiento de la matemática (p. ej. por qué sube la volatilidad) sin analizar las métricas a mano.

**Contexto y valor.** Es uno de los cuatro pilares diferenciales del producto (*AI-powered explainability*). Sobre una simulación ya completada (HU-2), el backend envía las métricas a Claude (Anthropic) y devuelve una explicación interpretable. Cada pregunta y respuesta queda registrada para trazabilidad.

**Prioridad:** Should Have · **Estimación:** M · **Endpoint:** `POST /math/simulations/{simulationId}/explain`

## Criterios de aceptación (BDD)

```gherkin
# language: es
Característica: Explicación de una simulación con IA

  Antecedentes:
    Dado un analista matemático autenticado
    Y una simulación en estado "COMPLETED" con sus métricas calculadas

  Escenario: Preguntar sobre una simulación completada
    Cuando pregunto "¿por qué la volatilidad es más alta que en la versión anterior?"
    Entonces recibo una explicación en lenguaje natural basada en las métricas de la simulación
    Y la pregunta, la respuesta y el modelo usado quedan registrados

  Escenario: Preguntar sobre una simulación aún no terminada
    Dado que la simulación está en estado "RUNNING"
    Cuando intento preguntar sobre ella
    Entonces la petición se rechaza indicando que la simulación no ha finalizado

  Escenario: Preguntar sobre una simulación inexistente
    Cuando pregunto sobre un identificador de simulación que no existe
    Entonces se devuelve un error "no encontrado"

  Escenario: IA no disponible
    Dado que la plataforma se ha desplegado sin clave de API de Anthropic
    Cuando intento preguntar sobre una simulación
    Entonces se informa de que la funcionalidad de IA no está disponible
    Y el resto de la plataforma sigue funcionando con normalidad
```

## Verificación INVEST

| Criterio | Cumplimiento |
|---|---|
| **I**ndependiente | Depende de la existencia de una simulación completada (HU-2), pero su implementación (integración con Claude) es autónoma. |
| **N**egociable | El modelo concreto de Claude y el formato del *prompt* son negociables; el valor (explicación interpretable) no. |
| **V**aliosa | Diferencial de producto que acelera el análisis del matemático y refuerza el posicionamiento con IA. |
| **E**stimable | Un endpoint + adaptador a la API de Anthropic + persistencia de la Q&A; alcance acotado. |
| **S**mall | Cabe en un sprint. |
| **T**estable | Verificable con un adaptador *fake* determinista (200, registro de la Q&A) y los casos de error (422/404/503). |

**Fuera de alcance:** el historial paginado de preguntas (`GET .../explanations`) es **post-MVP**; el *prompt caching* para reducir coste se difiere (ver [§1.5, D10](../readme.md#15-supuestos-y-decisiones-diferidas)).

---

## Dependencias

Depende directamente de: **[HU-2](HU-2.md)** (necesita una simulación `COMPLETED` que explicar).

> Solo se listan dependencias **directas** (reducción transitiva); las indirectas se alcanzan a través de ellas. Árbol completo en [stories.md](stories.md).

---

- Historia relacionada con: [§1.2 Características (bloque C4)](../readme.md#12-características-y-funcionalidades-principales) y [§1.3 Flujo 3](../readme.md#13-diseño-y-experiencia-de-usuario).
- Endpoint: catálogo [§4.2](../readme.md#42-catálogo-de-endpoints) (`/math/simulations/{id}/explain`).
- Integración: [§2.2.4 Servicios externos (Anthropic)](../readme.md#22-descripción-de-componentes-principales).
- Modelo de datos: [§3.2.10 simulation_explanations](../readme.md#32-descripción-de-entidades-principales).
- Tickets de trabajo: [`tickets/HU-8/`](../tickets/HU-8/) (BE-01 adaptador Anthropic/endpoint, FE-01 caja de pregunta, QA-01 tests).
