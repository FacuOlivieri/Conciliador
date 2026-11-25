package com.proyecto.Coinciliador.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeInicio;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.Carga;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.EstadoLugar.Valido;
import com.proyecto.Coinciliador.model.EstadoLugar.Vacio;
import com.proyecto.Coinciliador.model.Resumen;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// Cubre elegir, confirmar y descartar de la spec 002 (RF-4, RF-6 a RF-9, RF-11, RNF-1), con la carga real en memoria.
@SpringBootTest
@AutoConfigureMockMvc
class InicioControllerEleccionTest {

    private static final Pattern RECURSO_EXTERNO = Pattern.compile("(href|src)=\"https?://");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ICargaRepository repositorio;

    @BeforeEach
    void empezarSinTipoActivo() {
        repositorio.reiniciar(null);
    }

    @Test
    void elegirUnTipoSinCargaEnCursoAbreLaCargaDeEseTipo() throws Exception {
        mvc.perform(post("/conciliacion/iva"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carga"));

        assertThat(repositorio.tipoActivo()).contains(TipoConciliacion.IVA);
    }

    @Test
    void unSlugInexistenteVuelveAlInicioConP1YNoTocaLaCarga() throws Exception {
        repositorio.reiniciar(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());
        Carga antes = repositorio.obtener();

        MvcResult resultado = mvc.perform(post("/conciliacion/monotributo"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("aviso", MensajeInicio.P1))
                .andReturn();

        assertThat(repositorio.obtener()).isEqualTo(antes);
        assertThat(inicioConAviso(resultado)).contains(MensajeInicio.P1.getTexto()).contains("role=\"alert\"");
    }

    @Test
    void conUnArchivoValidoCargadoOtroTipoPideConfirmacionYNoTocaLaCarga() throws Exception {
        repositorio.reiniciar(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());
        Carga antes = repositorio.obtener();

        mvc.perform(post("/conciliacion/compras"))
                .andExpect(redirectedUrl("/conciliacion/compras/confirmar"));

        assertThat(repositorio.obtener()).isEqualTo(antes);
    }

    @Test
    void elMismoTipoConCargaEnCursoAbreLaCargaSinPerderNada() throws Exception {
        repositorio.reiniciar(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());
        Carga antes = repositorio.obtener();

        mvc.perform(post("/conciliacion/iva")).andExpect(redirectedUrl("/carga"));

        assertThat(repositorio.obtener()).isEqualTo(antes);
    }

    @Test
    void laConfirmacionMuestraD1LosDosTiposYUnEnlaceCancelarAlInicio() throws Exception {
        repositorio.reiniciar(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());

        String html = mvc.perform(get("/conciliacion/compras/confirmar"))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmar-cambio"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains(MensajeInicio.D1.getTexto()).contains("IVA").contains("Compras");
        assertThat(html).containsPattern("<a[^>]*href=\"/\"[^>]*>\\s*Cancelar");
        assertThat(html).contains("action=\"/conciliacion/compras/descartar\"").contains("Descartar y continuar");
        assertThat(RECURSO_EXTERNO.matcher(html).find()).isFalse();
    }

    @Test
    void cancelarEstaAntesQueElBotonQueDescarta() throws Exception {
        repositorio.reiniciar(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());

        String html = mvc.perform(get("/conciliacion/compras/confirmar"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(html.indexOf("Cancelar")).isPositive().isLessThan(html.indexOf("Descartar y continuar"));
    }

    @Test
    void descartarVaciaLaCargaConElTipoNuevoYAbreLaCarga() throws Exception {
        repositorio.reiniciar(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());
        repositorio.guardar(Lugar.CONTRAPARTE, valido());
        repositorio.confirmar();

        mvc.perform(post("/conciliacion/compras/descartar")).andExpect(redirectedUrl("/carga"));

        Carga carga = repositorio.obtener();
        assertThat(carga.tipo()).isEqualTo(TipoConciliacion.COMPRAS);
        assertThat(carga.propio()).isEqualTo(new Vacio());
        assertThat(carga.contraparte()).isEqualTo(new Vacio());
        assertThat(carga.confirmada()).isFalse();
    }

    @Test
    void abrirLaConfirmacionSinQueHagaFaltaRedirigeALaCargaYNoMuestraD1() throws Exception {
        repositorio.reiniciar(TipoConciliacion.IVA);

        mvc.perform(get("/conciliacion/compras/confirmar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carga"));
    }

    @Test
    void abrirLaConfirmacionSinTipoActivoRedirigeAlInicio() throws Exception {
        mvc.perform(get("/conciliacion/compras/confirmar")).andExpect(redirectedUrl("/"));
    }

    @Test
    void confirmarYDescartarConUnSlugInexistenteVuelvenAlInicioConP1SinTocarLaCarga() throws Exception {
        repositorio.reiniciar(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());
        Carga antes = repositorio.obtener();

        mvc.perform(get("/conciliacion/monotributo/confirmar"))
                .andExpect(redirectedUrl("/")).andExpect(flash().attribute("aviso", MensajeInicio.P1));
        mvc.perform(post("/conciliacion/monotributo/descartar"))
                .andExpect(redirectedUrl("/")).andExpect(flash().attribute("aviso", MensajeInicio.P1));

        assertThat(repositorio.obtener()).isEqualTo(antes);
    }

    private String inicioConAviso(MvcResult redireccion) throws Exception {
        return mvc.perform(get("/").flashAttrs(redireccion.getFlashMap()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static EstadoLugar valido() {
        byte[] bytes = "a;b".getBytes(StandardCharsets.UTF_8);
        ArchivoCargado archivo = ArchivoCargado.csv("datos.csv", bytes, "huella", ';', StandardCharsets.UTF_8);
        return new Valido(archivo, new Resumen("datos.csv", null, 1, List.of()));
    }
}
