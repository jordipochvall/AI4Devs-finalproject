# HU-23-DB-01 — Particionado temporal de `game_rounds` + migración del histórico

## Código
`HU-23-DB-01` — vinculado con **HU-23: Escalado de la auditoría: particionado y retención**.

## Título
Migración a `game_rounds` particionada por rango temporal, sin pérdida

## Descripción
Convertir `game_rounds` en una tabla **particionada por rango** (p. ej. mensual) y migrar el histórico existente a las particiones correspondientes sin pérdida ni duplicados, preservando la inmutabilidad y la cadena de integridad (HU-20). Es la migración de mayor riesgo del bloque y debe coordinarse con HU-20.

## Criterios de aceptación
- **AC1**: `game_rounds` queda particionada por rango temporal y las inserciones/consultas usan la partición correcta de forma transparente.
- **AC2**: La migración del histórico no pierde ni duplica ninguna partida.
- **AC3**: La inmutabilidad (triggers) y la cadena de integridad se mantienen tras la migración.

## Prioridad
Could Have

## Estimación
5 SP

## Equipo responsable
DB

## Etiquetas
`db`, `flyway`, `particionado`, `escalado`, `fase-post-mvp`

## Comentarios
- Decisión diferida D4. Coordinar con `HU-20` para no romper el encadenado de hashes.
- **Dependencias directas:** `HU-1-DB-01` (esquema `game_rounds`, externa).

## Enlaces y referencias
- Historia: [HU-23](../../stories/HU-23.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Decisión diferida: [§1.5 D4](../../readme.md#15-supuestos-y-decisiones-diferidas) · Modelo [§3.2.8](../../readme.md#32-descripción-de-entidades-principales).
