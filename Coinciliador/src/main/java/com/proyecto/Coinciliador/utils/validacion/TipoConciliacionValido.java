package com.proyecto.Coinciliador.utils.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** El texto debe ser el slug de un tipo de conciliación existente (RF-11). El mensaje es el código P1 del catálogo. */
@Documented
@Constraint(validatedBy = TipoConciliacionValidoValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TipoConciliacionValido {

    String message() default "P1";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
