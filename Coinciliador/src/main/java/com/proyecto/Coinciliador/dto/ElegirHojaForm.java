package com.proyecto.Coinciliador.dto;

import jakarta.validation.constraints.NotBlank;

/** Formulario para elegir o cambiar la hoja de un Excel (RF-15, RF-17). */
public record ElegirHojaForm(@NotBlank(message = "Elegí una hoja.") String hoja) {
}
