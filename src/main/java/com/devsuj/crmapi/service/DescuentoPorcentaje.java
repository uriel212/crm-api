package com.devsuj.crmapi.service;

import com.devsuj.crmapi.model.TipoPromocion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** valor = % del importe, redondeado a centavos (HALF_UP). */
@Component
public class DescuentoPorcentaje implements EstrategiaDescuento {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    @Override
    public TipoPromocion tipo() {
        return TipoPromocion.PORCENTAJE;
    }

    @Override
    public BigDecimal calcular(BigDecimal importe, int unidades, BigDecimal valor) {
        BigDecimal descuento = importe.multiply(valor).divide(CIEN, 2, RoundingMode.HALF_UP);
        return descuento.min(importe);
    }
}
