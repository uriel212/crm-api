package com.devsuj.crmapi.dto.postventa;

import com.devsuj.crmapi.validation.ConRangoFechas;
import com.devsuj.crmapi.validation.RangoFechasValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@RangoFechasValido
@Schema(description = "Contrato con un cliente. Nace en BORRADOR y solo se edita en BORRADOR.")
public record ContratoRequest(
        @Schema(example = "1") @NotNull(message = "El cliente es obligatorio.") Integer clienteId,
        @Schema(description = "Venta del mismo cliente que lo originó (opcional)", example = "4") Integer ventaId,
        @Schema(example = "Soporte anual de equipos") @NotBlank(message = "El título es obligatorio.")
        @Size(max = 150, message = "El título admite máximo 150 caracteres.") String titulo,
        @Schema(example = "2026-11-01") @NotNull(message = "La fecha de inicio es obligatoria.") LocalDate fechaInicio,
        @Schema(example = "2027-10-31") @NotNull(message = "La fecha de fin es obligatoria.") LocalDate fechaFin,
        @Schema(example = "24000.00") @NotNull(message = "El monto es obligatorio.")
        @PositiveOrZero(message = "El monto no puede ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "Máximo 10 enteros y 2 decimales.") BigDecimal monto)
        implements ConRangoFechas {
}
