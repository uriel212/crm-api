package com.devsuj.crmapi.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 1 punto por cada Q100 completos del total (Q12,495 → 124 puntos), igual que el seed. */
@Component
public class UnPuntoPorCadaCienQuetzales implements ReglaPuntos {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    @Override
    public int puntosPor(BigDecimal totalVenta) {
        return totalVenta.divide(CIEN, 0, RoundingMode.FLOOR).intValueExact();
    }
}
