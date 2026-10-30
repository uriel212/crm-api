package com.devsuj.crmapi.dto.catalogo;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoriaResponse(
        @Schema(example = "1") Integer id,
        @Schema(example = "Laptops") String nombre,
        String descripcion) {
}
