package com.devsuj.crmapi.dto.marketing;

import com.devsuj.crmapi.model.EstadoCampana;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Efectividad de una campaña (no se envían mensajes reales: se registran resultados)")
public record MetricasCampanaResponse(
        @Schema(example = "2") Integer campanaId,
        @Schema(example = "Regreso a clases 2026") String campanaNombre,
        @Schema(example = "FINALIZADA") EstadoCampana estado,
        @Schema(example = "9") long destinatarios,
        @Schema(description = "ABIERTO + RESPONDIO", example = "6") long abiertos,
        @Schema(example = "3") long respondieron,
        @Schema(description = "abiertos / destinatarios × 100", example = "66.67") BigDecimal tasaApertura,
        @Schema(description = "respondieron / destinatarios × 100", example = "33.33") BigDecimal tasaRespuesta,
        @Schema(example = "5000.00") BigDecimal presupuesto,
        @Schema(description = "presupuesto / respondieron; null si nadie respondió", example = "1666.67")
        BigDecimal costoPorRespuesta) {
}
