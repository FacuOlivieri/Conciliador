# Apache Commons CSV — mini tutorial

**Qué es:** librería para leer y escribir archivos CSV respetando el formato (comillas, separadores dentro de campos, saltos de línea).
**Para qué la usamos:** leer los CSV que sube el contador y, si hace falta, exportar resultados en CSV.
**Versión:** 1.14.1 (`org.apache.commons:commons-csv`, fijada en `pom.xml` como `commons-csv.version`).

## Por qué no `line.split(";")`

Un CSV real puede traer `"Pérez; Juan";1.234,56`: el `;` dentro de comillas no separa columnas. Commons CSV maneja eso, las comillas escapadas (`""`) y los saltos de línea dentro de un campo.

## Las tres piezas

| Clase | Rol |
|---|---|
| `CSVFormat` | Configuración: separador, encabezado, comillas. Se arma con `builder()` y se cierra con `get()` (`build()` está deprecado). |
| `CSVParser` | Lee y entrega registros. Es `Closeable`. |
| `CSVRecord` | Una fila. `get(0)` por índice (base 0) o `get("Columna")` por nombre si hay encabezado. |

## 1. Leer un CSV separado por `;` sin encabezado

```java
CSVFormat formato = CSVFormat.DEFAULT.builder()
        .setDelimiter(";")
        .get();

try (Reader lector = Files.newBufferedReader(Path.of("compras.csv"), StandardCharsets.UTF_8);
     CSVParser parser = formato.parse(lector)) {
    for (CSVRecord registro : parser) {
        System.out.println(registro.getRecordNumber() + ": " + registro.get(0));
    }
}
```

`registro.size()` da la cantidad de columnas de esa fila (puede variar entre filas).

## 2. Leer con encabezado

```java
CSVFormat formato = CSVFormat.DEFAULT.builder()
        .setDelimiter(";")
        .setHeader()               // toma la primera fila como nombres de columna
        .setSkipHeaderRecord(true) // y no la devuelve como dato
        .get();

for (CSVRecord registro : formato.parse(lector)) {
    String importe = registro.get("Importe");
}
```

## 3. Codificación y BOM

- Los CSV exportados por Excel en Windows suelen venir en **Latin-1 / Windows-1252**, no en UTF-8. Si los acentos salen como `�`, cambiar el charset: `Charset.forName("windows-1252")`.
- Muchos UTF-8 traen un **BOM** (bytes invisibles al inicio) que se pega al nombre de la primera columna (`"﻿Fecha"`). Se quita con `BOMInputStream` de Commons IO, que ya está en el proyecto como dependencia transitiva de Commons CSV:

```java
try (Reader lector = new InputStreamReader(
             BOMInputStream.builder().setPath(Path.of("ventas.csv")).get(),
             StandardCharsets.UTF_8);
     CSVParser parser = CSVFormat.EXCEL.builder().setHeader().get().parse(lector)) {
    // ...
}
```

## 4. Escribir un CSV

```java
CSVFormat formato = CSVFormat.DEFAULT.builder()
        .setDelimiter(";")
        .setHeader("Comprobante", "Importe")
        .get();

try (Writer escritor = Files.newBufferedWriter(Path.of("resultado.csv"), StandardCharsets.UTF_8);
     CSVPrinter impresor = new CSVPrinter(escritor, formato)) {
    impresor.printRecord("A-0001-00000123", "1234,56");
}
```

`printRecord` agrega comillas solo cuando hace falta y el salto de línea al final.

## Errores comunes

- **Coma decimal:** la librería entrega texto. `"1.234,56"` no se puede pasar directo a `new BigDecimal(...)`: hay que sacar el `.` de miles y cambiar `,` por `.` antes. Esa conversión vive en un Helper del proyecto, no en la librería.
- **Separador equivocado:** con `;` configurado y un archivo separado por `,`, cada fila llega como una sola columna. `registro.size() == 1` es una señal de que el separador no es el correcto.
- **Nombres de columna con BOM:** `get("Fecha")` falla con `IllegalArgumentException` aunque la columna exista. Ver sección 3.
- **Filas de distinto largo:** `get(i)` lanza `ArrayIndexOutOfBoundsException` si la fila tiene menos columnas. Chequear `size()` antes.
- **CSV mal formado** (comillas sin cerrar): el parser lanza `CSVException` / `IOException`.

## Referencias

- Guía de uso: https://commons.apache.org/proper/commons-csv/user-guide.html
- Javadoc: https://commons.apache.org/proper/commons-csv/apidocs/
