package com.proyecto.Coinciliador;

import static com.proyecto.Coinciliador.helper.FabricaArchivosPrueba.csv;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.helper.FabricaArchivosPrueba;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

// Cubre RNF-3: ni el nombre ni el contenido de los archivos se escriben en los registros de la aplicación.
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class LogsSinDatosTest {

    private static final String NOMBRE = "nombre-secreto-7391";
    private static final String CONTENIDO = "contenido-secreto-4827";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ICargaRepository repositorio;

    @BeforeEach
    void abrirUnaCargaDeIvaVacia() {
        repositorio.reiniciar(TipoConciliacion.IVA);
    }

    // Sin esta comprobación, los "no contiene" de abajo pasarían aunque la captura no estuviera funcionando.
    @Test
    void laCapturaVeLosRegistrosDeLaAplicacion(CapturedOutput salida) {
        LoggerFactory.getLogger(LogsSinDatosTest.class).info("marca {}", CONTENIDO);

        assertThat(salida.getAll()).contains(CONTENIDO);
    }

    @Test
    void unCsvValidoNoDejaRastroEnLosRegistros(CapturedOutput salida) throws Exception {
        subir("PROPIO", NOMBRE + ".csv", csvConContenidoMarcado());
        verPantalla();

        assertSinDatos(salida);
    }

    @Test
    void subirElegirHojaYConfirmarUnExcelNoDejaRastro(CapturedOutput salida) throws Exception {
        byte[] libro = FabricaArchivosPrueba.xlsx(l -> {
            l.createSheet(CONTENIDO).createRow(0).createCell(0).setCellValue(CONTENIDO);
            l.createSheet("Otra").createRow(0).createCell(0).setCellValue(CONTENIDO);
        });
        subir("PROPIO", NOMBRE + ".csv", csvConContenidoMarcado());
        subir("CONTRAPARTE", NOMBRE + ".xlsx", libro);

        mvc.perform(post("/carga/CONTRAPARTE/hoja").param("hoja", CONTENIDO)).andExpect(redirectedUrl("/carga"));
        mvc.perform(post("/carga/confirmar")).andExpect(redirectedUrl("/carga"));
        verPantalla();

        assertSinDatos(salida);
    }

    @Test
    void losRechazosNoDejanRastro(CapturedOutput salida) throws Exception {
        byte[] conCero = (CONTENIDO + "\u0000").getBytes(StandardCharsets.UTF_8);
        byte[] comillaSinCerrar = ("\"" + CONTENIDO + ";b\r\n1;2\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] noEsExcel = CONTENIDO.getBytes(StandardCharsets.UTF_8);

        subir("PROPIO", NOMBRE + ".ods", noEsExcel);
        subir("PROPIO", NOMBRE + ".csv", conCero);
        subir("PROPIO", NOMBRE + ".csv", comillaSinCerrar);
        subir("CONTRAPARTE", NOMBRE + ".xlsx", noEsExcel);
        verPantalla();

        assertSinDatos(salida);
    }

    @Test
    void unExcelConUnaCeldaDanadaSeRechazaConM5SinDejarRastro(CapturedOutput salida) throws Exception {
        subir("CONTRAPARTE", NOMBRE + ".xlsx", FabricaArchivosPrueba.xlsxConCeldaDanada(CONTENIDO));

        assertThat(verPantalla()).contains(MensajeCarga.M5.getTexto());
        assertSinDatos(salida);
    }

    private byte[] csvConContenidoMarcado() {
        return csv(';', StandardCharsets.UTF_8, false, List.of(List.of(CONTENIDO, "100")));
    }

    private void subir(String lugar, String nombre, byte[] bytes) throws Exception {
        mvc.perform(multipart("/carga/{lugar}", lugar).file(new MockMultipartFile("archivo", nombre, null, bytes)))
                .andExpect(redirectedUrl("/carga"));
    }

    private String verPantalla() throws Exception {
        return mvc.perform(get("/carga")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private void assertSinDatos(CapturedOutput salida) {
        assertThat(salida.getAll()).doesNotContain(NOMBRE).doesNotContain(CONTENIDO);
    }
}
