package com.proyecto.Coinciliador;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.MultipartConfigElement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// Cubre RF-5 (el límite de negocio de 10 MB lo aplica @TamanoMaximo) y RNF-1 (los archivos no se escriben en disco).
@SpringBootTest
class ConfiguracionMultipartTest {

    private static final long TREINTA_MB = 30L * 1024 * 1024;

    @Autowired
    private MultipartConfigElement multipart;

    @Test
    void elLimiteTecnicoEsDeTreintaMegabytes() {
        assertThat(multipart.getMaxFileSize()).isEqualTo(TREINTA_MB);
        assertThat(multipart.getMaxRequestSize()).isEqualTo(TREINTA_MB);
    }

    @Test
    void elUmbralDeDiscoSuperaElMaximoParaQueElArchivoNuncaSeEscribaEnDisco() {
        assertThat((long) multipart.getFileSizeThreshold()).isGreaterThan(multipart.getMaxFileSize());
    }
}
