# HU-3-FE-01 — Backoffice operador: auditoría

## Código
`HU-3-FE-01` — vinculado con **HU-3: El operador resuelve una reclamación con el replay**.

## Título
Backoffice operador: pantalla de auditoría con filtros y tabla paginada

## Descripción
Implementar la superficie del operador en la SPA (`frontend/src/operator/`), accesible en `/operator`. Incluye:

- **Selector de jugador** (autocompletar por email, consume `GET /operator/players`).
- **Filtros**: juego (combo), rango de fechas (`from`/`to`).
- **Tabla paginada** de rounds con columnas: `Round`, `Jugador`, `Juego`, `Apuesta`, `Premio`, `Fecha` y un botón **Replay** por fila.
- **Paginación** con `page`/`size` y controles de "anterior / siguiente / N de M" (envoltorio estándar de 4.1).
- Al pulsar **Replay**, navega a `/operator/replay/{roundId}` (la pantalla de replay vive en `HU-3-FE-02`).

## Criterios de aceptación
- **AC1**: Solo usuarios con rol `OPERATOR` pueden acceder a `/operator` (redirección a `/login` o `403`).
- **AC2**: Buscar un jugador por email muestra resultados con un máximo de 20 sugerencias.
- **AC3**: Los filtros se reflejan en query params de la URL (compartible y *back*-friendly).
- **AC4**: La tabla muestra las cantidades formateadas según `locale` (separador de miles, dos decimales, divisa).
- **AC5**: La paginación funciona y los controles deshabilitan correctamente "anterior" en la primera página y "siguiente" en la última.
- **AC6**: Coincide visualmente con el wireframe ASCII del apartado 1.3 (cabecera, filtros, tabla con botón Replay por fila).
- **AC7**: La UI funciona en **español e inglés**.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `tanstack-query`, `auditoria`, `paginacion`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-3-BE-01` (intra).

## Enlaces y referencias
- Historia: [HU-3](../../readme.md#5-historias-de-usuario).
- Funcionalidades del operador: [1.2 Características](../../readme.md#12-características-y-funcionalidades-principales), bloque B.
- Wireframe: [1.3 Diseño y UX](../../readme.md#13-diseño-y-experiencia-de-usuario), bloque "Backoffice operador".
- API: [4.2 Catálogo](../../readme.md#42-catálogo-de-endpoints), endpoints `/operator/players` y `/operator/rounds`.
