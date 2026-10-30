package com.devsuj.crmapi.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Ventas de un mes (siempre se devuelven los 12 meses, con 0 si no hubo ventas)")
public record VentasPorMesResponse(
        @Schema(example = "2026") int anio,
        @Schema(example = "9") int mes,
        @Schema(example = "3") long numeroVentas,
        @Schema(example = "84239.00") BigDecimal total) {
}
