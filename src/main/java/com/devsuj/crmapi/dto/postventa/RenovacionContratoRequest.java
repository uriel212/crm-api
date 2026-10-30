package com.devsuj.crmapi.dto.postventa;

import com.devsuj.crmapi.validation.ConRangoFechas;
import com.devsuj.crmapi.validation.RangoFechasValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@RangoFechasValido
@Schema(description = "Renovación: contrato nuevo en BORRADOR ligado al anterior")
public record RenovacionContratoRequest(
        @Schema(description = "Opcional; por defecto el del contrato anterior", example = "Soporte anual 2027")
        @Size(max = 150, message = "El título admite máximo 150 caracteres.") String titulo,
        @Schema(example = "2027-03-15") @NotNull(message = "La fecha de inicio es obligatoria.") LocalDate fechaInicio,
        @Schema(example = "2028-03-14") @NotNull(message = "La fecha de fin es obligatoria.") LocalDate fechaFin,
        @Schema(example = "36000.00") @NotNull(message = "El monto es obligatorio.")
        @PositiveOrZero(message = "El monto no puede ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "Máximo 10 enteros y 2 decimales.") BigDecimal monto)
        implements ConRangoFechas {
}
