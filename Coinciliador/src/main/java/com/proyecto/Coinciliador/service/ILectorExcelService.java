package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.Resumen;

/** Lee un archivo Excel (.xls o .xlsx) y decide su estado según sus hojas listables (RF-6, RF-14, RF-15). */
public interface ILectorExcelService {

    /**
     * Devuelve {@link EstadoLugar.Valido} si hay una sola hoja listable y
     * {@link EstadoLugar.PendienteDeHoja} si hay varias.
     *
     * @throws ArchivoRechazadoException con M3 (sin hojas listables), M5 (dañado) o M6 (cifrado)
     */
    EstadoLugar leer(String nombre, byte[] bytes);

    /**
     * Arma el resumen de una hoja de un Excel ya aceptado, releyendo sus bytes: no hace falta volver a subirlo (RF-17).
     *
     * @throws ArchivoRechazadoException con M4 si la hoja tiene más de 100.000 filas con datos
     */
    Resumen resumirHoja(ArchivoCargado archivo, String hoja);
}
