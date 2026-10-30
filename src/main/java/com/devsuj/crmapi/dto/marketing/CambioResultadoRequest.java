package com.devsuj.crmapi.dto.marketing;

import com.devsuj.crmapi.model.ResultadoCampana;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioResultadoRequest(
        @Schema(description = "Solo avanza: ENVIADO → ABIERTO → RESPONDIO", example = "RESPONDIO")
        @NotNull(message = "El resultado es obligatorio.") ResultadoCampana resultado) {
}
