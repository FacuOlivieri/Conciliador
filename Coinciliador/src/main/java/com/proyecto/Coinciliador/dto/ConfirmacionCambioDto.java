package com.proyecto.Coinciliador.dto;

/** Lo que muestra la pantalla que avisa que cambiar de tipo descarta los archivos subidos (RF-7). */
public record ConfirmacionCambioDto(
        String nombreTipoActivo,
        String nombreTipoElegido,
        String slugTipoElegido,
        String texto) {
}
