package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.model.EstadoLugar;

/** Lee un archivo CSV y arma su estado válido (RF-6, RF-7, RF-8, RF-12, RF-18). */
public interface ILectorCsvService {

    /**
     * @throws ArchivoRechazadoException con M3 (vacío), M4 (demasiadas filas) o M5 (no es texto o está mal formado)
     */
    EstadoLugar.Valido leer(String nombre, byte[] bytes);
}
