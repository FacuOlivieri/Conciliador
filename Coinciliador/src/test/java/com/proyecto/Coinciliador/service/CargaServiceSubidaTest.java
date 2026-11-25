package com.proyecto.Coinciliador.service;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.csv;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.filasFicticias;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.hojaConDatos;
import static org.assertj.core.api.Assertions.assertThat;

import com.proyecto.Coinciliador.enums.FormatoArchivo;
import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.helper.FabricaArchivosPrueba;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.Resumen;
import com.proyecto.Coinciliador.repository.CargaEnMemoriaRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

// Cubre RF-2 (una sola carga), RF-13 (el rechazo no toca el otro lugar), RF-19 (reemplazar valida de nuevo) y RF-21.
class CargaServiceSubidaTest {

    private final CargaEnMemoriaRepository repositorio = new CargaEnMemoriaRepository();
    private final LectorExcelService lectorExcel = new LectorExcelService();
    private final CargaService servicio = new CargaService(
            new AnalizadorArchivoService(new LectorCsvService(), lectorExcel), lectorExcel, repositorio);

    @Test
    void subirUnCsvValidoDejaElLugarValido() {
        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());

        EstadoLugar.Valido valido = (EstadoLugar.Valido) repositorio.obtener().propio();
        assertThat(valido.resumen().nombre()).isEqualTo("propio.csv");
        assertThat(valido.resumen().cantidadFilas()).isEqualTo(3);
    }

    @Test
    void subirUnArchivoInvalidoDejaElLugarRechazadoConSuMensaje() {
        servicio.subir(Lugar.PROPIO, "vacio.csv", new byte[0]);

        assertThat(repositorio.obtener().propio())
                .isEqualTo(new EstadoLugar.Rechazado("vacio.csv", MensajeCarga.M3));
    }

    @Test
    void unRechazoEnContraparteConservaElPropioValido() {
        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());
        EstadoLugar propio = repositorio.obtener().propio();

        servicio.subir(Lugar.CONTRAPARTE, "danado.xlsx", new byte[] {1, 2, 3});

        assertThat(repositorio.obtener().contraparte())
                .isEqualTo(new EstadoLugar.Rechazado("danado.xlsx", MensajeCarga.M5));
        assertThat(repositorio.obtener().propio()).isSameAs(propio);
    }

    @Test
    void registrarUnRechazoLoGuardaEnEseLugarSinTocarElOtro() {
        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());
        EstadoLugar propio = repositorio.obtener().propio();

        servicio.registrarRechazo(Lugar.CONTRAPARTE, "datos.ods", MensajeCarga.M1);

        assertThat(repositorio.obtener().contraparte())
                .isEqualTo(new EstadoLugar.Rechazado("datos.ods", MensajeCarga.M1));
        assertThat(repositorio.obtener().propio()).isSameAs(propio);
    }

    @Test
    void reemplazarUnExcelYaElegidoDescartaLaHojaElegida() {
        repositorio.guardar(Lugar.PROPIO, excelConHojaElegida("Compras"));
        byte[] nuevo = FabricaArchivosPrueba.xlsx(libro -> {
            hojaConDatos(libro, "Compras");
            hojaConDatos(libro, "Ventas");
        });

        servicio.subir(Lugar.PROPIO, "nuevo.xlsx", nuevo);

        assertThat(repositorio.obtener().propio()).isInstanceOf(EstadoLugar.PendienteDeHoja.class);
    }

    @Test
    void reemplazarUnArchivoValidoPorUnoInvalidoDejaElLugarRechazado() {
        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());

        servicio.subir(Lugar.PROPIO, "otro.csv", new byte[0]);

        assertThat(repositorio.obtener().propio())
                .isEqualTo(new EstadoLugar.Rechazado("otro.csv", MensajeCarga.M3));
    }

    @Test
    void reemplazarUnRechazadoPorUnoValidoDejaElLugarValido() {
        servicio.subir(Lugar.PROPIO, "vacio.csv", new byte[0]);

        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());

        assertThat(repositorio.obtener().propio()).isInstanceOf(EstadoLugar.Valido.class);
    }

    @Test
    void subirUnArchivoBorraLaConfirmacion() {
        repositorio.confirmar();

        servicio.subir(Lugar.CONTRAPARTE, "contraparte.csv", csvValido());

        assertThat(repositorio.obtener().confirmada()).isFalse();
    }

    @Test
    void permiteUnCsvEnPropioYUnExcelEnContraparte() {
        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());
        servicio.subir(Lugar.CONTRAPARTE, "contraparte.xlsx", FabricaArchivosPrueba.xlsxMinimo());

        assertThat(formatoDe(repositorio.obtener().propio())).isEqualTo(FormatoArchivo.CSV);
        assertThat(formatoDe(repositorio.obtener().contraparte())).isEqualTo(FormatoArchivo.EXCEL);
    }

    private byte[] csvValido() {
        return csv(';', StandardCharsets.UTF_8, false, filasFicticias(3));
    }

    private EstadoLugar excelConHojaElegida(String hoja) {
        ArchivoCargado archivo = ArchivoCargado.excel("viejo.xlsx", new byte[0], "huella", List.of("Compras", "Ventas"));
        return new EstadoLugar.Valido(archivo, new Resumen("viejo.xlsx", hoja, 1, List.of()));
    }

    private FormatoArchivo formatoDe(EstadoLugar estado) {
        return ((EstadoLugar.Valido) estado).archivo().formato();
    }
}
