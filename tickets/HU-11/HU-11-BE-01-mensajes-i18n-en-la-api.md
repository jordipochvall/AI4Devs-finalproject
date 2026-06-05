# HU-11-BE-01 — Mensajes i18n en la API

## Código
`HU-11-BE-01` — vinculado con **HU-11: El usuario utiliza la plataforma en su idioma (ES/EN)**.

## Título
Mensajes de la API internacionalizados (ES/EN) según `Accept-Language`

## Descripción
Configurar `MessageSource` en Spring (bundles `messages_es` / `messages_en`) y un `LocaleResolver` basado en la cabecera `Accept-Language`. Los mensajes de error RFC 9457 (*Problem Details*) y de validación se devuelven en el idioma solicitado. Por defecto, español.

## Criterios de aceptación
- **AC1**: Una petición con `Accept-Language: en` que produce un error de validación devuelve el `detail`/`errors` en inglés.
- **AC2**: Una petición con `Accept-Language: es` (o sin cabecera) devuelve los mensajes en español.
- **AC3**: Todas las claves de error usadas por los controllers existen en ambos bundles (sin claves sin traducir).
- **AC4**: El idioma no altera los códigos HTTP ni la estructura del *Problem Details*.

## Prioridad
Must Have

## Estimación
2 SP

## Equipo responsable
Backend

## Etiquetas
`backend`, `nova-web-api`, `i18n`, `rfc9457`

## Comentarios
- Complementa la parte de cliente (`HU-11-FE-01`): el frontend envía `Accept-Language`.
- **Dependencias directas:** `HU-1-DEV-01` (externa, esqueleto del backend/web-api donde viven el `MessageSource` y el manejo de errores RFC 9457).

## Enlaces y referencias
- Historia: [HU-11](../../stories/HU-11.md).
- Principios de la API (errores i18n): [4.1](../../readme.md#41-principios-de-diseño-y-convenciones).
