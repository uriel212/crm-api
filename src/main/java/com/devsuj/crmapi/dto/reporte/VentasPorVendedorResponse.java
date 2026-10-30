package com.devsuj.crmapi.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Ventas de un vendedor en el periodo")
public record VentasPorVendedorResponse(
        @Schema(example = "3") Integer usuarioId,
        @Schema(example = "Carla Mendoza") String usuarioNombre,
        @Schema(example = "8") long numeroVentas,
        @Schema(example = "350000.00") BigDecimal total,
        @Schema(example = "43750.00") BigDecimal ticketPromedio) {
}
