package com.devsuj.crmapi.dto.fidelizacion;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado de cuenta de puntos de un cliente")
public record SaldoPuntosResponse(
        @Schema(example = "1") Integer clienteId,
        @Schema(example = "Roberto Garza") String clienteNombre,
        @Schema(example = "2324") long puntosGanados,
        @Schema(example = "1000") long puntosCanjeados,
        @Schema(description = "puntosGanados − puntosCanjeados", example = "1324") long saldo) {
}
