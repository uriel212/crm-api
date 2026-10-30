package com.devsuj.crmapi.dto.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record DestinatarioRequest(
        @Schema(example = "11") @NotNull(message = "El cliente es obligatorio.") Integer clienteId) {
}
