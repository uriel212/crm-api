package com.devsuj.crmapi.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Reasigna varios clientes a un mismo responsable")
public record ReasignacionLoteRequest(
        @Schema(example = "[1, 2, 7]") @NotEmpty(message = "Indica al menos un cliente.")
        @Size(max = 500, message = "Máximo 500 clientes por lote.") List<@NotNull Integer> clienteIds,
        @Schema(example = "4") @NotNull(message = "Indica el nuevo responsable.") Integer vendedorId) {
}
