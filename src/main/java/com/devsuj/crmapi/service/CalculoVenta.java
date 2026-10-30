package com.devsuj.crmapi.service;

import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.Promocion;

import java.math.BigDecimal;
import java.util.List;

/** Resultado de {@link CalculadoraVenta}: importes de cada renglón y del total, con la promoción aplicada. */
public record CalculoVenta(List<Renglon> renglones, BigDecimal subtotal, BigDecimal descuento, Promocion promocion,
        BigDecimal total) {

    /** Un renglón ya calculado. {@code promocion} es null si no hubo descuento. */
    public record Renglon(Producto producto, int cantidad, BigDecimal precioUnitario, Promocion promocion,
            BigDecimal descuento, BigDecimal subtotal) {
    }

    /** Un renglón pedido: producto y cantidad. */
    public record Pedido(Producto producto, int cantidad) {
    }
}
