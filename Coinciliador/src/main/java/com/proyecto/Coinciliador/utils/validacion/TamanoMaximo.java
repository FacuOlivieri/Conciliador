package com.proyecto.Coinciliador.utils.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** El archivo subido no puede superar 10.485.760 bytes (RF-5). El mensaje es el código M2 del catálogo. */
@Documented
@Constraint(validatedBy = TamanoMaximoValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TamanoMaximo {

    String message() default "M2";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
