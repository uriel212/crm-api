package com.devsuj.crmapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de acceso")
public record LoginRequest(
        @Schema(example = "ana.torres@crmventas.test") @NotBlank(message = "El email es obligatorio.") String email,
        @Schema(example = "Password123") @NotBlank(message = "La contraseña es obligatoria.") String password) {
}
