package com.devsuj.crmapi.service;

import com.devsuj.crmapi.model.TipoPromocion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** valor = quetzales por unidad (en un renglón) o una sola vez (sobre el total). Nunca más que el importe. */
@Component
public class DescuentoMontoFijo implements EstrategiaDescuento {

    @Override
    public TipoPromocion tipo() {
        return TipoPromocion.MONTO_FIJO;
    }

    @Override
    public BigDecimal calcular(BigDecimal importe, int unidades, BigDecimal valor) {
        BigDecimal descuento = valor.multiply(BigDecimal.valueOf(unidades)).setScale(2, RoundingMode.HALF_UP);
        return descuento.min(importe);
    }
}
