package com.devsuj.crmapi.dto.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Meta mensual de un VENDEDOR (una por vendedor y mes)")
public record MetaVentaRequest(
        @Schema(example = "3") @NotNull(message = "El vendedor es obligatorio.") Integer usuarioId,
        @Schema(example = "2026") @NotNull(message = "El año es obligatorio.")
        @Min(value = 2020, message = "El año mínimo es 2020.") @Max(value = 2100, message = "El año máximo es 2100.")
        Integer anio,
        @Schema(example = "11") @NotNull(message = "El mes es obligatorio.")
        @Min(value = 1, message = "El mes va de 1 a 12.") @Max(value = 12, message = "El mes va de 1 a 12.") Integer mes,
        @Schema(example = "70000.00") @NotNull(message = "El monto objetivo es obligatorio.")
        @Positive(message = "El monto objetivo debe ser mayor que cero.")
        @Digits(integer = 10, fraction = 2, message = "Máximo 10 enteros y 2 decimales.") BigDecimal montoObjetivo) {
}
