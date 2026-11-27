# Tareas 003 — Tipos de comprobante
Spec: `spec.md` (aprobada) · Plan: `plan.md` (aprobado)

## Reglas para todas las tareas
- Una tarea por vez: primero el test en rojo, después el código, `./mvnw test` en verde, marcar la tarea y parar (constitución, regla 4).
- Los valores de prueba son tipos de comprobante inventados o del anexo A, nunca datos financieros reales.
- Al terminar cada tarea, sumar una línea por cada clase nueva en la sección "Módulos" del `README.md`.
- Al terminar cada tarea, sugerir un commit con el formato `feat: spec S3-Tx, <nombre descriptivo>` (regla de `CLAUDE.md`).
- Fuera de estas tareas: proteger el `.tsv` de ataques externos (plan §9, pendiente a futuro).

## Organización
El desglose dio **8 tareas**, dentro del máximo de 10, así que no hace falta dividir la spec. El orden sigue el §8 del plan: helpers puros, después el catálogo y al final el servicio.

---

[x] **T1. `NormalizacionHelper`.** RF-3, RF-5, RF-10
  - Dos funciones puras: `normalizar` y `recortar` (plan §2).
  - Hecho cuando: `NormalizacionHelperTest` pasa con estos casos:
    - `"Fact. A"`, `"FACT  A"`, `"fact-a"`, `"Fáct A"` y `"Fact.A"`, y también con tabulación y con espacio duro, dan `"fact a"`.
    - `"Caña"` da `"caña"`, `"N/C A"` da `"n c a"`, y la raya `–` separa como un espacio.
    - `"..."` y nulo dan `""`.
    - `"FC E"` y `"FCE"` dan formas distintas.
    - `recortar` quita el espacio duro de los extremos y no toca los del medio.

[x] **T2. `TipoComprobante`, `CatalogoHelper.deLinea` y `CatalogoHelper.validar`.** RF-1, RF-2
  - El record del modelo, la lectura de una línea y las reglas de RF-2. Todavía no se arman los índices.
  - Hecho cuando: `CatalogoHelperTest` pasa con estos casos:
    - `deLinea` convierte una línea con abreviaturas y otra sin ellas (lista vacía).
    - `validar` encuentra, cada uno por separado: código repetido, nombre sin letras, abreviatura sin letras, abreviatura repetida en otro tipo, y abreviatura igual al nombre oficial de otro tipo.
    - `validar` no da errores con un catálogo válido que tiene dos nombres oficiales repetidos.

[x] **T3. `CatalogoTiposComprobante` y `CatalogoHelper.armar`.** RF-2, RF-5, RF-9
  - Arma los dos índices: por código sin ceros y por forma normalizada.
  - Hecho cuando: `CatalogoHelperTest` suma estos casos:
    - `armar` lanza `IllegalArgumentException` con un catálogo inválido.
    - Con uno válido, `buscarCodigo("1")` devuelve el tipo 1.
    - `buscarForma("fc a")` devuelve una lista de 1 elemento.
    - Un nombre oficial repetido devuelve una lista de 2 tipos.
    - Una forma inexistente devuelve una lista vacía.

[ ] **T4. Catálogo real: `tipos-comprobante.tsv` y `CatalogoTipoComprobanteArchivoRepository`.** RF-1, RF-2, RNF-1, RNF-2
  - El `.tsv` se genera desde la tabla del anexo A (121 líneas, UTF-8). El repositorio lo lee al arrancar (plan §3.1).
  - Hecho cuando: `CatalogoTipoComprobanteArchivoRepositoryTest` pasa con estos casos:
    - El catálogo tiene 121 tipos.
    - Coincide fila por fila con el anexo A leído de `specs/003-tipos-comprobante/spec.md`: código, nombre exacto con tildes y comillas “T”, y abreviaturas.
    - Un `Reader` con una abreviatura repetida lanza `IllegalStateException`.

    Además, `contextLoads` sigue en verde.

[ ] **T5. `ResultadoReconocimiento` y `TipoComprobanteService`: códigos y largo.** RF-3, RF-6, RF-10, RF-11
  - Primera parte de `reconocer` (plan §3.2): nulo, más de 100 caracteres y solo dígitos.
  - Hecho cuando: `TipoComprobanteServiceTest` pasa con estos casos:
    - `"001"`, `"1"`, `" 1 "` y `"000000000000000000001"` dan `Reconocido(001)`.
    - `"0"`, `"00"` y `"14"` (un código que no está en la tabla) dan no reconocido.
    - Un valor de 101 caracteres da no reconocido.
    - Nulo da no reconocido con valor original `""`.
    - En todos los no reconocidos, el valor original vuelve sin cambios.

[ ] **T6. Servicio: código con texto.** RF-4, RF-7
  - La rama `código - texto` de `reconocer`.
  - Hecho cuando: el test suma estos casos:
    - `"1 - Factura A"`, `"001-FC A"` y `"1 – FACTURAS A"` (con raya) dan `Reconocido(001)`.
    - `"1 - Factura B"`, `"999 - Factura A"` y `"1-0"` dan no reconocido.
    - `"1 -"` da no reconocido.

[ ] **T7. Servicio: nombres, abreviaturas y casos límite restantes.** RF-5, RF-8, RF-9, RF-10
  - La última rama de `reconocer` y el resto de los casos límite de la spec.
  - Hecho cuando: el test suma estos casos:
    - `"Fact. A"`, `"FACT A"`, `"fact-a"`, `"Fáct A"`, `"Fact.A"`, `"N/C A"` y `"FACTURAS A"` se reconocen.
    - `"FC E"` da 019, y `"FCE"` da no reconocido.
    - `"NC"`, `"Factura"`, `"+1"`, `"-1"`, `"1.0"`, `"1,5"`, `"1,00"`, `"1A"` y `"FC A 0001-00001234"` dan no reconocido.
    - `"-"` y `"..."` dan no reconocido.
    - Un código no habitual en ventas, `"033"`, se reconoce.
    - Con un catálogo inventado de 2 tipos con el mismo nombre (pasado como lambda), ese nombre da no reconocido (RF-9), pero cada código se reconoce.

[ ] **T8. RNF-3 y cierre.** RNF-3
  - Antes de escribir el test, confirmar que `OutputCaptureExtension` existe en Spring Boot 4.2 (con Context7).
  - Hecho cuando:
    - Un test con captura de salida reconoce `"VALOR-SECRETO-123"` y comprueba que ese texto no aparece en la salida.
    - `grep` no encuentra loggers en el servicio, el repositorio ni los helpers nuevos.
    - El `README.md` tiene una línea por cada clase nueva.
    - `./mvnw test` corre la suite completa en verde.
    - La spec pasa a "implementada" después de `/sdd-validate`.
