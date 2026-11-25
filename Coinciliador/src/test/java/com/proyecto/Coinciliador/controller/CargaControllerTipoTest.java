package com.proyecto.Coinciliador.controller;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.csv;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.filasFicticias;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// Cubre cómo la pantalla de carga de la spec 001 usa el tipo elegido en la spec 002 (RF-5, RF-6, RF-10).
@SpringBootTest
@AutoConfigureMockMvc
class CargaControllerTipoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ICargaRepository repositorio;

    @BeforeEach
    void empezarSinTipoActivo() {
        repositorio.reiniciar(null);
    }

    @Test
    void conTipoActivoLaCargaMuestraElNombreDelTipoComoTituloYUnEnlaceAlInicio() throws Exception {
        mvc.perform(post("/conciliacion/ingresos-brutos")).andExpect(redirectedUrl("/carga"));

        String html = carga();

        assertThat(html).containsPattern("<h1[^>]*>\\s*" + TipoConciliacion.INGRESOS_BRUTOS.getNombre() + "\\s*</h1>");
        assertThat(html).containsPattern("<a[^>]*href=\"/\"[^>]*>\\s*Volver al inicio");
    }

    @Test
    void sinTipoActivoLaCargaRedirigeAlInicioSinMensaje() throws Exception {
        MvcResult resultado = mvc.perform(get("/carga"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        assertThat(resultado.getFlashMap().isEmpty()).isTrue();
    }

    @Test
    void subirUnArchivoVolverAlInicioYElegirElMismoTipoConservaElArchivo() throws Exception {
        mvc.perform(post("/conciliacion/iva"));
        subirAlPropio("propio.csv");

        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(post("/conciliacion/iva")).andExpect(redirectedUrl("/carga"));

        assertThat(carga()).contains("propio.csv");
    }

    @Test
    void elegirOtroTipoConUnArchivoCargadoPideConfirmacionYLaCargaSigueIgual() throws Exception {
        mvc.perform(post("/conciliacion/iva"));
        subirAlPropio("propio.csv");

        mvc.perform(post("/conciliacion/ventas")).andExpect(redirectedUrl("/conciliacion/ventas/confirmar"));

        assertThat(carga()).contains("propio.csv");
    }

    @Test
    void descartarAbreLaCargaDelTipoNuevoSinElArchivoAnterior() throws Exception {
        mvc.perform(post("/conciliacion/iva"));
        subirAlPropio("propio.csv");

        mvc.perform(post("/conciliacion/ventas/descartar")).andExpect(redirectedUrl("/carga"));

        String html = carga();
        assertThat(html).doesNotContain("propio.csv");
        assertThat(html).containsPattern("<h1[^>]*>\\s*" + TipoConciliacion.VENTAS.getNombre() + "\\s*</h1>");
    }

    private void subirAlPropio(String nombre) throws Exception {
        byte[] bytes = csv(';', StandardCharsets.UTF_8, false, filasFicticias(3));
        mvc.perform(multipart("/carga/PROPIO").file(new MockMultipartFile("archivo", nombre, null, bytes)))
                .andExpect(redirectedUrl("/carga"));
    }

    private String carga() throws Exception {
        return mvc.perform(get("/carga")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
