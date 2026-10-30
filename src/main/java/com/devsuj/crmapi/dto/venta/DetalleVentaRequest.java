package com.devsuj.crmapi.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Renglón de la venta. El precio y el descuento los calcula el servidor.")
public record DetalleVentaRequest(
        @Schema(example = "12") @NotNull(message = "El producto es obligatorio.") Integer productoId,
        @Schema(example = "5") @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        @Max(value = 10000, message = "La cantidad máxima por renglón es 10000.") Integer cantidad) {
}
