package com.proyecto.Coinciliador.service;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.csv;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.filasFicticias;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.hojaDeVolumen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.proyecto.Coinciliador.enums.FormatoArchivo;
import com.proyecto.Coinciliador.helper.FabricaArchivosPrueba;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.Resumen;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

// Cubre RF-3 (según la extensión), RF-21 (formatos distintos) y la prueba de volumen del riesgo D8.
class AnalizadorArchivoServiceTest {

    private static final EstadoLugar.Valido DEL_LECTOR_CSV = valido("lector csv");
    private static final EstadoLugar.Valido DEL_LECTOR_EXCEL = valido("lector excel");

    private final AnalizadorArchivoService conLectoresFalsos =
            new AnalizadorArchivoService((nombre, bytes) -> DEL_LECTOR_CSV, (nombre, bytes) -> DEL_LECTOR_EXCEL);

    private final AnalizadorArchivoService conLectoresReales =
            new AnalizadorArchivoService(new LectorCsvService(), new LectorExcelService());

    @Test
    void derivaUnCsvAlLectorCsvSinImportarLasMayusculas() {
        assertThat(conLectoresFalsos.analizar("datos.csv", new byte[0])).isSameAs(DEL_LECTOR_CSV);
        assertThat(conLectoresFalsos.analizar("DATOS.CSV", new byte[0])).isSameAs(DEL_LECTOR_CSV);
    }

    @Test
    void derivaUnXlsYUnXlsxAlLectorExcel() {
        assertThat(conLectoresFalsos.analizar("libro.xls", new byte[0])).isSameAs(DEL_LECTOR_EXCEL);
        assertThat(conLectoresFalsos.analizar("libro.xlsx", new byte[0])).isSameAs(DEL_LECTOR_EXCEL);
        assertThat(conLectoresFalsos.analizar("LIBRO.XLSX", new byte[0])).isSameAs(DEL_LECTOR_EXCEL);
    }

    @Test
    void decidePorLaUltimaExtension() {
        assertThat(conLectoresFalsos.analizar("datos.csv.xlsx", new byte[0])).isSameAs(DEL_LECTOR_EXCEL);
    }

    @Test
    void leeUnCsvYUnExcelRealesConFormatosDistintos() {
        byte[] bytesCsv = csv(';', StandardCharsets.UTF_8, false, filasFicticias(3));
        byte[] bytesExcel = FabricaArchivosPrueba.xlsxMinimo();

        EstadoLugar.Valido propio = (EstadoLugar.Valido) conLectoresReales.analizar("propio.csv", bytesCsv);
        EstadoLugar.Valido contraparte = (EstadoLugar.Valido) conLectoresReales.analizar("contraparte.xlsx", bytesExcel);

        assertThat(propio.archivo().formato()).isEqualTo(FormatoArchivo.CSV);
        assertThat(contraparte.archivo().formato()).isEqualTo(FormatoArchivo.EXCEL);
    }

    // Riesgo D8: POI carga la hoja entera en memoria. Con el máximo permitido tiene que alcanzar sin OutOfMemoryError.
    @Test
    void analizaUnXlsxDeCienMilFilasEnMenosDeTreintaSegundos() {
        byte[] bytes = FabricaArchivosPrueba.xlsx(libro -> hojaDeVolumen(libro, "Datos", 100_000));

        // Medido en T12 (8 CPU, 3 GB de heap): de 6 a más de 10 s según la corrida, por eso el límite es 30 s (D8).
        EstadoLugar estado = assertTimeoutPreemptively(Duration.ofSeconds(30),
                () -> conLectoresReales.analizar("volumen.xlsx", bytes));

        assertThat(((EstadoLugar.Valido) estado).resumen().cantidadFilas()).isEqualTo(100_000);
    }

    private static EstadoLugar.Valido valido(String marca) {
        ArchivoCargado archivo = ArchivoCargado.csv(marca, new byte[0], "huella", ';', StandardCharsets.UTF_8);
        return new EstadoLugar.Valido(archivo, new Resumen(marca, null, 1, List.of()));
    }
}
