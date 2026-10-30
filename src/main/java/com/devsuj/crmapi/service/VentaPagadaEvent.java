package com.devsuj.crmapi.service;

import com.devsuj.crmapi.model.Venta;

/**
 * Se publica al registrar una venta, dentro de su transacción. Otros módulos (fidelización) reaccionan
 * sin que VentaService los conozca (bajo acoplamiento).
 */
public record VentaPagadaEvent(Venta venta) {
}
