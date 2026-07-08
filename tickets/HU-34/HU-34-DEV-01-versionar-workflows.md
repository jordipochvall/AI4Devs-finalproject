# HU-34-DEV-01 — Comprometer `.github/workflows` al repositorio

## Código
`HU-34-DEV-01` — vinculado con **HU-34: Versionar y activar el pipeline de CI/CD para poder desplegar al VPS**.

## Título
`git add`/commit de `.github/workflows/ci.yml` y `cd.yml`, verificar que corren

## Descripción
`.github/` existe en disco (`ci.yml`, `cd.yml`) pero está sin *trackear* (`git status` lo marca `??`), por lo que GitHub Actions no lo ejecuta. Comprometer el directorio completo `.github/` al repositorio en la rama correspondiente y hacer un *push*/PR de prueba para confirmar que el *workflow* `CI` se dispara y termina en verde (build+test backend, frontend, JaCoCo, *perf job*).

## Criterios de aceptación
- **AC1**: `.github/workflows/ci.yml` y `.github/workflows/cd.yml` quedan comprometidos y presentes en el remoto.
- **AC2**: Un *push*/PR de prueba dispara el *workflow* `CI` y termina en verde.
- **AC3**: No se modifica el contenido de los *workflows* (sólo se versionan; el diseño ya viene de HU-24).

## Prioridad
Should Have

## Estimación
1 SP

## Equipo responsable
DevOps

## Etiquetas
`devops`, `ci-cd`, `git`

## Comentarios
- **Dependencias directas:** ninguna (es sólo `git add`/commit); previa a `HU-34-DEV-02`.

## Enlaces y referencias
- Historia: [HU-34](../../stories/HU-34.md).
- Índice de tickets del bloque 4: [tickets-4.md](../tickets-4.md).
