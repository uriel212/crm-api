package com.devsuj.crmapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Valida que {@code fechaFin >= fechaInicio} en un request que implementa {@link ConRangoFechas}. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RangoFechasValidador.class)
public @interface RangoFechasValido {

    String message() default "La fecha de fin no puede ser anterior a la fecha de inicio.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
