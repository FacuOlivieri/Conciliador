package com.proyecto.Coinciliador.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.proyecto.Coinciliador.enums.MensajeInicio;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

// Cubre RF-11 de la spec 002 con el servidor real: MockMvc no reproduce la reescritura de la URL con ";jsessionid".
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TipoInexistenteSinCookieTest {

    @LocalServerPort
    private int puerto;

    @Test
    void laPrimeraVisitaConUnTipoInexistenteLlegaALaPaginaInicialConP1() throws IOException, InterruptedException {
        try (HttpClient navegador = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()) {
            HttpRequest elegir = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/conciliacion/monotributo"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> respuesta = navegador.send(elegir, HttpResponse.BodyHandlers.ofString());

            assertThat(respuesta.statusCode()).isEqualTo(200);
            assertThat(respuesta.body()).contains(MensajeInicio.P1.getTexto());
        }
    }
}
