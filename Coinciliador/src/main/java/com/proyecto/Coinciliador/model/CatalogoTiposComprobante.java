package com.proyecto.Coinciliador.model;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * El catálogo de tipos con sus dos índices ya armados (RF-1, RF-3, RF-5, RF-9). Es inmutable.
 * porCodigo usa el código sin ceros adelante; porForma, la forma normalizada de nombres y abreviaturas.
 */
public record CatalogoTiposComprobante(
        Map<String, TipoComprobante> porCodigo, Map<String, List<TipoComprobante>> porForma) {

    public CatalogoTiposComprobante {
        porCodigo = Map.copyOf(porCodigo);
        porForma = porForma.entrySet().stream()
                .collect(toUnmodifiableMap(Map.Entry::getKey, entrada -> List.copyOf(entrada.getValue())));
    }

    public Optional<TipoComprobante> buscarCodigo(String codigo) {
        return Optional.ofNullable(porCodigo.get(codigo));
    }

    /** Los tipos que se escriben con esa forma normalizada; lista vacía si no hay ninguno. */
    public List<TipoComprobante> buscarForma(String forma) {
        return porForma.getOrDefault(forma, List.of());
    }
}
