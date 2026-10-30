package com.devsuj.crmapi.dto.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoPromocionRequest(
        @Schema(example = "false") @NotNull(message = "Indica si la promoción queda activa.") Boolean activa) {
}
