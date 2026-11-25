package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.model.EstadoLugar;

/** Analiza un archivo subido, sea CSV o Excel, y devuelve el estado que le corresponde (RF-3, RF-21). */
public interface IAnalizadorArchivoService {

    /**
     * @throws ArchivoRechazadoException si el archivo no se puede aceptar (M3 a M6)
     */
    EstadoLugar analizar(String nombre, byte[] bytes);
}
