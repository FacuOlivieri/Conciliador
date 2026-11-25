# Tareas 001 — Subida de archivos
Spec: `spec.md` (aprobada) · Plan: `plan.md` (aprobado)

## Reglas para todas las tareas
- Una tarea por vez: primero el test en rojo, después el código, `./mvnw test` en verde, marcar la tarea y parar (constitución, regla 4).
- Datos de prueba siempre ficticios y generados por código (RNF-6).
- Al terminar cada tarea, sumar una línea por clase nueva en la sección "Módulos" del `README.md`.

## Organización: 3 bloques
El desglose dio **22 tareas** (más de 10). Con tu autorización, en vez de partir la spec se agrupan en 3 bloques de hasta 10 tareas. Cada bloque termina con todos los tests en verde y deja algo verificable:

| Bloque | Tareas | Al terminar |
|---|---|---|
| A. Núcleo puro | T1–T7 | Modelo y helpers probados sin Spring |
| B. Lectura de archivos | T8–T12 | CSV y Excel se analizan con todas las reglas de rechazo |
| C. Carga, web e interfaz | T13–T22 | La aplicación funciona de punta a punta en el navegador |

Si preferís partir la spec (por ejemplo, 001 CSV + interfaz y 002 Excel), se puede hacer antes de empezar el bloque B.

---

## Bloque A — Núcleo puro

[x] **T1. Modelo base: enums, records y excepción.** RF-1, RF-4–RF-10, RF-20, RF-23, RNF-4
  - Crea `Lugar`, `FormatoArchivo`, `MensajeCarga` (M1–M8), `ArchivoCargado`, `Resumen`, `EstadoLugar` (sellada: `Vacio`, `Rechazado`, `PendienteDeHoja`, `Valido`) y `ArchivoRechazadoException`.
  - Hecho cuando: `MensajeCargaTest` comprueba que hay exactamente 8 mensajes y que cada texto es idéntico al catálogo de la spec; `LugarTest` comprueba las etiquetas "Propio" y "Contraparte".

[x] **T2. `ExtensionHelper`.** RF-3, RF-4
  - Hecho cuando: `ExtensionHelperTest` pasa con `.CSV`/`.XLSX`/`.XLS` aceptados, `datos.csv.xlsx` → `xlsx`, sin extensión y nombre terminado en punto → vacío, `.ods`/`.xlsm`/`.xlsb`/`.tsv` no permitidos.

[x] **T3. `ContenidoHelper` y `VistaPreviaHelper`.** Definiciones (celda/fila con datos), RF-18
  - Hecho cuando: `ContenidoHelperTest` pasa con celda de solo espacios → sin datos y fila con una celda con datos → con datos; `VistaPreviaHelperTest` pasa con 15 filas → 10, 25 columnas → 20 y celda de 60 caracteres → 50 caracteres terminados en "…", y con celdas cortas sin cambios.

[x] **T4. `AcumuladorFilas` y `HuellaHelper`.** RF-6, RF-7, RF-20
  - Hecho cuando: `AcumuladorFilasTest` pasa con filas vacías ignoradas, solo las primeras 10 guardadas, 100.000 filas aceptadas y la fila 100.001 → `ArchivoRechazadoException` con M4; `HuellaHelperTest` comprueba el SHA-256 conocido de un texto fijo y que dos arreglos iguales dan la misma huella.

[x] **T5. `CodificacionHelper`.** RF-8, RF-12
  - Hecho cuando: `CodificacionHelperTest` pasa con texto UTF-8 con acentos → UTF-8, el mismo texto en Windows-1252 → Windows-1252, bytes con 0x00 → no es texto, y BOM inicial eliminado.

[ ] **T6. `SeparadorHelper`.** RF-12
  - Hecho cuando: `SeparadorHelperTest` pasa con líneas separadas por `;`, `,` y tabulación, un `;` dentro de comillas que no cuenta, y empate → `;`.

[ ] **T7. Validaciones con Bean Validation y formularios.** RF-3, RF-4, RF-5, regla 5
  - Crea `@ExtensionPermitida`, `@TamanoMaximo` (con sus validadores), `SubidaArchivoForm` y `ElegirHojaForm`.
  - Hecho cuando: `ValidacionesArchivoTest` pasa con 10.485.760 bytes válido, 10.485.761 → M2, `.ods` → M1, `.csv` válido, y hoja en blanco inválida.

---

## Bloque B — Lectura de archivos

[ ] **T8. Fábrica de CSV de prueba y `LectorCsvService`.** RF-6, RF-7, RF-8, RF-12, RF-18
  - Hecho cuando: `LectorCsvServiceTest` pasa con: archivo de 0 bytes, solo BOM y solo `;;;` → M3; bytes binarios y un `.xlsx` renombrado a `.csv` → M5; comillas sin cerrar → M5; filas de distinto largo aceptadas; CSV válido → `Valido` con cantidad de filas y vista previa correctas.

[ ] **T9. Fábrica de Excel de prueba y apertura en `LectorExcelService`.** RF-6, RF-9, RF-10, RF-11
  - Hecho cuando: `LectorExcelServiceAperturaTest` pasa con: 0 bytes → M3; bytes aleatorios con extensión `.xlsx` → M5; `.xlsx` y `.xls` cifrados con contraseña → M6; `.xlsx` renombrado a `.xls` y `.xls` renombrado a `.xlsx` → se leen.

[ ] **T10. Hojas listables en `LectorExcelService`.** RF-6, RF-14, RF-15
  - Hecho cuando: `LectorExcelServiceHojasTest` pasa con: una hoja con datos → `Valido`; tres hojas con datos → `PendienteDeHoja` con sus tres nombres; hojas ocultas y vacías fuera de la lista; única hoja con datos oculta → M3.

[ ] **T11. Resumen de hoja y formato de celdas.** RF-7, RF-18 (decisiones D9 y D10)
  - Hecho cuando: `LectorExcelServiceResumenTest` pasa con: una fórmula muestra su resultado guardado y no la fórmula; `#REF!` se muestra tal cual; celda combinada con valor en la primera; filas ocultas contadas; número `1234,5` formateado en `es-AR`; hoja de más de 100.000 filas con datos → M4.

[ ] **T12. `AnalizadorArchivoService` y prueba de volumen.** RF-3, RF-21 (riesgo D8)
  - Hecho cuando: `AnalizadorArchivoServiceTest` pasa con `.csv` derivado al lector CSV y `.xls`/`.xlsx` al lector Excel; y un `.xlsx` generado con 100.000 filas se analiza dentro de `./mvnw test` sin `OutOfMemoryError` en menos de 10 s. Si falla, parar y revisar D8 antes de seguir.

---

## Bloque C — Carga, web e interfaz

[ ] **T13. `Carga` y `CargaEnMemoriaRepository`.** RF-2, RNF-1
  - Hecho cuando: `CargaEnMemoriaRepositoryTest` pasa con: guardar Propio no modifica Contraparte; cualquier guardado borra la confirmación; dos lecturas devuelven la misma carga.

[ ] **T14. `CargaService`: subir, rechazar y reemplazar.** RF-2, RF-13, RF-19
  - Hecho cuando: `CargaServiceSubidaTest` pasa con: rechazo en Contraparte → Propio válido se conserva; reemplazar un Excel ya elegido descarta la hoja; reemplazar un archivo válido por uno inválido deja el lugar en `Rechazado`.

[ ] **T15. `CargaService`: elegir hoja y confirmar.** RF-16, RF-17, RF-22, RF-23
  - Hecho cuando: `CargaServiceHojaYConfirmacionTest` pasa con: elegir hoja en `PendienteDeHoja` → `Valido` sin volver a subir; cambiar a otra hoja actualiza el resumen; hoja inexistente se ignora; confirmar con un lugar no válido no confirma; confirmar con ambos válidos → confirmada.

[ ] **T16. `PantallaCargaDto` y `PantallaCargaMapper`.** RF-18, RF-20, RF-21, RF-22
  - Hecho cuando: `PantallaCargaMapperTest` pasa con: Confirmar deshabilitado y texto de qué falta si un lugar no es válido; M7 cuando las huellas coinciden, aunque los nombres difieran; CSV + Excel ambos válidos → Confirmar habilitado; M8 solo si la carga está confirmada.

[ ] **T17. Configuración de multipart.** RF-5, RNF-1
  - Hecho cuando: `ConfiguracionMultipartTest` (contexto de Spring) comprueba `max-file-size` = 30 MB y `file-size-threshold` mayor que `max-file-size`.

[ ] **T18. `CargaController`: página y subida, con plantilla mínima.** RF-1, RF-4, RF-5, RF-13, RF-19, RNF-2, RNF-4
  - Plantilla funcional sin diseño, solo con estructura y textos.
  - Hecho cuando: `CargaControllerSubidaTest` (MockMvc) pasa con: `GET /` muestra las tarjetas Propio y Contraparte; `POST /carga/PROPIO` redirige a `/`; `.ods` → M1 dentro de la tarjeta de Propio; archivo de 10.485.761 bytes → M2; después de subir, un `GET /` nuevo sigue mostrando el archivo.

[ ] **T19. `CargaController`: hoja, confirmar y archivos muy grandes.** RF-5, RF-13, RF-15, RF-17, RF-22, RF-23
  - Crea `ManejadorGlobalExcepciones`.
  - Hecho cuando: `CargaControllerHojaYConfirmacionTest` pasa con: Excel con varias hojas → se muestran las opciones sin preselección; `POST /carga/CONTRAPARTE/hoja` → resumen visible; botón Confirmar con `disabled` hasta tener dos válidos; tras confirmar aparece M8; un archivo de más de 30 MB → M2 en la tarjeta correcta.

[ ] **T20. Nada sensible en los logs.** RNF-3
  - Hecho cuando: `LogsSinDatosTest` (con `OutputCaptureExtension`) sube un archivo con nombre y contenido marcados y comprueba que ninguno de los dos aparece en la salida, tanto en una subida válida como en una rechazada.

[ ] **T21. Diseño visual con `/impeccable`.** RF-18 (presentación), RNF-5
  - Tarea visual: se verifica en el navegador, no con test (constitución, regla 4).
  - Hecho cuando: en el navegador se ven las dos tarjetas, los 4 estados, la tabla de vista previa con recortes, M7 y M8; la página funciona con la red desactivada (sin CDN ni fuentes externas); los tests de T18–T19 siguen en verde.

[ ] **T22. Cierre: README y verificación completa.**
  - Hecho cuando: la sección "Módulos" del `README.md` tiene una línea por clase creada; `./mvnw test` pasa completo; cada RF de la matriz del plan tiene su test o verificación marcada. Después de esto la spec pasa a validación (`/sdd-validate`).
