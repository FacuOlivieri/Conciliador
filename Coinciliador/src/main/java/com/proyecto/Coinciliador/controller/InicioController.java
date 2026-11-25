package com.proyecto.Coinciliador.controller;

import com.proyecto.Coinciliador.dto.EleccionTipoForm;
import com.proyecto.Coinciliador.dto.InicioDto;
import com.proyecto.Coinciliador.enums.MensajeInicio;
import com.proyecto.Coinciliador.enums.ResultadoEleccion;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.mapper.InicioMapper;
import com.proyecto.Coinciliador.service.IConciliacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Página inicial: primera pantalla de la aplicación, donde el contador elige el tipo de conciliación (RF-1 a RF-4,
 * RF-6 a RF-9, RF-11). Solo orquesta; cada POST redirige (patrón PRG) y las decisiones las toma el servicio.
 */
@Controller
@RequiredArgsConstructor
public class InicioController {

    private static final String AVISO = "aviso";
    private static final String VOLVER_AL_INICIO = "redirect:/";
    private static final String ABRIR_LA_CARGA = "redirect:/carga";

    private final InicioMapper mapper;
    private final IConciliacionService servicio;

    @GetMapping("/")
    public String inicio(Model model) {
        // P1 llega como atributo flash cuando se vuelve de un tipo inexistente.
        InicioDto inicio = model.getAttribute(AVISO) instanceof MensajeInicio aviso
                ? mapper.aInicio(aviso)
                : mapper.aInicio();
        model.addAttribute("inicio", inicio);
        return "inicio";
    }

    @PostMapping("/conciliacion/{slug}")
    public String elegir(@Valid EleccionTipoForm form, BindingResult errores, RedirectAttributes aviso) {
        if (errores.hasErrors()) {
            return avisarTipoInexistente(aviso);
        }
        TipoConciliacion tipo = tipoDe(form);
        return switch (servicio.elegir(tipo)) {
            case ABRIR_CARGA -> ABRIR_LA_CARGA;
            case PEDIR_CONFIRMACION -> "redirect:/conciliacion/" + tipo.getSlug() + "/confirmar";
        };
    }

    @GetMapping("/conciliacion/{slug}/confirmar")
    public String confirmar(@Valid EleccionTipoForm form, BindingResult errores, RedirectAttributes aviso, Model model) {
        if (errores.hasErrors()) {
            return avisarTipoInexistente(aviso);
        }
        TipoConciliacion tipo = tipoDe(form);
        if (servicio.decidir(tipo) == ResultadoEleccion.PEDIR_CONFIRMACION) {
            model.addAttribute("confirmacion", mapper.aConfirmacion(servicio.tipoActivo().orElseThrow(), tipo));
            return "confirmar-cambio";
        }
        // Sin nada que confirmar (por ejemplo, un enlace abierto a mano): no se muestra D1.
        return servicio.tipoActivo().isPresent() ? ABRIR_LA_CARGA : VOLVER_AL_INICIO;
    }

    @PostMapping("/conciliacion/{slug}/descartar")
    public String descartar(@Valid EleccionTipoForm form, BindingResult errores, RedirectAttributes aviso) {
        if (errores.hasErrors()) {
            return avisarTipoInexistente(aviso);
        }
        servicio.descartarYAbrir(tipoDe(form));
        return ABRIR_LA_CARGA;
    }

    /** El slug ya pasó la validación, por eso el tipo siempre existe. */
    private TipoConciliacion tipoDe(EleccionTipoForm form) {
        return TipoConciliacion.deSlug(form.slug()).orElseThrow();
    }

    private String avisarTipoInexistente(RedirectAttributes aviso) {
        aviso.addFlashAttribute(AVISO, MensajeInicio.P1);
        return VOLVER_AL_INICIO;
    }
}
