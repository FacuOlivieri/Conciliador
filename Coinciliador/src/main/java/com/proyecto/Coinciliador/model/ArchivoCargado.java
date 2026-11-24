package com.proyecto.Coinciliador.model;

import com.proyecto.Coinciliador.enums.FormatoArchivo;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Archivo aceptado, con lo necesario para releerlo sin volver a subirlo (RF-17).
 * Los bytes no se copian a propósito: duplicar hasta 10 MB por archivo no aporta nada en una app local.
 * En un CSV, hojasListables está vacía; en un Excel, separador y codificacion son null.
 */
public record ArchivoCargado(
        String nombre,
        byte[] bytes,
        FormatoArchivo formato,
        String huella,
        List<String> hojasListables,
        Character separador,
        Charset codificacion) {

    public static ArchivoCargado csv(String nombre, byte[] bytes, String huella, char separador, Charset codificacion) {
        return new ArchivoCargado(nombre, bytes, FormatoArchivo.CSV, huella, List.of(), separador, codificacion);
    }

    public static ArchivoCargado excel(String nombre, byte[] bytes, String huella, List<String> hojasListables) {
        return new ArchivoCargado(nombre, bytes, FormatoArchivo.EXCEL, huella, List.copyOf(hojasListables), null, null);
    }
}
