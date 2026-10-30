package com.devsuj.crmapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RangoFechasValidador implements ConstraintValidator<RangoFechasValido, ConRangoFechas> {

    @Override
    public boolean isValid(ConRangoFechas valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.fechaInicio() == null || valor.fechaFin() == null) {
            return true;
        }
        if (!valor.fechaFin().isBefore(valor.fechaInicio())) {
            return true;
        }
        contexto.disableDefaultConstraintViolation();
        contexto.buildConstraintViolationWithTemplate(contexto.getDefaultConstraintMessageTemplate())
                .addPropertyNode("fechaFin").addConstraintViolation();
        return false;
    }
}
