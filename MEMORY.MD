# MEMORY

## Estado actual (2026-11-25)
- Spec activa: `Coinciliador/specs/001-subida-archivos-csv/` (spec y plan aprobados).
- Tareas: 9 de 22 hechas (T1–T9). Bloque B (lectura de archivos) va por T10.
- `./mvnw test` en verde (95 tests) al cerrar T9. T9 todavía sin commit.
- Siguiente paso: commit de T9 y luego `/sdd-implement 001-subida-archivos-csv T10` (hojas listables en `LectorExcelService`).

## Decisiones
- `LectorExcelService.abrir(byte[])` es de paquete (no público) y devuelve un `Workbook` POI que quien lo llama debe cerrar. La interfaz `ILectorExcelService` se crea en T10, cuando exista `leer`, para no exponer tipos de POI en el contrato. M6 sale de `EncryptedDocumentException`; cualquier otra falla de POI es M5.
- Los lectores lanzan `ArchivoRechazadoException` y no devuelven el rechazo: `CargaService` (T14) lo atrapa y arma el estado `Rechazado`. `LectorCsvService.leer` devuelve `EstadoLugar.Valido`. No se propaga la causa de la excepción de Commons CSV (puede traer datos del archivo, RNF-3).
- `FabricaArchivosPrueba` (en `src/test/.../helper/`) tiene `csv(...)`, `filasFicticias(n)`, `xlsxMinimo()`, `xlsMinimo()`, `xlsxCifrado(clave)` y `xlsCifrado(clave)`; T10 y T11 le suman libros con varias hojas, ocultas, fórmulas y combinadas.
- Las restricciones `@ExtensionPermitida` y `@TamanoMaximo` llevan como mensaje el código `"M1"`/`"M2"` (una anotación no admite el texto del enum); el controlador lo traduce con `MensajeCarga.valueOf`. Sin archivo (`null`) la extensión falla con M1.
- `SeparadorHelper`: el puntaje de un separador es cuántas líneas repiten su misma cuenta (>0); no se maneja un campo entrecomillado que cruce líneas (el lector CSV de T8 parsea con Commons CSV igual).
- `AcumuladorFilas` es la única clase con estado del paquete helper (una instancia por lectura); las filas vacías no cuentan para el límite de 100.000 y la vista previa guarda las 10 primeras filas con datos.
- 22 tareas en 3 bloques (A núcleo, B lectura de archivos, C web e interfaz) en vez de partir la spec. Se puede partir antes del bloque B (CSV+interfaz / Excel).
- Datos de prueba siempre ficticios y generados por código (RNF-6).
- El recorte de celdas usa "…" (un solo carácter, U+2026), no tres puntos: así la celda mide exactamente 50.

## Errores a evitar
- `String` es inmutable: `substring` devuelve un texto nuevo, hay que usar el `return`.
- Revisar que el test pase con `./mvnw test` antes de marcar una tarea `[x]`.

## Pendiente cruzado con la spec 002 (aplicar al llegar a T13)
- La 002 (página inicial, sesión "Front Worktree") pide 4 ajustes a la 001: ruta `/carga`, campo `tipo` + `reiniciar(tipo)` en `Carga`, `tituloTipo` en `PantallaCargaDto`, redirección a `/` y enlace "Volver al inicio". Detalle en `.claude/worktrees/front/Coinciliador/specs/002-pagina-inicial/plan.md` §8 (sin mergear).
- Acuerdo: los aplico yo con `/sdd-change` antes de T13; la otra sesión no toca archivos de la 001.
- `TipoConciliacion` (IVA, VENTAS, COMPRAS, INGRESOS_BRUTOS) ya existe en la 002, sin commitear: leerlo en `.claude/worktrees/front/Coinciliador/src/main/java/com/proyecto/Coinciliador/enums/TipoConciliacion.java`. No crearlo yo; si lo necesito en mi rama antes del merge, pedirle a esa sesión un commit solo con el enum.
