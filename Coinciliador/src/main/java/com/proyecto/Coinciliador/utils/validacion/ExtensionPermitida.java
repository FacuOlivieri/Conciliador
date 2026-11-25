package com.proyecto.Coinciliador.utils.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** El archivo subido debe tener extensión .csv, .xlsx o .xls (RF-3, RF-4). El mensaje es el código M1 del catálogo. */
@Documented
@Constraint(validatedBy = ExtensionPermitidaValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionPermitida {

    String message() default "M1";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
