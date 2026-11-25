package com.proyecto.Coinciliador.dto;

import com.proyecto.Coinciliador.utils.validacion.TipoConciliacionValido;

/** Tipo de conciliación elegido, tal como llega en la URL (RF-11). */
public record EleccionTipoForm(@TipoConciliacionValido String slug) {
}
