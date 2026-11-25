package com.proyecto.Coinciliador.utils.validacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.proyecto.Coinciliador.dto.EleccionTipoForm;
import com.proyecto.Coinciliador.enums.MensajeInicio;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

// Cubre RF-11 (tipo inexistente, mensaje P1) y la regla 5 de la constitución (entrada validada con Bean Validation).
class TipoConciliacionValidoValidatorTest {

    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @ValueSource(strings = {"iva", "IVA", "ventas", "compras", "ingresos-brutos"})
    void aceptaElSlugDeUnTipoExistente(String slug) {
        assertThat(mensajes(slug)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "monotributo", "ingresos_brutos"})
    void rechazaConP1UnTextoQueNoEsUnTipo(String slug) {
        assertThat(mensajes(slug)).containsExactly(MensajeInicio.P1.name());
    }

    private List<String> mensajes(String slug) {
        return validador.validate(new EleccionTipoForm(slug)).stream().map(v -> v.getMessage()).toList();
    }
}
