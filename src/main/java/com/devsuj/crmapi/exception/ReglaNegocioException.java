package com.devsuj.crmapi.exception;

import java.util.Map;

/**
 * Una regla del negocio impide la operación (HTTP 409). {@code datos} viaja como propiedades extra
 * del ProblemDetail para que el frontend pueda reaccionar (por ejemplo, cuántos clientes tiene un vendedor).
 */
public class ReglaNegocioException extends RuntimeException {

    private final transient Map<String, Object> datos;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, Map.of());
    }

    public ReglaNegocioException(String mensaje, Map<String, Object> datos) {
        super(mensaje);
        this.datos = Map.copyOf(datos);
    }

    public Map<String, Object> getDatos() {
        return datos;
    }
}
