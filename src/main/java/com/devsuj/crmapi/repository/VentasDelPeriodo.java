package com.devsuj.crmapi.repository;

import java.math.BigDecimal;

/** Proyección: cuánto vendió un vendedor en un mes. */
public record VentasDelPeriodo(Integer usuarioId, Integer anio, Integer mes, Long numeroVentas, BigDecimal total) {
}
