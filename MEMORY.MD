# MEMORY

## Estado actual (2026-11-25)
- Spec activa: `Coinciliador/specs/001-subida-archivos-csv/` (spec y plan aprobados).
- Tareas: 3 de 22 hechas (T1, T2, T3). Bloque A (núcleo puro) va por T4.
- `./mvnw test` en verde (43 tests) al cerrar T3. T3 todavía sin commit.
- Siguiente paso: commit de T3 (`feat: spec S1-T3, ContenidoHelper y VistaPreviaHelper`) y luego `/sdd-implement 001-subida-archivos-csv T4` (`AcumuladorFilas` y `HuellaHelper`).

## Decisiones
- 22 tareas en 3 bloques (A núcleo, B lectura de archivos, C web e interfaz) en vez de partir la spec. Se puede partir antes del bloque B (CSV+interfaz / Excel).
- Datos de prueba siempre ficticios y generados por código (RNF-6).
- El recorte de celdas usa "…" (un solo carácter, U+2026), no tres puntos: así la celda mide exactamente 50.

## Errores a evitar
- `String` es inmutable: `substring` devuelve un texto nuevo, hay que usar el `return`.
- Revisar que el test pase con `./mvnw test` antes de marcar una tarea `[x]`.
