package com.proyecto.Coinciliador.mapper;

import com.proyecto.Coinciliador.dto.LugarVistaDto;
import com.proyecto.Coinciliador.dto.PantallaCargaDto;
import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.Carga;
import com.proyecto.Coinciliador.model.EstadoLugar;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Convierte la carga en lo que muestra la pantalla: título, estado de cada lugar, Confirmar, M7 y M8 (RF-18 a RF-24). */
@Component
public class PantallaCargaMapper {

    public PantallaCargaDto aPantalla(Carga carga) {
        boolean mismoArchivo = mismoArchivo(carga);
        return new PantallaCargaDto(
                carga.tipo() == null ? "" : carga.tipo().getNombre(),
                lugar(Lugar.PROPIO, carga.propio()),
                lugar(Lugar.CONTRAPARTE, carga.contraparte()),
                carga.ambosValidos(),
                textoFalta(carga),
                mismoArchivo,
                mismoArchivo ? MensajeCarga.M7.getTexto() : null,
                carga.confirmada(),
                carga.confirmada() ? MensajeCarga.M8.getTexto() : null);
    }

    private LugarVistaDto lugar(Lugar lugar, EstadoLugar estado) {
        String etiqueta = lugar.getEtiqueta();
        return switch (estado) {
            case EstadoLugar.Vacio vacio -> LugarVistaDto.vacio(etiqueta);
            case EstadoLugar.Rechazado r -> LugarVistaDto.rechazado(etiqueta, r.nombreIntentado(), r.mensaje().getTexto());
            case EstadoLugar.PendienteDeHoja p ->
                    LugarVistaDto.pendienteDeHoja(etiqueta, p.archivo().nombre(), p.archivo().hojasListables());
            case EstadoLugar.Valido v -> LugarVistaDto.valido(etiqueta, v.archivo().hojasListables(), v.resumen());
        };
    }

    /** Qué lugar falta o no es válido, para explicar por qué Confirmar está deshabilitado (RF-22). */
    private String textoFalta(Carga carga) {
        List<String> faltantes = new ArrayList<>();
        agregarFaltante(faltantes, Lugar.PROPIO, carga.propio());
        agregarFaltante(faltantes, Lugar.CONTRAPARTE, carga.contraparte());
        if (faltantes.isEmpty()) {
            return "";
        }
        String texto = String.join("; ", faltantes);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private void agregarFaltante(List<String> faltantes, Lugar lugar, EstadoLugar estado) {
        String etiqueta = lugar.getEtiqueta();
        switch (estado) {
            case EstadoLugar.Vacio vacio -> faltantes.add(MensajeCarga.M9.getTexto().formatted(etiqueta));
            case EstadoLugar.Rechazado rechazado -> faltantes.add(MensajeCarga.M10.getTexto().formatted(etiqueta));
            case EstadoLugar.PendienteDeHoja pendiente -> faltantes.add(MensajeCarga.M11.getTexto().formatted(etiqueta));
            case EstadoLugar.Valido valido -> { }
        }
    }

    /** Dos archivos con la misma huella son el mismo archivo, aunque se llamen distinto (RF-20). */
    private boolean mismoArchivo(Carga carga) {
        Optional<ArchivoCargado> propio = archivoDe(carga.propio());
        Optional<ArchivoCargado> contraparte = archivoDe(carga.contraparte());
        return propio.isPresent() && contraparte.isPresent()
                && propio.get().huella().equals(contraparte.get().huella());
    }

    private Optional<ArchivoCargado> archivoDe(EstadoLugar estado) {
        return switch (estado) {
            case EstadoLugar.PendienteDeHoja p -> Optional.of(p.archivo());
            case EstadoLugar.Valido v -> Optional.of(v.archivo());
            default -> Optional.empty();
        };
    }
}
