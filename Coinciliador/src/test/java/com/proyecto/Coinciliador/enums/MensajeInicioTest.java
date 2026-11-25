package com.proyecto.Coinciliador.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

// Cubre los mensajes de la página inicial de la spec 002 (RF-7, RF-11, RNF-3).
class MensajeInicioTest {

    @Test
    void tieneExactamenteDosMensajes() {
        assertThat(MensajeInicio.values()).containsExactly(MensajeInicio.P1, MensajeInicio.D1);
    }

    @Test
    void cadaTextoCoincideConElCatalogoDeLaSpec() {
        assertThat(MensajeInicio.P1.getTexto())
                .isEqualTo("Ese tipo de conciliación no existe. Elegí uno de la lista.");
        assertThat(MensajeInicio.D1.getTexto())
                .isEqualTo("Si cambiás de tipo se descartan los archivos que ya subiste. ¿Querés continuar?");
    }
}
