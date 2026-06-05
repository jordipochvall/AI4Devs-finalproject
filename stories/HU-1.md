# HU-1 — El jugador realiza un giro

> **Como** jugador registrado,
> **quiero** girar un slot apostando saldo virtual,
> **para** entretenerme y tener la posibilidad de ganar premios.

**Contexto y valor.** Es la interacción central del producto y el flujo más recorrido (lobby → juego → giro). Sostiene la propuesta de valor para el perfil jugador.

**Prioridad:** Must Have · **Estimación:** L · **Endpoint:** `POST /player/games/{gameId}/spin` ★

## Criterios de aceptación (BDD)

```gherkin
# language: es
Característica: Giro en un juego de slot

  Antecedentes:
    Dado un jugador autenticado con 1.000,00 € de saldo virtual
    Y el juego "Espacial" (5x3) activo, con apuesta entre 0,20 € y 10,00 €

  Escenario: Giro sin premio
    Dado que selecciono una apuesta de 1,00 €
    Cuando ejecuto un giro cuyo resultado no tiene combinaciones ganadoras
    Entonces mi saldo pasa a 999,00 €
    Y se muestra la rejilla resultante
    Y la partida queda registrada en mi historial

  Escenario: Giro con premio en línea de pago
    Dado que selecciono una apuesta de 1,00 €
    Cuando ejecuto un giro que alinea 3 símbolos "COMET" en una payline
    Entonces se acredita el premio correspondiente en mi saldo
    Y la línea ganadora se resalta en la rejilla

  Escenario: Activación de la ronda de free spins
    Dado que juego el slot 5x3 "Espacial"
    Cuando un giro produce 3 o más símbolos "SCATTER"
    Entonces se otorga una ronda de giros gratis
    Y la ronda completa se resuelve y se reproduce giro a giro

  Escenario: Apuesta superior al saldo disponible
    Dado que mi saldo es de 0,50 €
    Cuando intento ejecutar un giro con una apuesta de 1,00 €
    Entonces el giro se rechaza con el mensaje "saldo insuficiente"
    Y mi saldo permanece en 0,50 €

  Escenario: Doble envío del mismo giro (idempotencia)
    Dado que envío un giro con una cabecera Idempotency-Key
    Cuando la misma petición se reenvía por un reintento de red
    Entonces se devuelve el resultado del primer giro
    Y no se ejecuta un segundo giro ni se descuenta saldo de nuevo
```

## Verificación INVEST

| Criterio | Cumplimiento |
|---|---|
| **I**ndependiente | Se construye y prueba sin depender de HU-2 ni HU-3; solo requiere los juegos semilla. |
| **N**egociable | El alcance de animaciones y audio es ajustable sin alterar el objetivo de la historia. |
| **V**aliosa | Es la propuesta de valor central para el jugador; sin ella no hay producto. |
| **E**stimable | Alcance acotado a un único flujo de petición/respuesta; el equipo puede tallarla. |
| **S**mall | Es de las mayores del backlog; si excede un sprint se divide en "giro base" y "ronda de free spins". |
| **T**estable | Cada criterio es un escenario Gherkin ejecutable como test de integración y E2E. |

**Fuera de alcance:** auto-spin con *safeguards* (historia independiente), pagos reales.

---

## Dependencias

Depende directamente de: **[HU-5](HU-5.md)** (lobby y `config`/saldo del jugador).

> Solo se listan dependencias **directas** (reducción transitiva); las indirectas se alcanzan a través de ellas. Árbol completo en [stories.md](stories.md).

---

- Historia en el readme: [§5. Historias de usuario](../readme.md#5-historias-de-usuario)
- Tickets de trabajo asociados: [`tickets/HU-1/`](../tickets/HU-1/)
- Endpoint prioritario: [§4.4.3 Giro](../readme.md#44-especificación-openapi-31-y-ejemplos--endpoints-prioritarios)
- Nota técnica: el giro lo resuelve el `SpinKernel` (núcleo data-oriented), que en producción se materializa a `Round` vía `MaterializingSink` — ver [§2.1.7](../readme.md#217-motor-de-juego-núcleo-data-oriented-y-doble-materialización).
