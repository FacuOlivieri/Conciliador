package com.proyecto.Coinciliador.service;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.hojaConDatos;
import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.hojaVacia;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.proyecto.Coinciliador.enums.FormatoArchivo;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.helper.FabricaArchivosPrueba;
import com.proyecto.Coinciliador.helper.HuellaHelper;
import com.proyecto.Coinciliador.model.EstadoLugar;
import java.util.List;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

// Cubre RF-6 (única hoja con datos oculta = vacío, M3), RF-14 (una hoja listable) y RF-15 (varias).
class LectorExcelServiceHojasTest {

    private final LectorExcelService lector = new LectorExcelService();

    @Test
    void conUnaSolaHojaConDatosDevuelveUnArchivoValidoConEsaHoja() {
        byte[] bytes = FabricaArchivosPrueba.xlsx(libro -> hojaConDatos(libro, "Ventas"));

        EstadoLugar estado = lector.leer("libro.xlsx", bytes);

        EstadoLugar.Valido valido = (EstadoLugar.Valido) estado;
        assertThat(valido.archivo().formato()).isEqualTo(FormatoArchivo.EXCEL);
        assertThat(valido.archivo().huella()).isEqualTo(HuellaHelper.sha256(bytes));
        assertThat(valido.archivo().hojasListables()).containsExactly("Ventas");
        assertThat(valido.resumen().nombre()).isEqualTo("libro.xlsx");
        assertThat(valido.resumen().hoja()).isEqualTo("Ventas");
        assertThat(valido.resumen().cantidadFilas()).isEqualTo(1);
    }

    @Test
    void conVariasHojasConDatosQuedaPendienteDeHojaConSusNombres() {
        byte[] bytes = FabricaArchivosPrueba.xlsx(libro -> {
            hojaConDatos(libro, "Ventas");
            hojaConDatos(libro, "Compras");
            hojaConDatos(libro, "Resumen");
        });

        EstadoLugar estado = lector.leer("libro.xlsx", bytes);

        assertThat(estado).isInstanceOf(EstadoLugar.PendienteDeHoja.class);
        assertThat(((EstadoLugar.PendienteDeHoja) estado).archivo().hojasListables())
                .containsExactly("Ventas", "Compras", "Resumen");
    }

    @Test
    void funcionaIgualConUnXlsConVariasHojas() {
        byte[] bytes = FabricaArchivosPrueba.xls(libro -> {
            hojaConDatos(libro, "Ventas");
            hojaConDatos(libro, "Compras");
        });

        EstadoLugar estado = lector.leer("libro.xls", bytes);

        assertThat(((EstadoLugar.PendienteDeHoja) estado).archivo().hojasListables())
                .containsExactly("Ventas", "Compras");
    }

    @Test
    void dejaFueraLasHojasOcultasMuyOcultasYSinDatos() {
        byte[] bytes = FabricaArchivosPrueba.xlsx(libro -> {
            hojaConDatos(libro, "Ventas");
            hojaConDatos(libro, "Oculta");
            hojaConDatos(libro, "MuyOculta");
            hojaVacia(libro, "SinFilas");
            libro.createSheet("SoloEspacios").createRow(0).createCell(0).setCellValue("   ");
            hojaConDatos(libro, "Compras");
            libro.setSheetVisibility(libro.getSheetIndex("Oculta"), SheetVisibility.HIDDEN);
            libro.setSheetVisibility(libro.getSheetIndex("MuyOculta"), SheetVisibility.VERY_HIDDEN);
        });

        EstadoLugar estado = lector.leer("libro.xlsx", bytes);

        assertThat(((EstadoLugar.PendienteDeHoja) estado).archivo().hojasListables())
                .containsExactly("Ventas", "Compras");
    }

    @Test
    void rechazaConM3SiLaUnicaHojaConDatosEstaOculta() {
        byte[] bytes = FabricaArchivosPrueba.xlsx(libro -> {
            ocultar(libro, hojaConDatos(libro, "Datos").getSheetName());
            hojaVacia(libro, "Vacia");
        });

        assertThatThrownBy(() -> lector.leer("libro.xlsx", bytes))
                .isInstanceOfSatisfying(ArchivoRechazadoException.class,
                        e -> assertThat(e.getMensajeCarga()).isEqualTo(MensajeCarga.M3));
    }

    @Test
    void conservaLasColumnasAlineadasSiLaPrimeraCeldaEstaVacia() {
        byte[] bytes = FabricaArchivosPrueba.xlsx(libro ->
                libro.createSheet("Datos").createRow(0).createCell(1).setCellValue("segunda"));

        EstadoLugar.Valido valido = (EstadoLugar.Valido) lector.leer("libro.xlsx", bytes);

        assertThat(valido.resumen().vistaPrevia()).containsExactly(List.of("", "segunda"));
    }

    private void ocultar(Workbook libro, String nombre) {
        libro.setSheetVisibility(libro.getSheetIndex(nombre), SheetVisibility.HIDDEN);
    }
}
