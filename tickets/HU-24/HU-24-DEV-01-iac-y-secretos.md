# HU-24-DEV-01 — IaC del entorno cloud + gestión de secretos

## Código
`HU-24-DEV-01` — vinculado con **HU-24: Despliegue cloud con entrega continua**.

## Título
Infraestructura como código y gestión de secretos para el entorno cloud

## Descripción
Definir la infraestructura del entorno cloud (API, web, Postgres gestionado, red) como **código** (IaC) y la inyección de secretos (BBDD, `JWT_SECRET`, `ANTHROPIC_API_KEY`) desde un gestor de secretos, nunca embebidos en la imagen. Materializa la decisión diferida D9 sobre la base de la CI existente (`HU-1-DEV-01`).

## Criterios de aceptación
- **AC1**: La infraestructura del entorno se describe y aprovisiona como código, reproducible.
- **AC2**: Los secretos se inyectan desde un gestor de secretos; no aparecen en imágenes ni en el repo.
- **AC3**: El entorno levanta API + web + Postgres con comprobación de salud.

## Prioridad
Should Have

## Estimación
5 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `iac`, `cloud`, `secretos`, `fase-post-mvp`

## Comentarios
- Decisión diferida D9. El proveedor cloud y la herramienta de IaC son negociables.
- **Dependencias directas:** `HU-1-DEV-01` (CI/Docker, externa, fundación).

## Enlaces y referencias
- Historia: [HU-24](../../stories/HU-24.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Decisión diferida: [§1.5 D9](../../readme.md#15-supuestos-y-decisiones-diferidas) · Infra y despliegue [§2.4](../../readme.md#24-infraestructura-y-despliegue).
