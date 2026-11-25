# MEMORY

## Estado actual (2026-11-25)
- Spec activa: `Coinciliador/specs/001-subida-archivos-csv/` (spec y plan aprobados).
- Tareas: 19 de 22 hechas (T1–T19). Faltan T20 (logs), T21 (diseño) y T22 (cierre).
- `./mvnw test` en verde (186 tests) al cerrar T19, ya commiteada.
- Siguiente paso: `/sdd-implement 001-subida-archivos-csv T20` (nada sensible en los logs). `CLAUDE.md` pide commitear cada tarea al terminarla.

## Decisiones
- `CargaController` lee la carga con `ICargaService.cargaActual()` (no toca el repositorio) y la plantilla usa `fragmentos/tarjeta-lugar.html`. En `main` todavía no existe `GET /` (lo trae la 002): hasta mergearla, `/carga` redirige a una página 404 y la pantalla no se puede ver en el navegador. Sin Spring Security no hay protección CSRF.
- Multipart: 30 MB por archivo y petición, umbral de disco 31 MB (D4). Además `server.tomcat.max-swallow-size=-1`: sin eso Tomcat corta la conexión con un archivo de más de 30 MB. `ArchivoMuyGrandeTest` lo prueba con un socket que envía todo antes de leer, como un navegador (el `HttpClient` de Java no detectaba el corte; MockMvc tampoco).
- `PantallaCargaMapper` arma un `PantallaCargaDto` con dos `LugarVistaDto` (estado como enum `Estado`: VACIO, RECHAZADO, PENDIENTE_DE_HOJA, VALIDO). Todos los textos al usuario salen de `MensajeCarga`, incluidos los fragmentos de "qué falta" (M9–M11, en el catálogo de la spec): el mapper solo los completa, los une y pone la mayúscula (el usuario pidió tenerlos en un solo lugar). M7 usa la huella de cualquier lugar con archivo, incluso pendiente de hoja.
- `elegirHoja` relee los bytes guardados con `ILectorExcelService.resumirHoja`; si la hoja supera 100.000 filas el lugar queda `Rechazado(M4)` (RF-7). `confirmar` usa `Carga.ambosValidos()`. `ILectorExcelService` ahora tiene dos métodos: en tests ya no se puede simular con una lambda (usar una subclase anónima de `LectorExcelService`).
- `CargaService.subir` captura `ArchivoRechazadoException` y guarda `Rechazado(nombre, mensaje)`; `registrarRechazo` es para M1 y M2, que detecta Bean Validation antes de leer el archivo. `ICargaService` se completa en T15 con elegir hoja y confirmar.
- `Carga` es un record inmutable (cada cambio devuelve una nueva); `CargaEnMemoriaRepository` guarda la referencia con métodos `synchronized`. `guardar` ya borra la confirmación, por eso no hay un `desconfirmar()` aparte (el pseudocódigo del plan lo menciona, pero sobra). `tipoActivo()` devuelve `Optional<TipoConciliacion>` (vacío hasta elegir tipo), igual que `TipoConciliacion.deSlug`.
- `LectorExcelService` arma el `Resumen` con `DataFormatter(es-AR)` (columnas alineadas desde A). De una fórmula se muestra el resultado guardado (`getCachedFormulaResultType`), porque `DataFormatter` sin evaluador devuelve el texto de la fórmula. Hoja de gráfico: queda afuera porque no tiene filas, pero no hay test (POI 5.5.1 no permite crear una).
- `LectorExcelService.abrir(byte[])` es de paquete y devuelve un `Workbook` POI que quien lo llama debe cerrar; la interfaz `ILectorExcelService` expone `leer` y `resumirHoja`, sin tipos de POI. M6 sale de `EncryptedDocumentException`; cualquier otra falla de POI al abrir es M5.
- Los lectores lanzan `ArchivoRechazadoException` y no devuelven el rechazo: `CargaService` (T14) lo atrapa y arma el estado `Rechazado`. `LectorCsvService.leer` devuelve `EstadoLugar.Valido`. No se propaga la causa de la excepción de Commons CSV (puede traer datos del archivo, RNF-3).
- `FabricaArchivosPrueba` (en `src/test/.../helper/`) tiene `csv(...)`, `filasFicticias(n)`, `xlsx(armado)`, `xls(armado)`, `hojaConDatos`, `hojaVacia`, `xlsxMinimo()`, `xlsMinimo()` y los cifrados; T11 le suma fórmulas, combinadas y filas ocultas.
- Las restricciones `@ExtensionPermitida` y `@TamanoMaximo` llevan como mensaje el código `"M1"`/`"M2"` (una anotación no admite el texto del enum); el controlador lo traduce con `MensajeCarga.valueOf`. Sin archivo (`null`) la extensión falla con M1.
- `SeparadorHelper`: el puntaje de un separador es cuántas líneas repiten su misma cuenta (>0); no se maneja un campo entrecomillado que cruce líneas (el lector CSV de T8 parsea con Commons CSV igual).
- `AcumuladorFilas` es la única clase con estado del paquete helper (una instancia por lectura); las filas vacías no cuentan para el límite de 100.000 y la vista previa guarda las 10 primeras filas con datos.
- 22 tareas en 3 bloques (A núcleo, B lectura de archivos, C web e interfaz) en vez de partir la spec.
- Datos de prueba siempre ficticios y generados por código (RNF-6).
- El recorte de celdas usa "…" (un solo carácter, U+2026), no tres puntos: así la celda mide exactamente 50.

## Errores a evitar
- `ArchivoCargado` lleva un `byte[]`: dos records con arreglos iguales no son `equals`. En los tests guardar y comparar la misma instancia, no crear otra con el mismo contenido.
- D8 revisado en T12: un xlsx de 100.000 filas x 3 columnas (2 MB) tardó de 6 a más de 10 s con la API normal de POI (abrir el libro solo ~5 s; sin problemas de memoria). Decisión del usuario: mantener la API normal y subir el límite del test a 30 s. Un xlsx de 10 MB (el máximo) puede tardar bastante más: si molesta en el uso real, pasar a lectura en streaming. Crear el libro en el test tarda 16-21 s.
- `DataFormatter.formatCellValue` sin `FormulaEvaluator` devuelve el texto de la fórmula, no su resultado: para fórmulas leer el valor guardado.
- `String` es inmutable: `substring` devuelve un texto nuevo, hay que usar el `return`.
- Revisar que el test pase con `./mvnw test` antes de marcar una tarea `[x]`.

## Spec 002 (sesión "Front Worktree")
- Sus ajustes a la 001 ya están aplicados y `TipoConciliacion` está en main. Se le avisó que T13 y T14 terminaron; `tipoActivo()` devuelve `Optional`. La rama temporal `tipo-conciliacion-para-001` se puede borrar.

## Pendientes a futuro (no implementar sin spec y aprobación)
- **Spring Security** (pedido del usuario, pensando en escalar): hoy no hay protección CSRF ni login, y cualquiera que llegue al puerto ve la carga. Es una dependencia nueva: requiere aprobación (constitución, regla 1), spec propia y su tutorial en `docs/librerias/`. Mientras tanto la app escucha en todas las interfaces de red; fijar `server.address=127.0.0.1` la limitaría a la propia PC.
