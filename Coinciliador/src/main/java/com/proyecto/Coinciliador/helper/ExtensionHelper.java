package com.proyecto.Coinciliador.helper;

import java.util.Locale;
import java.util.Set;

/** Funciones puras sobre la extensión del nombre de un archivo (RF-3, RF-4). */
public final class ExtensionHelper {

    private static final Set<String> PERMITIDAS = Set.of("csv", "xlsx", "xls");

    private ExtensionHelper() {
    }

    /** Texto después del último punto, en minúsculas; vacío si no hay extensión. */
    public static String extension(String nombre) {
        if (nombre == null) {
            return "";
        }

        int indiceDelPunto = nombre.lastIndexOf('.');
        if (indiceDelPunto == -1) {
            return "";
        }

        return nombre.toLowerCase(Locale.ROOT).substring(indiceDelPunto + 1);
    }

    public static boolean esPermitida(String nombre) {
        return PERMITIDAS.contains(extension(nombre));
    }
}
