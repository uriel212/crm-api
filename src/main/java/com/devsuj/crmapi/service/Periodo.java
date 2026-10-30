package com.devsuj.crmapi.service;

import com.devsuj.crmapi.exception.DatosInvalidosException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Rango de días completos [desde, hasta] (ambos incluidos) para filtros y reportes. Para consultar se usa
 * {@link #inicio()} (inclusive) y {@link #fin()} (exclusivo: el día siguiente a {@code hasta}).
 */
public record Periodo(LocalDate desde, LocalDate hasta) {

    public Periodo {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new DatosInvalidosException("La fecha 'hasta' no puede ser anterior a 'desde'.");
        }
    }

    /** Usa los valores por defecto cuando no llega alguna fecha. */
    public static Periodo de(LocalDate desde, LocalDate hasta, LocalDate desdePorDefecto, LocalDate hastaPorDefecto) {
        return new Periodo(desde != null ? desde : desdePorDefecto, hasta != null ? hasta : hastaPorDefecto);
    }

    /** Del primer día del mes de {@code hoy} hasta {@code hoy}. */
    public static Periodo mesEnCurso(LocalDate desde, LocalDate hasta, LocalDate hoy) {
        return de(desde, hasta, hoy.withDayOfMonth(1), hoy);
    }

    public LocalDateTime inicio() {
        return desde == null ? null : desde.atStartOfDay();
    }

    public LocalDateTime fin() {
        return hasta == null ? null : hasta.plusDays(1).atStartOfDay();
    }
}
