package com.devsuj.crmapi.dto.oportunidad;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Nueva oportunidad (empieza en PROSPECTO)")
public record OportunidadRequest(
        @Schema(example = "11") @NotNull(message = "El cliente es obligatorio.") Integer clienteId,
        @Schema(example = "Punto de venta y laptop") @NotBlank(message = "El título es obligatorio.")
        @Size(max = 150, message = "El título admite máximo 150 caracteres.") String titulo,
        @Schema(example = "25000.00") @NotNull(message = "El monto estimado es obligatorio.")
        @PositiveOrZero(message = "El monto no puede ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "Máximo 10 enteros y 2 decimales.") BigDecimal montoEstimado,
        @Schema(example = "2026-12-15") LocalDate fechaCierreEstimada,
        @Schema(description = "Responsable (solo GERENTE/ADMIN; por defecto quien la registra)", example = "3")
        Integer usuarioId,
        @Schema(description = "Oportunidad PERDIDA del mismo cliente de la que esta es un nuevo intento", example = "6")
        Integer oportunidadAnteriorId) {
}
