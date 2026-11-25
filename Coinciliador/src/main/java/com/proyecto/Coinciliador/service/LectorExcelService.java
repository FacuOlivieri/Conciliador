package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.helper.AcumuladorFilas;
import com.proyecto.Coinciliador.helper.ContenidoHelper;
import com.proyecto.Coinciliador.helper.HuellaHelper;
import com.proyecto.Coinciliador.helper.VistaPreviaHelper;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.Resumen;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

/**
 * Lee un Excel con POI: lo abre por contenido, calcula las hojas listables y arma el resumen
 * de la hoja usada (RF-6, RF-9, RF-10, RF-11, RF-14, RF-15). El formato fino de las celdas se afina en T11.
 */
@Service
public class LectorExcelService implements ILectorExcelService {

    // D10: el formato de los números no debe cambiar según la PC donde corra la aplicación.
    private static final Locale ES_AR = Locale.forLanguageTag("es-AR");

    @Override
    public EstadoLugar leer(String nombre, byte[] bytes) {
        DataFormatter formato = new DataFormatter(ES_AR); // no es seguro entre hilos: uno por lectura
        try (Workbook libro = abrir(bytes)) {
            List<String> listables = hojasListables(libro, formato);
            if (listables.isEmpty()) {
                throw new ArchivoRechazadoException(MensajeCarga.M3);
            }
            ArchivoCargado archivo = ArchivoCargado.excel(nombre, bytes, HuellaHelper.sha256(bytes), listables);
            if (listables.size() > 1) {
                return new EstadoLugar.PendienteDeHoja(archivo);
            }
            return new EstadoLugar.Valido(archivo, resumenDeHoja(nombre, libro.getSheet(listables.get(0)), formato));
        } catch (IOException e) {
            throw new ArchivoRechazadoException(MensajeCarga.M5);
        }
    }

    /**
     * Abre el libro detectando .xls o .xlsx por su contenido, sin mirar el nombre (RF-11).
     * Quien lo recibe tiene que cerrarlo.
     *
     * @throws ArchivoRechazadoException con M3 (vacío), M5 (no es Excel o está dañado) o M6 (cifrado)
     */
    Workbook abrir(byte[] bytes) {
        if (bytes.length == 0) {
            throw new ArchivoRechazadoException(MensajeCarga.M3);
        }
        try {
            return WorkbookFactory.create(new ByteArrayInputStream(bytes));
        } catch (EncryptedDocumentException e) {
            throw new ArchivoRechazadoException(MensajeCarga.M6);
        } catch (IOException | RuntimeException e) {
            // POI lanza tipos muy distintos ante un archivo dañado o una zip bomb: todos son M5 (RF-9).
            throw new ArchivoRechazadoException(MensajeCarga.M5);
        }
    }

    @Override
    public Resumen resumirHoja(ArchivoCargado archivo, String hoja) {
        try (Workbook libro = abrir(archivo.bytes())) {
            return resumenDeHoja(archivo.nombre(), libro.getSheet(hoja), new DataFormatter(ES_AR));
        } catch (IOException e) {
            throw new ArchivoRechazadoException(MensajeCarga.M5);
        }
    }

    /** Hojas visibles con al menos una fila con datos. Una hoja de gráfico no tiene filas, así que queda afuera. */
    private List<String> hojasListables(Workbook libro, DataFormatter formato) {
        return IntStream.range(0, libro.getNumberOfSheets())
                .filter(indice -> libro.getSheetVisibility(indice) == SheetVisibility.VISIBLE)
                .filter(indice -> tieneFilaConDatos(libro.getSheetAt(indice), formato))
                .mapToObj(libro::getSheetName)
                .toList();
    }

    private boolean tieneFilaConDatos(Sheet hoja, DataFormatter formato) {
        for (Row fila : hoja) {
            if (ContenidoHelper.filaConDatos(celdas(fila, formato))) {
                return true;
            }
        }
        return false;
    }

    private Resumen resumenDeHoja(String nombre, Sheet hoja, DataFormatter formato) {
        AcumuladorFilas acumulador = new AcumuladorFilas();
        for (Row fila : hoja) {
            acumulador.agregar(celdas(fila, formato));
        }
        return new Resumen(nombre, hoja.getSheetName(), acumulador.cantidad(),
                VistaPreviaHelper.recortar(acumulador.primeras()));
    }

    /** Desde la columna A, con "" en las celdas inexistentes, para que las columnas queden alineadas. */
    private List<String> celdas(Row fila, DataFormatter formato) {
        List<String> celdas = new ArrayList<>();
        for (int columna = 0; columna < fila.getLastCellNum(); columna++) {
            celdas.add(texto(fila.getCell(columna), formato));
        }
        return celdas;
    }

    private String texto(Cell celda, DataFormatter formato) {
        if (celda == null) {
            return "";
        }
        return celda.getCellType() == CellType.FORMULA ? resultadoGuardado(celda, formato) : formato.formatCellValue(celda);
    }

    /**
     * D9: de una fórmula se muestra el resultado que el archivo trae guardado, no la fórmula ni un recálculo.
     * Sin evaluador, DataFormatter devolvería el texto de la fórmula.
     */
    private String resultadoGuardado(Cell celda, DataFormatter formato) {
        return switch (celda.getCachedFormulaResultType()) {
            case NUMERIC -> formato.formatRawCellContents(celda.getNumericCellValue(),
                    celda.getCellStyle().getDataFormat(), celda.getCellStyle().getDataFormatString());
            case STRING -> celda.getStringCellValue();
            case BOOLEAN -> String.valueOf(celda.getBooleanCellValue()).toUpperCase(Locale.ROOT);
            case ERROR -> FormulaError.forInt(celda.getErrorCellValue()).getString();
            default -> "";
        };
    }
}
