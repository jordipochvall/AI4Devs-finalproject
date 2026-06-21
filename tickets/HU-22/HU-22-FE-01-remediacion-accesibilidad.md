# HU-22-FE-01 — Remediación de accesibilidad por superficie

## Código
`HU-22-FE-01` — vinculado con **HU-22: Accesibilidad WCAG 2.1 AA**.

## Título
Adecuación a WCAG 2.1 AA de las superficies jugador, operador y matemático

## Descripción
Elevar las tres superficies a WCAG 2.1 AA: navegación por teclado con foco visible, nombres/roles/estados ARIA en los controles, contraste mínimo AA, anuncio de resultados del giro y de los mensajes de juego responsable, y respeto de `prefers-reduced-motion` en las animaciones del giro y la cinemática de free spins.

## Criterios de aceptación
- **AC1**: Todas las acciones clave del jugador (seleccionar juego, ajustar apuesta, girar) son operables solo con teclado, con foco visible.
- **AC2**: Los controles exponen nombre, rol y estado a un lector de pantalla; los resultados y mensajes se anuncian.
- **AC3**: El contraste de texto cumple AA; con "movimiento reducido" las animaciones del giro se atenúan o desactivan.
- **AC4**: Las superficies de operador y matemático también cumplen los criterios anteriores en sus vistas principales.

## Prioridad
Should Have

## Estimación
5 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `accesibilidad`, `wcag`

## Comentarios
- Transversal: prioriza la superficie de jugador (la más rica en interacción/animación).
- **Dependencias directas:** `HU-1-FE-01` (superficies FE; transversal a operador y matemático).

## Enlaces y referencias
- Historia: [HU-22](../../stories/HU-22.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Decisión diferida: [§1.5 D8](../../readme.md#15-supuestos-y-decisiones-diferidas) · Diseño y UX [§1.3](../../readme.md#13-diseño-y-experiencia-de-usuario).
