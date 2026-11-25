package com.proyecto.Coinciliador.service;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.TEXTO_CELDA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.exception.ArchivoRechazadoException;
import com.proyecto.Coinciliador.helper.FabricaArchivosPrueba;
import java.util.Arrays;
import java.util.Random;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

// Cubre RF-6 (vacío, M3), RF-9 (no es Excel o dañado, M5), RF-10 (cifrado, M6) y RF-11 (se abre por contenido).
class LectorExcelServiceAperturaTest {

    private final LectorExcelService lector = new LectorExcelService();

    @Test
    void rechazaConM3UnArchivoDeCeroBytes() {
        assertRechazaCon(new byte[0], MensajeCarga.M3);
    }

    @Test
    void rechazaConM5BytesAleatorios() {
        byte[] bytes = new byte[300];
        new Random(42).nextBytes(bytes);

        assertRechazaCon(bytes, MensajeCarga.M5);
    }

    @Test
    void rechazaConM5UnXlsxDanado() {
        byte[] completo = FabricaArchivosPrueba.xlsxMinimo();

        assertRechazaCon(Arrays.copyOf(completo, completo.length / 2), MensajeCarga.M5);
    }

    @Test
    void rechazaConM6UnXlsxCifradoConContrasena() {
        assertRechazaCon(FabricaArchivosPrueba.xlsxCifrado("clave-ficticia"), MensajeCarga.M6);
    }

    @Test
    void rechazaConM6UnXlsCifradoConContrasena() {
        assertRechazaCon(FabricaArchivosPrueba.xlsCifrado("clave-ficticia"), MensajeCarga.M6);
    }

    // RF-11: la apertura no recibe el nombre, así que un .xlsx llamado .xls (o al revés) se lee igual.
    @Test
    void abreUnXlsxPorSuContenido() throws Exception {
        try (Workbook libro = lector.abrir(FabricaArchivosPrueba.xlsxMinimo())) {
            assertThat(textoDeA1(libro)).isEqualTo(TEXTO_CELDA);
        }
    }

    @Test
    void abreUnXlsPorSuContenido() throws Exception {
        try (Workbook libro = lector.abrir(FabricaArchivosPrueba.xlsMinimo())) {
            assertThat(textoDeA1(libro)).isEqualTo(TEXTO_CELDA);
        }
    }

    private String textoDeA1(Workbook libro) {
        return libro.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
    }

    private void assertRechazaCon(byte[] bytes, MensajeCarga esperado) {
        assertThatThrownBy(() -> lector.abrir(bytes))
                .isInstanceOfSatisfying(ArchivoRechazadoException.class,
                        e -> assertThat(e.getMensajeCarga()).isEqualTo(esperado));
    }
}
