# HU-3 — El operador resuelve una reclamación con el replay

> **Como** operador,
> **quiero** localizar y reproducir visualmente una partida concreta de un jugador,
> **para** resolver una reclamación demostrando el resultado exacto del giro.

**Contexto y valor.** Es el diferencial de soporte y *compliance*: convierte una disputa "su palabra contra la nuestra" en una prueba reproducible, apoyada en el registro auditable y el RNG determinista.

**Prioridad:** Must Have · **Estimación:** M · **Endpoint:** `GET /operator/rounds/{roundId}/replay` ★

## Criterios de aceptación (BDD)

```gherkin
# language: es
Característica: Auditoría y replay de una partida

  Antecedentes:
    Dado un operador autenticado
    Y un jugador "user42" con partidas registradas

  Escenario: Localizar la partida reclamada
    Cuando filtro la auditoría por el jugador "user42" y un rango de fechas
    Entonces obtengo la lista de sus partidas con apuesta, premio y fecha

  Escenario: Reproducir el giro de forma determinista
    Dado que he localizado la partida reclamada
    Cuando solicito su replay
    Entonces se reproduce la animación exacta del giro
    Y los símbolos, las líneas ganadoras y el premio coinciden con los registrados

  Escenario: El replay es reproducible
    Dado una misma partida auditada
    Cuando reproduzco su replay varias veces
    Entonces el resultado es idéntico en todas las reproducciones

  Escenario: Partida inexistente
    Cuando solicito el replay de un identificador de partida que no existe
    Entonces se devuelve un error "no encontrado"
```

## Verificación INVEST

| Criterio | Cumplimiento |
|---|---|
| **I**ndependiente | No depende de HU-1 ni HU-2; opera sobre partidas ya registradas (datos semilla o de QA). |
| **N**egociable | La riqueza visual del *replay* es negociable; la fidelidad determinista no. |
| **V**aliosa | Reduce el coste y el tiempo de resolución de disputas y respalda la transparencia ante la DGOJ. |
| **E**stimable | El replay **renderiza el `result` inmutable** ya guardado (no recalcula); alcance claro. |
| **S**mall | La auditoría con filtros y el *replay* caben juntos en un sprint. |
| **T**estable | El determinismo se verifica reproduciendo el mismo `roundId` y comparando resultados. |

**Fuera de alcance:** compartir el *replay* con el jugador mediante un enlace público (mejora posterior).

---

## Dependencias

Depende directamente de: **[HU-1](HU-1.md)** (genera los giros que se auditan/reproducen y aporta el `<SlotGame>` para el render).

> Solo se listan dependencias **directas** (reducción transitiva); las indirectas se alcanzan a través de ellas. Árbol completo en [stories.md](stories.md).

---

- Historia en el readme: [§5. Historias de usuario](../readme.md#5-historias-de-usuario)
- Tickets de trabajo asociados: [`tickets/HU-3/`](../tickets/HU-3/)
- Endpoint prioritario: [§4.4.5 Replay](../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios)
- Nota técnica: el replay sigue el modelo **guardar-y-renderizar** (renderiza el `result` inmutable de `game_rounds`, no recalcula), por lo que el motor puede evolucionar sin mantener versiones antiguas — ver [§2.5.3](../readme.md#253-rng-criptográficamente-fuerte-y-replay-determinista).
