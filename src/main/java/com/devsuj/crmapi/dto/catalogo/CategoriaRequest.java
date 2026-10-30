package com.devsuj.crmapi.dto.catalogo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @Schema(example = "Redes") @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre admite máximo 80 caracteres.") String nombre,
        @Schema(example = "Routers, switches y access points") @Size(max = 255) String descripcion) {
}
