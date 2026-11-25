package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

// Cubre RF-8 (texto o binario) y RF-12 (codificación y BOM del CSV).
class CodificacionHelperTest {

    private static final String TEXTO = "Factura;Importe;Señor Ñandú;áéíóú";
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    @Test
    void detectaUtf8CuandoDecodificaSinErrores() {
        byte[] bytes = TEXTO.getBytes(StandardCharsets.UTF_8);

        assertThat(CodificacionHelper.detectar(bytes)).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    void detectaWindows1252CuandoNoEsUtf8Valido() {
        byte[] bytes = TEXTO.getBytes(WINDOWS_1252);

        assertThat(CodificacionHelper.detectar(bytes)).isEqualTo(WINDOWS_1252);
    }

    @Test
    void unTextoSoloAsciiSeConsideraUtf8() {
        assertThat(CodificacionHelper.detectar("a;b;c".getBytes(StandardCharsets.US_ASCII)))
                .isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    void unTextoUtf8ConBomSeConsideraUtf8() {
        byte[] bytes = ("﻿" + TEXTO).getBytes(StandardCharsets.UTF_8);

        assertThat(CodificacionHelper.detectar(bytes)).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    void losBytesConCeroNoSonTexto() {
        assertThat(CodificacionHelper.esTexto(new byte[] {'a', 0x00, 'b'})).isFalse();
    }

    @Test
    void losBytesSinCeroSonTexto() {
        assertThat(CodificacionHelper.esTexto(TEXTO.getBytes(WINDOWS_1252))).isTrue();
    }

    @Test
    void quitaElBomInicial() {
        assertThat(CodificacionHelper.quitarBom("﻿Fecha;Importe")).isEqualTo("Fecha;Importe");
    }

    @Test
    void noCambiaUnTextoSinBom() {
        assertThat(CodificacionHelper.quitarBom("Fecha;Importe")).isEqualTo("Fecha;Importe");
    }
}
