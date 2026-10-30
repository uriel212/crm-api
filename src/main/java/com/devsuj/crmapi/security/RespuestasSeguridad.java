package com.devsuj.crmapi.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Respuestas 401 y 403 que genera el filtro de seguridad (antes de llegar a un controller),
 * con el mismo formato ProblemDetail que el resto de la API.
 */
@Component
@RequiredArgsConstructor
public class RespuestasSeguridad {

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPoint noAutenticado() {
        return (peticion, respuesta, ex) -> escribir(respuesta, HttpStatus.UNAUTHORIZED, "No autenticado",
                "Debes iniciar sesión o tu token es inválido o expiró.");
    }

    public AccessDeniedHandler accesoDenegado() {
        return (peticion, respuesta, ex) -> escribir(respuesta, HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permiso para realizar esta acción.");
    }

    private void escribir(HttpServletResponse respuesta, HttpStatus status, String titulo, String detalle)
            throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
        problema.setTitle(titulo);
        respuesta.setStatus(status.value());
        respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(respuesta.getOutputStream(), problema);
    }
}
