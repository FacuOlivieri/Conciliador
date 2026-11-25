package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.enums.ResultadoEleccion;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import java.util.Optional;

/** Casos de uso de la página inicial: elegir el tipo de conciliación, descartar la carga y consultar el tipo activo. */
public interface IConciliacionService {

    /** Dice qué pasaría al elegir ese tipo, sin cambiar nada: sirve para las páginas que se piden con GET. */
    ResultadoEleccion decidir(TipoConciliacion tipo);

    /** Decide qué pasa al elegir el tipo; solo cambia la carga si se puede abrir sin perder nada. */
    ResultadoEleccion elegir(TipoConciliacion tipo);

    /** Descarta la carga actual y abre una vacía del tipo nuevo; solo se llama tras confirmar (D1). */
    void descartarYAbrir(TipoConciliacion tipo);

    Optional<TipoConciliacion> tipoActivo();
}
