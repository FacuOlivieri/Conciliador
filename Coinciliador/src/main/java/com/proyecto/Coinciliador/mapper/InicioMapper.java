package com.proyecto.Coinciliador.mapper;

import java.util.Arrays;
import java.util.List;

import com.proyecto.Coinciliador.dto.ConfirmacionCambioDto;
import com.proyecto.Coinciliador.dto.InicioDto;
import com.proyecto.Coinciliador.dto.TipoVistaDto;
import com.proyecto.Coinciliador.enums.MensajeInicio;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import org.springframework.stereotype.Component;

/** Convierte los tipos y los mensajes de la spec 002 en lo que las plantillas muestran (RF-2, RF-3, RF-7, RF-11). */
@Component
public class InicioMapper {

    public InicioDto aInicio() {
        return new InicioDto(tarjetas(), null);
    }

    public InicioDto aInicio(MensajeInicio aviso) {
        return new InicioDto(tarjetas(), aviso.getTexto());
    }

    public ConfirmacionCambioDto aConfirmacion(TipoConciliacion activo, TipoConciliacion elegido) {
        return new ConfirmacionCambioDto(
                activo.getNombre(), elegido.getNombre(), elegido.getSlug(), MensajeInicio.D1.getTexto());
    }

    private List<TipoVistaDto> tarjetas() {
        return Arrays.stream(TipoConciliacion.values())
                .map(tipo -> new TipoVistaDto(tipo.getSlug(), tipo.getNombre(), tipo.getAyuda()))
                .toList();
    }
}
