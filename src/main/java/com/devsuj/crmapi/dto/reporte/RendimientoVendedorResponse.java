package com.devsuj.crmapi.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Rendimiento de un VENDEDOR en un mes: ventas contra meta y conversión de oportunidades")
public record RendimientoVendedorResponse(
        @Schema(example = "3") Integer usuarioId,
        @Schema(example = "Carla Mendoza") String usuarioNombre,
        @Schema(example = "1") long numeroVentas,
        @Schema(example = "25794.00") BigDecimal totalVendido,
        @Schema(description = "null si no tiene meta ese mes", example = "65000.00") BigDecimal metaObjetivo,
        @Schema(description = "totalVendido / metaObjetivo × 100; null sin meta", example = "39.68") BigDecimal avanceMeta,
        @Schema(example = "0") long oportunidadesGanadas,
        @Schema(example = "0") long oportunidadesPerdidas,
        @Schema(example = "0.00") BigDecimal tasaConversion) {
}
