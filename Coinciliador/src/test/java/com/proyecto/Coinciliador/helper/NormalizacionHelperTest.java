package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NormalizacionHelperTest {

    @ParameterizedTest
    @ValueSource(strings = {"Fact. A", "FACT  A", "fact-a", "Fáct A", "Fact.A", "Fact\tA", "Fact A",
            " (Fact/A)\r\n"})
    void lasDistintasEscriturasDanLaMismaForma(String valor) {
        assertThat(NormalizacionHelper.normalizar(valor)).isEqualTo("fact a");
    }

    @ParameterizedTest
    // la última escribe la ñ descompuesta: n seguida de la virgulilla suelta
    @ValueSource(strings = {"Caña", "CAÑA", "Caña"})
    void conservaLaEnie(String valor) {
        assertThat(NormalizacionHelper.normalizar(valor)).isEqualTo("caña");
    }

    @Test
    void quitaTildesYDieresis() {
        assertThat(NormalizacionHelper.normalizar("DÉBITO pingüino São")).isEqualTo("debito pinguino sao");
    }

    @Test
    void losSignosSeparanPalabras() {
        assertThat(NormalizacionHelper.normalizar("N/C A")).isEqualTo("n c a");
        assertThat(NormalizacionHelper.normalizar("1–Factura—A")).isEqualTo("1 factura a");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "...", "-", "   "})
    void sinLetrasNiDigitosQuedaVacia(String valor) {
        assertThat(NormalizacionHelper.normalizar(valor)).isEmpty();
    }

    @Test
    void nuloQuedaVacio() {
        assertThat(NormalizacionHelper.normalizar(null)).isEmpty();
    }

    @Test
    void separarOJuntarLetrasDaFormasDistintas() {
        assertThat(NormalizacionHelper.normalizar("FC E")).isEqualTo("fc e");
        assertThat(NormalizacionHelper.normalizar("FCE")).isEqualTo("fce");
    }

    @Test
    void recortarQuitaLosEspaciosDeLosExtremosYNoLosDelMedio() {
        assertThat(NormalizacionHelper.recortar("  \t1  A  \n")).isEqualTo("1  A");
        assertThat(NormalizacionHelper.recortar(" ")).isEmpty();
    }
}
