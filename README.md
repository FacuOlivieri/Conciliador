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

**`enums/` (spec 002, página inicial)**
- `TipoConciliacion`: los cuatro tipos de conciliación (IVA, Ventas, Compras, Ingresos Brutos) con nombre, slug para la URL y frase de ayuda; `deSlug` busca un tipo por su slug.
- `MensajeInicio`: catálogo de mensajes P1 y D1 de la spec 002; única fuente de esos textos.
- `ResultadoEleccion`: qué pasa al elegir un tipo: `ABRIR_CARGA` o `PEDIR_CONFIRMACION`.

**`helper/` (spec 002, página inicial)**
- `CargaEnCursoHelper`: dice si hay una carga en curso: algún lugar con archivo aceptado (aunque falte la hoja) o carga confirmada; vacío y rechazado no cuentan.
- `EleccionTipoHelper`: decide al elegir un tipo: pide confirmación solo si hay carga en curso de otro tipo; si no, abre la carga.

**`dto/`, `mapper/` y `utils/validacion/` (spec 002, página inicial)**
- `TipoVistaDto`: una tarjeta de la página inicial (slug, nombre y frase de ayuda de un tipo).
- `InicioDto`: las cuatro tarjetas y, si hay que avisar algo, el texto del mensaje (null si no).
- `ConfirmacionCambioDto`: lo que muestra la pantalla de confirmación: tipo activo, tipo elegido (con su slug) y el texto D1.
- `InicioMapper`: arma los DTOs anteriores desde `TipoConciliacion` y `MensajeInicio`; las plantillas no calculan nada.
- `EleccionTipoForm`: el tipo elegido tal como llega en la URL (slug), validado con `@TipoConciliacionValido`.
- `TipoConciliacionValido` y `TipoConciliacionValidoValidator`: restricción de Bean Validation; el slug debe ser el de un tipo existente, si no el mensaje es el código P1.

**`model/`**
- `Lugar`: los dos lugares de carga, Propio y Contraparte, con su etiqueta para la vista.
- `FormatoArchivo`: formato real del archivo (CSV o EXCEL; `.xls` y `.xlsx` son ambos EXCEL).
- `MensajeCarga`: catálogo de mensajes M1–M8 de la spec 001; única fuente de esos textos.
- `ArchivoCargado`: archivo aceptado con sus bytes, huella y datos para releerlo (hojas o separador y codificación).
- `Resumen`: lo que ve el contador de un archivo válido: nombre, hoja, cantidad de filas y vista previa.
- `EstadoLugar`: interfaz sellada con los estados de un lugar: `Vacio`, `Rechazado`, `PendienteDeHoja`, `Valido`.

**`helper/`**
- `ExtensionHelper`: extensión de un nombre de archivo en minúsculas y si es una de las permitidas (`csv`, `xlsx`, `xls`).
- `ContenidoHelper`: decide si una celda (algún carácter distinto de espacio) o una fila (alguna celda con datos) tiene datos.
- `VistaPreviaHelper`: recorta las filas para la vista previa: 10 filas, 20 columnas y celdas de hasta 50 caracteres (el recorte termina en "…").
- `AcumuladorFilas`: recibe las filas de a una; cuenta las que tienen datos, guarda las 10 primeras para la vista previa y rechaza con M4 al pasar las 100.000 filas.
- `HuellaHelper`: calcula el SHA-256 de los bytes de un archivo en hexadecimal, para detectar que Propio y Contraparte son el mismo.

**`exception/`**
- `ArchivoRechazadoException`: rechazo de un archivo con su `MensajeCarga`; no lleva nombre ni contenido.
