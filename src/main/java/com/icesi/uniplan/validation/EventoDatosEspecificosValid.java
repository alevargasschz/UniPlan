package com.icesi.uniplan.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = EventoDatosEspecificosValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface EventoDatosEspecificosValid {
    String message() default "El tipo de evento no coincide con la clase de detalles";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
