# MEMORY

## Estado actual (2026-11-25)
- Spec activa: `Coinciliador/specs/001-subida-archivos-csv/` (spec y plan aprobados).
- Tareas: 13 de 22 hechas (T1–T13). Bloque C (carga, web e interfaz) va por T14.
- `./mvnw test` en verde (132 tests) al cerrar T13. T13 todavía sin commit.
- Siguiente paso: commit de T13 y luego `/sdd-implement 001-subida-archivos-csv T14` (`CargaService`: subir, rechazar y reemplazar). Al terminar T14, avisar a la sesión "Front Worktree" (retoma su T6) y decirle que `tipoActivo()` devuelve `Optional<TipoConciliacion>`.

## Decisiones
- `Carga` es un record inmutable (cada cambio devuelve una nueva); `CargaEnMemoriaRepository` guarda la referencia con métodos `synchronized`. `guardar` ya borra la confirmación, por eso no hay un `desconfirmar()` aparte (el pseudocódigo del plan lo menciona, pero sobra). `tipoActivo()` devuelve `Optional<TipoConciliacion>` (vacío hasta elegir tipo), igual que `TipoConciliacion.deSlug`.
- `LectorExcelService` arma el `Resumen` con `DataFormatter(es-AR)` (columnas alineadas desde A). De una fórmula se muestra el resultado guardado (`getCachedFormulaResultType`), porque `DataFormatter` sin evaluador devuelve el texto de la fórmula. Hoja de gráfico: queda afuera porque no tiene filas, pero no hay test (POI 5.5.1 no permite crear una).
- `LectorExcelService.abrir(byte[])` es de paquete y devuelve un `Workbook` POI que quien lo llama debe cerrar; la interfaz `ILectorExcelService` solo expone `leer`, sin tipos de POI. M6 sale de `EncryptedDocumentException`; cualquier otra falla de POI al abrir es M5.
- Los lectores lanzan `ArchivoRechazadoException` y no devuelven el rechazo: `CargaService` (T14) lo atrapa y arma el estado `Rechazado`. `LectorCsvService.leer` devuelve `EstadoLugar.Valido`. No se propaga la causa de la excepción de Commons CSV (puede traer datos del archivo, RNF-3).
- `FabricaArchivosPrueba` (en `src/test/.../helper/`) tiene `csv(...)`, `filasFicticias(n)`, `xlsx(armado)`, `xls(armado)`, `hojaConDatos`, `hojaVacia`, `xlsxMinimo()`, `xlsMinimo()` y los cifrados; T11 le suma fórmulas, combinadas y filas ocultas.
- Las restricciones `@ExtensionPermitida` y `@TamanoMaximo` llevan como mensaje el código `"M1"`/`"M2"` (una anotación no admite el texto del enum); el controlador lo traduce con `MensajeCarga.valueOf`. Sin archivo (`null`) la extensión falla con M1.
- `SeparadorHelper`: el puntaje de un separador es cuántas líneas repiten su misma cuenta (>0); no se maneja un campo entrecomillado que cruce líneas (el lector CSV de T8 parsea con Commons CSV igual).
- `AcumuladorFilas` es la única clase con estado del paquete helper (una instancia por lectura); las filas vacías no cuentan para el límite de 100.000 y la vista previa guarda las 10 primeras filas con datos.
- 22 tareas en 3 bloques (A núcleo, B lectura de archivos, C web e interfaz) en vez de partir la spec. Se puede partir antes del bloque B (CSV+interfaz / Excel).
- Datos de prueba siempre ficticios y generados por código (RNF-6).
- El recorte de celdas usa "…" (un solo carácter, U+2026), no tres puntos: así la celda mide exactamente 50.

## Errores a evitar
- `ArchivoCargado` lleva un `byte[]`: dos records con arreglos iguales no son `equals`. En los tests guardar y comparar la misma instancia, no crear otra con el mismo contenido.
- D8 revisado en T12: un xlsx de 100.000 filas x 3 columnas (2 MB) tardó de 6 a más de 10 s con la API normal de POI (abrir el libro solo ~5 s; sin problemas de memoria). Decisión del usuario: mantener la API normal y subir el límite del test a 30 s. Un xlsx de 10 MB (el máximo) puede tardar bastante más: si molesta en el uso real, pasar a lectura en streaming. Crear el libro en el test tarda 16-21 s.
- `DataFormatter.formatCellValue` sin `FormulaEvaluator` devuelve el texto de la fórmula, no su resultado: para fórmulas leer el valor guardado.
- Con 100.001 filas, `LectorExcelServiceResumenTest` tarda ~7 s (crear y leer el libro en memoria): dato a mano para el riesgo D8 de T12 (criterio: menos de 10 s).
- `String` es inmutable: `substring` devuelve un texto nuevo, hay que usar el `return`.
- Revisar que el test pase con `./mvnw test` antes de marcar una tarea `[x]`.

## Pendiente cruzado con la spec 002 (aplicar al llegar a T13)
- La 002 (página inicial, sesión "Front Worktree") pide 4 ajustes a la 001: ruta `/carga`, campo `tipo` + `reiniciar(tipo)` en `Carga`, `tituloTipo` en `PantallaCargaDto`, redirección a `/` y enlace "Volver al inicio". Detalle en `.claude/worktrees/front/Coinciliador/specs/002-pagina-inicial/plan.md` §8 (sin mergear).
- Acuerdo: los aplico yo con `/sdd-change` antes de T13; la otra sesión no toca archivos de la 001.
- `TipoConciliacion` (IVA, VENTAS, COMPRAS, INGRESOS_BRUTOS) ya está en main (4f6b463, traído con cherry-pick desde la rama temporal `tipo-conciliacion-para-001`, que se puede borrar). Lo usa `Carga.tipo` en T13; no crear otro.
