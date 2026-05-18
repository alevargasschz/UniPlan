package com.icesi.uniplan.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = UsuarioDatosEspecificosValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface UsuarioDatosEspecificosValid {
    String message() default "El tipo de usuario no coincide con la clase de datos_especificos";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
