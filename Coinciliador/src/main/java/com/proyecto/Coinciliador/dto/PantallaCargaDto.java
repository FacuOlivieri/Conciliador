package com.proyecto.Coinciliador.dto;

/**
 * Todo lo que la pantalla de carga necesita, ya calculado: la plantilla no decide nada (regla 3).
 * textoFalta va vacío si Confirmar está habilitado; textoAdvertencia y mensajeFinal son null si no corresponden.
 */
public record PantallaCargaDto(
        String tituloTipo,
        LugarVistaDto propio,
        LugarVistaDto contraparte,
        boolean confirmarHabilitado,
        String textoFalta,
        boolean advertenciaMismoArchivo,
        String textoAdvertencia,
        boolean confirmada,
        String mensajeFinal) {
}
