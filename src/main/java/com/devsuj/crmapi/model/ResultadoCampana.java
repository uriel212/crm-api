package com.devsuj.crmapi.model;

/** Resultado de un destinatario; solo avanza: ENVIADO → ABIERTO → RESPONDIO. */
public enum ResultadoCampana {
    ENVIADO, ABIERTO, RESPONDIO;

    public boolean puedeCambiarA(ResultadoCampana destino) {
        return destino.ordinal() > ordinal();
    }
}
