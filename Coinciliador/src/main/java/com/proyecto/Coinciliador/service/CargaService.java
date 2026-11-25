package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Orquesta la subida de archivos: analiza, traduce un rechazo en estado del lugar y guarda.
 * No escribe en los registros el nombre ni el contenido del archivo (RNF-3).
 */
@Service
@RequiredArgsConstructor
public class CargaService implements ICargaService {

    private final IAnalizadorArchivoService analizador;
    private final ICargaRepository repositorio;

    @Override
    public void subir(Lugar lugar, String nombre, byte[] bytes) {
        repositorio.guardar(lugar, analizar(nombre, bytes));
    }

    @Override
    public void registrarRechazo(Lugar lugar, String nombre, MensajeCarga mensaje) {
        repositorio.guardar(lugar, new EstadoLugar.Rechazado(nombre, mensaje));
    }

    private EstadoLugar analizar(String nombre, byte[] bytes) {
        try {
            return analizador.analizar(nombre, bytes);
        } catch (ArchivoRechazadoException e) {
            return new EstadoLugar.Rechazado(nombre, e.getMensajeCarga());
        }
    }
}
