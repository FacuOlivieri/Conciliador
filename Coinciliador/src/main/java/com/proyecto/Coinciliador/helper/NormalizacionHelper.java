package com.proyecto.Coinciliador.helper;

import java.text.Normalizer;
import java.util.Locale;

/** Funciones puras que dejan un texto listo para comparar tipos de comprobante (RF-3, RF-5, RF-10). */
public final class NormalizacionHelper {

    private NormalizacionHelper() {
    }

    /** Minúsculas, sin tildes ni diéresis (la ñ queda) y solo palabras de a-z, ñ y 0-9 separadas por un espacio. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return quitarTildes(texto.toLowerCase(Locale.ROOT))
                .replaceAll("[^a-zñ0-9]+", " ")
                .strip();
    }

    /** Quita los espacios de los extremos, también el espacio duro, que {@code strip()} deja. */
    public static String recortar(String texto) {
        return texto.replaceAll("(?U)^\\s+|\\s+$", "");
    }

    private static String quitarTildes(String texto) {
        String descompuesto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        // Borra todas las marcas menos la virgulilla de la ñ, que NFC vuelve a unir.
        String sinMarcas = descompuesto.replaceAll("(?!(?<=n)\\u0303)\\p{M}", "");
        return Normalizer.normalize(sinMarcas, Normalizer.Form.NFC);
    }
}
