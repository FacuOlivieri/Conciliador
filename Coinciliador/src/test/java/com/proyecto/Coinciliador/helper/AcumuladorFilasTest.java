package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import java.util.List;
import org.junit.jupiter.api.Test;

// Cubre RF-6 (filas con datos), RF-7 (máximo 100.000 filas, M4) y RF-18 (guarda las 10 primeras).
class AcumuladorFilasTest {

    private static final List<String> FILA_CON_DATOS = List.of("dato");

    @Test
    void ignoraLasFilasVacias() {
        AcumuladorFilas acumulador = new AcumuladorFilas();

        acumulador.agregar(List.of());
        acumulador.agregar(List.of("", "  "));
        acumulador.agregar(FILA_CON_DATOS);

        assertThat(acumulador.cantidad()).isEqualTo(1);
    }

    @Test
    void guardaSoloLasPrimerasDiezFilas() {
        AcumuladorFilas acumulador = new AcumuladorFilas();

        for (int numero = 1; numero <= 15; numero++) {
            acumulador.agregar(List.of("fila " + numero));
        }

        assertThat(acumulador.cantidad()).isEqualTo(15);
        assertThat(acumulador.primeras()).hasSize(10)
                .first().isEqualTo(List.of("fila 1"));
        assertThat(acumulador.primeras().get(9)).isEqualTo(List.of("fila 10"));
    }

    @Test
    void aceptaCienMilFilas() {
        AcumuladorFilas acumulador = new AcumuladorFilas();

        for (int numero = 0; numero < 100_000; numero++) {
            acumulador.agregar(FILA_CON_DATOS);
        }

        assertThat(acumulador.cantidad()).isEqualTo(100_000);
    }

    @Test
    void rechazaConM4LaFilaCienMilUno() {
        AcumuladorFilas acumulador = new AcumuladorFilas();
        for (int numero = 0; numero < 100_000; numero++) {
            acumulador.agregar(FILA_CON_DATOS);
        }

        assertThatThrownBy(() -> acumulador.agregar(FILA_CON_DATOS))
                .isInstanceOfSatisfying(ArchivoRechazadoException.class,
                        e -> assertThat(e.getMensajeCarga()).isEqualTo(MensajeCarga.M4));
    }

    @Test
    void lasFilasVaciasNoCuentanParaElLimite() {
        AcumuladorFilas acumulador = new AcumuladorFilas();
        for (int numero = 0; numero < 100_000; numero++) {
            acumulador.agregar(FILA_CON_DATOS);
        }

        acumulador.agregar(List.of(" "));

        assertThat(acumulador.cantidad()).isEqualTo(100_000);
    }
}
