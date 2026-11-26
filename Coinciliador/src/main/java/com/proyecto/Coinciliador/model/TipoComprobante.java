package com.proyecto.Coinciliador.model;

import java.util.List;

/**
 * Un tipo de la tabla de comprobantes del Libro IVA Digital (RF-1).
 * nombreOficial va tal como figura en ARCA (RNF-2); abreviaturas puede estar vacía.
 */
public record TipoComprobante(int codigo, String nombreOficial, List<String> abreviaturas) {

    public TipoComprobante {
        abreviaturas = List.copyOf(abreviaturas);
    }
}
