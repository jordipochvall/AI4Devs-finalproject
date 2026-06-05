# HU-6 — El operador gestiona jugadores y recarga su saldo

> **Como** operador,
> **quiero** buscar a un jugador y recargar su saldo virtual,
> **para** que pueda seguir jugando (en ausencia de pagos reales en esta versión).

**Contexto y valor.** Es el "combustible" del flujo del jugador: sin recarga, el saldo virtual semilla se agota y HU-1 deja de poder ejecutarse. La recarga es una operación con efecto económico, por lo que es **idempotente** (cabecera `Idempotency-Key`).

**Prioridad:** Must Have · **Estimación:** S · **Endpoints:** `GET /operator/players`, `POST /operator/players/{playerId}/wallet/recharge`

## Criterios de aceptación (BDD)

```gherkin
# language: es
Característica: Gestión de jugadores y recarga de saldo

  Antecedentes:
    Dado un operador autenticado
    Y un jugador "ana@example.com" con 0,50 € de saldo virtual

  Escenario: Buscar un jugador por email
    Cuando busco jugadores cuyo email contenga "ana"
    Entonces obtengo una lista paginada que incluye a "ana@example.com" con su saldo actual

  Escenario: Recargar el saldo de un jugador
    Cuando recargo 50,00 € al jugador "ana@example.com"
    Entonces su saldo pasa a 50,50 €
    Y queda registrado un movimiento de tipo "RECHARGE" con el operador que lo realizó

  Escenario: Recarga con importe no válido
    Cuando intento recargar un importe de 0,00 € o negativo
    Entonces la recarga se rechaza con un error de validación
    Y el saldo del jugador no cambia

  Escenario: Recarga a un jugador inexistente
    Cuando intento recargar a un jugador que no existe
    Entonces se devuelve un error "no encontrado"

  Escenario: Doble envío de la misma recarga (idempotencia)
    Dado que envío una recarga con una cabecera Idempotency-Key
    Cuando la misma petición se reenvía por un reintento de red
    Entonces el saldo solo se incrementa una vez

  Escenario: Acceso por un rol no autorizado
    Dado que estoy autenticado con rol "PLAYER"
    Cuando intento recargar el saldo de un jugador
    Entonces el acceso se deniega por falta de permisos
```

## Verificación INVEST

| Criterio | Cumplimiento |
|---|---|
| **I**ndependiente | Depende solo de la autenticación (HU-4); puede probarse con jugadores semilla. |
| **N**egociable | El límite máximo de recarga o el formato de búsqueda son negociables. |
| **V**aliosa | Habilita la continuidad del juego y simula el flujo de aprovisionamiento de saldo de cara a la futura integración de pagos. |
| **E**stimable | Dos endpoints, uno de lectura y uno de escritura idempotente; alcance claro. |
| **S**mall | Pequeña; cabe en parte de un sprint. |
| **T**estable | Verificable con tests de integración (200/422/404, idempotencia, 403) y comprobación del movimiento en el ledger. |

**Fuera de alcance:** alta/baja de jugadores y edición de su perfil; límites de pérdida y autoexclusión (**post-MVP**).

---

## Dependencias

Depende directamente de: **[HU-4](HU-4.md)** (autenticación del operador). *(El mecanismo de idempotencia que reutiliza la recarga es una dependencia a nivel de ticket: `HU-6-BE-01` → `HU-1-BE-02`.)*

> Solo se listan dependencias **directas** (reducción transitiva); las indirectas se alcanzan a través de ellas. Árbol completo en [stories.md](stories.md).

---

- Historia relacionada con: [§1.2 Características (bloque B)](../readme.md#12-características-y-funcionalidades-principales).
- Endpoints: catálogo [§4.2](../readme.md#42-catálogo-de-endpoints) (`/operator/players`, `/operator/players/{id}/wallet/recharge`).
- Idempotencia: [§2.5.4](../readme.md#25-seguridad) y modelo [§3.2.11 idempotency_keys](../readme.md#32-descripción-de-entidades-principales).
- Ledger: [§3.2.4 wallet_transactions](../readme.md#32-descripción-de-entidades-principales).
- Tickets de trabajo: [`tickets/HU-6/`](../tickets/HU-6/) (BE-01 jugadores/recarga, FE-01 backoffice, QA-01 tests).
