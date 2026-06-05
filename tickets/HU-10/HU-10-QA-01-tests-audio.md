# HU-10-QA-01 — Tests de la capa de audio

## Código
`HU-10-QA-01` — vinculado con **HU-10: El jugador disfruta de audio inmersivo**.

## Título
Tests de componente de la capa de audio

## Descripción
- **Test de componente**: ante los eventos de juego (spin, win, big win, free spin trigger) se invoca la reproducción de la pista/SFX correcta (verificable mockeando Howler.js).
- **Persistencia del mute**: activar el silencio y recargar mantiene el estado silenciado.
- **Idioma de la voz**: con `locale = en`, el evento especial selecciona la pista de voz en inglés.

## Criterios de aceptación
- **AC1**: Cada evento dispara la llamada de audio esperada (con Howler.js mockeado).
- **AC2**: El estado de mute persiste tras recargar.
- **AC3**: La selección de pista de voz respeta el `locale`.

## Prioridad
Could Have

## Estimación
1 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `audio`, `componente`

## Comentarios
- **Dependencias directas:** `HU-10-FE-01` (intra).

## Enlaces y referencias
- Historia: [HU-10](../../stories/HU-10.md).
- Estrategia de tests: [2.6](../../readme.md#26-tests).
