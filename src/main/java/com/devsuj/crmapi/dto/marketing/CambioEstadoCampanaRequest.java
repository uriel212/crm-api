package com.devsuj.crmapi.dto.marketing;

import com.devsuj.crmapi.model.EstadoCampana;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoCampanaRequest(
        @Schema(example = "ACTIVA") @NotNull(message = "El estado es obligatorio.") EstadoCampana estado) {
}
