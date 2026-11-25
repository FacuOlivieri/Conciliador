package com.proyecto.Coinciliador.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.ResultadoEleccion;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.Carga;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.EstadoLugar.Valido;
import com.proyecto.Coinciliador.model.EstadoLugar.Vacio;
import com.proyecto.Coinciliador.model.Resumen;
import com.proyecto.Coinciliador.repository.CargaEnMemoriaRepository;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Cubre los casos de uso de la spec 002 (RF-4, RF-6, RF-7, RF-8, RF-9) con el repositorio real en memoria.
class ConciliacionServiceTest {

    private ICargaRepository repositorio;
    private ConciliacionService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new CargaEnMemoriaRepository();
        servicio = new ConciliacionService(repositorio);
    }

    @Test
    void sinTipoActivoElegirFijaElTipoYAbreLaCarga() {
        assertThat(servicio.elegir(TipoConciliacion.IVA)).isEqualTo(ResultadoEleccion.ABRIR_CARGA);
        assertThat(servicio.tipoActivo()).contains(TipoConciliacion.IVA);
    }

    @Test
    void otroTipoSinCargaEnCursoCambiaElTipoYAbreLaCarga() {
        servicio.elegir(TipoConciliacion.IVA);

        assertThat(servicio.elegir(TipoConciliacion.VENTAS)).isEqualTo(ResultadoEleccion.ABRIR_CARGA);
        assertThat(servicio.tipoActivo()).contains(TipoConciliacion.VENTAS);
    }

    @Test
    void otroTipoConUnArchivoValidoPideConfirmacionYDejaTodoIntacto() {
        servicio.elegir(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());
        Carga antes = repositorio.obtener();

        assertThat(servicio.elegir(TipoConciliacion.COMPRAS)).isEqualTo(ResultadoEleccion.PEDIR_CONFIRMACION);
        assertThat(repositorio.obtener()).isEqualTo(antes);
        assertThat(servicio.tipoActivo()).contains(TipoConciliacion.IVA);
    }

    @Test
    void elMismoTipoConCargaEnCursoLaDejaIntactaYAbreLaCarga() {
        servicio.elegir(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.CONTRAPARTE, valido());
        Carga antes = repositorio.obtener();

        assertThat(servicio.elegir(TipoConciliacion.IVA)).isEqualTo(ResultadoEleccion.ABRIR_CARGA);
        assertThat(repositorio.obtener()).isEqualTo(antes);
    }

    @Test
    void descartarYAbrirVaciaAmbosLugaresSinConfirmacionYConElTipoNuevo() {
        servicio.elegir(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());
        repositorio.guardar(Lugar.CONTRAPARTE, valido());
        repositorio.confirmar();

        servicio.descartarYAbrir(TipoConciliacion.INGRESOS_BRUTOS);

        Carga carga = repositorio.obtener();
        assertThat(carga.propio()).isEqualTo(new Vacio());
        assertThat(carga.contraparte()).isEqualTo(new Vacio());
        assertThat(carga.confirmada()).isFalse();
        assertThat(carga.tipo()).isEqualTo(TipoConciliacion.INGRESOS_BRUTOS);
    }

    @Test
    void decidirNoCambiaLaCargaNiElTipoActivo() {
        servicio.elegir(TipoConciliacion.IVA);
        repositorio.guardar(Lugar.PROPIO, valido());
        Carga antes = repositorio.obtener();

        assertThat(servicio.decidir(TipoConciliacion.COMPRAS)).isEqualTo(ResultadoEleccion.PEDIR_CONFIRMACION);
        assertThat(servicio.decidir(TipoConciliacion.IVA)).isEqualTo(ResultadoEleccion.ABRIR_CARGA);
        assertThat(repositorio.obtener()).isEqualTo(antes);
    }

    @Test
    void decidirSinCargaEnCursoNoReiniciaElTipo() {
        servicio.elegir(TipoConciliacion.IVA);

        assertThat(servicio.decidir(TipoConciliacion.VENTAS)).isEqualTo(ResultadoEleccion.ABRIR_CARGA);
        assertThat(servicio.tipoActivo()).contains(TipoConciliacion.IVA);
    }

    @Test
    void sinNingunaEleccionNoHayTipoActivo() {
        assertThat(servicio.tipoActivo()).isEmpty();
    }

    private static EstadoLugar valido() {
        byte[] bytes = "a;b".getBytes(StandardCharsets.UTF_8);
        ArchivoCargado archivo = ArchivoCargado.csv("datos.csv", bytes, "huella", ';', StandardCharsets.UTF_8);
        return new Valido(archivo, new Resumen("datos.csv", null, 1, List.of()));
    }
}
