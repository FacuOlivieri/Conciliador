package com.proyecto.Coinciliador.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Catálogo de mensajes de la página inicial (spec 002). Única fuente de estos textos. */
@Getter
@RequiredArgsConstructor
public enum MensajeInicio {

    P1("Ese tipo de conciliación no existe. Elegí uno de la lista."),
    D1("Si cambiás de tipo se descartan los archivos que ya subiste. ¿Querés continuar?");

    private final String texto;
}
