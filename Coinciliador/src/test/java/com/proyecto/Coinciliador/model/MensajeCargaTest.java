package com.proyecto.Coinciliador.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

// Cubre el catálogo de mensajes de la spec 001 (RF-4 a RF-10, RF-20, RF-23, RNF-4).
class MensajeCargaTest {

    // Copia literal del catálogo de spec.md, sin el formato Markdown.
    private static final Map<MensajeCarga, String> CATALOGO = Map.of(
            MensajeCarga.M1, "Formato no permitido. Subí un archivo .csv, .xlsx o .xls.",
            MensajeCarga.M2, "El archivo supera el límite de 10 MB.",
            MensajeCarga.M3, "El archivo no contiene datos.",
            MensajeCarga.M4, "El archivo supera el máximo de 100.000 filas con datos.",
            MensajeCarga.M5, "No se pudo leer el archivo: está dañado o no es un CSV o Excel válido.",
            MensajeCarga.M6, "El archivo está protegido con contraseña. Quitale la contraseña desde Excel y volvé a subirlo.",
            MensajeCarga.M7, "Propio y Contraparte son el mismo archivo. Revisá si es lo que querés conciliar.",
            MensajeCarga.M8, "Archivos listos para conciliar.");

    @Test
    void tieneExactamenteOchoMensajes() {
        assertThat(MensajeCarga.values()).hasSize(8);
    }

    @Test
    void cadaTextoCoincideConElCatalogoDeLaSpec() {
        CATALOGO.forEach((mensaje, texto) -> assertThat(mensaje.getTexto()).isEqualTo(texto));
    }
}
