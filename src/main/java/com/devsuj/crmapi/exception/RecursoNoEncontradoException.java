package com.devsuj.crmapi.exception;

/** El recurso pedido no existe (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super("No existe " + recurso + " con id " + id + ".");
    }
}
