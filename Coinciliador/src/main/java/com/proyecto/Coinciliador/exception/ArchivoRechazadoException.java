package com.proyecto.Coinciliador.exception;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import lombok.Getter;

/** Rechazo de un archivo con su motivo del catálogo (RF-4 a RF-10). Nunca lleva el nombre ni el contenido (RNF-3). */
@Getter
public class ArchivoRechazadoException extends RuntimeException {

    private final MensajeCarga mensajeCarga;

    public ArchivoRechazadoException(MensajeCarga mensajeCarga) {
        super(mensajeCarga.name());
        this.mensajeCarga = mensajeCarga;
    }
}
