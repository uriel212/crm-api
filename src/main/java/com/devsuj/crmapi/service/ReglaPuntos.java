package com.devsuj.crmapi.service;

import java.math.BigDecimal;

/** Cuántos puntos gana un cliente por una compra (OCP: otra regla = otra implementación). */
public interface ReglaPuntos {

    int puntosPor(BigDecimal totalVenta);
}
