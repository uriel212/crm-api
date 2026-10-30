package com.devsuj.crmapi.dto.postventa;

import com.devsuj.crmapi.model.PrioridadTicket;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Corrige asunto, descripción y prioridad (no CERRADO). También es el cuerpo del seguimiento.")
public record TicketActualizarRequest(
        @Schema(example = "Monitor sin señal por USB-C") @NotBlank(message = "El asunto es obligatorio.")
        @Size(max = 150, message = "El asunto admite máximo 150 caracteres.") String asunto,
        @Size(max = 5000, message = "La descripción admite máximo 5000 caracteres.") String descripcion,
        @Schema(example = "ALTA") @NotNull(message = "La prioridad es obligatoria.") PrioridadTicket prioridad) {
}
