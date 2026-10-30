package com.devsuj.crmapi.repository;

/** Puntos ganados y canjeados de un cliente; saldo = ganados − canjeados. */
public record TotalesPuntos(long ganados, long canjeados) {

    /** Lo usa la consulta JPQL: la suma puede llegar como Integer o Long según la base. */
    public TotalesPuntos(Number ganados, Number canjeados) {
        this(ganados == null ? 0 : ganados.longValue(), canjeados == null ? 0 : canjeados.longValue());
    }

    public long saldo() {
        return ganados - canjeados;
    }
}
