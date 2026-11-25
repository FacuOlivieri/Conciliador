package com.proyecto.Coinciliador.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.proyecto.Coinciliador.dto.ConfirmacionCambioDto;
import com.proyecto.Coinciliador.dto.InicioDto;
import com.proyecto.Coinciliador.dto.TipoVistaDto;
import com.proyecto.Coinciliador.enums.MensajeInicio;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import org.junit.jupiter.api.Test;

// Cubre lo que las plantillas de la spec 002 reciben ya calculado (RF-2, RF-3, RF-7, RF-11).
class InicioMapperTest {

    private final InicioMapper mapper = new InicioMapper();

    @Test
    void armaUnaTarjetaPorTipoEnElOrdenDeLaSpec() {
        InicioDto inicio = mapper.aInicio();

        assertThat(inicio.tipos()).extracting(TipoVistaDto::slug)
                .containsExactly("iva", "ventas", "compras", "ingresos-brutos");
        assertThat(inicio.tipos()).extracting(TipoVistaDto::nombre)
                .containsExactly("IVA", "Ventas", "Compras", "Ingresos Brutos");
    }

    @Test
    void cadaTarjetaTraeLaFraseDeAyudaDeSuTipo() {
        InicioDto inicio = mapper.aInicio();

        TipoVistaDto iva = inicio.tipos().getFirst();
        assertThat(iva.ayuda()).isEqualTo(TipoConciliacion.IVA.getAyuda());
        assertThat(inicio.tipos()).extracting(TipoVistaDto::ayuda)
                .containsExactlyElementsOf(java.util.Arrays.stream(TipoConciliacion.values())
                        .map(TipoConciliacion::getAyuda).toList());
    }

    @Test
    void sinAvisoNoHayMensaje() {
        assertThat(mapper.aInicio().mensaje()).isNull();
    }

    @Test
    void conAvisoMuestraElTextoDelMensaje() {
        InicioDto inicio = mapper.aInicio(MensajeInicio.P1);

        assertThat(inicio.mensaje()).isEqualTo(MensajeInicio.P1.getTexto());
        assertThat(inicio.tipos()).hasSize(4);
    }

    @Test
    void laConfirmacionNombraAmbosTiposYTraeElTextoD1() {
        ConfirmacionCambioDto confirmacion = mapper.aConfirmacion(
                TipoConciliacion.IVA, TipoConciliacion.INGRESOS_BRUTOS);

        assertThat(confirmacion.nombreTipoActivo()).isEqualTo("IVA");
        assertThat(confirmacion.nombreTipoElegido()).isEqualTo("Ingresos Brutos");
        assertThat(confirmacion.slugTipoElegido()).isEqualTo("ingresos-brutos");
        assertThat(confirmacion.texto()).isEqualTo(MensajeInicio.D1.getTexto());
    }
}
