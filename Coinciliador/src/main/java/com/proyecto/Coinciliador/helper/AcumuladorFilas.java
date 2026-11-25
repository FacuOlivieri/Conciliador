package com.proyecto.Coinciliador.helper;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Recibe las filas de a una: cuenta las que tienen datos, guarda las primeras 10
 * para la vista previa y rechaza con M4 al pasar las 100.000 (RF-6, RF-7, RF-18).
 * Tiene estado: se crea una instancia por lectura de archivo.
 */
public class AcumuladorFilas {

    private static final int MAXIMO_FILAS = 100_000;
    private static final int FILAS_GUARDADAS = 10;

    private final List<List<String>> primeras = new ArrayList<>();
    private int cantidad;

    public void agregar(List<String> fila) {
        if (!ContenidoHelper.filaConDatos(fila)) {
            return;
        }
        if (cantidad == MAXIMO_FILAS) {
            throw new ArchivoRechazadoException(MensajeCarga.M4);
        }
        cantidad++;
        if (primeras.size() < FILAS_GUARDADAS) {
            primeras.add(fila);
        }
    }

    public int cantidad() {
        return cantidad;
    }

    public List<List<String>> primeras() {
        return List.copyOf(primeras);
    }
}
