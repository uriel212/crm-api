package com.devsuj.crmapi.dto.reporte;

import com.devsuj.crmapi.model.EtapaOportunidad;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Oportunidades en una etapa del pipeline (siempre se devuelven las 6 etapas)")
public record PipelineEtapaResponse(
        @Schema(example = "PROPUESTA") EtapaOportunidad etapa,
        @Schema(example = "2") long cantidad,
        @Schema(example = "130000.00") BigDecimal montoEstimado) {
}
