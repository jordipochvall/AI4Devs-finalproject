# HU-4-QA-01 — Tests de autenticación y autorización

## Código
`HU-4-QA-01` — vinculado con **HU-4: Registro y autenticación de usuarios**.

## Título
Tests de autenticación y autorización

## Descripción
Suite de tests del flujo de auth, cubriendo casos felices y de borde:

- **Integration (Failsafe + Testcontainers)** sobre `register` y `login`: `201` + JWT en registro válido, `422` por menor de edad, `409` por email duplicado, `200` + JWT en login válido, `401` por credenciales inválidas.
- **Autorización**: matriz de roles × superficies — un JWT de cada rol solo accede a su grupo de rutas; el resto recibe `403`; sin token, `401`.
- **Unit**: validación de mayoría de edad en el caso de uso de registro (límite exacto de 18 años) y verificación de que el hash BCrypt no es reversible ni se expone.

## Criterios de aceptación
- **AC1**: Todos los códigos (`201/200/401/403/409/422`) están cubiertos por tests automáticos.
- **AC2**: La matriz de autorización por rol se verifica para las cuatro superficies (`/player`, `/operator`, `/math` y `/auth`).
- **AC3**: El test de edad cubre el límite exacto (justo 18 cumplidos pasa; un día menos falla).
- **AC4**: Ningún test ni log expone passwords o tokens.

## Prioridad
Must Have

## Estimación
2 SP

## Equipo responsable
QA

## Etiquetas
`qa`, `testcontainers`, `seguridad`, `auth`, `dgoj`

## Comentarios
- **Dependencias directas:** `HU-4-BE-01` (intra).

## Enlaces y referencias
- Historia: [HU-4](../../stories/HU-4.md).
- Estrategia de tests: [2.6](../../readme.md#26-tests).
- Seguridad: [2.5.1](../../readme.md#25-seguridad).
