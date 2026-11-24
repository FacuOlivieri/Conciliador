package com.proyecto.Coinciliador.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Los dos lugares de carga de una conciliación (RF-1). */
@Getter
@RequiredArgsConstructor
public enum Lugar {

    PROPIO("Propio"),
    CONTRAPARTE("Contraparte");

    private final String etiqueta;
}
