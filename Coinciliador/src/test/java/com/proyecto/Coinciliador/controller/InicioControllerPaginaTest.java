package com.proyecto.Coinciliador.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import com.proyecto.Coinciliador.enums.MensajeInicio;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.mapper.InicioMapper;
import com.proyecto.Coinciliador.service.ICargaService;
import com.proyecto.Coinciliador.service.IConciliacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Cubre la página inicial de la spec 002 (RF-1, RF-2, RF-3, RNF-1).
@WebMvcTest(InicioController.class)
@Import(InicioMapper.class)
class InicioControllerPaginaTest {

    @Autowired
    private MockMvc mockMvc;

    // El manejador global de excepciones de la spec 001 también entra en este slice y necesita el servicio.
    @MockitoBean
    private ICargaService cargaService;

    @MockitoBean
    private IConciliacionService conciliacionService;

    @Test
    void laRaizMuestraLaPaginaInicial() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"));
    }

    @Test
    void muestraLosCuatroTiposEnElOrdenDeLaSpecConSuFraseDeAyuda() throws Exception {
        String html = pagina();

        int anterior = -1;
        for (TipoConciliacion tipo : TipoConciliacion.values()) {
            int posicion = html.indexOf(tipo.getAyuda());
            assertThat(posicion).as("frase de ayuda de %s", tipo).isGreaterThan(anterior);
            assertThat(html).contains(tipo.getNombre());
            anterior = posicion;
        }
    }

    @Test
    void cadaTipoEsUnFormularioPostConUnBoton() throws Exception {
        String html = pagina();

        for (TipoConciliacion tipo : TipoConciliacion.values()) {
            Pattern formulario = Pattern.compile(
                    "<form[^>]*method=\"post\"[^>]*action=\"/conciliacion/" + tipo.getSlug() + "\"[^>]*>.*?<button",
                    Pattern.DOTALL);
            assertThat(html).as("formulario de %s", tipo).containsPattern(formulario);
        }
    }

    @Test
    void noUsaRecursosExternos() throws Exception {
        assertThat(pagina()).doesNotContainPattern("(href|src)=\"(https?:)?//");
    }

    @Test
    void sinMotivoNoMuestraNingunAviso() throws Exception {
        assertThat(pagina())
                .doesNotContain(MensajeInicio.P1.getTexto())
                .doesNotContain("role=\"alert\"");
    }

    private String pagina() throws Exception {
        return mockMvc.perform(get("/")).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
