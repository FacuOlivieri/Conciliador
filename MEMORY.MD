# MEMORY

## Estado actual (2026-11-25)
- Spec activa: `Coinciliador/specs/001-subida-archivos-csv/` (spec y plan aprobados).
- Tareas: 5 de 22 hechas (T1–T5). Bloque A (núcleo puro) va por T6.
- `./mvnw test` en verde (59 tests) al cerrar T5. T5 todavía sin commit.
- Siguiente paso: commit de T5 y luego `/sdd-implement 001-subida-archivos-csv T6` (`SeparadorHelper`).

## Decisiones
- `AcumuladorFilas` es la única clase con estado del paquete helper (una instancia por lectura); las filas vacías no cuentan para el límite de 100.000 y la vista previa guarda las 10 primeras filas con datos.
- 22 tareas en 3 bloques (A núcleo, B lectura de archivos, C web e interfaz) en vez de partir la spec. Se puede partir antes del bloque B (CSV+interfaz / Excel).
- Datos de prueba siempre ficticios y generados por código (RNF-6).
- El recorte de celdas usa "…" (un solo carácter, U+2026), no tres puntos: así la celda mide exactamente 50.

## Errores a evitar
- `String` es inmutable: `substring` devuelve un texto nuevo, hay que usar el `return`.
- Revisar que el test pase con `./mvnw test` antes de marcar una tarea `[x]`.
