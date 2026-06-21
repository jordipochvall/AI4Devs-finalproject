# HU-20-QA-01 — Tests de integridad y detección de manipulación

## Código
`HU-20-QA-01` — vinculado con **HU-20: Integridad *tamper-evident* de la auditoría**.

## Título
Tests de la cadena de integridad y de la detección de alteraciones

## Descripción
Verificar que la verificación confirma cadenas intactas y que una alteración manual de una fila se detecta, identificando la primera ruptura. Integración (Failsafe + Testcontainers): se inserta actividad, se verifica intacta, se altera ilícitamente una fila por SQL y se comprueba la detección.

## Criterios de aceptación
- **AC1**: IT — sobre actividad registrada normalmente, la verificación reporta cadena intacta.
- **AC2**: IT — tras alterar una fila por SQL directo, la verificación falla e identifica la primera ruptura.
- **AC3**: IT — la verificación respeta el rol (`OPERATOR`) y el aislamiento por operador.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `integridad`, `dgoj`

## Comentarios
- **Dependencias directas:** `HU-20-BE-01`, `HU-20-FE-01`.

## Enlaces y referencias
- Historia: [HU-20](../../stories/HU-20.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
