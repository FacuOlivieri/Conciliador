package com.proyecto.Coinciliador.helper;

import java.util.List;

/** Funciones puras que deciden si una celda o una fila tiene datos (definiciones de la spec, RF-6). */
public final class ContenidoHelper {

    private ContenidoHelper() {
    }

    /** Verdadero si la celda tiene al menos un carácter distinto de espacio. */
    public static boolean celdaConDatos(String celda) {
        return celda != null && !celda.isBlank();
    }

    /** Verdadero si al menos una celda de la fila tiene datos. */
    public static boolean filaConDatos(List<String> fila) {
        return fila.stream().anyMatch(ContenidoHelper::celdaConDatos);
    }
}
