package com.proyecto.Coinciliador.helper;

import com.proyecto.Coinciliador.enums.ResultadoEleccion;
import com.proyecto.Coinciliador.enums.TipoConciliacion;

/** Función pura que decide qué pasa cuando el contador elige un tipo de conciliación (RF-4, RF-6, RF-7). */
public final class EleccionTipoHelper {

    private EleccionTipoHelper() {
    }

    /**
     * Pide confirmación solo si hay carga en curso de otro tipo; en cualquier otro caso abre la carga.
     * tipoActivo es null mientras todavía no se eligió ningún tipo.
     */
    public static ResultadoEleccion decidir(TipoConciliacion tipoActivo, boolean hayCargaEnCurso,
            TipoConciliacion tipoElegido) {
        boolean cambiaDeTipo = tipoActivo != null && tipoActivo != tipoElegido;
        return hayCargaEnCurso && cambiaDeTipo
                ? ResultadoEleccion.PEDIR_CONFIRMACION
                : ResultadoEleccion.ABRIR_CARGA;
    }
}
