package com.devsuj.crmapi.model;

/**
 * Etapas del pipeline. Regla (provisional, P8): entre etapas abiertas se avanza o retrocede libremente;
 * cualquier abierta puede pasar a PERDIDA; GANADA solo la pone una venta ligada; GANADA y PERDIDA son finales.
 */
public enum EtapaOportunidad {
    PROSPECTO, CALIFICADA, PROPUESTA, NEGOCIACION, GANADA, PERDIDA;

    public boolean esCerrada() {
        return this == GANADA || this == PERDIDA;
    }

    /** Transición manual (endpoint de etapa). GANADA no se pone a mano: la pone la venta. */
    public boolean puedeCambiarA(EtapaOportunidad destino) {
        return !esCerrada() && destino != this && destino != GANADA;
    }
}
