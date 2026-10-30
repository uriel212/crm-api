package com.devsuj.crmapi.config;

import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

/** Agrega 401 y 403 a todos los endpoints protegidos para no repetirlos en cada controller (DRY). */
@Component
public class OpenApiRespuestasComunes implements OpenApiCustomizer {

    @Override
    public void customise(io.swagger.v3.oas.models.OpenAPI openApi) {
        openApi.getPaths().forEach((ruta, item) -> {
            if (ruta.equals("/api/auth/login")) {
                return;
            }
            item.readOperations().forEach(operacion -> {
                operacion.getResponses().putIfAbsent("401",
                        new ApiResponse().description("Sin token o token inválido/expirado"));
                operacion.getResponses().putIfAbsent("403",
                        new ApiResponse().description("El rol o el alcance del usuario no lo permite"));
            });
        });
    }
}
