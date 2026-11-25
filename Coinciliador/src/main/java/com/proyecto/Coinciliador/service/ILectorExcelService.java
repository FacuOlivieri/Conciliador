package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.model.EstadoLugar;

/** Lee un archivo Excel (.xls o .xlsx) y decide su estado según sus hojas listables (RF-6, RF-14, RF-15). */
public interface ILectorExcelService {

    /**
     * Devuelve {@link EstadoLugar.Valido} si hay una sola hoja listable y
     * {@link EstadoLugar.PendienteDeHoja} si hay varias.
     *
     * @throws ArchivoRechazadoException con M3 (sin hojas listables), M5 (dañado) o M6 (cifrado)
     */
    EstadoLugar leer(String nombre, byte[] bytes);
}
