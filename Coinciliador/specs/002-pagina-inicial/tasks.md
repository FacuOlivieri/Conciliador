# Tareas 002 — Página inicial
Spec: `spec.md` (aprobada) · Plan: `plan.md` (aprobado)

## Reglas para todas las tareas
- Una tarea por vez: primero el test en rojo, después el código, `./mvnw test` en verde, marcar la tarea y parar (constitución, regla 4). Las tareas solo visuales se verifican en el navegador.
- Datos de prueba siempre ficticios y generados por código.
- Al terminar cada tarea, sumar una línea por clase nueva en la sección "Módulos" del `README.md`.
- El commit se hace al terminar la implementación de la spec, con el formato `feat: spec S2-Tx, <nombre descriptivo>` (regla de `CLAUDE.md`).
- No se editan `tasks.md`, `plan.md` ni `MEMORY.md` de la spec 001: los ajustes que esta spec le pide los aplica la sesión de la 001 con `/sdd-change` (ver plan §8).

## Organización: 2 bloques
El desglose dio **10 tareas**, dentro del máximo, así que no hace falta dividir la spec.

| Bloque | Tareas | Depende de la spec 001 | Al terminar |
|---|---|---|---|
| A. Núcleo y página | T1–T5 | No | Tipos, reglas puras y página inicial visibles, sin acciones reales |
| B. Elección e integración | T6–T10 | Sí: T13, T14 y T18 de la 001 y los 4 ajustes del plan §8 aplicados | La página inicial funciona de punta a punta con la carga |

---

## Bloque A — Núcleo y página

[x] **T1. `TipoConciliacion`, `MensajeInicio` y `ResultadoEleccion`.** RF-2, RF-3, RF-11, RNF-3
  - Crea los tres enums en `enums/`. Es lo primero porque la sesión de la 001 necesita `TipoConciliacion` en T13.
  - Hecho cuando: `TipoConciliacionTest` pasa con exactamente 4 tipos en el orden IVA, Ventas, Compras, Ingresos Brutos, con nombre y frase de ayuda idénticos al catálogo de la spec, y `deSlug` con `iva`, `IVA` y `ingresos-brutos` → el tipo, y con nulo, en blanco y `monotributo` → vacío; `MensajeInicioTest` comprueba que hay exactamente 2 mensajes (P1 y D1) con el texto de la spec.

[x] **T2. `CargaEnCursoHelper` y `EleccionTipoHelper`.** RF-4, RF-6, RF-7
  - Dos funciones puras, sin Spring.
  - Hecho cuando: `CargaEnCursoHelperTest` pasa con vacío+vacío → no, válido+vacío → sí, solo pendiente de hoja → sí, rechazado+vacío → no, rechazado+válido → sí y confirmada → sí; `EleccionTipoHelperTest` pasa con sin tipo activo → abrir, mismo tipo con carga en curso → abrir, otro tipo sin carga en curso → abrir y otro tipo con carga en curso → pedir confirmación.

[x] **T3. Validación del tipo: `@TipoConciliacionValido` y `EleccionTipoForm`.** RF-11, regla 5
  - Crea la restricción, su validador y el formulario que recibe el *slug*.
  - Hecho cuando: `TipoConciliacionValidoValidatorTest` pasa con `iva` y `IVA` válidos, y con `monotributo`, en blanco y nulo inválidos con el mensaje P1.

[x] **T4. DTOs e `InicioMapper`.** RF-2, RF-3, RF-7
  - Crea `InicioDto`, `TipoVistaDto`, `ConfirmacionCambioDto` e `InicioMapper`.
  - Hecho cuando: `InicioMapperTest` pasa con 4 tarjetas en orden, cada una con slug, nombre y ayuda correctos; mensaje P1 presente solo si se lo pide; y `ConfirmacionCambioDto` con el nombre del tipo activo, el del elegido y el texto D1.

[x] **T5. `InicioController` (solo `GET /`) y plantilla mínima.** RF-1, RF-2, RF-3, RNF-1
  - Plantilla funcional sin diseño: título, cuatro tarjetas como formularios con botón, y el lugar para P1. CSS mínimo local. Las acciones de las tarjetas todavía no existen.
  - Hecho cuando: `InicioControllerPaginaTest` (MockMvc) pasa con `GET /` → 200, las 4 tarjetas en orden con su frase de ayuda, cada una dentro de un `<form method="post">` con `<button>`, y ningún `href` ni `src` que empiece con `http://` o `https://`.

---

## Bloque B — Elección e integración con la carga

Dependencias: T13 (`Carga` con `tipo` y `reiniciar`), T14 y T18 (`CargaController`) de la spec 001, con los ajustes del plan §8 ya aplicados por la otra sesión (ruta `/carga`, `tituloTipo`, enlace de vuelta y redirección sin tipo). Si esos cambios no están, parar y revisar con esa sesión antes de seguir.

[ ] **T6. `ConciliacionService`.** RF-4, RF-6, RF-7, RF-8, RF-9
  - Crea `IConciliacionService` y `ConciliacionService` con `elegir`, `descartarYAbrir` y `tipoActivo`, usando el repositorio real en memoria.
  - Hecho cuando: `ConciliacionServiceTest` pasa con: elegir sin carga en curso fija el tipo y devuelve abrir; otro tipo con un archivo válido cargado devuelve pedir confirmación y deja la carga y el tipo intactos; el mismo tipo con carga en curso la deja intacta; `descartarYAbrir` deja ambos lugares vacíos, sin confirmación y con el tipo nuevo.

[ ] **T7. `InicioController`: elegir, confirmar y descartar.** RF-4, RF-6, RF-7, RF-8, RF-9, RF-11, RNF-1
  - Agrega `POST /conciliacion/{slug}`, `GET /conciliacion/{slug}/confirmar` y `POST /conciliacion/{slug}/descartar`, la plantilla `confirmar-cambio.html` y el mensaje P1 por redirección con atributo *flash*.
  - Hecho cuando: `InicioControllerEleccionTest` (MockMvc) pasa con: `POST /conciliacion/iva` sin carga → redirige a `/carga`; slug inexistente → redirige a `/`, muestra P1 y no toca la carga; con un archivo válido cargado, otro tipo → redirige a la confirmación, que muestra D1 y un enlace "Cancelar" con `href="/"`; `POST …/descartar` → redirige a `/carga` con ambos lugares vacíos; abrir la confirmación sin que haga falta → redirige a `/carga` o a `/` y no muestra D1; las páginas nuevas no tienen recursos externos.

[ ] **T8. Integración con la pantalla de carga.** RF-5, RF-6, RF-10
  - Solo pruebas sobre lo que aplicó la sesión de la 001. Si algo falla, no se corrige acá: se avisa a esa sesión.
  - Hecho cuando: `CargaControllerTipoTest` (MockMvc) pasa con: `GET /carga` con tipo activo muestra el nombre del tipo como título y un enlace `href="/"`; `GET /carga` sin tipo activo → redirige a `/` sin mensaje; subir un archivo, volver al inicio y elegir el mismo tipo → la carga conserva el archivo.

[ ] **T9. Diseño visual con `/impeccable`.** RF-3 (presentación), RNF-1, RNF-2
  - Tarea visual: se verifica en el navegador, no con test (constitución, regla 4). Empieza con `/impeccable init`; conviene coordinarlo con la sesión de la 001 para que su T21 use el mismo contexto de diseño.
  - Hecho cuando: en el navegador se ven las cuatro tarjetas y la pantalla de confirmación con el aviso D1, el mensaje P1 y el título del tipo en la carga; se recorre todo el flujo con Tab y Enter sin usar el ratón; la página funciona con la red desactivada, sin CDN ni fuentes externas; los tests de T5, T7 y T8 siguen en verde.

[ ] **T10. Cierre: README y verificación completa.**
  - Hecho cuando: la sección "Módulos" del `README.md` tiene una línea por clase creada; `./mvnw test` pasa completo; cada RF y RNF de la matriz del plan tiene su test o su verificación marcada. Después de esto la spec pasa a validación (`/sdd-validate`) y se hace el commit.

## Cobertura de RF y RNF
| RF / RNF | Tareas |
|---|---|
| RF-1 | T5 |
| RF-2, RF-3 | T1, T4, T5, T9 |
| RF-4 | T2, T6, T7 |
| RF-5 | T8 |
| RF-6 | T2, T6, T7, T8 |
| RF-7 | T2, T4, T6, T7 |
| RF-8 | T6, T7 |
| RF-9 | T6, T7 |
| RF-10 | T8 |
| RF-11 | T1, T3, T7 |
| RNF-1 | T5, T7, T9 |
| RNF-2 | T9 |
| RNF-3 | T1 |
