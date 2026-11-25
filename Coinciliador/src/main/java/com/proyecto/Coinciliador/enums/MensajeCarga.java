package com.proyecto.Coinciliador.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Catálogo de mensajes de la spec 001. Única fuente de estos textos. */
@Getter
@RequiredArgsConstructor
public enum MensajeCarga {

    M1("Formato no permitido. Subí un archivo .csv, .xlsx o .xls."),
    M2("El archivo supera el límite de 10 MB."),
    M3("El archivo no contiene datos."),
    M4("El archivo supera el máximo de 100.000 filas con datos."),
    M5("No se pudo leer el archivo: está dañado o no es un CSV o Excel válido."),
    M6("El archivo está protegido con contraseña. Quitale la contraseña desde Excel y volvé a subirlo."),
    M7("Propio y Contraparte son el mismo archivo. Revisá si es lo que querés conciliar."),
    M8("Archivos listos para conciliar."),
    // M9 a M11 son fragmentos de "qué falta" (RF-22); %s es el nombre del lugar.
    M9("falta cargar el archivo de %s"),
    M10("el archivo de %s no es válido"),
    M11("falta elegir la hoja de %s");

    private final String texto;
}
