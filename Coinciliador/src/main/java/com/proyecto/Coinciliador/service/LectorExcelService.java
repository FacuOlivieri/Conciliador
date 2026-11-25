package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

/**
 * Lee un Excel con POI. Por ahora solo sabe abrirlo (RF-6, RF-9, RF-10, RF-11);
 * las hojas listables y el resumen se suman en T10 y T11, junto con su interfaz.
 */
@Service
public class LectorExcelService {

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
}
