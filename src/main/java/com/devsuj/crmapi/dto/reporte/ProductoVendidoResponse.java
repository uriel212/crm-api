package com.devsuj.crmapi.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Producto con sus unidades vendidas en el periodo")
public record ProductoVendidoResponse(
        @Schema(example = "13") Integer productoId,
        @Schema(example = "ESET Endpoint Security") String productoNombre,
        @Schema(example = "60") long unidades,
        @Schema(example = "53940.00") BigDecimal total) {
}
