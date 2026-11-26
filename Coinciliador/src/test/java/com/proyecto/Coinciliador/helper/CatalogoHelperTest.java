package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.proyecto.Coinciliador.model.TipoComprobante;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CatalogoHelperTest {

    @Test
    void deLineaLeeCodigoNombreYAbreviaturas() {
        assertThat(CatalogoHelper.deLinea("001\tFACTURAS A\tFactura A|FC A"))
                .isEqualTo(tipo(1, "FACTURAS A", "Factura A", "FC A"));
    }

    @Test
    void deLineaSinAbreviaturasDejaLaListaVacia() {
        assertThat(CatalogoHelper.deLinea("066\tDESPACHO DE IMPORTACION\t"))
                .isEqualTo(tipo(66, "DESPACHO DE IMPORTACION"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"001\tFACTURAS A", "001\tFACTURAS A\tFC A\tFA", "A\tFACTURAS A\t"})
    void deLineaRechazaUnaLineaMalArmada(String linea) {
        assertThatIllegalArgumentException().isThrownBy(() -> CatalogoHelper.deLinea(linea));
    }

    @Test
    void validarAceptaNombresOficialesRepetidos() {
        List<TipoComprobante> tipos = List.of(
                tipo(1, "COMPROBANTE REPETIDO", "CR1"),
                tipo(2, "COMPROBANTE REPETIDO"),
                // repetir una forma dentro del mismo tipo no es ambiguo
                tipo(3, "TIQUE", "Tique", "N/C A", "N C A"));

        assertThat(CatalogoHelper.validar(tipos)).isEmpty();
    }

    @Test
    void validarEncuentraUnCodigoRepetido() {
        assertThat(CatalogoHelper.validar(List.of(tipo(1, "FACTURAS A"), tipo(1, "FACTURAS B"))))
                .containsExactly("Código repetido: 1");
    }

    @Test
    void validarEncuentraUnNombreSinLetras() {
        assertThat(CatalogoHelper.validar(List.of(tipo(1, "123 -"))))
                .containsExactly("Sin letras en el código 1: \"123 -\"");
    }

    @Test
    void validarEncuentraUnaAbreviaturaSinLetras() {
        assertThat(CatalogoHelper.validar(List.of(tipo(1, "FACTURAS A", "FA", "..."))))
                .containsExactly("Sin letras en el código 1: \"...\"");
    }

    @Test
    void validarEncuentraUnaAbreviaturaRepetidaEnOtroTipo() {
        List<TipoComprobante> tipos = List.of(tipo(1, "FACTURAS A", "Fact. A"), tipo(6, "FACTURAS B", "FACT A"));

        assertThat(CatalogoHelper.validar(tipos)).containsExactly(
                "Abreviatura que también es de otro tipo, en el código 1: \"Fact. A\"",
                "Abreviatura que también es de otro tipo, en el código 6: \"FACT A\"");
    }

    @Test
    void validarEncuentraUnaAbreviaturaIgualAlNombreDeOtroTipo() {
        List<TipoComprobante> tipos = List.of(tipo(1, "FACTURAS A"), tipo(6, "FACTURAS B", "Facturas A"));

        assertThat(CatalogoHelper.validar(tipos))
                .containsExactly("Abreviatura que también es de otro tipo, en el código 6: \"Facturas A\"");
    }

    private static TipoComprobante tipo(int codigo, String nombre, String... abreviaturas) {
        return new TipoComprobante(codigo, nombre, List.of(abreviaturas));
    }
}
