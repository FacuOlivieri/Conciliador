package com.proyecto.Coinciliador.helper;

import java.util.List;

/** Funciones puras que recortan las filas para la vista previa (RF-18). */
public final class VistaPreviaHelper {

    private static final int MAXIMO_FILAS = 10;
    private static final int MAXIMO_COLUMNAS = 20;
    private static final int MAXIMO_CARACTERES = 50;
    
    private VistaPreviaHelper() {
    }

    /** Primeras 10 filas, con hasta 20 columnas cada una y las celdas recortadas. */
    public static List<List<String>> recortar(List<List<String>> filas) {
        return filas.stream()
                .limit(MAXIMO_FILAS)
                .map(VistaPreviaHelper::recortarFila)
                .toList();
    }

    private static List<String> recortarFila(List<String> fila) {
        return fila.stream()
                .limit(MAXIMO_COLUMNAS)
                .map(VistaPreviaHelper::recortarCelda)
                .toList();
    }

    private static String recortarCelda(String celda) {
        if (celda.length() > MAXIMO_CARACTERES) {
            return celda.substring(0, MAXIMO_CARACTERES - 1) + "…";

        }
        return celda;
    }
}
