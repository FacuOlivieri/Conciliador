package com.proyecto.Coinciliador.dto;

import com.proyecto.Coinciliador.model.Resumen;
import java.util.List;

/**
 * Todo lo que la plantilla necesita de un lugar de carga (Propio o Contraparte), ya calculado (RF-13, RF-15, RF-17, RF-18).
 * Los campos que no corresponden al estado quedan en null, vacíos o en cero.
 */
public record LugarVistaDto(
        String etiqueta,
        Estado estado,
        String nombreArchivo,
        String mensajeRechazo,
        List<String> hojas,
        boolean mostrarSelectorHoja,
        String hojaUsada,
        int cantidadFilas,
        List<List<String>> vistaPrevia) {

    public enum Estado { VACIO, RECHAZADO, PENDIENTE_DE_HOJA, VALIDO }

    public static LugarVistaDto vacio(String etiqueta) {
        return new LugarVistaDto(etiqueta, Estado.VACIO, null, null, List.of(), false, null, 0, List.of());
    }

    public static LugarVistaDto rechazado(String etiqueta, String nombreIntentado, String mensaje) {
        return new LugarVistaDto(etiqueta, Estado.RECHAZADO, nombreIntentado, mensaje, List.of(), false, null, 0, List.of());
    }

    /** Excel con varias hojas listables y ninguna elegida: se muestran las opciones, sin preselección (RF-15). */
    public static LugarVistaDto pendienteDeHoja(String etiqueta, String nombre, List<String> hojas) {
        return new LugarVistaDto(etiqueta, Estado.PENDIENTE_DE_HOJA, nombre, null, hojas, true, null, 0, List.of());
    }

    /** El selector solo se ofrece si hay más de una hoja listable para cambiar (RF-17). */
    public static LugarVistaDto valido(String etiqueta, List<String> hojasListables, Resumen resumen) {
        return new LugarVistaDto(etiqueta, Estado.VALIDO, resumen.nombre(), null, hojasListables,
                hojasListables.size() > 1, resumen.hoja(), resumen.cantidadFilas(), resumen.vistaPrevia());
    }
}
