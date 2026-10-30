package com.devsuj.crmapi.dto.interaccion;

import com.devsuj.crmapi.model.EstadoInteraccion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Cierra una actividad PROGRAMADA como REALIZADA (con su resultado) o CANCELADA")
public record CambioEstadoInteraccionRequest(
        @Schema(example = "REALIZADA") @NotNull(message = "El estado es obligatorio.") EstadoInteraccion estado,
        @Schema(example = "El cliente pidió cotización formal") @Size(max = 5000) String resultado) {
}
