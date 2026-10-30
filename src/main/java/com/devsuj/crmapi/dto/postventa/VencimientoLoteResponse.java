package com.devsuj.crmapi.dto.postventa;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Contratos VIGENTE con fecha de fin pasada que se marcaron VENCIDO")
public record VencimientoLoteResponse(
        @Schema(example = "1") int vencidos,
        @Schema(example = "[5]") List<Integer> contratoIds) {
}
