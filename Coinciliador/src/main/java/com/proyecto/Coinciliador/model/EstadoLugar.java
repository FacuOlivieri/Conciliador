package com.proyecto.Coinciliador.model;

import com.proyecto.Coinciliador.enums.MensajeCarga;

/**
 * Situación de un lugar de carga (Propio o Contraparte). Sellada: el compilador conoce
 * todos los estados y obliga a contemplarlos en cada switch (RF-13, RF-15, RF-16, RF-18).
 */
public sealed interface EstadoLugar {
    record Vacio() implements EstadoLugar {}
    record Rechazado(String nombreIntentado, MensajeCarga mensaje) implements EstadoLugar {}
    record PendienteDeHoja(ArchivoCargado archivo) implements EstadoLugar {}
    record Valido(ArchivoCargado archivo, Resumen resumen) implements EstadoLugar {}
}
