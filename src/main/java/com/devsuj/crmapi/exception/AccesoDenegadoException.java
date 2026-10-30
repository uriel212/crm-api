package com.devsuj.crmapi.exception;

import org.springframework.security.access.AccessDeniedException;

/** El usuario tiene sesión pero el registro no está dentro de su alcance (HTTP 403). */
public class AccesoDenegadoException extends AccessDeniedException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
