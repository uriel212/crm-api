package com.devsuj.crmapi.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Cambio de contraseña. passwordActual es obligatoria cuando el usuario cambia la suya.")
public record CambioPasswordRequest(
        @Schema(example = "Password123") String passwordActual,
        @Schema(example = "NuevaClave2026") @NotBlank(message = "La nueva contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.") String passwordNueva) {
}
