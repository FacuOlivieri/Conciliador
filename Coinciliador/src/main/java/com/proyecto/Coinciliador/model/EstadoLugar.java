package com.proyecto.Coinciliador.model;

/**
 * Situación de un lugar de carga (Propio o Contraparte). Sellada: el compilador conoce
 * todos los estados y obliga a contemplarlos en cada switch (RF-13, RF-15, RF-16, RF-18).
 */
public sealed interface EstadoLugar {

    /** Todavía no se subió ningún archivo. */
    record Vacio() implements EstadoLugar {}

    /** El último archivo subido se rechazó; el nombre se muestra en pantalla, nunca en logs (RNF-3). */
    record Rechazado(String nombreIntentado, MensajeCarga mensaje) implements EstadoLugar {}

    /** Excel con varias hojas listables y ninguna elegida: todavía no es válido (RF-16). */
    record PendienteDeHoja(ArchivoCargado archivo) implements EstadoLugar {}

    /** Archivo aceptado y listo, con su resumen (RF-18). */
    record Valido(ArchivoCargado archivo, Resumen resumen) implements EstadoLugar {}
}
