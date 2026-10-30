package com.devsuj.crmapi.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Porcentajes con 2 decimales (DRY entre metas y reportes). */
public final class Porcentajes {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private Porcentajes() {
    }

    /** parte / total × 100; 0 si el total es 0 o null. */
    public static BigDecimal de(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0 || parte == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        return parte.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal de(long parte, long total) {
        return de(BigDecimal.valueOf(parte), BigDecimal.valueOf(total));
    }
}
