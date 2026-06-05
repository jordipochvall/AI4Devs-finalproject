# HU-5 — El jugador accede al lobby y consulta su saldo

> **Como** jugador autenticado,
> **quiero** ver el catálogo de juegos disponibles y mi saldo virtual, y abrir un juego,
> **para** elegir a qué jugar antes de empezar a girar.

**Contexto y valor.** Es el flujo de navegación previo al giro (HU-1): el lobby muestra las carátulas y el saldo, y al abrir un juego se descarga su `config` (apartado 3.3) para que el cliente lo renderice. Sin esta historia, el jugador no tiene forma de llegar a la pantalla de juego.

**Prioridad:** Must Have · **Estimación:** S · **Endpoints:** `GET /player/games`, `GET /player/games/{gameId}`, `GET /player/wallet`

## Criterios de aceptación (BDD)

```gherkin
# language: es
Característica: Lobby y saldo del jugador

  Antecedentes:
    Dado un jugador autenticado con 1.000,00 € de saldo virtual

  Escenario: Ver el catálogo de juegos activos
    Cuando accedo al lobby
    Entonces veo las carátulas, el nombre y la temática de los juegos activos
    Y no se muestran los juegos marcados como inactivos

  Escenario: Consultar el saldo virtual
    Cuando consulto mi saldo en el lobby
    Entonces veo 1.000,00 € con su divisa

  Escenario: Abrir un juego para jugar
    Cuando selecciono el juego "Espacial"
    Entonces se carga su configuración (rejilla, símbolos, líneas de pago, paytable y bonus)
    Y se muestra la pantalla de juego lista para girar

  Escenario: Abrir un juego inexistente o inactivo
    Cuando solicito un juego que no existe o está inactivo
    Entonces se devuelve un error "no encontrado"

  Escenario: Idioma de la interfaz
    Dado que mi idioma configurado es inglés
    Cuando accedo al lobby
    Entonces los textos de la interfaz se muestran en inglés
```

## Verificación INVEST

| Criterio | Cumplimiento |
|---|---|
| **I**ndependiente | Depende solo de la autenticación (HU-4) y de los datos semilla de juegos; no de HU-1/2/3. |
| **N**egociable | La disposición visual del lobby y el detalle de la carátula son negociables. |
| **V**aliosa | Es el punto de entrada y descubrimiento del catálogo; condiciona la conversión a juego. |
| **E**stimable | Tres lecturas sencillas y dos pantallas; alcance claro. |
| **S**mall | Pequeña; cabe en parte de un sprint. |
| **T**estable | Verificable con tests de integración (catálogo, detalle, 404) y E2E del lobby. |

**Fuera de alcance:** historial de partidas del jugador y extracto de movimientos del wallet, ambos **post-MVP**.

---

## Dependencias

Depende directamente de: **[HU-4](HU-4.md)** (sesión autenticada del jugador).

> Solo se listan dependencias **directas** (reducción transitiva); las indirectas se alcanzan a través de ellas. Árbol completo en [stories.md](stories.md).

---

- Historia relacionada con: [§1.2 Características (bloque A)](../readme.md#12-características-y-funcionalidades-principales) y [§1.3 Flujo 1](../readme.md#13-diseño-y-experiencia-de-usuario).
- Endpoints: catálogo [§4.2](../readme.md#42-catálogo-de-endpoints) (`/player/games`, `/player/games/{id}`, `/player/wallet`).
- Esquema del `config`: [§3.3](../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
- Tickets de trabajo: [`tickets/HU-5/`](../tickets/HU-5/) (BE-01 catálogo/wallet, FE-01 lobby, QA-01 tests).
