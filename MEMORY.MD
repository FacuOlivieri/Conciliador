# MEMORY.md - Conciliador

Memoria del proyecto de Conciliador entre sesiones. Maximo de ~50 lineas: resume o elimina lo que ya no aporte

## Estado Actual

- Scaffold Spring Boot 4.2.0 / Java 25 sin lógica propia.
- Dependencias agregadas: `poi-ooxml` 5.5.1 y `commons-csv` 1.14.1 (versión fijada en `pom.xml`, Boot no las gestiona). Tutoriales en `docs/librerias/`.
- Spec 001 (`specs/001-subida-archivos-csv/spec.md`) **aprobada** (2026-11-24): 23 RF, catálogo de mensajes M1-M8, 0 dudas abiertas.
- Constitución actualizada (2026-11-24): regla 1 incluye POI y Commons CSV; regla 4 pide datasource solo si hay persistencia.

## Decisiones (y por que)

- Sin base de datos ni historial: el usuario solo necesita el resultado de la conciliación.
- Distribución con `jpackage` (instalador con runtime): el usuario final no es técnico y no va a instalar un JDK.
- Spec 001 cubre solo subida/validación; conciliar y descargar van en specs siguientes, para tener unidades chicas y testeables.
- Validación mínima (extensión, 10 MB, no vacío); el contenido (columnas, importes) se valida en la spec de conciliación porque cada tipo (IVA, Ventas…) usa columnas distintas.
- Se aceptan `.csv`, `.xlsx` y `.xls` (pedido del usuario). `.xls` no suma dependencias: el módulo `poi` ya viene con `poi-ooxml`.
- Excel con varias hojas: el usuario elige la hoja.
- Tras subir: resumen (nombre, hoja, filas) + vista previa de 10 filas, para confirmar el archivo antes de conciliar.
- Archivos se llaman Propio / Contraparte: la conciliación necesita saber cuál es la base.
- 10 MB = 10.485.760 bytes (como lo muestra Windows). Máx. 100.000 filas con datos (evita archivos chicos que se expanden).
- Filas sin datos se ignoran en conteo y vista previa: una sola definición de "fila".
- Solo se listan hojas visibles con datos; extensión `.xls`/`.xlsx` intercambiable (se detecta por contenido).
- Plan 001: estado en repositorio singleton en memoria (app local de 1 usuario; HttpSession expira). Multipart a 30 MB y umbral de disco 31 MB: el límite de 10 MB lo aplica `@TamanoMaximo` y nada toca disco (default de Spring `file-size-threshold=0B` escribe a temporal). Regla 5: Bean Validation para extensión/tamaño; legibilidad/vacío/filas con excepción de dominio.

## Aprendizajes y errores a evitar

- `./mvnw -q dependency:tree` no muestra el árbol: correrlo sin `-q`.
- IntelliJ puede autoimportar clases equivocadas (`jdk.jpackage.internal.model.Application`) y romper el build: si algo no compila, mirar primero los imports. Maven es la referencia, no el IDE.
- IntelliJ: abrir `Coinciliador/Coinciliador/pom.xml` (la carpeta de afuera tiene un módulo vacío → "JVM target 5"); SDK llamado "25"; Maven home = "Bundled (Maven 3)" porque el wrapper 3.10.1 rompe el importador ("Unable to create injector").
- Cada dependencia nueva lleva un mini tutorial en `docs/librerias/`.

## Proximos Pasos

- Plan 001 aprobado. `tasks.md` con 22 tareas en 3 bloques (A núcleo puro T1–T7, B lectores T8–T12, C carga/web/UI T13–T22); se agrupó en bloques en lugar de partir la spec (autorizado por el usuario).
- `tasks.md` aprobado. **T1 hecha** (modelo + excepción, 5 tests en verde). Próximo: T2 `ExtensionHelper`.
- T12 es un punto de control: si el `.xlsx` de 100.000 filas falla por memoria o tiempo, revisar D8 antes de seguir.
