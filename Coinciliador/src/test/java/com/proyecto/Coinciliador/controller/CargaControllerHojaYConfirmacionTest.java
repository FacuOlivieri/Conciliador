package com.proyecto.Coinciliador.controller;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.csv;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.filasFicticias;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.hojaConFilas;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.helper.FabricaArchivosPrueba;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

// Cubre RF-13, RF-15, RF-17, RF-18, RF-20, RF-22 y RF-23: elegir hoja, resumen, M7, Confirmar y M8 en la pantalla.
@SpringBootTest
@AutoConfigureMockMvc
class CargaControllerHojaYConfirmacionTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ICargaRepository repositorio;

    @BeforeEach
    void abrirUnaCargaDeIvaVacia() {
        repositorio.reiniciar(TipoConciliacion.IVA);
    }

    @Test
    void unExcelConVariasHojasMuestraLasOpcionesSinPreseleccion() throws Exception {
        subirLibroConDosHojas("CONTRAPARTE");

        String tarjeta = tarjeta(pantalla(), "CONTRAPARTE");

        assertThat(tarjeta).contains("action=\"/carga/CONTRAPARTE/hoja\"").contains("value=\"Ventas\"")
                .contains("value=\"Compras\"").contains("Usar esta hoja").doesNotContain("checked");
    }

    @Test
    void elegirUnaHojaMuestraElResumenYPermiteCambiarla() throws Exception {
        subirLibroConDosHojas("CONTRAPARTE");

        elegirHoja("CONTRAPARTE", "Compras");

        String tarjeta = tarjeta(pantalla(), "CONTRAPARTE");
        assertThat(tarjeta).contains("libro.xlsx").contains("Hoja: <span>Compras</span>")
                .contains("3 filas con datos").contains("<table").contains(FabricaArchivosPrueba.TEXTO_CELDA)
                .containsPattern("value=\"Compras\"[^>]*checked").contains("value=\"Ventas\"");
    }

    @Test
    void unCsvValidoMuestraSuResumenSinSelectorDeHoja() throws Exception {
        subir("PROPIO", "propio.csv", csvValido());

        String tarjeta = tarjeta(pantalla(), "PROPIO");

        assertThat(tarjeta).contains("3 filas con datos").contains("<td>Proveedor 1</td>")
                .doesNotContain("/hoja\"").doesNotContain("Hoja:");
    }

    @Test
    void unaSolaFilaSeEscribeEnSingular() throws Exception {
        subir("PROPIO", "libro.xlsx", FabricaArchivosPrueba.xlsxMinimo());

        assertThat(tarjeta(pantalla(), "PROPIO")).contains("1 fila con datos").doesNotContain("/hoja\"");
    }

    @Test
    void lasCeldasDeLaVistaPreviaSeMuestranEscapadas() throws Exception {
        byte[] bytes = csv(';', StandardCharsets.UTF_8, false, List.of(List.of("<i>x</i>", "y")));

        subir("PROPIO", "propio.csv", bytes);

        assertThat(pantalla()).doesNotContain("<i>x</i>").contains("&lt;i&gt;x");
    }

    @Test
    void unaHojaEnBlancoOInexistenteNoCambiaNada() throws Exception {
        subirLibroConDosHojas("CONTRAPARTE");

        elegirHoja("CONTRAPARTE", " ");
        elegirHoja("CONTRAPARTE", "NoExiste");

        assertThat(tarjeta(pantalla(), "CONTRAPARTE")).contains("Usar esta hoja").doesNotContain("<table");
    }

    @Test
    void confirmarEstaDeshabilitadoHastaTenerDosArchivosValidosYExplicaQueFalta() throws Exception {
        assertThat(botonConfirmar(pantalla())).contains("disabled");
        subir("PROPIO", "propio.csv", csvValido());

        String html = pantalla();
        assertThat(botonConfirmar(html)).contains("disabled");
        assertThat(html).contains("Falta cargar el archivo de Contraparte");

        subir("CONTRAPARTE", "contraparte.xlsx", FabricaArchivosPrueba.xlsxMinimo());
        assertThat(botonConfirmar(pantalla())).doesNotContain("disabled");
    }

    @Test
    void despuesDeConfirmarRedirigeALaCargaYMuestraM8() throws Exception {
        subir("PROPIO", "propio.csv", csvValido());
        subir("CONTRAPARTE", "contraparte.xlsx", FabricaArchivosPrueba.xlsxMinimo());
        assertThat(pantalla()).doesNotContain(MensajeCarga.M8.getTexto());

        mvc.perform(post("/carga/confirmar")).andExpect(redirectedUrl("/carga"));

        assertThat(pantalla()).contains(MensajeCarga.M8.getTexto());
    }

    @Test
    void confirmarSinDosArchivosValidosNoMuestraM8() throws Exception {
        subir("PROPIO", "propio.csv", csvValido());

        mvc.perform(post("/carga/confirmar")).andExpect(redirectedUrl("/carga"));

        assertThat(pantalla()).doesNotContain(MensajeCarga.M8.getTexto());
    }

    @Test
    void elMismoArchivoEnLosDosLugaresMuestraM7SinImpedirConfirmar() throws Exception {
        subir("PROPIO", "uno.csv", csvValido());
        subir("CONTRAPARTE", "otro.csv", csvValido());

        String html = pantalla();

        assertThat(html).contains(MensajeCarga.M7.getTexto());
        assertThat(botonConfirmar(html)).doesNotContain("disabled");
    }

    private void subirLibroConDosHojas(String lugar) throws Exception {
        subir(lugar, "libro.xlsx", FabricaArchivosPrueba.xlsx(libro -> {
            hojaConFilas(libro, "Ventas", 1);
            hojaConFilas(libro, "Compras", 3);
        }));
    }

    private byte[] csvValido() {
        return csv(';', StandardCharsets.UTF_8, false, filasFicticias(3));
    }

    private void subir(String lugar, String nombre, byte[] bytes) throws Exception {
        mvc.perform(multipart("/carga/{lugar}", lugar).file(new MockMultipartFile("archivo", nombre, null, bytes)))
                .andExpect(redirectedUrl("/carga"));
    }

    private void elegirHoja(String lugar, String hoja) throws Exception {
        mvc.perform(post("/carga/{lugar}/hoja", lugar).param("hoja", hoja)).andExpect(redirectedUrl("/carga"));
    }

    private String pantalla() throws Exception {
        return mvc.perform(get("/carga")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String tarjeta(String html, String lugar) {
        return entre(html, "id=\"lugar-" + lugar + "\"", "</section>");
    }

    private String botonConfirmar(String html) {
        return entre(html, "id=\"confirmar\"", ">");
    }

    private String entre(String html, String desde, String hasta) {
        int inicio = html.indexOf(desde);
        assertThat(inicio).as(desde).isNotNegative();
        return html.substring(inicio, html.indexOf(hasta, inicio));
    }
}
