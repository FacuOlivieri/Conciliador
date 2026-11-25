package com.proyecto.Coinciliador.dto;

import java.util.List;

/** Todo lo que necesita la página inicial, ya calculado. mensaje es null cuando no hay nada que avisar (RF-2, RF-3, RF-11). */
public record InicioDto(List<TipoVistaDto> tipos, String mensaje) {

    public InicioDto {
        tipos = List.copyOf(tipos);
    }
}
