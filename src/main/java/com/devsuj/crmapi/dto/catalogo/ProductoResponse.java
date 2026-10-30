package com.devsuj.crmapi.dto.catalogo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProductoResponse(
        @Schema(example = "7") Integer id,
        @Schema(example = "3") Integer categoriaId,
        @Schema(example = "Accesorios") String categoriaNombre,
        @Schema(example = "Mouse Logitech MX Master 3S") String nombre,
        String descripcion,
        @Schema(example = "1899.00") BigDecimal precio,
        @Schema(example = "1900") Integer precioPuntos,
        @Schema(example = "40") int stock,
        @Schema(example = "10") int stockMinimo,
        @Schema(example = "true") boolean activo,
        @Schema(description = "stock <= stockMinimo", example = "false") boolean stockBajo,
        @Schema(description = "Tiene precio en puntos", example = "true") boolean canjeable) {
}
