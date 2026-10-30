package com.devsuj.crmapi.dto.cliente;

import com.devsuj.crmapi.model.EstadoCliente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoClienteRequest(
        @Schema(example = "INACTIVO") @NotNull(message = "El estado es obligatorio.") EstadoCliente estado) {
}
