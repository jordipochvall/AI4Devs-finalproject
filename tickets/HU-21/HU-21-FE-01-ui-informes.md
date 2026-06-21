# HU-21-FE-01 — UI de generación y descarga de informes RFJ

## Código
`HU-21-FE-01` — vinculado con **HU-21: Generación de informes regulatorios DGOJ (RFJ)**.

## Título
Pantalla de informes regulatorios en el backoffice del operador

## Descripción
Añadir a la superficie del operador una vista para seleccionar un periodo, generar el informe RFJ (`POST /operator/reports/rfj`) y descargarlo. Si la generación se bloquea por integridad rota, muestra el aviso correspondiente.

## Criterios de aceptación
- **AC1**: El operador selecciona un periodo y genera/descarga el informe.
- **AC2**: Si la generación se bloquea por integridad, se muestra el motivo sin romper la UI.
- **AC3**: La UI funciona en **español e inglés** y solo es accesible con rol `OPERATOR`.

## Prioridad
Could Have

## Estimación
2 SP

## Equipo responsable
Frontend

## Etiquetas
`frontend`, `react`, `dgoj`, `informes`, `i18n`

## Comentarios
- **Dependencias directas:** `HU-21-BE-01`.

## Enlaces y referencias
- Historia: [HU-21](../../stories/HU-21.md).
- Índice de tickets post-MVP: [tickets-2.md](../tickets-2.md).
- Diseño y UX: [§1.3](../../readme.md#13-diseño-y-experiencia-de-usuario).
