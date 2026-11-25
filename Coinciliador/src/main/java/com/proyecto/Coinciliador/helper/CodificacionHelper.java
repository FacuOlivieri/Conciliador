package com.proyecto.Coinciliador.helper;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/** Funciones puras para distinguir texto de binario y detectar la codificación de un CSV (RF-8, RF-12). */
public final class CodificacionHelper {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final char BOM = '﻿';

    private CodificacionHelper() {
    }

    /** Falso si algún byte vale 0x00: un CSV de texto nunca lo contiene (RF-8). */
    public static boolean esTexto(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0) {
                return false;
            }
        }
        return true;
    }

    /** UTF-8 si los bytes lo decodifican en modo estricto; si no, Windows-1252 (D6). */
    public static Charset detectar(byte[] bytes) {
        try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
            return StandardCharsets.UTF_8;
        } catch (CharacterCodingException e) {
            return WINDOWS_1252;
        }
    }

    /** Quita el BOM inicial, si lo hay, para que no ensucie el primer encabezado. */
    public static String quitarBom(String texto) {
        return !texto.isEmpty() && texto.charAt(0) == BOM ? texto.substring(1) : texto;
    }
}
