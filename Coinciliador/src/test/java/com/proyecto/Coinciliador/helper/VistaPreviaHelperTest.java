package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

// Cubre RF-18: la vista previa se recorta a 10 filas, 20 columnas y 50 caracteres por celda.
class VistaPreviaHelperTest {

    @Test
    void dejaLasPrimerasDiezFilas() {
        List<List<String>> filas = IntStream.rangeClosed(1, 15)
                .mapToObj(numero -> List.of("fila " + numero))
                .toList();

        assertThat(VistaPreviaHelper.recortar(filas)).isEqualTo(filas.subList(0, 10));
    }

    @Test
    void dejaLasPrimerasVeinteColumnas() {
        List<String> fila = IntStream.rangeClosed(1, 25)
                .mapToObj(numero -> "columna " + numero)
                .toList();

        assertThat(VistaPreviaHelper.recortar(List.of(fila)))
                .containsExactly(fila.subList(0, 20));
    }

    @Test
    void recortaUnaCeldaLargaACincuentaCaracteresTerminadosEnPuntosSuspensivos() {
        List<List<String>> filas = List.of(List.of("a".repeat(60)));

        String celda = VistaPreviaHelper.recortar(filas).get(0).get(0);

        assertThat(celda).hasSize(50).isEqualTo("a".repeat(49) + "…");
    }

    @Test
    void noCambiaLasCeldasCortasNiLasDeCincuentaCaracteres() {
        List<List<String>> filas = List.of(
                List.of("Fecha", "", "a".repeat(50)),
                Collections.singletonList("100"));

        assertThat(VistaPreviaHelper.recortar(filas)).isEqualTo(filas);
    }
}
