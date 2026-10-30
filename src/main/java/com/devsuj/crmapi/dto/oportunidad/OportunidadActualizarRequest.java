package com.devsuj.crmapi.dto.oportunidad;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos editables de una oportunidad abierta")
public record OportunidadActualizarRequest(
        @Schema(example = "Punto de venta, laptop e impresora") @NotBlank(message = "El título es obligatorio.")
        @Size(max = 150) String titulo,
        @Schema(example = "32000.00") @NotNull(message = "El monto estimado es obligatorio.")
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal montoEstimado,
        @Schema(example = "2026-12-20") LocalDate fechaCierreEstimada) {
}
