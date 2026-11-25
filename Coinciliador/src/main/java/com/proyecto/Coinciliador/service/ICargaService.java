package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;

/** Casos de uso de la carga de archivos (RF-2, RF-13, RF-19). Se completa en T15 con hoja y confirmación. */
public interface ICargaService {

    /**
     * Analiza el archivo y lo guarda en ese lugar, reemplazando lo que hubiera (RF-19). Si no sirve,
     * el lugar queda rechazado con su motivo. El otro lugar no se toca (RF-13).
     */
    void subir(Lugar lugar, String nombre, byte[] bytes);

    /** Guarda en ese lugar un rechazo que ya detectó la validación de entrada (extensión o tamaño, M1 o M2). */
    void registrarRechazo(Lugar lugar, String nombre, MensajeCarga mensaje);
}
