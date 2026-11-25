# Coinciliador

Aplicación local para contadores que concilia archivos Excel (`.xlsx`, `.xls`) y CSV (IVA, Ventas, Compras, Ingresos Brutos, etc.). Se sube cada archivo, se cruzan en memoria y se descarga el resultado. No guarda historial ni usa base de datos.

> **Estado:** scaffold de Spring Boot. Todavía no hay lógica de conciliación; las secciones marcadas como *planificado* describen el diseño acordado.

## Stack

- Java 25, Spring Boot 4.2.0, Spring MVC + Thymeleaf, Bean Validation
- `org.apache.poi:poi-ooxml` 5.5.1: lectura y escritura de `.xlsx` y `.xls` ([tutorial](docs/librerias/apache-poi.md))
- `org.apache.commons:commons-csv` 1.14.1: lectura y escritura de CSV ([tutorial](docs/librerias/commons-csv.md))

Spring Boot no gestiona estas versiones, por eso están fijadas como propiedades en `pom.xml` (`poi.version`, `commons-csv.version`).

## Flujo previsto

1. El usuario sube dos archivos desde el navegador (multipart de Spring MVC).
2. Un lector por formato (Excel o CSV) los convierte a registros comunes.
3. Un Service cruza los registros y calcula diferencias con `BigDecimal`.
4. El resultado se descarga como Excel. No se persiste nada.

Los CSV argentinos suelen usar `;` como separador y coma decimal (`1.234,56`); el parseo de importes vive en un Helper propio.

## Desarrollo

Requiere JDK 25. Desde esta carpeta:

```bash
./mvnw spring-boot:run      # ejecuta la aplicación (mvnw.cmd desde PowerShell)
./mvnw test                 # corre los tests
./mvnw package              # genera el jar
```

## Distribución (planificado)

El destinatario no necesita instalar Java: se empaqueta con `jpackage` (incluido en el JDK) a un instalador de Windows con el runtime embebido.

Pasos previstos, aún sin probar:

1. `./mvnw package` para generar el jar.
2. `jpackage` sobre ese jar con `--type exe` o `--type msi`.
3. Al abrir la app, se levanta el servidor local y se abre el navegador en `localhost`.

Pendiente de verificar: `jpackage --type exe/msi` en Windows requiere WiX Toolset instalado en la máquina que construye.

## Módulos

Paquete base `com.proyecto.Coinciliador`.

**`model/`**
- `Lugar`: los dos lugares de carga, Propio y Contraparte, con su etiqueta para la vista.
- `FormatoArchivo`: formato real del archivo (CSV o EXCEL; `.xls` y `.xlsx` son ambos EXCEL).
- `MensajeCarga`: catálogo de mensajes M1–M11 de la spec 001; única fuente de esos textos. M9–M11 son los fragmentos de "qué falta" para confirmar.
- `ArchivoCargado`: archivo aceptado con sus bytes, huella y datos para releerlo (hojas o separador y codificación).
- `Resumen`: lo que ve el contador de un archivo válido: nombre, hoja, cantidad de filas y vista previa.
- `EstadoLugar`: interfaz sellada con los estados de un lugar: `Vacio`, `Rechazado`, `PendienteDeHoja`, `Valido`.
- `Carga`: la única carga de la aplicación: estado de Propio y Contraparte, si fue confirmada y su tipo de conciliación (nulo hasta que se elige). Es inmutable: cada cambio devuelve una carga nueva; cambiar un lugar borra la confirmación; `vacia(tipo)` la deja sin archivos ni rechazos y `ambosValidos()` dice si Propio y Contraparte son válidos.

**`helper/`**
- `ExtensionHelper`: extensión de un nombre de archivo en minúsculas y si es una de las permitidas (`csv`, `xlsx`, `xls`).
- `ContenidoHelper`: decide si una celda (algún carácter distinto de espacio) o una fila (alguna celda con datos) tiene datos.
- `VistaPreviaHelper`: recorta las filas para la vista previa: 10 filas, 20 columnas y celdas de hasta 50 caracteres (el recorte termina en "…").
- `AcumuladorFilas`: recibe las filas de a una; cuenta las que tienen datos, guarda las 10 primeras para la vista previa y rechaza con M4 al pasar las 100.000 filas.
- `HuellaHelper`: calcula el SHA-256 de los bytes de un archivo en hexadecimal, para detectar que Propio y Contraparte son el mismo.
- `CodificacionHelper`: decide si unos bytes son texto (sin 0x00), detecta si el CSV está en UTF-8 o Windows-1252 y quita el BOM inicial.
- `SeparadorHelper`: detecta (desde una lista de líneas o desde el texto entero) si un CSV usa `;`, `,` o tabulación mirando las primeras 20 líneas con datos (ignora lo que está entre comillas; gana el separador con cuenta constante por línea y, en empate, `;`).

**`repository/`**
- `ICargaRepository` y `CargaEnMemoriaRepository`: guardan la carga en memoria, sin disco, compartida por todas las pestañas. Guardar un lugar no toca el otro y borra la confirmación; `reiniciar(tipo)` descarta todo y deja ambos lugares vacíos con ese tipo.

**`exception/`**
- `ArchivoRechazadoException`: rechazo de un archivo con su `MensajeCarga`; no lleva nombre ni contenido.

**`dto/`**
- `SubidaArchivoForm`: formulario de subida; el archivo se valida con `@ExtensionPermitida` y `@TamanoMaximo`.
- `ElegirHojaForm`: nombre de la hoja elegida de un Excel (`@NotBlank`).
- `LugarVistaDto`: todo lo que la plantilla necesita de un lugar (Propio o Contraparte): su estado (vacío, rechazado, pendiente de hoja o válido), el nombre, el mensaje de rechazo, las hojas, si se muestra el selector de hoja y el resumen con su vista previa.
- `PantallaCargaDto`: todo lo que necesita la pantalla de carga ya calculado: título del tipo, los dos lugares, si Confirmar está habilitado y el texto de qué falta, la advertencia M7 y el mensaje M8.

**`controller/`**
- `CargaController`: pantalla de carga. `GET /carga` la muestra (sin tipo de conciliación elegido redirige a `/` sin mensaje) y `POST /carga/{lugar}` sube un archivo; si la validación de entrada falla guarda el rechazo M1 o M2. Siempre redirige a `/carga` después de subir.

**`templates/`**
- `carga.html` y `fragmentos/tarjeta-lugar.html`: plantilla mínima sin diseño, con el título del tipo, el enlace "Volver al inicio" y una tarjeta por lugar con su estado y su formulario de subida.

**`mapper/`**
- `PantallaCargaMapper`: convierte la carga en `PantallaCargaDto`. Calcula el título del tipo, habilita Confirmar solo con ambos lugares válidos y explica qué falta, avisa M7 si las huellas de los dos archivos coinciden (aunque los nombres difieran) y muestra M8 solo si la carga está confirmada.

**`utils/validacion/`**
- `ExtensionPermitida` y `ExtensionPermitidaValidator`: restricción de Bean Validation, el archivo debe ser `.csv`, `.xlsx` o `.xls` (mensaje M1; sin archivo también falla).
- `TamanoMaximo` y `TamanoMaximoValidator`: restricción de Bean Validation, el archivo no puede superar 10.485.760 bytes (mensaje M2).

**`service/`**
- `ILectorCsvService` y `LectorCsvService`: leen un CSV. Rechazan con M3 (vacío), M5 (no es texto o comillas sin cerrar) o M4 (más de 100.000 filas con datos). Si es válido, detectan codificación y separador, cuentan las filas con datos y arman la vista previa.
- `ILectorExcelService` y `LectorExcelService`: leen un Excel con POI. Lo abren por contenido (`.xls` o `.xlsx`) y rechazan con M3 (sin hojas listables), M4 (más de 100.000 filas con datos), M5 (no es Excel o dañado) o M6 (cifrado). Calculan las hojas listables (visibles y con al menos una fila con datos): con una devuelven el archivo válido con su resumen; con varias, queda pendiente de elegir hoja. `resumirHoja` arma el resumen de una hoja releyendo los bytes ya guardados. Las celdas se muestran como en Excel, con números en es-AR; de una fórmula se muestra su resultado guardado y los errores (`#REF!`) tal cual.
- `IAnalizadorArchivoService` y `AnalizadorArchivoService`: según la extensión del nombre, derivan el archivo al lector de CSV (`.csv`) o al de Excel (cualquier otra), y devuelven el estado resultante. Permiten que Propio y Contraparte tengan formatos distintos.
- `ICargaService` y `CargaService`: orquestan la carga. `subir` analiza el archivo y lo guarda en su lugar reemplazando lo anterior; si se rechaza, el lugar queda `Rechazado` con su motivo y el otro lugar no se toca. `registrarRechazo` guarda un rechazo ya detectado por la validación de entrada (M1 o M2). `elegirHoja` elige o cambia la hoja de un Excel sin volver a subirlo (se ignora si el lugar no tiene un Excel o la hoja no existe; si supera el máximo de filas queda rechazado con M4). `confirmar` confirma solo si Propio y Contraparte son válidos. `cargaActual` entrega la carga para mostrarla.

**Configuración (`application.properties`)**
- Subida de archivos: hasta 30 MB por archivo y por petición (tope técnico; el límite de negocio de 10 MB lo aplica `@TamanoMaximo`) y umbral de disco de 31 MB, así los archivos se quedan en memoria y nunca se escriben en un temporal.

**Pruebas (`src/test`)**
- `FabricaArchivosPrueba`: genera por código los CSV, los Excel (`.xlsx` y `.xls`) con las hojas que pida el test, y los Excel cifrados, con datos ficticios.
