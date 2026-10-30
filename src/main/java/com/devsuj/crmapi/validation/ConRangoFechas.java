package com.devsuj.crmapi.validation;

import java.time.LocalDate;

/** Lo implementan los requests con fecha de inicio y fin (campaña, promoción, contrato). */
public interface ConRangoFechas {

    LocalDate fechaInicio();

    LocalDate fechaFin();
}
