package com.devsuj.crmapi.dto.fidelizacion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Canje de puntos por un producto con precio en puntos")
public record CanjeRequest(
        @Schema(example = "7") @NotNull(message = "El producto es obligatorio.") Integer productoId,
        @Schema(example = "1") @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        @Max(value = 100, message = "La cantidad máxima por canje es 100.") Integer cantidad) {
}
