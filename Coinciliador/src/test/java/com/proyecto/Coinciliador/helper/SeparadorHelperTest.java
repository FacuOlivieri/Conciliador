package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

// Cubre RF-12: detección del separador (D7) entre ';', ',' y tabulación.
class SeparadorHelperTest {

    @Test
    void detectaPuntoYComa() {
        assertThat(SeparadorHelper.detectar(List.of("Fecha;Importe", "01/01;100", "02/01;200"))).isEqualTo(';');
    }

    @Test
    void detectaComa() {
        assertThat(SeparadorHelper.detectar(List.of("Fecha,Importe", "01/01,100", "02/01,200"))).isEqualTo(',');
    }

    @Test
    void detectaTabulacion() {
        assertThat(SeparadorHelper.detectar(List.of("Fecha\tImporte", "01/01\t100", "02/01\t200"))).isEqualTo('\t');
    }

    @Test
    void ignoraLosSeparadoresDentroDeComillas() {
        List<String> lineas = List.of("\"a;b;c;d\",x,y", "\"e;f;g;h\",x,y");

        assertThat(SeparadorHelper.detectar(lineas)).isEqualTo(',');
    }

    @Test
    void ganaElSeparadorConCuentaConstanteAunqueOtroAparezcaEnMasLineas() {
        List<String> lineas = List.of("a;b;c,d", "a;b;c;d,e", "a;b,f");

        assertThat(SeparadorHelper.detectar(lineas)).isEqualTo(',');
    }

    @Test
    void ganaPuntoYComaEnUnEmpate() {
        assertThat(SeparadorHelper.detectar(List.of("a;b", "a,b"))).isEqualTo(';');
    }

    @Test
    void sinSeparadoresDevuelvePuntoYComa() {
        assertThat(SeparadorHelper.detectar(List.of("Total", "100"))).isEqualTo(';');
    }

    @Test
    void ignoraLasLineasEnBlancoYMiraSoloLasPrimerasVeinteConDatos() {
        List<String> lineas = new ArrayList<>(List.of("", "  "));
        for (int numero = 0; numero < 20; numero++) {
            lineas.add("a;b");
        }
        for (int numero = 0; numero < 30; numero++) {
            lineas.add("a,b");
        }

        assertThat(SeparadorHelper.detectar(lineas)).isEqualTo(';');
    }
}
