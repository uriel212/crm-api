package com.devsuj.crmapi.model;

import java.util.Map;
import java.util.Set;

/** BORRADOR → VIGENTE | CANCELADO; VIGENTE → VENCIDO | CANCELADO. VENCIDO y CANCELADO son finales. */
public enum EstadoContrato {
    BORRADOR, VIGENTE, VENCIDO, CANCELADO;

    private static final Map<EstadoContrato, Set<EstadoContrato>> TRANSICIONES = Map.of(
            BORRADOR, Set.of(VIGENTE, CANCELADO),
            VIGENTE, Set.of(VENCIDO, CANCELADO),
            VENCIDO, Set.of(),
            CANCELADO, Set.of());

    public boolean puedeCambiarA(EstadoContrato destino) {
        return TRANSICIONES.get(this).contains(destino);
    }
}
