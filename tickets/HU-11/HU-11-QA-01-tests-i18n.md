# HU-11-QA-01 — Tests de internacionalización

## Código
`HU-11-QA-01` — vinculado con **HU-11: El usuario utiliza la plataforma en su idioma (ES/EN)**.

## Título
Tests de i18n (UI y API)

## Descripción
- **E2E/UI (Playwright)**: conmutar el idioma actualiza los textos sin recargar; la preferencia persiste tras recargar; recorrido por las tres superficies en inglés sin literales en español ni claves sin resolver.
- **Integration (API)**: una operación de error con `Accept-Language: en` devuelve el mensaje en inglés y con `es` en español.
- **Test de cobertura de claves**: verificar que toda clave usada existe en ambos bundles (`es` y `en`), tanto en frontend como en backend.

## Criterios de aceptación
- **AC1**: Verificada la conmutación en caliente y la persistencia.
- **AC2**: Verificados los mensajes de error de la API en ambos idiomas.
- **AC3**: El test de cobertura de claves falla si falta alguna traducción en cualquiera de los dos idiomas.

## Prioridad
Should Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `playwright`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-11-FE-01`, `HU-11-BE-01` (intra).

## Enlaces y referencias
- Historia: [HU-11](../../stories/HU-11.md).
- Estrategia de tests: [2.6](../../readme.md#26-tests).
