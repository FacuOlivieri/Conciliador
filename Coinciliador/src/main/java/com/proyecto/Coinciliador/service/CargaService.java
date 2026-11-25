package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.enums.FormatoArchivo;
import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.Carga;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Orquesta la carga: sube archivos, elige la hoja de un Excel y confirma. Traduce un rechazo en estado del lugar.
 * No escribe en los registros el nombre ni el contenido del archivo (RNF-3).
 */
@Service
@RequiredArgsConstructor
public class CargaService implements ICargaService {

    private final IAnalizadorArchivoService analizador;
    private final ILectorExcelService lectorExcel;
    private final ICargaRepository repositorio;

    @Override
    public Carga cargaActual() {
        return repositorio.obtener();
    }

    @Override
    public void subir(Lugar lugar, String nombre, byte[] bytes) {
        repositorio.guardar(lugar, analizar(nombre, bytes));
    }

    @Override
    public void registrarRechazo(Lugar lugar, String nombre, MensajeCarga mensaje) {
        repositorio.guardar(lugar, new EstadoLugar.Rechazado(nombre, mensaje));
    }

    @Override
    public void elegirHoja(Lugar lugar, String hoja) {
        excelElegible(repositorio.obtener().estado(lugar))
                .filter(archivo -> archivo.hojasListables().contains(hoja))
                .ifPresent(archivo -> repositorio.guardar(lugar, estadoConHoja(archivo, hoja)));
    }

    @Override
    public void confirmar() {
        if (repositorio.obtener().ambosValidos()) {
            repositorio.confirmar();
        }
    }

    private EstadoLugar analizar(String nombre, byte[] bytes) {
        try {
            return analizador.analizar(nombre, bytes);
        } catch (ArchivoRechazadoException e) {
            return new EstadoLugar.Rechazado(nombre, e.getMensajeCarga());
        }
    }

    /** El archivo del lugar, solo si es un Excel que admite elegir hoja: pendiente de hoja o ya válido. */
    private Optional<ArchivoCargado> excelElegible(EstadoLugar estado) {
        return switch (estado) {
            case EstadoLugar.PendienteDeHoja pendiente -> Optional.of(pendiente.archivo());
            case EstadoLugar.Valido valido when valido.archivo().formato() == FormatoArchivo.EXCEL ->
                    Optional.of(valido.archivo());
            default -> Optional.empty();
        };
    }

    private EstadoLugar estadoConHoja(ArchivoCargado archivo, String hoja) {
        try {
            return new EstadoLugar.Valido(archivo, lectorExcel.resumirHoja(archivo, hoja));
        } catch (ArchivoRechazadoException e) {
            return new EstadoLugar.Rechazado(archivo.nombre(), e.getMensajeCarga());
        }
    }
}
