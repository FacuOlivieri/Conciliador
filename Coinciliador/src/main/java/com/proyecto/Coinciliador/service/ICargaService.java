package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.model.Carga;

/** Casos de uso de la carga de archivos (RF-2, RF-13, RF-19). Se completa en T15 con hoja y confirmación. */
public interface ICargaService {

    /** La carga tal como está ahora, para mostrarla. */
    Carga cargaActual();

    /**
     * Analiza el archivo y lo guarda en ese lugar, reemplazando lo que hubiera (RF-19). Si no sirve,
     * el lugar queda rechazado con su motivo. El otro lugar no se toca (RF-13).
     */
    void subir(Lugar lugar, String nombre, byte[] bytes);

    /** Guarda en ese lugar un rechazo que ya detectó la validación de entrada (extensión o tamaño, M1 o M2). */
    void registrarRechazo(Lugar lugar, String nombre, MensajeCarga mensaje);

    /**
     * Elige o cambia la hoja de un Excel ya cargado, sin volver a subirlo (RF-16, RF-17). Se ignora si el lugar no
     * tiene un Excel o la hoja no está entre las listables. Si la hoja supera el máximo de filas, el lugar queda rechazado.
     */
    void elegirHoja(Lugar lugar, String hoja);

    /** Confirma la carga solo si Propio y Contraparte son válidos (RF-22, RF-23). El control también es del servidor. */
    void confirmar();
}
