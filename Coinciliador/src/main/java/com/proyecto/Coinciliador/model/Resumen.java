package com.proyecto.Coinciliador.model;

import java.util.List;

/**
 * Resumen que ve el contador de un archivo válido (RF-18).
 * hoja es null cuando el archivo es un CSV; vistaPrevia ya viene recortada.
 */
public record Resumen(String nombre, String hoja, int cantidadFilas, List<List<String>> vistaPrevia) {

    public Resumen {
        vistaPrevia = List.copyOf(vistaPrevia);
    }
}
