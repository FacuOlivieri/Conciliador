package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.proyecto.Coinciliador.enums.ResultadoEleccion;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import org.junit.jupiter.api.Test;

// Cubre la decisión al elegir un tipo de la spec 002 (RF-4, RF-6, RF-7).
class EleccionTipoHelperTest {

    @Test
    void sinTipoActivoSeAbreLaCarga() {
        ResultadoEleccion resultado = EleccionTipoHelper.decidir(null, false, TipoConciliacion.IVA);

        assertThat(resultado).isEqualTo(ResultadoEleccion.ABRIR_CARGA);
    }

    @Test
    void elMismoTipoConCargaEnCursoAbreLaCargaSinConfirmar() {
        ResultadoEleccion resultado = EleccionTipoHelper.decidir(TipoConciliacion.IVA, true, TipoConciliacion.IVA);

        assertThat(resultado).isEqualTo(ResultadoEleccion.ABRIR_CARGA);
    }

    @Test
    void otroTipoSinCargaEnCursoAbreLaCarga() {
        ResultadoEleccion resultado = EleccionTipoHelper.decidir(TipoConciliacion.IVA, false, TipoConciliacion.COMPRAS);

        assertThat(resultado).isEqualTo(ResultadoEleccion.ABRIR_CARGA);
    }

    @Test
    void otroTipoConCargaEnCursoPideConfirmacion() {
        ResultadoEleccion resultado = EleccionTipoHelper.decidir(TipoConciliacion.IVA, true, TipoConciliacion.COMPRAS);

        assertThat(resultado).isEqualTo(ResultadoEleccion.PEDIR_CONFIRMACION);
    }
}
