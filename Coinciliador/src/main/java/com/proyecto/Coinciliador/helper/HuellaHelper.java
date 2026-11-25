package com.proyecto.Coinciliador.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Función pura que calcula la huella de un archivo para detectar que Propio y Contraparte son el mismo (RF-20). */
public final class HuellaHelper {

    private HuellaHelper() {
    }

    /** SHA-256 de los bytes, en hexadecimal minúscula. */
    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
