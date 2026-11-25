package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.enums.ResultadoEleccion;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.helper.CargaEnCursoHelper;
import com.proyecto.Coinciliador.helper.EleccionTipoHelper;
import com.proyecto.Coinciliador.model.Carga;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Orquesta la elección del tipo de conciliación (RF-4, RF-6 a RF-9). No lee archivos: solo usa la carga guardada. */
@Service
@RequiredArgsConstructor
public class ConciliacionService implements IConciliacionService {

    private final ICargaRepository repositorio;

    @Override
    public ResultadoEleccion elegir(TipoConciliacion tipo) {
        Optional<TipoConciliacion> activo = repositorio.tipoActivo();
        ResultadoEleccion resultado = EleccionTipoHelper.decidir(
                activo.orElse(null), hayCargaEnCurso(repositorio.obtener()), tipo);
        if (resultado == ResultadoEleccion.ABRIR_CARGA && !activo.equals(Optional.of(tipo))) {
            repositorio.reiniciar(tipo);
        }
        return resultado;
    }

    @Override
    public void descartarYAbrir(TipoConciliacion tipo) {
        repositorio.reiniciar(tipo);
    }

    @Override
    public Optional<TipoConciliacion> tipoActivo() {
        return repositorio.tipoActivo();
    }

    private static boolean hayCargaEnCurso(Carga carga) {
        return CargaEnCursoHelper.hayCargaEnCurso(carga.propio(), carga.contraparte(), carga.confirmada());
    }
}
