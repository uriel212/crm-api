package com.devsuj.crmapi.dto.interaccion;

import com.devsuj.crmapi.model.TipoInteraccion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Corrige o reprograma una interacción (el estado se cambia con PATCH /estado)")
public record InteraccionActualizarRequest(
        @Schema(example = "LLAMADA") @NotNull(message = "El tipo es obligatorio.") TipoInteraccion tipo,
        @Schema(example = "2026-10-29T09:00:00") @NotNull(message = "La fecha es obligatoria.") LocalDateTime fecha,
        @Schema(example = "Seguimiento de cotización") @NotBlank(message = "El asunto es obligatorio.")
        @Size(max = 150) String asunto,
        @Size(max = 5000) String descripcion) {
}
