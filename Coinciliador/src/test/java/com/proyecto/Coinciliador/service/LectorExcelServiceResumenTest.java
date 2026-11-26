package com.proyecto.Coinciliador.service;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.hojaConFilas;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.helper.FabricaArchivosPrueba;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.Resumen;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Cubre RF-7 (más de 100.000 filas, M4) y RF-18 (resumen de la hoja), con las decisiones D9 y D10.
class LectorExcelServiceResumenTest {

    private final LectorExcelService lector = new LectorExcelService();
    private Locale localeOriginal;

    // El locale de la PC no debe influir: se fuerza uno distinto de es-AR para que el test falle si no se fija (D10).
    @BeforeEach
    void usarOtroLocaleEnLaJvm() {
        localeOriginal = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restaurarElLocale() {
        Locale.setDefault(localeOriginal);
    }

    @Test
    void unaFormulaMuestraSuResultadoGuardadoYNoLaFormula() {
        Resumen resumen = resumenDe(libro -> {
            Row fila = libro.createSheet("Datos").createRow(0);
            fila.createCell(0).setCellValue(2);
            fila.createCell(1).setCellValue(3);
            fila.createCell(2).setCellFormula("A1+B1");
            fila.createCell(3).setCellFormula("\"to\"&\"tal\"");
            libro.getCreationHelper().createFormulaEvaluator().evaluateAll();
        });

        assertThat(resumen.vistaPrevia()).containsExactly(List.of("2", "3", "5", "total"));
    }

    @Test
    void unErrorSeMuestraTalCualSeaUnValorOUnResultadoDeFormula() {
        Resumen resumen = resumenDe(libro -> {
            Row fila = libro.createSheet("Datos").createRow(0);
            fila.createCell(0).setCellErrorValue(FormulaError.REF.getCode());
            fila.createCell(1).setCellFormula("1/0");
            libro.getCreationHelper().createFormulaEvaluator().evaluateAll();
        });

        assertThat(resumen.vistaPrevia()).containsExactly(List.of("#REF!", "#DIV/0!"));
    }

    @Test
    void unaCeldaCombinadaMuestraElValorEnLaPrimeraCelda() {
        Resumen resumen = resumenDe(libro -> {
            Sheet hoja = libro.createSheet("Datos");
            hoja.createRow(0).createCell(0).setCellValue("Título");
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        });

        assertThat(resumen.vistaPrevia().get(0).get(0)).isEqualTo("Título");
        assertThat(resumen.cantidadFilas()).isEqualTo(1);
    }

    @Test
    void lasFilasOcultasSeCuentanYSeMuestran() {
        Resumen resumen = resumenDe(libro -> {
            Sheet hoja = libro.createSheet("Datos");
            hoja.createRow(0).createCell(0).setCellValue("visible 1");
            hoja.createRow(1).createCell(0).setCellValue("oculta");
            hoja.createRow(2).createCell(0).setCellValue("visible 2");
            hoja.getRow(1).setZeroHeight(true);
        });

        assertThat(resumen.cantidadFilas()).isEqualTo(3);
        assertThat(resumen.vistaPrevia()).extracting(fila -> fila.get(0))
                .containsExactly("visible 1", "oculta", "visible 2");
    }

    @Test
    void losNumerosSeFormateanEnEspanolDeArgentinaSinImportarElLocaleDeLaPc() {
        Resumen resumen = resumenDe(libro -> {
            Row fila = libro.createSheet("Datos").createRow(0);
            fila.createCell(0).setCellValue(1234.5);
            fila.createCell(1).setCellValue(1234.5);
            fila.getCell(1).setCellStyle(conFormato(libro, "#,##0.00"));
            fila.createCell(2).setCellFormula("A1*2");
            fila.getCell(2).setCellStyle(conFormato(libro, "#,##0.00"));
            libro.getCreationHelper().createFormulaEvaluator().evaluateAll();
        });

        assertThat(resumen.vistaPrevia()).containsExactly(List.of("1234,5", "1.234,50", "2.469,00"));
    }

    // 4 de marzo: día y mes distintos, para que un orden invertido no pase.
    @Test
    void todasLasFechasSeMuestranComoDiaMesAnioEnXlsxYEnXls() {
        Consumer<Workbook> fechas = libro -> {
            Row fila = libro.createSheet("Datos").createRow(0);
            fila.createCell(0).setCellValue(LocalDateTime.of(2026, 3, 4, 15, 30));
            fila.createCell(1).setCellValue(LocalDateTime.of(2026, 3, 4, 15, 30));
            fila.createCell(2).setCellFormula("A1");
            fila.createCell(3).setCellValue(LocalDateTime.of(2026, 3, 4, 0, 0));
            fila.getCell(0).setCellStyle(conFormato(libro, "m/d/yy"));        // predefinido 14
            fila.getCell(1).setCellStyle(conFormato(libro, "m/d/yy h:mm"));   // predefinido 22
            fila.getCell(2).setCellStyle(conFormato(libro, "m/d/yy"));
            fila.getCell(3).setCellStyle(conFormato(libro, "dd/mm/yyyy"));    // propio del archivo
            fila.createCell(4).setCellValue(LocalDateTime.of(2026, 3, 4, 0, 0));
            fila.getCell(4).setCellStyle(conFormato(libro, "d \"de\" mmmm \"de\" yyyy"));
            fila.createCell(5).setCellValue(LocalDateTime.of(2026, 3, 4, 15, 30));
            fila.getCell(5).setCellStyle(conFormato(libro, "h:mm"));           // hora sin fecha
            libro.getCreationHelper().createFormulaEvaluator().evaluateAll();
        };
        List<String> esperado = List.of("04/03/26", "04/03/26 15:30", "04/03/26", "04/03/26", "04/03/26", "15:30");

        assertThat(resumenDe(fechas).vistaPrevia()).containsExactly(esperado);
        EstadoLugar xls = lector.leer("libro.xls", FabricaArchivosPrueba.xls(fechas));
        assertThat(((EstadoLugar.Valido) xls).resumen().vistaPrevia()).containsExactly(esperado);
    }

    // Excel no tiene fechas negativas: un número así con formato de fecha se lee como número, sin romper la lectura.
    @Test
    void unNumeroNegativoConFormatoDeFechaNoRompeLaLectura() {
        Resumen resumen = resumenDe(libro -> {
            Row fila = libro.createSheet("Datos").createRow(0);
            fila.createCell(0).setCellValue(-1);
            fila.getCell(0).setCellStyle(conFormato(libro, "dd/mm/yyyy"));
            fila.createCell(1).setCellValue("sigue");
        });

        assertThat(resumen.vistaPrevia().get(0).get(1)).isEqualTo("sigue");
    }

    @Test
    void rechazaConM4UnaHojaConMasDeCienMilFilasConDatos() {
        byte[] bytes = FabricaArchivosPrueba.xlsx(libro -> hojaConFilas(libro, "Datos", 100_001));

        assertThatThrownBy(() -> lector.leer("libro.xlsx", bytes))
                .isInstanceOfSatisfying(ArchivoRechazadoException.class,
                        e -> assertThat(e.getMensajeCarga()).isEqualTo(MensajeCarga.M4));
    }

    private CellStyle conFormato(Workbook libro, String formato) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setDataFormat(libro.createDataFormat().getFormat(formato));
        return estilo;
    }

    private Resumen resumenDe(Consumer<Workbook> armado) {
        byte[] bytes = FabricaArchivosPrueba.xlsx(armado);
        return ((EstadoLugar.Valido) lector.leer("libro.xlsx", bytes)).resumen();
    }
}
