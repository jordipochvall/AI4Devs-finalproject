# HU-25-QA-01 — Tests de aislamiento entre operadores

## Código
`HU-25-QA-01` — vinculado con **HU-25: Gestión multi-operador (super-admin)**.

## Título
Tests de alta de operador y de no-fuga de datos entre tenants

## Descripción
Verificar el alta de un operador y, sobre dos operadores con actividad, que ninguna superficie (jugadores, partidas, simulaciones) filtra datos del otro. Integración (Failsafe + Testcontainers) con aserciones explícitas de aislamiento.

## Criterios de aceptación
- **AC1**: IT — `POST /admin/operators` crea operador + usuario operador que inicia sesión con datos vacíos.
- **AC2**: IT — con dos operadores con actividad, ninguna consulta de un operador devuelve datos del otro.
- **AC3**: IT — `/admin/*` solo accesible con rol `ADMIN`; otros roles → `403`.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `multi-tenant`, `seguridad`

## Comentarios
- **Dependencias directas:** `HU-25-BE-01`, `HU-25-FE-01`.

## Enlaces y referencias
- Historia: [HU-25](../../stories/HU-25.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
