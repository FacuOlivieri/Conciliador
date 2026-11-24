package com.proyecto.Coinciliador.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

// Cubre RF-1: dos lugares de carga, Propio y Contraparte.
class LugarTest {

    @Test
    void hayDosLugares() {
        assertThat(Lugar.values()).containsExactly(Lugar.PROPIO, Lugar.CONTRAPARTE);
    }

    @Test
    void cadaLugarTieneSuEtiquetaParaLaVista() {
        assertThat(Lugar.PROPIO.getEtiqueta()).isEqualTo("Propio");
        assertThat(Lugar.CONTRAPARTE.getEtiqueta()).isEqualTo("Contraparte");
    }
}
