package com.proyecto.Coinciliador.helper;

import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.EstadoLugar.PendienteDeHoja;
import com.proyecto.Coinciliador.model.EstadoLugar.Rechazado;
import com.proyecto.Coinciliador.model.EstadoLugar.Valido;
import com.proyecto.Coinciliador.model.EstadoLugar.Vacio;

/** Función pura que dice si hay una carga en curso, según la definición de la spec 002 (RF-6, RF-7, RF-9). */
public final class CargaEnCursoHelper {

    private CargaEnCursoHelper() {
    }

    /**
     * Hay carga en curso si algún lugar tiene un archivo aceptado (aunque falte elegir la hoja)
     * o si la carga ya fue confirmada. Un lugar vacío o con un archivo rechazado no cuenta.
     */
    public static boolean hayCargaEnCurso(EstadoLugar propio, EstadoLugar contraparte, boolean confirmada) {
        return confirmada || tieneArchivoAceptado(propio) || tieneArchivoAceptado(contraparte);
    }

    // El switch sobre la interfaz sellada obliga a decidir cada estado nuevo que se agregue.
    private static boolean tieneArchivoAceptado(EstadoLugar estado) {
        return switch (estado) {
            case Vacio _, Rechazado _ -> false;
            case PendienteDeHoja _, Valido _ -> true;
        };
    }
}
