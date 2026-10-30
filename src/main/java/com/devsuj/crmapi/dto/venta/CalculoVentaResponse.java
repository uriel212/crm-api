package com.devsuj.crmapi.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Simulación de importes: lo que costaría la venta hoy, sin guardarla")
public record CalculoVentaResponse(
        List<DetalleVentaResponse> detalles,
        @Schema(description = "Suma de los subtotales de los renglones", example = "12995.00") BigDecimal subtotal,
        @Schema(description = "Descuento de la promoción sobre el total", example = "500.00") BigDecimal descuento,
        @Schema(example = "4") Integer promocionId,
        @Schema(example = "Q500 de descuento en compras desde Q10,000") String promocionNombre,
        @Schema(description = "subtotal − descuento", example = "12495.00") BigDecimal total) {
}
