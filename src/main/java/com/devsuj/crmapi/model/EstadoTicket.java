package com.devsuj.crmapi.model;

import java.util.Map;
import java.util.Set;

/** ABIERTO ↔ EN_PROCESO → CERRADO (también ABIERTO → CERRADO). CERRADO no se reabre (P21). */
public enum EstadoTicket {
    ABIERTO, EN_PROCESO, CERRADO;

    private static final Map<EstadoTicket, Set<EstadoTicket>> TRANSICIONES = Map.of(
            ABIERTO, Set.of(EN_PROCESO, CERRADO),
            EN_PROCESO, Set.of(ABIERTO, CERRADO),
            CERRADO, Set.of());

    public boolean puedeCambiarA(EstadoTicket destino) {
        return TRANSICIONES.get(this).contains(destino);
    }
}
