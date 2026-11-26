package com.proyecto.Coinciliador.helper;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/** Función pura que decide cómo mostrar una fecha de Excel: siempre dd/mm/aa, con la hora si la tenía (RF-18). */
public final class FechaExcelHelper {

    // Lo que no es parte de la máscara: texto entre comillas, [corchetes] y caracteres escapados o de relleno.
    private static final Pattern LITERALES = Pattern.compile("\"[^\"]*\"|\\[[^\\]]*\\]|[\\\\_*].");

    private FechaExcelHelper() {
    }

    /**
     * Patrón de Java para ese formato de Excel; vacío si no muestra una fecha (un número, una hora sola o un
     * formato desconocido). No se usa DateUtil de POI porque no reconoce las fechas con texto, como d "de" mmmm.
     */
    public static Optional<String> patron(String formatoExcel) {
        if (formatoExcel == null) {
            return Optional.empty();
        }
        String mascara = LITERALES.matcher(formatoExcel).replaceAll("").toLowerCase(Locale.ROOT);
        boolean tieneFecha = mascara.contains("y") || mascara.contains("d") || mascara.contains("mmm");
        if (!tieneFecha) {
            return Optional.empty();
        }
        boolean tieneHora = mascara.contains("h") || mascara.contains("s");
        return Optional.of(tieneHora ? "dd/MM/yy HH:mm" : "dd/MM/yy");
    }
}
