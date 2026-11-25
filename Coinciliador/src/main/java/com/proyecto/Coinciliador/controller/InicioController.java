package com.proyecto.Coinciliador.controller;

import com.proyecto.Coinciliador.mapper.InicioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Página inicial: primera pantalla de la aplicación, donde el contador elige el tipo de conciliación (RF-1 a RF-3). */
@Controller
@RequiredArgsConstructor
public class InicioController {

    private final InicioMapper mapper;

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("inicio", mapper.aInicio());
        return "inicio";
    }
}
