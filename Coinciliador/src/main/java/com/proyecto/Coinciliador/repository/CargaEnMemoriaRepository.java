package com.proyecto.Coinciliador.repository;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.model.Carga;
import com.proyecto.Coinciliador.model.EstadoLugar;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Única instancia (singleton de Spring) que guarda la carga en memoria: sin disco (RNF-1) y
 * compartida por todas las pestañas (RNF-2). Los métodos son sincronizados porque la guardan varias peticiones.
 */
@Repository
public class CargaEnMemoriaRepository implements ICargaRepository {

    private Carga carga = Carga.vacia(null);

    @Override
    public synchronized Carga obtener() {
        return carga;
    }

    @Override
    public synchronized void guardar(Lugar lugar, EstadoLugar estado) {
        carga = carga.conEstado(lugar, estado);
    }

    @Override
    public synchronized void confirmar() {
        carga = carga.confirmar();
    }

    @Override
    public synchronized Optional<TipoConciliacion> tipoActivo() {
        return Optional.ofNullable(carga.tipo());
    }

    @Override
    public synchronized void reiniciar(TipoConciliacion tipo) {
        carga = Carga.vacia(tipo);
    }
}
