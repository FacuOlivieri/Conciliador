package com.proyecto.Coinciliador.utils.validacion;

import com.proyecto.Coinciliador.enums.TipoConciliacion;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Valida el slug con {@link TipoConciliacion#deSlug}. Un texto nulo o en blanco no es válido. */
public class TipoConciliacionValidoValidator implements ConstraintValidator<TipoConciliacionValido, String> {

    @Override
    public boolean isValid(String slug, ConstraintValidatorContext contexto) {
        return TipoConciliacion.deSlug(slug).isPresent();
    }
}
