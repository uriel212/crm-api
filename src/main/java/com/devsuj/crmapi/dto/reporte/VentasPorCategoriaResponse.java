package com.devsuj.crmapi.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Ventas de una categoría (suma de renglones, antes del descuento sobre el total de la venta)")
public record VentasPorCategoriaResponse(
        @Schema(example = "1") Integer categoriaId,
        @Schema(example = "Laptops") String categoriaNombre,
        @Schema(example = "16") long unidades,
        @Schema(example = "289985.00") BigDecimal total) {
}
