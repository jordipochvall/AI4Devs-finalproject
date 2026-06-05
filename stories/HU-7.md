# HU-7 — El matemático edita y versiona la matemática de un juego

> **Como** analista matemático,
> **quiero** consultar la matemática de un juego y crear una nueva versión editando su configuración,
> **para** iterar sobre el RTP y la volatilidad antes de simular y validar.

**Contexto y valor.** Es el paso previo a HU-2 (simular): sin poder crear versiones de `config`, el matemático solo podría simular la configuración semilla. Cada guardado genera una **versión inmutable nueva** (nunca modifica una existente), lo que da trazabilidad y permite *rollback*.

**Prioridad:** Must Have · **Estimación:** M · **Endpoints:** `GET /math/games`, `GET /math/configs/{configId}`, `POST /math/games/{gameId}/configs`

## Criterios de aceptación (BDD)

```gherkin
# language: es
Característica: Edición y versionado de la matemática de un juego

  Antecedentes:
    Dado un analista matemático autenticado
    Y el juego "Espacial" con una versión de configuración activa (versión 1)

  Escenario: Consultar los juegos y su versión activa
    Cuando accedo al backoffice matemático
    Entonces veo la lista de juegos con su versión de matemática activa

  Escenario: Consultar el detalle de una versión
    Cuando abro la versión 1 del juego "Espacial"
    Entonces veo su configuración completa (símbolos, reels, paylines, paytable, bonus) y su RTP objetivo declarado

  Escenario: Crear una nueva versión válida
    Dado que parto de la versión 1
    Cuando modifico el peso de un símbolo, declaro el RTP objetivo y guardo
    Entonces se crea la versión 2 con el RTP objetivo que he declarado
    Y la versión 1 permanece inalterada

  Escenario: Rechazo de una configuración inválida
    Cuando guardo una configuración que referencia un símbolo inexistente en una payline
    Entonces el guardado se rechaza con el detalle del error de validación
    Y no se crea ninguna versión nueva

  Escenario: Acceso por un rol no autorizado
    Dado que estoy autenticado con rol "OPERATOR"
    Cuando intento crear una versión de matemática
    Entonces el acceso se deniega por falta de permisos
```

## Verificación INVEST

| Criterio | Cumplimiento |
|---|---|
| **I**ndependiente | Depende de la autenticación (HU-4) y del esquema de `config` (3.3); no de HU-2/3. |
| **N**egociable | La riqueza del editor (texto JSON vs editor visual) es negociable; el versionado inmutable no. |
| **V**aliosa | Habilita la iteración matemática, base del ciclo de diseño del *game studio*. |
| **E**stimable | Dos lecturas y una escritura con validación de invariantes (3.3.3); alcance claro. |
| **S**mall | Cabe en un sprint. |
| **T**estable | Verificable con tests de integración (201 con el `rtpTarget` declarado, 422 con `errors`, inmutabilidad de versiones previas, 403). |

**Fuera de alcance:** la **publicación** (activar una versión, `POST /math/games/{id}/publish`) y el listado del histórico de versiones, ambos **post-MVP**. El versionado habilita **simular cualquier versión** (la simulación opera sobre un `configId` concreto); pero en el MVP el jugador siempre juega la `config` semilla activa: **activar/servir una versión nueva al jugador es post-MVP** (ver nota en [§4.2](../readme.md#42-catálogo-de-endpoints)).

---

## Dependencias

Depende directamente de: **[HU-4](HU-4.md)** (autenticación del matemático).

> Solo se listan dependencias **directas** (reducción transitiva); las indirectas se alcanzan a través de ellas. Árbol completo en [stories.md](stories.md).

---

- Historia relacionada con: [§1.2 Características (bloque C)](../readme.md#12-características-y-funcionalidades-principales) y [§1.3 Flujo 3](../readme.md#13-diseño-y-experiencia-de-usuario).
- Endpoints: catálogo [§4.2](../readme.md#42-catálogo-de-endpoints) (`/math/games`, `/math/configs/{id}`, `/math/games/{id}/configs`).
- Esquema y validación del `config`: [§3.3](../readme.md#33-esquema-del-json-de-configuración-de-juego-game_configsconfig).
- Modelo de datos: [§3.2.6 game_configs](../readme.md#32-descripción-de-entidades-principales).
- Tickets de trabajo: [`tickets/HU-7/`](../tickets/HU-7/) (BE-01 endpoints edición/versionado, FE-01 editor, QA-01 tests).
