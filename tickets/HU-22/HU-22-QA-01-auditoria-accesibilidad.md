# HU-22-QA-01 — Auditoría de accesibilidad (axe) + pruebas de teclado/lector

## Código
`HU-22-QA-01` — vinculado con **HU-22: Accesibilidad WCAG 2.1 AA**.

## Título
Pruebas automáticas y manuales de accesibilidad de las vistas principales

## Descripción
Ejecutar la auditoría automática (axe-core) sobre las vistas principales y realizar pruebas manuales de navegación por teclado y lector de pantalla, comprobando que no hay violaciones de nivel A/AA.

## Criterios de aceptación
- **AC1**: La auditoría automática no reporta violaciones A/AA en las vistas principales de las tres superficies.
- **AC2**: La operación completa del jugador es posible solo con teclado, con foco visible en cada paso.
- **AC3**: Un lector de pantalla anuncia controles, resultados del giro y mensajes de juego responsable.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `accesibilidad`, `wcag`, `axe`

## Comentarios
- **Dependencias directas:** `HU-22-FE-01`.

## Enlaces y referencias
- Historia: [HU-22](../../stories/HU-22.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Estrategia de tests: [§2.6](../../readme.md#26-tests).
