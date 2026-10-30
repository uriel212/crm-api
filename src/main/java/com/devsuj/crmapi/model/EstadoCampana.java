package com.devsuj.crmapi.model;

/** FINALIZADA y CANCELADA cierran la campaña (no se edita), pero el estado se puede corregir a mano. */
public enum EstadoCampana {
    PLANEADA, ACTIVA, FINALIZADA, CANCELADA;

    public boolean estaCerrada() {
        return this == FINALIZADA || this == CANCELADA;
    }
}
