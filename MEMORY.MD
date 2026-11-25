# MEMORY

## Estado actual (2026-11-25)
- Spec activa: `Coinciliador/specs/001-subida-archivos-csv/` (spec y plan aprobados).
- Tareas: 7 de 22 hechas (T1–T7). Bloque A (núcleo puro) completo; sigue el bloque B desde T8.
- `./mvnw test` en verde (74 tests) al cerrar T7. T7 todavía sin commit.
- Siguiente paso: commit de T7 y luego `/sdd-implement 001-subida-archivos-csv T8` (`LectorCsvService` y fábrica de CSV de prueba).

## Decisiones
- Las restricciones `@ExtensionPermitida` y `@TamanoMaximo` llevan como mensaje el código `"M1"`/`"M2"` (una anotación no admite el texto del enum); el controlador lo traduce con `MensajeCarga.valueOf`. Sin archivo (`null`) la extensión falla con M1.
- `SeparadorHelper`: el puntaje de un separador es cuántas líneas repiten su misma cuenta (>0); no se maneja un campo entrecomillado que cruce líneas (el lector CSV de T8 parsea con Commons CSV igual).
- `AcumuladorFilas` es la única clase con estado del paquete helper (una instancia por lectura); las filas vacías no cuentan para el límite de 100.000 y la vista previa guarda las 10 primeras filas con datos.
- 22 tareas en 3 bloques (A núcleo, B lectura de archivos, C web e interfaz) en vez de partir la spec. Se puede partir antes del bloque B (CSV+interfaz / Excel).
- Datos de prueba siempre ficticios y generados por código (RNF-6).
- El recorte de celdas usa "…" (un solo carácter, U+2026), no tres puntos: así la celda mide exactamente 50.

## Errores a evitar
- `String` es inmutable: `substring` devuelve un texto nuevo, hay que usar el `return`.
- Revisar que el test pase con `./mvnw test` antes de marcar una tarea `[x]`.
