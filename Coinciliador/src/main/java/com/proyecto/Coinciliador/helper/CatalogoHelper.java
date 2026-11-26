package com.proyecto.Coinciliador.helper;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

import com.proyecto.Coinciliador.model.TipoComprobante;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** Funciones puras que leen y validan el catálogo de tipos de comprobante (RF-1, RF-2). */
public final class CatalogoHelper {

    private CatalogoHelper() {
    }

    /** Lee una línea {@code código⇥nombre oficial⇥abreviaturas separadas por |}. */
    public static TipoComprobante deLinea(String linea) {
        String[] columnas = linea.split("\t", -1);
        if (columnas.length != 3) {
            throw new IllegalArgumentException("La línea del catálogo no tiene 3 columnas: " + linea);
        }
        List<String> abreviaturas = columnas[2].isEmpty() ? List.of() : List.of(columnas[2].split("\\|"));
        return new TipoComprobante(Integer.parseInt(columnas[0]), columnas[1], abreviaturas);
    }

    /** Errores que incumplen RF-2; lista vacía si el catálogo es válido. */
    public static List<String> validar(List<TipoComprobante> tipos) {
        List<String> errores = new ArrayList<>(codigosRepetidos(tipos));
        errores.addAll(textosSinLetras(tipos));
        errores.addAll(abreviaturasAmbiguas(tipos));
        return errores;
    }

    private static List<String> codigosRepetidos(List<TipoComprobante> tipos) {
        Set<Integer> vistos = new HashSet<>();
        return tipos.stream()
                .filter(tipo -> !vistos.add(tipo.codigo()))
                .map(tipo -> "Código repetido: " + tipo.codigo())
                .toList();
    }

    private static List<String> textosSinLetras(List<TipoComprobante> tipos) {
        return tipos.stream()
                .flatMap(tipo -> textos(tipo)
                        .filter(texto -> !NormalizacionHelper.normalizar(texto).matches(".*[a-zñ].*"))
                        .map(texto -> error("Sin letras", tipo, texto)))
                .toList();
    }

    /** Los nombres oficiales sí pueden repetirse entre tipos (RF-9); las abreviaturas no. */
    private static List<String> abreviaturasAmbiguas(List<TipoComprobante> tipos) {
        Map<String, Set<Integer>> codigosPorForma = codigosPorForma(tipos);
        return tipos.stream()
                .flatMap(tipo -> tipo.abreviaturas().stream()
                        .filter(texto -> codigosPorForma.get(NormalizacionHelper.normalizar(texto)).size() > 1)
                        .map(texto -> error("Abreviatura que también es de otro tipo,", tipo, texto)))
                .toList();
    }

    private static Map<String, Set<Integer>> codigosPorForma(List<TipoComprobante> tipos) {
        return tipos.stream()
                .flatMap(tipo -> textos(tipo)
                        .map(texto -> Map.entry(NormalizacionHelper.normalizar(texto), tipo.codigo())))
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toSet())));
    }

    private static Stream<String> textos(TipoComprobante tipo) {
        return Stream.concat(Stream.of(tipo.nombreOficial()), tipo.abreviaturas().stream());
    }

    private static String error(String problema, TipoComprobante tipo, String texto) {
        return problema + " en el código " + tipo.codigo() + ": \"" + texto + "\"";
    }
}
