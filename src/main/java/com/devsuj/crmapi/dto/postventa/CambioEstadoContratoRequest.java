package com.devsuj.crmapi.dto.postventa;

import com.devsuj.crmapi.model.EstadoContrato;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoContratoRequest(
        @Schema(example = "VIGENTE") @NotNull(message = "El estado es obligatorio.") EstadoContrato estado) {
}
