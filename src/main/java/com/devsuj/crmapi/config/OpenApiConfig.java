package com.devsuj.crmapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Datos generales de Swagger UI y esquema de seguridad "bearerAuth": con el botón Authorize se pega
 * el token de {@code POST /api/auth/login} y Swagger lo envía en el header Authorization.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CRM de Ventas · API",
                version = "1.0",
                description = """
                        API REST del CRM de Ventas. Montos en quetzales (Q), fechas en hora de Guatemala.
                        1) Llama a POST /api/auth/login con email y contraseña.
                        2) Copia el token y pégalo en el botón Authorize.
                        Roles: VENDEDOR (solo lo suyo), GERENTE (su equipo) y ADMIN (todo)."""),
        security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_SEGURIDAD))
@SecurityScheme(
        name = OpenApiConfig.ESQUEMA_SEGURIDAD,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {

    public static final String ESQUEMA_SEGURIDAD = "bearerAuth";
}
