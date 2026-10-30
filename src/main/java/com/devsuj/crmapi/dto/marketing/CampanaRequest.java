package com.devsuj.crmapi.dto.marketing;

import com.devsuj.crmapi.model.CanalCampana;
import com.devsuj.crmapi.model.SegmentoCampana;
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
@Schema(description = "Datos de una campaña. Nace PLANEADA; el estado se cambia con PATCH /estado.")
public record CampanaRequest(
        @Schema(example = "Fin de año 2026") @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres.") String nombre,
        @Schema(example = "CORREO") @NotNull(message = "El canal es obligatorio.") CanalCampana canal,
        @Schema(example = "Cierra el año con equipo nuevo") @NotBlank(message = "El asunto es obligatorio.")
        @Size(max = 150, message = "El asunto admite máximo 150 caracteres.") String asunto,
        @Schema(example = "Aprovecha Q500 de descuento en compras desde Q10,000.") @NotBlank(message = "El mensaje es obligatorio.")
        @Size(max = 5000, message = "El mensaje admite máximo 5000 caracteres.") String mensaje,
        @Schema(example = "ACTIVOS") @NotNull(message = "El segmento es obligatorio.") SegmentoCampana segmento,
        @Schema(example = "2026-12-01") @NotNull(message = "La fecha de inicio es obligatoria.") LocalDate fechaInicio,
        @Schema(example = "2026-12-31") @NotNull(message = "La fecha de fin es obligatoria.") LocalDate fechaFin,
        @Schema(example = "3000.00") @NotNull(message = "El presupuesto es obligatorio.")
        @PositiveOrZero(message = "El presupuesto no puede ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "Máximo 8 enteros y 2 decimales.") BigDecimal presupuesto)
        implements ConRangoFechas {
}
