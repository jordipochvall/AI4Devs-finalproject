# HU-9 — El jugador usa auto-spin con safeguards de juego responsable

> **Como** jugador autenticado,
> **quiero** lanzar una tanda de giros automáticos que se detenga sola en condiciones de seguridad,
> **para** jugar de forma cómoda sin perder el control de mi gasto.

**Contexto y valor.** Recoge la feature [1.2-A5](../readme.md#12-características-y-funcionalidades-principales), que la HU-1 difiere explícitamente como historia independiente. Es comportamiento de cliente sobre el endpoint de giro (no añade endpoint nuevo): repite el `spin` y aplica límites de parada alineados con el espíritu de juego responsable de la DGOJ.

**Prioridad:** Should Have · **Estimación:** S · **Endpoint:** reutiliza `POST /player/games/{gameId}/spin` ★ (sin endpoint nuevo)

## Criterios de aceptación (BDD)

```gherkin
# language: es
Característica: Auto-spin con safeguards

  Antecedentes:
    Dado un jugador autenticado en la pantalla de un juego
    Y una apuesta seleccionada de 1,00 €

  Escenario: Lanzar una tanda de auto-spin
    Cuando activo el auto-spin para 25 giros
    Entonces se ejecutan giros automáticos de forma secuencial
    Y el saldo y la rejilla se actualizan en cada giro

  Escenario: Parada automática al alcanzar el número de giros
    Dado que he lanzado un auto-spin de 25 giros
    Cuando se completa el giro número 25
    Entonces el auto-spin se detiene automáticamente

  Escenario: Parada automática por umbral de saldo (safeguard)
    Dado que he configurado un límite de parada si el saldo baja de 10,00 €
    Cuando un giro deja el saldo por debajo de 10,00 €
    Entonces el auto-spin se detiene y se muestra un mensaje de pausa de juego responsable

  Escenario: Parada manual
    Dado que hay un auto-spin en curso
    Cuando pulso "Detener"
    Entonces el auto-spin se detiene tras el giro actual

  Escenario: Parada por saldo insuficiente para la apuesta
    Dado que hay un auto-spin en curso
    Cuando el saldo es inferior a la apuesta configurada
    Entonces el auto-spin se detiene sin ejecutar más giros
```

## Verificación INVEST

| Criterio | Cumplimiento |
|---|---|
| **I**ndependiente | Se apoya en el `spin` de HU-1, pero su lógica (bucle + safeguards) es autónoma y se prueba por separado. |
| **N**egociable | Los tipos de límite ofrecidos (nº de giros, umbral de saldo, pérdida máxima) son negociables. |
| **V**aliosa | Mejora la experiencia y materializa el compromiso de juego responsable del producto. |
| **E**stimable | Comportamiento de cliente acotado sobre un endpoint ya existente. |
| **S**mall | Pequeña; cabe en parte de un sprint. |
| **T**estable | Verificable con tests E2E (parada por nº de giros, por umbral y manual). |

**Fuera de alcance:** límites de pérdida persistentes entre sesiones y autoexclusión (**post-MVP**, ver [§1.5, D7](../readme.md#15-supuestos-y-decisiones-diferidas)).

---

## Dependencias

Depende directamente de: **[HU-1](HU-1.md)** (extiende el `<SlotGame>` y el endpoint de giro).

> Solo se listan dependencias **directas** (reducción transitiva); las indirectas se alcanzan a través de ellas. Árbol completo en [stories.md](stories.md).

---

- Feature de origen: [§1.2-A5](../readme.md#12-características-y-funcionalidades-principales).
- Endpoint reutilizado: [§4.4.3 Giro](../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios).
- Relación: extiende [HU-1](HU-1.md). Tickets de trabajo: [`tickets/HU-9/`](../tickets/HU-9/) (FE-01 auto-spin, QA-01 tests).
