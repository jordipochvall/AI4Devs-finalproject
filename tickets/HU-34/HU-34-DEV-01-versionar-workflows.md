# HU-34-DEV-01 — Comprometer `.github/workflows` al repositorio

## Código
`HU-34-DEV-01` — vinculado con **HU-34: Versionar y activar el pipeline de CI/CD para poder desplegar al VPS**.

## Título
`git add`/commit de `.github/workflows/ci.yml` y `cd.yml`, verificar que corren

## Descripción
`.github/` existía en disco (`ci.yml`, `cd.yml`) pero sin *trackear* (`git status` lo marcaba `??`), por lo que GitHub Actions no lo ejecutaba. Comprometidos `ci.yml`/`cd.yml` sin cambios de contenido (commit `chore(ci): HU-34-DEV-01`). **Nota:** `.github/` también contenía `.github/modernize/java-upgrade/` (artefactos de una extensión de VS Code para modernización de Java, con rutas/IDs de sesión locales) — se ha dejado **fuera** del commit a propósito, no es código del proyecto.

## Criterios de aceptación
- **AC1**: `.github/workflows/ci.yml` y `.github/workflows/cd.yml` quedan comprometidos. ✅ (commit local; **pendiente el `push`**, no solicitado todavía.)
- **AC2**: Un *push*/PR de prueba dispara el *workflow* `CI` y termina en verde. ⏳ Pendiente de `push` a `origin`.
- **AC3**: No se modifica el contenido de los *workflows* (sólo se versionan; el diseño ya viene de HU-24). ✅

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
