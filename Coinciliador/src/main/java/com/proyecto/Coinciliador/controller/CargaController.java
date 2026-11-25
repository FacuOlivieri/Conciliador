package com.proyecto.Coinciliador.controller;

import com.proyecto.Coinciliador.dto.SubidaArchivoForm;
import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.mapper.PantallaCargaMapper;
import com.proyecto.Coinciliador.model.Carga;
import com.proyecto.Coinciliador.service.ICargaService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

/** Pantalla de carga de archivos. Solo orquesta; cada POST redirige a la pantalla (patrón PRG, RNF-2). */
@Controller
@RequiredArgsConstructor
public class CargaController {

    private static final String REDIRIGIR_A_LA_CARGA = "redirect:/carga";

    private final ICargaService servicio;
    private final PantallaCargaMapper mapper;

    /** Sin un tipo de conciliación elegido, vuelve a la página inicial (spec 002) sin mensaje. */
    @GetMapping("/carga")
    public String mostrar(Model modelo) {
        Carga carga = servicio.cargaActual();
        if (carga.tipo() == null) {
            return "redirect:/";
        }
        modelo.addAttribute("pantalla", mapper.aPantalla(carga));
        return "carga";
    }

    @PostMapping("/carga/{lugar}")
    public String subir(@PathVariable Lugar lugar, @Valid @ModelAttribute("subida") SubidaArchivoForm formulario,
            BindingResult errores) throws IOException {
        if (errores.hasErrors()) {
            servicio.registrarRechazo(lugar, nombreDe(formulario.archivo()), motivo(errores));
        } else {
            servicio.subir(lugar, nombreDe(formulario.archivo()), formulario.archivo().getBytes());
        }
        return REDIRIGIR_A_LA_CARGA;
    }

    private String nombreDe(MultipartFile archivo) {
        return archivo == null ? "" : Objects.toString(archivo.getOriginalFilename(), "");
    }

    /** M2 solo si el único problema es el tamaño; extensión mala, sin archivo o un envío que no es un archivo son M1. */
    private MensajeCarga motivo(BindingResult errores) {
        List<String> codigos = errores.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        boolean soloTamano = codigos.contains(MensajeCarga.M2.name()) && !codigos.contains(MensajeCarga.M1.name());
        return soloTamano ? MensajeCarga.M2 : MensajeCarga.M1;
    }
}
