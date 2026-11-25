package com.proyecto.Coinciliador.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Genera por código los archivos de prueba, con datos ficticios (RNF-6, D12). Nunca se guardan en disco. */
public final class FabricaArchivosPrueba {

    private static final byte[] BOM_UTF8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private FabricaArchivosPrueba() {
    }

    /** Filas ficticias de tres columnas: comprobante, proveedor e importe. Sin comas ni punto y coma. */
    public static List<List<String>> filasFicticias(int cantidad) {
        return IntStream.rangeClosed(1, cantidad)
                .mapToObj(numero -> List.of("Comprobante " + numero, "Proveedor " + numero, String.valueOf(numero * 100)))
                .toList();
    }

    /**
     * Arma un CSV con ese separador y codificación. Une las celdas sin comillas, por eso
     * las celdas de prueba no deben contener el separador. El BOM solo tiene sentido en UTF-8.
     */
    public static byte[] csv(char separador, Charset codificacion, boolean conBom, List<List<String>> filas) {
        String texto = String.join("\r\n", filas.stream()
                .map(fila -> String.join(String.valueOf(separador), fila))
                .toList());
        byte[] contenido = texto.getBytes(codificacion);
        return conBom ? concatenar(BOM_UTF8, contenido) : contenido;
    }

    /** Un .xlsx real y mínimo: sirve para probar un Excel renombrado a .csv. */
    public static byte[] xlsxMinimo() {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            libro.createSheet("Hoja1").createRow(0).createCell(0).setCellValue("dato ficticio");
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] concatenar(byte[] primero, byte[] segundo) {
        byte[] resultado = new byte[primero.length + segundo.length];
        System.arraycopy(primero, 0, resultado, 0, primero.length);
        System.arraycopy(segundo, 0, resultado, primero.length, segundo.length);
        return resultado;
    }
}
