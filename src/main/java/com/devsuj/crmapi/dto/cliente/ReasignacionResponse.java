package com.devsuj.crmapi.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReasignacionResponse(
        @Schema(example = "3") int reasignados,
        @Schema(example = "4") Integer vendedorId,
        @Schema(example = "Jorge Pérez") String vendedorNombre) {
}
