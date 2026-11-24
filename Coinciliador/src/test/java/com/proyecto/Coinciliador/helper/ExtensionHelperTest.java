package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

// Cubre RF-3 y RF-4: extensiones permitidas y casos límite de la spec 001.
class ExtensionHelperTest {

    @ParameterizedTest
    @CsvSource({
            "compras.csv,       csv",
            "VENTAS.XLSX,       xlsx",
            "Libro IVA.Xls,     xls",
            "datos.csv.xlsx,    xlsx",
            "planilla.ods,      ods"
    })
    void devuelveLoQueVieneDespuesDelUltimoPuntoEnMinusculas(String nombre, String esperada) {
        assertThat(ExtensionHelper.extension(nombre)).isEqualTo(esperada);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"sin_extension", "termina_en_punto."})
    void devuelveVacioSiNoHayExtension(String nombre) {
        assertThat(ExtensionHelper.extension(nombre)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a.csv", "a.CSV", "a.xlsx", "a.XLSX", "a.xls", "a.XLS", "datos.csv.xlsx"})
    void aceptaCsvXlsxYXlsSinImportarMayusculas(String nombre) {
        assertThat(ExtensionHelper.esPermitida(nombre)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"a.ods", "a.xlsm", "a.xlsb", "a.tsv", "sin_extension", "termina_en_punto.", "a.xlsx.pdf"})
    void rechazaCualquierOtraExtension(String nombre) {
        assertThat(ExtensionHelper.esPermitida(nombre)).isFalse();
    }
}
