package com.devsuj.crmapi.dto.postventa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Extiende un contrato VIGENTE: nueva fecha de fin, posterior a la actual")
public record ExtensionContratoRequest(
        @Schema(example = "2028-12-31") @NotNull(message = "La nueva fecha de fin es obligatoria.") LocalDate fechaFin) {
}
