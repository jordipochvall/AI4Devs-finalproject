# HU-10-FE-01 — Capa de audio inmersivo

## Código
`HU-10-FE-01` — vinculado con **HU-10: El jugador disfruta de audio inmersivo**.

## Título
Capa de audio (Howler.js): música por temática, SFX y voz de locutor

## Descripción
Implementar la capa de audio del cliente con **Howler.js**: música ambiente en bucle por temática (egipcia, frutas, espacial), SFX comunes (spin, win, big win, free spin trigger), voz de locutor en eventos especiales (Big Win, Free Spins) y un **toggle de silencio persistente** (en el store de sesión). La voz de locutor respeta el idioma del usuario (ES/EN). Los assets se sirven desde `frontend/public/assets/<theme>/`.

## Criterios de aceptación
- **AC1**: Al entrar en un juego, suena la música ambiente de su temática en bucle.
- **AC2**: Cada acción dispara su SFX (spin, win, big win, free spin trigger).
- **AC3**: Los eventos especiales reproducen la voz de locutor en el idioma del usuario.
- **AC4**: El toggle de mute persiste entre sesiones (al volver, sigue silenciado).
- **AC5**: La capa de audio no bloquea ni degrada la animación del giro.

## Prioridad
Should Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `howler`, `audio`, `i18n`

## Comentarios
- Se integra sobre `<SlotGame>` (`HU-1-FE-01`) sin acoplar la lógica de juego.
- Palanca de recorte documentada: voz de locutor solo en ES.
- **Dependencias directas:** `HU-1-FE-01` (externa, `<SlotGame>`).

## Enlaces y referencias
- Historia: [HU-10](../../stories/HU-10.md).
- Funcionalidad A8: [1.2](../../readme.md#12-características-y-funcionalidades-principales).
- Frontend/assets: [2.2.2, 2.3](../../readme.md#22-descripción-de-componentes-principales).
