package com.proyecto.Coinciliador.controller;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.csv;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.filasFicticias;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

// Cubre RF-1, RF-4, RF-5, RF-13, RF-19, RNF-2 y RNF-4: la pantalla de carga y la subida de archivos.
@SpringBootTest
@AutoConfigureMockMvc
class CargaControllerSubidaTest {

    private static final int LIMITE = 10_485_760;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ICargaRepository repositorio;

    @BeforeEach
    void abrirUnaCargaDeIvaVacia() {
        repositorio.reiniciar(TipoConciliacion.IVA);
    }

    @Test
    void muestraElTituloElEnlaceAlInicioYLasDosTarjetas() throws Exception {
        String html = pantalla();

        assertThat(html).contains("IVA").contains("href=\"/\"").contains("Volver al inicio");
        assertThat(tarjeta(html, "PROPIO")).contains("Propio").contains("action=\"/carga/PROPIO\"")
                .contains("Todavía no cargaste un archivo.");
        assertThat(tarjeta(html, "CONTRAPARTE")).contains("Contraparte").contains("action=\"/carga/CONTRAPARTE\"");
    }

    @Test
    void sinTipoActivoRedirigeAlInicioSinMensaje() throws Exception {
        repositorio.reiniciar(null);

        mvc.perform(get("/carga")).andExpect(redirectedUrl("/")).andExpect(flash().attributeCount(0));
    }

    @Test
    void despuesDeSubirRedirigeYUnaPantallaNuevaSigueMostrandoElArchivo() throws Exception {
        byte[] bytes = csv(';', StandardCharsets.UTF_8, false, filasFicticias(3));

        subir("PROPIO", "propio.csv", bytes);

        assertThat(tarjeta(pantalla(), "PROPIO")).contains("propio.csv");
        assertThat(tarjeta(pantalla(), "PROPIO")).contains("propio.csv");
        assertThat(tarjeta(pantalla(), "CONTRAPARTE")).doesNotContain("propio.csv");
    }

    @Test
    void unaExtensionNoPermitidaMuestraM1EnSuTarjeta() throws Exception {
        subir("PROPIO", "datos.ods", new byte[] {1});

        String html = pantalla();
        assertThat(tarjeta(html, "PROPIO")).contains("datos.ods").contains(MensajeCarga.M1.getTexto())
                .contains("role=\"alert\"").contains("aria-invalid=\"true\"");
        assertThat(tarjeta(html, "CONTRAPARTE")).doesNotContain(MensajeCarga.M1.getTexto());
    }

    @Test
    void unArchivoDeUnByteMasQueElLimiteMuestraM2() throws Exception {
        subir("CONTRAPARTE", "grande.csv", new byte[LIMITE + 1]);

        String html = pantalla();
        assertThat(tarjeta(html, "CONTRAPARTE")).contains(MensajeCarga.M2.getTexto());
        assertThat(tarjeta(html, "PROPIO")).doesNotContain(MensajeCarga.M2.getTexto());
    }

    @Test
    void unRechazoDelLectorTambienQuedaEnSuTarjetaYConservaElOtroLugar() throws Exception {
        subir("PROPIO", "propio.csv", csv(';', StandardCharsets.UTF_8, false, filasFicticias(2)));

        subir("CONTRAPARTE", "vacio.csv", new byte[0]);

        String html = pantalla();
        assertThat(tarjeta(html, "CONTRAPARTE")).contains(MensajeCarga.M3.getTexto());
        assertThat(tarjeta(html, "PROPIO")).contains("propio.csv");
    }

    @Test
    void unEnvioSinArchivoMuestraM1SinFallar() throws Exception {
        mvc.perform(multipart("/carga/PROPIO")).andExpect(redirectedUrl("/carga"));

        assertThat(tarjeta(pantalla(), "PROPIO")).contains(MensajeCarga.M1.getTexto());
    }

    @Test
    void unTextoEnLugarDelArchivoMuestraM1SinFallar() throws Exception {
        mvc.perform(post("/carga/PROPIO").param("archivo", "no soy un archivo")).andExpect(redirectedUrl("/carga"));

        assertThat(tarjeta(pantalla(), "PROPIO")).contains(MensajeCarga.M1.getTexto());
    }

    @Test
    void elNombreDelArchivoSeMuestraEscapado() throws Exception {
        subir("PROPIO", "<b>x</b>.ods", new byte[] {1});

        assertThat(pantalla()).doesNotContain("<b>x</b>").contains("&lt;b&gt;x");
    }

    private void subir(String lugar, String nombre, byte[] bytes) throws Exception {
        mvc.perform(multipart("/carga/{lugar}", lugar).file(new MockMultipartFile("archivo", nombre, null, bytes)))
                .andExpect(redirectedUrl("/carga"));
    }

    private String pantalla() throws Exception {
        return mvc.perform(get("/carga")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String tarjeta(String html, String lugar) {
        int inicio = html.indexOf("id=\"lugar-" + lugar + "\"");
        assertThat(inicio).as("tarjeta de " + lugar).isNotNegative();
        return html.substring(inicio, html.indexOf("</section>", inicio));
    }
}
