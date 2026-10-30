package com.devsuj.crmapi.service;

import com.devsuj.crmapi.model.TipoPromocion;

import java.math.BigDecimal;

/**
 * Cómo descuenta un tipo de promoción (OCP: un tipo nuevo es una clase nueva, sin tocar la calculadora).
 * El resultado nunca supera el importe.
 */
public interface EstrategiaDescuento {

    TipoPromocion tipo();

    /**
     * @param importe  lo que se descuenta (cantidad × precio de un renglón, o el subtotal de la venta)
     * @param unidades unidades del renglón; 1 cuando se descuenta sobre el total de la venta
     * @param valor    valor de la promoción (porcentaje o quetzales)
     */
    BigDecimal calcular(BigDecimal importe, int unidades, BigDecimal valor);
}
