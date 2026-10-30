package com.devsuj.crmapi.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Conversión de oportunidades cerradas en el periodo (por la fecha de su cierre)")
public record ConversionResponse(
        @Schema(example = "2026-01-01") LocalDate desde,
        @Schema(example = "2026-10-20") LocalDate hasta,
        @Schema(example = "5") long ganadas,
        @Schema(example = "2") long perdidas,
        @Schema(description = "ganadas / (ganadas + perdidas) × 100", example = "71.43") BigDecimal tasaConversion,
        @Schema(description = "Abiertas hoy (no depende del periodo)", example = "9") long abiertas) {
}
