package com.devsuj.crmapi.dto.meta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Meta mensual con su avance contra las ventas reales del vendedor en ese mes")
public record MetaVentaResponse(
        @Schema(example = "4") Integer id,
        @Schema(example = "3") Integer usuarioId,
        @Schema(example = "Carla Mendoza") String usuarioNombre,
        @Schema(example = "2026") int anio,
        @Schema(example = "10") int mes,
        @Schema(example = "65000.00") BigDecimal montoObjetivo,
        @Schema(description = "Suma del total de las ventas del vendedor en el mes", example = "12495.00")
        BigDecimal montoVendido,
        @Schema(example = "2") long numeroVentas,
        @Schema(description = "montoVendido / montoObjetivo × 100 (puede pasar de 100)", example = "19.22")
        BigDecimal porcentajeAvance,
        @Schema(description = "Lo que falta para cumplirla (0 si ya se cumplió)", example = "52505.00") BigDecimal faltante,
        @Schema(example = "false") boolean cumplida) {
}
