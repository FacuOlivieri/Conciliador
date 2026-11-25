package com.proyecto.Coinciliador.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import org.junit.jupiter.api.Test;

// Cubre el catálogo de mensajes de la spec 001 (RF-4 a RF-10, RF-20, RF-23, RNF-4).
class MensajeCargaTest {

    // Copia literal del catálogo de spec.md, sin el formato Markdown.
    private static final Map<String, String> CATALOGO = Map.ofEntries(
            Map.entry("M1", "Formato no permitido. Subí un archivo .csv, .xlsx o .xls."),
            Map.entry("M2", "El archivo supera el límite de 10 MB."),
            Map.entry("M3", "El archivo no contiene datos."),
            Map.entry("M4", "El archivo supera el máximo de 100.000 filas con datos."),
            Map.entry("M5", "No se pudo leer el archivo: está dañado o no es un CSV o Excel válido."),
            Map.entry("M6", "El archivo está protegido con contraseña. Quitale la contraseña desde Excel y volvé a subirlo."),
            Map.entry("M7", "Propio y Contraparte son el mismo archivo. Revisá si es lo que querés conciliar."),
            Map.entry("M8", "Archivos listos para conciliar."),
            Map.entry("M9", "falta cargar el archivo de %s"),
            Map.entry("M10", "el archivo de %s no es válido"),
            Map.entry("M11", "falta elegir la hoja de %s"));

    @Test
    void tieneExactamenteOnceMensajes() {
        assertThat(MensajeCarga.values()).hasSize(11);
    }

    @Test
    void cadaTextoCoincideConElCatalogoDeLaSpec() {
        CATALOGO.forEach((codigo, texto) -> assertThat(MensajeCarga.valueOf(codigo).getTexto()).isEqualTo(texto));
    }
}
