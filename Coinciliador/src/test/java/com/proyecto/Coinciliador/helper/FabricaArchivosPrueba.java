package com.proyecto.Coinciliador.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Genera por código los archivos de prueba, con datos ficticios (RNF-6, D12). Nunca se guardan en disco. */
public final class FabricaArchivosPrueba {

    /** Texto de la celda A1 de la hoja "Hoja1" de los Excel mínimos. */
    public static final String TEXTO_CELDA = "dato ficticio";

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

    /** Un .xlsx real y mínimo: una hoja "Hoja1" con {@link #TEXTO_CELDA} en A1. */
    public static byte[] xlsxMinimo() {
        return conUnaCelda(new XSSFWorkbook());
    }

    /** Un .xls real y mínimo (formato binario antiguo), con el mismo contenido que {@link #xlsxMinimo()}. */
    public static byte[] xlsMinimo() {
        return conUnaCelda(new HSSFWorkbook());
    }

    /** Un .xlsx cifrado con contraseña de apertura (cifrado agile, el de Excel moderno). */
    public static byte[] xlsxCifrado(String clave) {
        try (POIFSFileSystem sistema = new POIFSFileSystem(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Encryptor cifrador = new EncryptionInfo(EncryptionMode.agile).getEncryptor();
            cifrador.confirmPassword(clave);
            try (OPCPackage paquete = OPCPackage.open(new ByteArrayInputStream(xlsxMinimo()));
                 OutputStream cifrado = cifrador.getDataStream(sistema)) {
                paquete.save(cifrado);
            }
            sistema.writeFilesystem(salida);
            return salida.toByteArray();
        } catch (IOException | GeneralSecurityException | InvalidFormatException e) {
            throw new IllegalStateException("No se pudo generar el .xlsx cifrado de prueba", e);
        }
    }

    /** Un .xls cifrado con contraseña de apertura. La clave de POI es de hilo y se limpia siempre. */
    public static byte[] xlsCifrado(String clave) {
        Biff8EncryptionKey.setCurrentUserPassword(clave);
        try {
            return xlsMinimo();
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }

    /** Un .xlsx armado por el test: recibe el libro vacío y le agrega las hojas que necesite. */
    public static byte[] xlsx(Consumer<Workbook> armado) {
        return escribir(new XSSFWorkbook(), armado);
    }

    /** Igual que {@link #xlsx(Consumer)}, pero en el formato binario antiguo .xls. */
    public static byte[] xls(Consumer<Workbook> armado) {
        return escribir(new HSSFWorkbook(), armado);
    }

    /** Agrega una hoja visible con {@link #TEXTO_CELDA} en A1. */
    public static Sheet hojaConDatos(Workbook libro, String nombre) {
        Sheet hoja = libro.createSheet(nombre);
        hoja.createRow(0).createCell(0).setCellValue(TEXTO_CELDA);
        return hoja;
    }

    /** Agrega una hoja visible con esa cantidad de filas, cada una con un dato en A. */
    public static Sheet hojaConFilas(Workbook libro, String nombre, int cantidad) {
        Sheet hoja = libro.createSheet(nombre);
        for (int fila = 0; fila < cantidad; fila++) {
            hoja.createRow(fila).createCell(0).setCellValue(TEXTO_CELDA);
        }
        return hoja;
    }

    /** Agrega una hoja de volumen: filas de tres columnas (comprobante, proveedor e importe numérico) distintas entre sí. */
    public static Sheet hojaDeVolumen(Workbook libro, String nombre, int cantidad) {
        Sheet hoja = libro.createSheet(nombre);
        for (int fila = 0; fila < cantidad; fila++) {
            Row nueva = hoja.createRow(fila);
            nueva.createCell(0).setCellValue("Comprobante " + fila);
            nueva.createCell(1).setCellValue("Proveedor " + (fila % 500));
            nueva.createCell(2).setCellValue(fila * 1.25);
        }
        return hoja;
    }

    /** Agrega una hoja visible sin ninguna fila. */
    public static Sheet hojaVacia(Workbook libro, String nombre) {
        return libro.createSheet(nombre);
    }

    /** Un .xlsx que se abre bien pero trae texto en una celda numérica: falla recién al leer esa celda. */
    public static byte[] xlsxConCeldaDanada(String contenido) {
        byte[] sano = xlsx(libro -> libro.createSheet("Datos").createRow(0).createCell(0).setCellValue(123456));
        return reemplazarEnEntrada(sano, "xl/worksheets/sheet1.xml", "<v>123456.0</v>", "<v>" + contenido + "</v>");
    }

    // Un .xlsx es un zip: se copia entrada por entrada, cambiando el texto de una sola.
    private static byte[] reemplazarEnEntrada(byte[] zip, String nombre, String buscado, String reemplazo) {
        try (ZipInputStream entrada = new ZipInputStream(new ByteArrayInputStream(zip));
             ByteArrayOutputStream salida = new ByteArrayOutputStream();
             ZipOutputStream nuevo = new ZipOutputStream(salida)) {
            for (ZipEntry parte = entrada.getNextEntry(); parte != null; parte = entrada.getNextEntry()) {
                byte[] datos = entrada.readAllBytes();
                if (parte.getName().equals(nombre)) {
                    datos = reemplazar(datos, buscado, reemplazo);
                }
                nuevo.putNextEntry(new ZipEntry(parte.getName()));
                nuevo.write(datos);
                nuevo.closeEntry();
            }
            nuevo.finish();
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] reemplazar(byte[] datos, String buscado, String reemplazo) {
        String texto = new String(datos, StandardCharsets.UTF_8);
        if (!texto.contains(buscado)) {
            throw new IllegalStateException("El .xlsx de prueba no contiene " + buscado);
        }
        return texto.replace(buscado, reemplazo).getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] conUnaCelda(Workbook libro) {
        return escribir(libro, armado -> hojaConDatos(armado, "Hoja1"));
    }

    private static byte[] escribir(Workbook libro, Consumer<Workbook> armado) {
        try (libro; ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            armado.accept(libro);
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
