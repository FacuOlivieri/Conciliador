package com.proyecto.Coinciliador.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

// Cubre la lista fija de tipos de la spec 002 (RF-2, RF-3, RF-11).
class TipoConciliacionTest {

    // Copia literal del catálogo de textos de spec.md, sin el formato Markdown.
    private static final Map<TipoConciliacion, String> AYUDAS = Map.of(
            TipoConciliacion.IVA, "Cruzá tu libro de IVA con lo informado por AFIP.",
            TipoConciliacion.VENTAS, "Cruzá tus registros de ventas con los de tus clientes.",
            TipoConciliacion.COMPRAS, "Cruzá tus registros de compras con los de tus proveedores.",
            TipoConciliacion.INGRESOS_BRUTOS, "Cruzá tus registros de Ingresos Brutos con los del organismo recaudador.");

    @Test
    void tieneExactamenteCuatroTiposEnElOrdenDeLaSpec() {
        assertThat(TipoConciliacion.values()).containsExactly(
                TipoConciliacion.IVA,
                TipoConciliacion.VENTAS,
                TipoConciliacion.COMPRAS,
                TipoConciliacion.INGRESOS_BRUTOS);
    }

    @Test
    void cadaTipoTieneSuNombreVisible() {
        assertThat(TipoConciliacion.IVA.getNombre()).isEqualTo("IVA");
        assertThat(TipoConciliacion.VENTAS.getNombre()).isEqualTo("Ventas");
        assertThat(TipoConciliacion.COMPRAS.getNombre()).isEqualTo("Compras");
        assertThat(TipoConciliacion.INGRESOS_BRUTOS.getNombre()).isEqualTo("Ingresos Brutos");
    }

    @Test
    void cadaFraseDeAyudaCoincideConElCatalogoDeLaSpec() {
        AYUDAS.forEach((tipo, ayuda) -> assertThat(tipo.getAyuda()).isEqualTo(ayuda));
    }

    @Test
    void cadaTipoTieneSuSlugParaLaUrl() {
        assertThat(TipoConciliacion.IVA.getSlug()).isEqualTo("iva");
        assertThat(TipoConciliacion.VENTAS.getSlug()).isEqualTo("ventas");
        assertThat(TipoConciliacion.COMPRAS.getSlug()).isEqualTo("compras");
        assertThat(TipoConciliacion.INGRESOS_BRUTOS.getSlug()).isEqualTo("ingresos-brutos");
    }

    @Test
    void deSlugEncuentraElTipoSinDistinguirMayusculas() {
        assertThat(TipoConciliacion.deSlug("iva")).contains(TipoConciliacion.IVA);
        assertThat(TipoConciliacion.deSlug("IVA")).contains(TipoConciliacion.IVA);
        assertThat(TipoConciliacion.deSlug("ingresos-brutos")).contains(TipoConciliacion.INGRESOS_BRUTOS);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "monotributo", "ingresos_brutos", "iva "})
    void deSlugDevuelveVacioSiElTextoNoEsUnTipo(String slug) {
        Optional<TipoConciliacion> tipo = TipoConciliacion.deSlug(slug);

        assertThat(tipo).isEmpty();
    }
}
