package com.devsuj.crmapi.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Renglón con el precio congelado al momento de la venta")
public record DetalleVentaResponse(
        @Schema(example = "12") Integer productoId,
        @Schema(example = "Microsoft 365 Empresa Estándar") String productoNombre,
        @Schema(example = "5") int cantidad,
        @Schema(example = "2899.00") BigDecimal precioUnitario,
        @Schema(description = "Descuento del renglón por promoción de producto", example = "1500.00") BigDecimal descuento,
        @Schema(description = "cantidad × precioUnitario − descuento", example = "12995.00") BigDecimal subtotal,
        @Schema(example = "3") Integer promocionId,
        @Schema(example = "Q300 menos por licencia de Microsoft 365") String promocionNombre) {
}
