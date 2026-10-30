package com.devsuj.crmapi.exception;

/**
 * Los datos son válidos en forma pero incoherentes entre sí (HTTP 400). Si se indica {@code campo}, el
 * ProblemDetail trae {@code errores: {campo: mensaje}}, igual que un error de Bean Validation.
 */
public class DatosInvalidosException extends RuntimeException {

    private final String campo;

    public DatosInvalidosException(String mensaje) {
        this(mensaje, null);
    }

    public DatosInvalidosException(String mensaje, String campo) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
