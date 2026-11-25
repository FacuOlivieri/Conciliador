package com.proyecto.Coinciliador.service;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.csv;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.filasFicticias;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.hojaConFilas;
import static org.assertj.core.api.Assertions.assertThat;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.helper.FabricaArchivosPrueba;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.Resumen;
import com.proyecto.Coinciliador.repository.CargaEnMemoriaRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

// Cubre RF-16 (sin hoja elegida no es válido), RF-17 (cambiar de hoja sin resubir), RF-22 y RF-23 (confirmar).
class CargaServiceHojaYConfirmacionTest {

    private final CargaEnMemoriaRepository repositorio = new CargaEnMemoriaRepository();
    private final LectorExcelService lectorExcel = new LectorExcelService();
    private final AnalizadorArchivoService analizador = new AnalizadorArchivoService(new LectorCsvService(), lectorExcel);
    private final CargaService servicio = new CargaService(analizador, lectorExcel, repositorio);

    @Test
    void unExcelConVariasHojasQuedaPendienteHastaQueSeElijaUna() {
        subirLibroConDosHojas(Lugar.PROPIO);

        assertThat(repositorio.obtener().propio()).isInstanceOf(EstadoLugar.PendienteDeHoja.class);
        assertThat(repositorio.obtener().ambosValidos()).isFalse();
    }

    @Test
    void elegirUnaHojaEnPendienteDeHojaLoDejaValidoSinVolverASubir() {
        subirLibroConDosHojas(Lugar.PROPIO);

        servicio.elegirHoja(Lugar.PROPIO, "Compras");

        Resumen resumen = ((EstadoLugar.Valido) repositorio.obtener().propio()).resumen();
        assertThat(resumen.hoja()).isEqualTo("Compras");
        assertThat(resumen.cantidadFilas()).isEqualTo(3);
        assertThat(resumen.nombre()).isEqualTo("libro.xlsx");
    }

    @Test
    void cambiarAOtraHojaActualizaElResumen() {
        subirLibroConDosHojas(Lugar.PROPIO);
        servicio.elegirHoja(Lugar.PROPIO, "Compras");

        servicio.elegirHoja(Lugar.PROPIO, "Ventas");

        Resumen resumen = ((EstadoLugar.Valido) repositorio.obtener().propio()).resumen();
        assertThat(resumen.hoja()).isEqualTo("Ventas");
        assertThat(resumen.cantidadFilas()).isEqualTo(1);
    }

    @Test
    void unaHojaInexistenteSeIgnora() {
        subirLibroConDosHojas(Lugar.PROPIO);
        EstadoLugar antes = repositorio.obtener().propio();

        servicio.elegirHoja(Lugar.PROPIO, "NoExiste");

        assertThat(repositorio.obtener().propio()).isSameAs(antes);
    }

    @Test
    void elegirHojaEnUnLugarVacioOConUnCsvSeIgnora() {
        servicio.subir(Lugar.CONTRAPARTE, "contraparte.csv", csvValido());
        EstadoLugar csv = repositorio.obtener().contraparte();

        servicio.elegirHoja(Lugar.PROPIO, "Ventas");
        servicio.elegirHoja(Lugar.CONTRAPARTE, "Ventas");

        assertThat(repositorio.obtener().propio()).isEqualTo(new EstadoLugar.Vacio());
        assertThat(repositorio.obtener().contraparte()).isSameAs(csv);
    }

    @Test
    void cambiarDeHojaBorraLaConfirmacion() {
        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());
        subirLibroConDosHojas(Lugar.CONTRAPARTE);
        servicio.elegirHoja(Lugar.CONTRAPARTE, "Ventas");
        servicio.confirmar();

        servicio.elegirHoja(Lugar.CONTRAPARTE, "Compras");

        assertThat(repositorio.obtener().confirmada()).isFalse();
    }

    @Test
    void unaHojaConMasDeCienMilFilasDejaElLugarRechazadoConM4() {
        LectorExcelService lectorQueRechaza = new LectorExcelService() {
            @Override
            public Resumen resumirHoja(ArchivoCargado archivo, String hoja) {
                throw new ArchivoRechazadoException(MensajeCarga.M4);
            }
        };
        CargaService conLectorQueRechaza = new CargaService(analizador, lectorQueRechaza, repositorio);
        subirLibroConDosHojas(Lugar.PROPIO);

        conLectorQueRechaza.elegirHoja(Lugar.PROPIO, "Compras");

        assertThat(repositorio.obtener().propio())
                .isEqualTo(new EstadoLugar.Rechazado("libro.xlsx", MensajeCarga.M4));
    }

    @Test
    void confirmarConUnLugarNoValidoNoConfirma() {
        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());
        subirLibroConDosHojas(Lugar.CONTRAPARTE);

        servicio.confirmar();

        assertThat(repositorio.obtener().confirmada()).isFalse();
    }

    @Test
    void confirmarConUnLugarVacioNoConfirma() {
        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());

        servicio.confirmar();

        assertThat(repositorio.obtener().confirmada()).isFalse();
    }

    @Test
    void confirmarConAmbosValidosConfirma() {
        servicio.subir(Lugar.PROPIO, "propio.csv", csvValido());
        servicio.subir(Lugar.CONTRAPARTE, "contraparte.xlsx", FabricaArchivosPrueba.xlsxMinimo());

        servicio.confirmar();

        assertThat(repositorio.obtener().confirmada()).isTrue();
    }

    private void subirLibroConDosHojas(Lugar lugar) {
        byte[] libro = FabricaArchivosPrueba.xlsx(l -> {
            hojaConFilas(l, "Ventas", 1);
            hojaConFilas(l, "Compras", 3);
        });
        servicio.subir(lugar, "libro.xlsx", libro);
    }

    private byte[] csvValido() {
        return csv(';', StandardCharsets.UTF_8, false, filasFicticias(3));
    }
}
