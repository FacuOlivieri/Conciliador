package com.proyecto.Coinciliador.helper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Función pura que detecta el separador de un CSV: ';', ',' o tabulación (RF-12, D7). */
public final class SeparadorHelper {

    // El orden importa: ante un empate gana el primero, ';' (lo usual en Argentina).
    private static final List<Character> CANDIDATOS = List.of(';', ',', '\t');
    private static final int LINEAS_A_MIRAR = 20;

    private SeparadorHelper() {
    }

    /** Gana el separador que más líneas tienen con la misma cantidad; empate → ';'. */
    public static char detectar(List<String> lineas) {
        List<String> muestra = lineas.stream()
                .filter(ContenidoHelper::celdaConDatos)
                .limit(LINEAS_A_MIRAR)
                .toList();
        char ganador = CANDIDATOS.get(0);
        for (char candidato : CANDIDATOS) {
            if (puntaje(muestra, candidato) > puntaje(muestra, ganador)) {
                ganador = candidato;
            }
        }
        return ganador;
    }

    /** Cantidad de líneas que repiten la misma cuenta (mayor a cero) del separador. */
    private static long puntaje(List<String> lineas, char separador) {
        return lineas.stream()
                .map(linea -> cuenta(linea, separador))
                .filter(cuenta -> cuenta > 0)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .values().stream()
                .max(Long::compare)
                .orElse(0L);
    }

    /** Cuántas veces aparece el separador en la línea, sin contar el que está entre comillas. */
    private static int cuenta(String linea, char separador) {
        boolean entreComillas = false;
        int cuenta = 0;
        for (char caracter : linea.toCharArray()) {
            if (caracter == '"') {
                entreComillas = !entreComillas;
            } else if (caracter == separador && !entreComillas) {
                cuenta++;
            }
        }
        return cuenta;
    }
}
