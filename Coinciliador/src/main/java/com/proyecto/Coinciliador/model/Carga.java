package com.proyecto.Coinciliador.model;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.TipoConciliacion;

/**
 * La carga actual: el estado de Propio y de Contraparte, si fue confirmada y su tipo de conciliación (RF-2, RF-24).
 * Es inmutable: cada cambio devuelve una carga nueva, así dos pestañas no se pisan un objeto compartido.
 * tipo es null hasta que se elige uno en la página inicial (spec 002).
 */
public record Carga(TipoConciliacion tipo, EstadoLugar propio, EstadoLugar contraparte, boolean confirmada) {

    /** Carga sin archivos ni mensajes de rechazo, sin confirmar y con ese tipo (RF-24). */
    public static Carga vacia(TipoConciliacion tipo) {
        return new Carga(tipo, new EstadoLugar.Vacio(), new EstadoLugar.Vacio(), false);
    }

    public EstadoLugar estado(Lugar lugar) {
        return lugar == Lugar.PROPIO ? propio : contraparte;
    }

    /** Cambia solo el estado de ese lugar y borra la confirmación: lo ya confirmado deja de valer. */
    public Carga conEstado(Lugar lugar, EstadoLugar estado) {
        return lugar == Lugar.PROPIO
                ? new Carga(tipo, estado, contraparte, false)
                : new Carga(tipo, propio, estado, false);
    }

    /** Verdadero si Propio y Contraparte están ambos en estado válido (RF-22). */
    public boolean ambosValidos() {
        return propio instanceof EstadoLugar.Valido && contraparte instanceof EstadoLugar.Valido;
    }

    public Carga confirmar() {
        return new Carga(tipo, propio, contraparte, true);
    }
}
