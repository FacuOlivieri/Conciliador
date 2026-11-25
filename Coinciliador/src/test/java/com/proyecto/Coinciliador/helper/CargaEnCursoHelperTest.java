package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.EstadoLugar.PendienteDeHoja;
import com.proyecto.Coinciliador.model.EstadoLugar.Rechazado;
import com.proyecto.Coinciliador.model.EstadoLugar.Valido;
import com.proyecto.Coinciliador.model.EstadoLugar.Vacio;
import com.proyecto.Coinciliador.model.Resumen;
import org.junit.jupiter.api.Test;

// Cubre la definición de "carga en curso" de la spec 002 (RF-6, RF-7, RF-9).
class CargaEnCursoHelperTest {

    private static final EstadoLugar VACIO = new Vacio();
    private static final EstadoLugar RECHAZADO = new Rechazado("datos.ods", MensajeCarga.M1);
    private static final EstadoLugar VALIDO = valido();
    private static final EstadoLugar PENDIENTE = pendienteDeHoja();

    @Test
    void sinArchivosNoHayCargaEnCurso() {
        assertThat(CargaEnCursoHelper.hayCargaEnCurso(VACIO, VACIO, false)).isFalse();
    }

    @Test
    void unArchivoValidoYElOtroLugarVacioEsCargaEnCurso() {
        assertThat(CargaEnCursoHelper.hayCargaEnCurso(VALIDO, VACIO, false)).isTrue();
        assertThat(CargaEnCursoHelper.hayCargaEnCurso(VACIO, VALIDO, false)).isTrue();
    }

    @Test
    void unExcelPendienteDeHojaYaEsCargaEnCurso() {
        assertThat(CargaEnCursoHelper.hayCargaEnCurso(PENDIENTE, VACIO, false)).isTrue();
    }

    @Test
    void unArchivoRechazadoConElOtroLugarVacioNoEsCargaEnCurso() {
        assertThat(CargaEnCursoHelper.hayCargaEnCurso(RECHAZADO, VACIO, false)).isFalse();
        assertThat(CargaEnCursoHelper.hayCargaEnCurso(VACIO, RECHAZADO, false)).isFalse();
    }

    @Test
    void unRechazadoConElOtroLugarValidoEsCargaEnCurso() {
        assertThat(CargaEnCursoHelper.hayCargaEnCurso(RECHAZADO, VALIDO, false)).isTrue();
    }

    @Test
    void unaCargaConfirmadaEsCargaEnCurso() {
        assertThat(CargaEnCursoHelper.hayCargaEnCurso(VALIDO, VALIDO, true)).isTrue();
    }

    private static EstadoLugar valido() {
        byte[] bytes = "a;b".getBytes(StandardCharsets.UTF_8);
        ArchivoCargado archivo = ArchivoCargado.csv("datos.csv", bytes, "huella", ';', StandardCharsets.UTF_8);
        return new Valido(archivo, new Resumen("datos.csv", null, 1, List.of()));
    }

    private static EstadoLugar pendienteDeHoja() {
        byte[] bytes = {1, 2, 3};
        return new PendienteDeHoja(ArchivoCargado.excel("libro.xlsx", bytes, "huella", List.of("Hoja1", "Hoja2")));
    }
}
