package com.proyecto.Coinciliador.exception;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.service.ICargaService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Un archivo por encima del tope técnico de 30 MB se corta antes de llegar al controlador. Se muestra el mismo M2
 * en la tarjeta del lugar, deducido de la URL (RF-5, RF-13). El nombre no se conoce: el archivo no se llegó a leer.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ManejadorGlobalExcepciones {

    private final ICargaService servicio;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String archivoDemasiadoGrande(HttpServletRequest peticion) {
        Arrays.stream(Lugar.values())
                .filter(lugar -> peticion.getRequestURI().endsWith("/carga/" + lugar.name()))
                .findFirst()
                .ifPresent(lugar -> servicio.registrarRechazo(lugar, "", MensajeCarga.M2));
        return "redirect:/carga";
    }
}
