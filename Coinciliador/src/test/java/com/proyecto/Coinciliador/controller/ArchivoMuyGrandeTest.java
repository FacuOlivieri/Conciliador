package com.proyecto.Coinciliador.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.repository.ICargaRepository;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

// Cubre RF-5 y RF-13 por encima del tope técnico de 30 MB. Usa un servidor real: MockMvc no aplica los límites
// de multipart ni reproduce que Tomcat corte la conexión antes de que el navegador reciba la respuesta.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArchivoMuyGrandeTest {

    private static final int TREINTA_Y_UN_MB = 31 * 1024 * 1024;
    private static final String LIMITE = "separador-de-prueba";

    @Value("${local.server.port}")
    private int puerto;

    @Autowired
    private ICargaRepository repositorio;

    @Test
    void unArchivoDeMasDeTreintaMegabytesMuestraM2EnSuTarjeta() throws Exception {
        repositorio.reiniciar(TipoConciliacion.IVA);

        String respuesta = subirArchivoEnorme("CONTRAPARTE");

        assertThat(respuesta).startsWith("HTTP/1.1 302").containsPattern("Location: \\S*/carga\\r\\n");
        String html = pantalla();
        assertThat(tarjeta(html, "CONTRAPARTE")).contains(MensajeCarga.M2.getTexto());
        assertThat(tarjeta(html, "PROPIO")).doesNotContain(MensajeCarga.M2.getTexto());
    }

    /** Como un navegador: envía el archivo entero antes de leer la respuesta. Si Tomcat corta la conexión, falla. */
    private String subirArchivoEnorme(String lugar) throws IOException {
        byte[] inicio = ("--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"archivo\"; filename=\"enorme.csv\""
                + "\r\nContent-Type: text/csv\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] fin = ("\r\n--" + LIMITE + "--\r\n").getBytes(StandardCharsets.UTF_8);
        String cabeceras = "POST /carga/" + lugar + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n"
                + "Content-Type: multipart/form-data; boundary=" + LIMITE + "\r\n"
                + "Content-Length: " + (inicio.length + TREINTA_Y_UN_MB + fin.length) + "\r\n\r\n";
        try (Socket conexion = new Socket("localhost", puerto)) {
            OutputStream salida = conexion.getOutputStream();
            salida.write(cabeceras.getBytes(StandardCharsets.US_ASCII));
            salida.write(inicio);
            salida.write(new byte[TREINTA_Y_UN_MB]);
            salida.write(fin);
            salida.flush();
            return new String(conexion.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String pantalla() throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/carga")).build();
        try (HttpClient cliente = HttpClient.newHttpClient()) {
            return cliente.send(peticion, HttpResponse.BodyHandlers.ofString()).body();
        }
    }

    private String tarjeta(String html, String lugar) {
        int inicio = html.indexOf("id=\"lugar-" + lugar + "\"");
        assertThat(inicio).as("tarjeta de " + lugar).isNotNegative();
        return html.substring(inicio, html.indexOf("</section>", inicio));
    }
}
