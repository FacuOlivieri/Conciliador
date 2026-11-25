package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

// Cubre las definiciones "celda con datos" y "fila con datos" de la spec 001.
class ContenidoHelperTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "     "})
    void unaCeldaVaciaODeSoloEspaciosNoTieneDatos(String celda) {
        assertThat(ContenidoHelper.celdaConDatos(celda)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "  0  ", "#REF!"})
    void unaCeldaConAlgunCaracterDistintoDeEspacioTieneDatos(String celda) {
        assertThat(ContenidoHelper.celdaConDatos(celda)).isTrue();
    }

    @Test
    void unaFilaConUnaCeldaConDatosTieneDatos() {
        assertThat(ContenidoHelper.filaConDatos(List.of("", "   ", "100"))).isTrue();
    }

    @Test
    void unaFilaSinCeldasConDatosNoTieneDatos() {
        assertThat(ContenidoHelper.filaConDatos(List.of("", "   "))).isFalse();
        assertThat(ContenidoHelper.filaConDatos(List.of())).isFalse();
    }
}
