# Apache POI — mini tutorial

**Qué es:** librería para leer y escribir archivos de Microsoft Office desde Java.
**Para qué la usamos:** leer los Excel que sube el contador (`.xlsx` y `.xls`) y, más adelante, generar el Excel con el resultado de la conciliación.
**Versión:** 5.5.1 (`org.apache.poi:poi-ooxml`, fijada en `pom.xml` como `poi.version`).

## Módulos que entran al proyecto

| Módulo | Formato | Clases principales |
|---|---|---|
| `poi` | `.xls` (Excel 97-2003, formato binario) | `HSSFWorkbook` |
| `poi-ooxml` | `.xlsx` (Excel 2007+, XML comprimido) | `XSSFWorkbook` |

Solo declaramos `poi-ooxml`; `poi` entra solo como dependencia transitiva. No hace falta usar las clases `HSSF*`/`XSSF*` directamente: las interfaces de `org.apache.poi.ss.usermodel` (`Workbook`, `Sheet`, `Row`, `Cell`) sirven para ambos formatos.

## 1. Abrir un Excel sin importar el formato

`WorkbookFactory` mira el **contenido** del archivo (no la extensión) y devuelve el tipo correcto.

```java
try (Workbook libro = WorkbookFactory.create(new File("ventas.xls"))) {
    System.out.println("Hojas: " + libro.getNumberOfSheets());
}
```

- Con un `File` usa menos memoria que con un `InputStream`. Si solo tenés un stream (por ejemplo, un archivo subido), envolvelo en `BufferedInputStream`.
- `Workbook` es `Closeable`: usá siempre `try-with-resources`.

## 2. Listar las hojas

```java
for (int i = 0; i < libro.getNumberOfSheets(); i++) {
    String nombre = libro.getSheetName(i);
    boolean oculta = libro.isSheetHidden(i);
    System.out.println(nombre + (oculta ? " (oculta)" : ""));
}
Sheet hoja = libro.getSheetAt(0);          // por índice, base 0
Sheet otra = libro.getSheet("Compras");    // por nombre, null si no existe
```

## 3. Leer las primeras N filas como texto

`DataFormatter` devuelve cada celda **tal como se ve en Excel** (fechas, decimales y porcentajes con su formato). Es lo indicado para una vista previa.

```java
DataFormatter formato = new DataFormatter();
int leidas = 0;
for (Row fila : hoja) {                     // solo recorre filas que existen
    if (leidas++ == 10) break;
    List<String> valores = new ArrayList<>();
    for (Cell celda : fila) {
        valores.add(formato.formatCellValue(celda));
    }
    System.out.println(valores);
}
```

## 4. Leer una celda con su tipo

```java
Cell celda = fila.getCell(2);               // null si la celda no existe
switch (celda.getCellType()) {
    case STRING  -> celda.getStringCellValue();
    case NUMERIC -> DateUtil.isCellDateFormatted(celda)
                        ? celda.getLocalDateTimeCellValue()
                        : celda.getNumericCellValue();   // double
    case BOOLEAN -> celda.getBooleanCellValue();
    case FORMULA -> celda.getCachedFormulaResultType();  // tipo del último resultado calculado
    default      -> null;                                // BLANK, ERROR
}
```

Para importes: `getNumericCellValue()` devuelve `double`. La constitución exige `BigDecimal`, así que convertir con `BigDecimal.valueOf(valor)` (no con `new BigDecimal(double)`, que arrastra errores binarios).

## 5. Escribir un Excel

```java
try (Workbook libro = new XSSFWorkbook();
     OutputStream salida = new FileOutputStream("resultado.xlsx")) {
    Sheet hoja = libro.createSheet("Resultado");
    Row fila = hoja.createRow(0);
    fila.createCell(0).setCellValue("Comprobante");
    fila.createCell(1).setCellValue(1234.56);
    libro.write(salida);
}
```

## Errores comunes

- **Filas o celdas `null`:** `sheet.getRow(i)` y `row.getCell(j)` devuelven `null` si nunca se escribieron. Con el `for` sobre la hoja o la fila solo se recorren las que existen.
- **Contar filas:** `getLastRowNum()` es el índice de la última fila (base 0), no la cantidad, y cuenta filas que pueden estar vacías.
- **Fechas:** en Excel una fecha es un número con formato de fecha. Sin `DateUtil.isCellDateFormatted` se lee como `45291.0`.
- **Fórmulas con `DataFormatter`:** `formatCellValue(celda)` sin `FormulaEvaluator` devuelve el **texto de la fórmula** (`A1+B1`), no su resultado. Para mostrar el resultado guardado en el archivo hay que leer `getCachedFormulaResultType()` y formatear ese valor (`formatRawCellContents` para números). Verificado en la tarea T11.
- **Locale de los números:** `new DataFormatter()` usa el locale de la PC. Para que `1234.5` salga siempre `1234,5`, crear `new DataFormatter(Locale.forLanguageTag("es-AR"))`. `DataFormatter` no es seguro entre hilos: uno por lectura.
- **Fechas:** aun con locale `es-AR`, `DataFormatter` muestra los formatos predefinidos 14 y 22 con el mes primero, y `DateUtil.isCellDateFormatted` no reconoce fechas con texto (`d "de" mmmm`). El proyecto decide con `FechaExcelHelper` sobre `getDataFormatString()` y formatea `getLocalDateTimeCellValue()` (respeta el calendario 1904). Un número negativo no es una fecha válida. Verificado en la tarea T24.
- **Archivo con contraseña:** `WorkbookFactory.create` lanza `EncryptedDocumentException`.
- **Archivo que no es Excel:** `WorkbookFactory.create` lanza `IOException` porque no reconoce el formato.
- **Extensión cambiada:** un `.xlsx` renombrado a `.xls` igual se abre, porque `WorkbookFactory` detecta por contenido. Relevante para una de las dudas abiertas de la spec 001.
- **Archivos grandes al escribir:** para decenas de miles de filas, `SXSSFWorkbook` mantiene solo una ventana de filas en memoria. Llamar a `dispose()` al terminar para borrar sus temporales.

## Referencias

- Guía rápida: https://poi.apache.org/components/spreadsheet/quick-guide.html
- Javadoc: https://poi.apache.org/apidocs/
