# HU-11-FE-01 — i18next: bundles y conmutación de idioma

## Código
`HU-11-FE-01` — vinculado con **HU-11: El usuario utiliza la plataforma en su idioma (ES/EN)**.

## Título
Infraestructura i18next: bundles ES/EN, conmutación en caliente y persistencia

## Descripción
Configurar **i18next + react-i18next** en la SPA: bundles `es` y `en` con *namespaces* por superficie (`player`, `operator`, `math`, `shared`), selector de idioma con **conmutación en caliente** (sin recarga) y persistencia de la preferencia (store de sesión + envío del idioma del usuario). Todas las pantallas consumen claves de traducción, sin literales incrustados.

## Criterios de aceptación
- **AC1**: La interfaz se muestra por defecto en el idioma del usuario (`locale`).
- **AC2**: Cambiar el idioma actualiza los textos al instante, sin recargar la página.
- **AC3**: La preferencia persiste entre sesiones.
- **AC4**: No quedan literales sin traducir ni claves sin resolver en las tres superficies.
- **AC5**: La petición a la API incluye la cabecera `Accept-Language` acorde al idioma activo.

## Prioridad
Must Have

## Estimación
3 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `i18next`, `i18n`, `transversal`

## Comentarios
- Transversal a HU-1…HU-10 y HU-12: cada pantalla aporta sus claves.
- El recorte a "solo ES con arquitectura preparada" es una palanca documentada del PRD.
- **Dependencias directas:** `HU-1-DEV-01` (externa, esqueleto de la SPA). Ticket transversal: lo consumen las superficies de UI a través de la cadena de historias (HU-4 → resto).

## Enlaces y referencias
- Historia: [HU-11](../../stories/HU-11.md).
- Funcionalidad A9: [1.2](../../readme.md#12-características-y-funcionalidades-principales).
- Frontend: [2.2.2](../../readme.md#22-descripción-de-componentes-principales).
