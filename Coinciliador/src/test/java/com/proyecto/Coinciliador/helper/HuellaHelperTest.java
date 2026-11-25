package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

// Cubre RF-20: la huella SHA-256 permite detectar que Propio y Contraparte son el mismo archivo.
class HuellaHelperTest {

    @Test
    void calculaElSha256ConocidoDeUnTextoFijo() {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);

        assertThat(HuellaHelper.sha256(bytes))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void dosArreglosIgualesDanLaMismaHuella() {
        byte[] uno = {1, 2, 3};
        byte[] otro = {1, 2, 3};

        assertThat(HuellaHelper.sha256(uno)).isEqualTo(HuellaHelper.sha256(otro));
    }

    @Test
    void arreglosDistintosDanHuellasDistintas() {
        assertThat(HuellaHelper.sha256(new byte[] {1})).isNotEqualTo(HuellaHelper.sha256(new byte[] {2}));
    }
}
