package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.helper.AcumuladorFilas;
import com.proyecto.Coinciliador.helper.CodificacionHelper;
import com.proyecto.Coinciliador.helper.HuellaHelper;
import com.proyecto.Coinciliador.helper.SeparadorHelper;
import com.proyecto.Coinciliador.helper.VistaPreviaHelper;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.Resumen;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

/** Lee un CSV: texto o binario, codificación, BOM, separador, parseo con Commons CSV, conteo y vista previa. */
@Service
public class LectorCsvService implements ILectorCsvService {

    @Override
    public EstadoLugar.Valido leer(String nombre, byte[] bytes) {
        validarContenido(bytes);
        Charset codificacion = CodificacionHelper.detectar(bytes);
        String texto = CodificacionHelper.quitarBom(new String(bytes, codificacion));
        char separador = SeparadorHelper.detectar(texto);
        AcumuladorFilas acumulador = acumular(texto, separador);
        if (acumulador.cantidad() == 0) {
            throw new ArchivoRechazadoException(MensajeCarga.M3);
        }
        ArchivoCargado archivo = ArchivoCargado.csv(nombre, bytes, HuellaHelper.sha256(bytes), separador, codificacion);
        return new EstadoLugar.Valido(archivo, resumen(nombre, acumulador));
    }

    private void validarContenido(byte[] bytes) {
        if (bytes.length == 0) {
            throw new ArchivoRechazadoException(MensajeCarga.M3);
        }
        if (!CodificacionHelper.esTexto(bytes)) {
            throw new ArchivoRechazadoException(MensajeCarga.M5);
        }
    }

    private AcumuladorFilas acumular(String texto, char separador) {
        CSVFormat formato = CSVFormat.DEFAULT.builder().setDelimiter(separador).get();
        AcumuladorFilas acumulador = new AcumuladorFilas();
        try (CSVParser parser = formato.parse(new StringReader(texto))) {
            for (CSVRecord registro : parser) {
                acumulador.agregar(registro.toList());
            }
        } catch (IOException | UncheckedIOException e) {
            // Comillas sin cerrar u otro CSV mal formado. No se propaga la causa: puede traer datos del archivo (RNF-3).
            throw new ArchivoRechazadoException(MensajeCarga.M5);
        }
        return acumulador;
    }

    private Resumen resumen(String nombre, AcumuladorFilas acumulador) {
        return new Resumen(nombre, null, acumulador.cantidad(), VistaPreviaHelper.recortar(acumulador.primeras()));
    }
}
