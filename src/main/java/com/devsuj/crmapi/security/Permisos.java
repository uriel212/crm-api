package com.devsuj.crmapi.security;

/** Expresiones de {@code @PreAuthorize} reutilizables (DRY): un solo lugar para los roles de cada regla. */
public final class Permisos {

    public static final String GERENTE_O_ADMIN = "hasAnyRole('GERENTE', 'ADMIN')";
    public static final String SOLO_ADMIN = "hasRole('ADMIN')";

    private Permisos() {
    }
}
