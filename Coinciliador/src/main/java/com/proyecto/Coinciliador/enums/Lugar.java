package com.proyecto.Coinciliador.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Lugar {

    PROPIO("Propio"),
    CONTRAPARTE("Contraparte");

    private final String etiqueta;
}
