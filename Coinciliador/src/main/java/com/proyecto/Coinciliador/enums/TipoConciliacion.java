package com.proyecto.Coinciliador.enums;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Tipos de conciliación de la spec 002. Única fuente de la lista, su orden y sus frases de ayuda. */
@Getter
@RequiredArgsConstructor
public enum TipoConciliacion {

    IVA("IVA", "iva",
            "Cruzá tu libro de IVA con lo informado por AFIP."),
    VENTAS("Ventas", "ventas",
            "Cruzá tus registros de ventas con los de tus clientes."),
    COMPRAS("Compras", "compras",
            "Cruzá tus registros de compras con los de tus proveedores."),
    INGRESOS_BRUTOS("Ingresos Brutos", "ingresos-brutos",
            "Cruzá tus registros de Ingresos Brutos con los del organismo recaudador.");

    private final String nombre;
    private final String slug;
    private final String ayuda;

    /** Devuelve el tipo con ese slug, sin distinguir mayúsculas, o vacío si no existe. */
    public static Optional<TipoConciliacion> deSlug(String slug) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.slug.equalsIgnoreCase(slug))
                .findFirst();
    }
}
