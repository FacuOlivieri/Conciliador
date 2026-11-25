package com.proyecto.Coinciliador.utils.validacion;

import static org.assertj.core.api.Assertions.assertThat;

import com.proyecto.Coinciliador.dto.ElegirHojaForm;
import com.proyecto.Coinciliador.dto.SubidaArchivoForm;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

// Cubre RF-3, RF-4 (extensión, M1), RF-5 (tamaño, M2), RF-15/RF-17 (hoja elegida) y la regla 5 de la constitución.
class ValidacionesArchivoTest {

    private static final int LIMITE = 10_485_760;

    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceptaUnArchivoDeExactamenteElLimite() {
        assertThat(mensajes(subida("datos.csv", LIMITE))).isEmpty();
    }

    @Test
    void rechazaConM2UnByteMasQueElLimite() {
        assertThat(mensajes(subida("datos.csv", LIMITE + 1))).containsExactly(MensajeCarga.M2.name());
    }

    @Test
    void rechazaConM1UnaExtensionNoPermitida() {
        assertThat(mensajes(subida("datos.ods", 10))).containsExactly(MensajeCarga.M1.name());
    }

    @Test
    void aceptaUnCsvValido() {
        assertThat(mensajes(subida("datos.csv", 10))).isEmpty();
    }

    @Test
    void rechazaConM1CuandoNoLlegaArchivo() {
        assertThat(mensajes(new SubidaArchivoForm(null))).containsExactly(MensajeCarga.M1.name());
    }

    @Test
    void rechazaUnaHojaEnBlanco() {
        assertThat(validador.validate(new ElegirHojaForm("  "))).hasSize(1);
        assertThat(validador.validate(new ElegirHojaForm(null))).hasSize(1);
    }

    @Test
    void aceptaUnaHojaConNombre() {
        assertThat(validador.validate(new ElegirHojaForm("Ventas"))).isEmpty();
    }

    private SubidaArchivoForm subida(String nombre, int bytes) {
        return new SubidaArchivoForm(new MockMultipartFile("archivo", nombre, null, new byte[bytes]));
    }

    private List<String> mensajes(SubidaArchivoForm form) {
        return validador.validate(form).stream().map(v -> v.getMessage()).toList();
    }
}
