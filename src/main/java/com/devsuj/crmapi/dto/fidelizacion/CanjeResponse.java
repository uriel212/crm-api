package com.devsuj.crmapi.dto.fidelizacion;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Canje registrado y saldo que le queda al cliente")
public record CanjeResponse(
        MovimientoPuntosResponse movimiento,
        @Schema(example = "1324") long saldo) {
}
