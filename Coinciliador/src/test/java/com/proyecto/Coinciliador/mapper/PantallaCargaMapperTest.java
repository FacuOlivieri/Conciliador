package com.proyecto.Coinciliador.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.proyecto.Coinciliador.dto.LugarVistaDto;
import com.proyecto.Coinciliador.dto.PantallaCargaDto;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.Carga;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.Resumen;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

// Cubre RF-18 (resumen), RF-20 (M7), RF-21 (formatos distintos), RF-22 (Confirmar y qué falta), RF-23 (M8) y RF-24 (título).
class PantallaCargaMapperTest {

    private static final EstadoLugar VACIO = new EstadoLugar.Vacio();

    private final PantallaCargaMapper mapper = new PantallaCargaMapper();

    @Test
    void elTituloEsElNombreDelTipoActivo() {
        assertThat(pantalla(carga(TipoConciliacion.INGRESOS_BRUTOS, VACIO, VACIO, false)).tituloTipo())
                .isEqualTo("Ingresos Brutos");
        assertThat(pantalla(carga(TipoConciliacion.VENTAS, VACIO, VACIO, false)).tituloTipo()).isEqualTo("Ventas");
    }

    @Test
    void conAmbosLugaresVaciosConfirmarEstaDeshabilitadoYDiceQueFaltanLosDos() {
        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA, VACIO, VACIO, false));

        assertThat(pantalla.confirmarHabilitado()).isFalse();
        assertThat(pantalla.textoFalta())
                .isEqualTo("Falta cargar el archivo de Propio; falta cargar el archivo de Contraparte");
        assertThat(pantalla.propio().estado()).isEqualTo(LugarVistaDto.Estado.VACIO);
    }

    @Test
    void siSoloFaltaLaContraparteElTextoNoMencionaPropio() {
        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA, validoCsv("a.csv", "h1"), VACIO, false));

        assertThat(pantalla.confirmarHabilitado()).isFalse();
        assertThat(pantalla.textoFalta()).contains("Contraparte").doesNotContain("Propio");
    }

    @Test
    void unLugarRechazadoMuestraSuMensajeYDiceQueNoEsValido() {
        EstadoLugar rechazado = new EstadoLugar.Rechazado("datos.ods", MensajeCarga.M1);

        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA, validoCsv("a.csv", "h1"), rechazado, false));

        assertThat(pantalla.contraparte().estado()).isEqualTo(LugarVistaDto.Estado.RECHAZADO);
        assertThat(pantalla.contraparte().nombreArchivo()).isEqualTo("datos.ods");
        assertThat(pantalla.contraparte().mensajeRechazo()).isEqualTo(MensajeCarga.M1.getTexto());
        assertThat(pantalla.textoFalta()).contains("Contraparte").contains("no es válido");
    }

    @Test
    void unExcelPendienteDeHojaMuestraLasHojasYPidePedirLaEleccion() {
        EstadoLugar pendiente = new EstadoLugar.PendienteDeHoja(excel("libro.xlsx", "h2", List.of("Ventas", "Compras")));

        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA, validoCsv("a.csv", "h1"), pendiente, false));

        assertThat(pantalla.confirmarHabilitado()).isFalse();
        assertThat(pantalla.textoFalta()).contains("elegir la hoja").contains("Contraparte");
        assertThat(pantalla.contraparte().estado()).isEqualTo(LugarVistaDto.Estado.PENDIENTE_DE_HOJA);
        assertThat(pantalla.contraparte().hojas()).containsExactly("Ventas", "Compras");
        assertThat(pantalla.contraparte().mostrarSelectorHoja()).isTrue();
        assertThat(pantalla.contraparte().hojaUsada()).isNull();
    }

    @Test
    void unCsvYUnExcelValidosHabilitanConfirmar() {
        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA,
                validoCsv("propio.csv", "h1"), validoExcel("contra.xlsx", "h2", List.of("Hoja1"), "Hoja1"), false));

        assertThat(pantalla.confirmarHabilitado()).isTrue();
        assertThat(pantalla.textoFalta()).isEmpty();
    }

    @Test
    void elResumenDeUnArchivoValidoLlegaALaVista() {
        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA, validoCsv("propio.csv", "h1"), VACIO, false));

        LugarVistaDto propio = pantalla.propio();
        assertThat(propio.estado()).isEqualTo(LugarVistaDto.Estado.VALIDO);
        assertThat(propio.nombreArchivo()).isEqualTo("propio.csv");
        assertThat(propio.cantidadFilas()).isEqualTo(2);
        assertThat(propio.vistaPrevia()).containsExactly(List.of("a", "b"), List.of("c", "d"));
        assertThat(propio.mostrarSelectorHoja()).isFalse();
        assertThat(propio.hojaUsada()).isNull();
    }

    @Test
    void unExcelValidoConVariasHojasPermiteCambiarDeHoja() {
        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA, VACIO,
                validoExcel("libro.xlsx", "h2", List.of("Ventas", "Compras"), "Compras"), false));

        assertThat(pantalla.contraparte().hojaUsada()).isEqualTo("Compras");
        assertThat(pantalla.contraparte().hojas()).containsExactly("Ventas", "Compras");
        assertThat(pantalla.contraparte().mostrarSelectorHoja()).isTrue();
    }

    @Test
    void unExcelValidoConUnaSolaHojaNoMuestraSelector() {
        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA, VACIO,
                validoExcel("libro.xlsx", "h2", List.of("Hoja1"), "Hoja1"), false));

        assertThat(pantalla.contraparte().mostrarSelectorHoja()).isFalse();
    }

    @Test
    void avisaM7CuandoLasHuellasCoincidenAunqueLosNombresDifieran() {
        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA,
                validoCsv("propio.csv", "misma"), validoCsv("otro-nombre.csv", "misma"), false));

        assertThat(pantalla.advertenciaMismoArchivo()).isTrue();
        assertThat(pantalla.textoAdvertencia()).isEqualTo(MensajeCarga.M7.getTexto());
        assertThat(pantalla.confirmarHabilitado()).isTrue();
    }

    @Test
    void noAvisaM7SiLasHuellasSonDistintasOFaltaUnArchivo() {
        assertThat(pantalla(carga(TipoConciliacion.IVA,
                validoCsv("a.csv", "h1"), validoCsv("b.csv", "h2"), false)).advertenciaMismoArchivo()).isFalse();
        assertThat(pantalla(carga(TipoConciliacion.IVA,
                validoCsv("a.csv", "h1"), VACIO, false)).advertenciaMismoArchivo()).isFalse();
        assertThat(pantalla(carga(TipoConciliacion.IVA, validoCsv("a.csv", "h1"),
                new EstadoLugar.Rechazado("x.ods", MensajeCarga.M1), false)).advertenciaMismoArchivo()).isFalse();
    }

    @Test
    void avisaM7TambienSiUnoDeLosDosEstaPendienteDeHoja() {
        EstadoLugar pendiente = new EstadoLugar.PendienteDeHoja(excel("libro.xlsx", "misma", List.of("A", "B")));

        PantallaCargaDto pantalla = pantalla(carga(TipoConciliacion.IVA,
                validoExcel("libro.xlsx", "misma", List.of("A", "B"), "A"), pendiente, false));

        assertThat(pantalla.advertenciaMismoArchivo()).isTrue();
    }

    @Test
    void elMensajeM8SoloApareceSiLaCargaEstaConfirmada() {
        EstadoLugar propio = validoCsv("a.csv", "h1");
        EstadoLugar contraparte = validoCsv("b.csv", "h2");

        PantallaCargaDto sinConfirmar = pantalla(carga(TipoConciliacion.IVA, propio, contraparte, false));
        PantallaCargaDto confirmada = pantalla(carga(TipoConciliacion.IVA, propio, contraparte, true));

        assertThat(sinConfirmar.confirmada()).isFalse();
        assertThat(sinConfirmar.mensajeFinal()).isNull();
        assertThat(confirmada.confirmada()).isTrue();
        assertThat(confirmada.mensajeFinal()).isEqualTo(MensajeCarga.M8.getTexto());
    }

    private PantallaCargaDto pantalla(Carga carga) {
        return mapper.aPantalla(carga);
    }

    private Carga carga(TipoConciliacion tipo, EstadoLugar propio, EstadoLugar contraparte, boolean confirmada) {
        return new Carga(tipo, propio, contraparte, confirmada);
    }

    private ArchivoCargado csv(String nombre, String huella) {
        return ArchivoCargado.csv(nombre, new byte[0], huella, ';', StandardCharsets.UTF_8);
    }

    private ArchivoCargado excel(String nombre, String huella, List<String> hojas) {
        return ArchivoCargado.excel(nombre, new byte[0], huella, hojas);
    }

    private EstadoLugar.Valido validoCsv(String nombre, String huella) {
        List<List<String>> vista = List.of(List.of("a", "b"), List.of("c", "d"));
        return new EstadoLugar.Valido(csv(nombre, huella), new Resumen(nombre, null, 2, vista));
    }

    private EstadoLugar.Valido validoExcel(String nombre, String huella, List<String> hojas, String hojaUsada) {
        return new EstadoLugar.Valido(excel(nombre, huella, hojas), new Resumen(nombre, hojaUsada, 1, List.of()));
    }
}
