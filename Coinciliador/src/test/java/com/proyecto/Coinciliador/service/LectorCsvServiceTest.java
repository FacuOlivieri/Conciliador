package com.proyecto.Coinciliador.service;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.csv;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.filasFicticias;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.proyecto.Coinciliador.enums.FormatoArchivo;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.helper.FabricaArchivosPrueba;
import com.proyecto.Coinciliador.helper.HuellaHelper;
import com.proyecto.Coinciliador.model.EstadoLugar;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

// Cubre RF-6 (vacío, M3), RF-7 (M4), RF-8 (no es texto o comillas sin cerrar, M5), RF-12 y RF-18 para CSV.
class LectorCsvServiceTest {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    private final LectorCsvService lector = new LectorCsvService();

    @Test
    void rechazaConM3UnArchivoDeCeroBytes() {
        assertRechazaCon(new byte[0], MensajeCarga.M3);
    }

    @Test
    void rechazaConM3UnArchivoQueSoloTieneBom() {
        assertRechazaCon(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, MensajeCarga.M3);
    }

    @Test
    void rechazaConM3UnArchivoDeSoloSeparadoresYLineasEnBlanco() {
        assertRechazaCon(";;;\r\n\r\n;;;\r\n".getBytes(StandardCharsets.UTF_8), MensajeCarga.M3);
    }

    @Test
    void rechazaConM5UnArchivoBinario() {
        assertRechazaCon(new byte[] {'a', 0x00, 'b', 0x01}, MensajeCarga.M5);
    }

    @Test
    void rechazaConM5UnArchivoBinarioSinNingunByteCero() {
        assertRechazaCon(new byte[] {1, 2, 3, 8, 14, (byte) 0x80, (byte) 0xFF, '\n', 27, 31, (byte) 0xFE},
                MensajeCarga.M5);
    }

    @Test
    void rechazaConM5UnXlsxRenombradoACsv() {
        assertRechazaCon(FabricaArchivosPrueba.xlsxMinimo(), MensajeCarga.M5);
    }

    @Test
    void rechazaConM5UnaComillaSinCerrar() {
        assertRechazaCon("\"abc;def\r\n1;2\r\n".getBytes(StandardCharsets.UTF_8), MensajeCarga.M5);
    }

    @Test
    void rechazaConM4MasDeCienMilFilasConDatos() {
        assertRechazaCon(csv(';', StandardCharsets.UTF_8, false, filasFicticias(100_001)), MensajeCarga.M4);
    }

    @Test
    void aceptaFilasDeDistintoLargoYLasCuenta() {
        List<List<String>> filas = List.of(List.of("a", "b", "c"), List.of("d"), List.of("e", "f"));

        EstadoLugar.Valido valido = lector.leer("datos.csv", csv(';', StandardCharsets.UTF_8, false, filas));

        assertThat(valido.resumen().cantidadFilas()).isEqualTo(3);
    }

    @Test
    void noCuentaLasFilasSinDatos() {
        List<List<String>> filas = List.of(List.of("a", "b"), List.of("", ""), List.of("  ", ""), List.of("c", "d"));

        EstadoLugar.Valido valido = lector.leer("datos.csv", csv(';', StandardCharsets.UTF_8, false, filas));

        assertThat(valido.resumen().cantidadFilas()).isEqualTo(2);
    }

    @Test
    void devuelveUnArchivoValidoConSuResumen() {
        byte[] bytes = csv(';', StandardCharsets.UTF_8, false, filasFicticias(15));

        EstadoLugar.Valido valido = lector.leer("compras.csv", bytes);

        assertThat(valido.archivo().formato()).isEqualTo(FormatoArchivo.CSV);
        assertThat(valido.archivo().huella()).isEqualTo(HuellaHelper.sha256(bytes));
        assertThat(valido.archivo().separador()).isEqualTo(';');
        assertThat(valido.archivo().codificacion()).isEqualTo(StandardCharsets.UTF_8);
        assertThat(valido.resumen().nombre()).isEqualTo("compras.csv");
        assertThat(valido.resumen().hoja()).isNull();
        assertThat(valido.resumen().cantidadFilas()).isEqualTo(15);
        assertThat(valido.resumen().vistaPrevia()).isEqualTo(filasFicticias(15).subList(0, 10));
    }

    @Test
    void detectaLaComaYLaTabulacion() {
        List<List<String>> filas = filasFicticias(3);

        assertThat(lector.leer("a.csv", csv(',', StandardCharsets.UTF_8, false, filas)).archivo().separador())
                .isEqualTo(',');
        assertThat(lector.leer("a.csv", csv('\t', StandardCharsets.UTF_8, false, filas)).archivo().separador())
                .isEqualTo('\t');
    }

    @Test
    void leeBienLosAcentosEnWindows1252() {
        List<List<String>> filas = List.of(List.of("Señor Ñandú", "áéíóú"));

        EstadoLugar.Valido valido = lector.leer("datos.csv", csv(';', WINDOWS_1252, false, filas));

        assertThat(valido.archivo().codificacion()).isEqualTo(WINDOWS_1252);
        assertThat(valido.resumen().vistaPrevia()).isEqualTo(filas);
    }

    @Test
    void quitaElBomDelPrimerEncabezado() {
        EstadoLugar.Valido valido = lector.leer("datos.csv", csv(';', StandardCharsets.UTF_8, true, filasFicticias(2)));

        assertThat(valido.resumen().vistaPrevia().get(0).get(0)).isEqualTo("Comprobante 1");
    }

    @Test
    void recortaLaVistaPreviaDeUnaFilaAncha() {
        List<String> fila = new ArrayList<>(List.of("x".repeat(60)));
        for (int columna = 2; columna <= 25; columna++) {
            fila.add("c" + columna);
        }

        EstadoLugar.Valido valido = lector.leer("datos.csv", csv(';', StandardCharsets.UTF_8, false, List.of(fila)));

        assertThat(valido.resumen().vistaPrevia().get(0)).hasSize(20);
        assertThat(valido.resumen().vistaPrevia().get(0).get(0)).isEqualTo("x".repeat(49) + "…");
    }

    private void assertRechazaCon(byte[] bytes, MensajeCarga esperado) {
        assertThatThrownBy(() -> lector.leer("datos.csv", bytes))
                .isInstanceOfSatisfying(ArchivoRechazadoException.class,
                        e -> assertThat(e.getMensajeCarga()).isEqualTo(esperado));
    }
}
