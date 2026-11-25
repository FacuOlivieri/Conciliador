package com.proyecto.Coinciliador.repository;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.model.Carga;
import com.proyecto.Coinciliador.model.EstadoLugar;
import java.util.Optional;

/** Guarda la única carga de la aplicación (RF-2). */
public interface ICargaRepository {

    Carga obtener();

    /** Reemplaza solo el estado de ese lugar y borra la confirmación. */
    void guardar(Lugar lugar, EstadoLugar estado);

    void confirmar();

    /** El tipo de conciliación de la carga, o vacío si todavía no se eligió ninguno (spec 002). */
    Optional<TipoConciliacion> tipoActivo();

    /** Descarta todo: ambos lugares vacíos, sin confirmación y con ese tipo (RF-24). */
    void reiniciar(TipoConciliacion tipo);
}
