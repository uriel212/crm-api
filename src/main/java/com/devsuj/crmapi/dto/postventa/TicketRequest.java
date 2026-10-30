package com.devsuj.crmapi.dto.postventa;

import com.devsuj.crmapi.model.PrioridadTicket;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Nuevo ticket (nace ABIERTO)")
public record TicketRequest(
        @Schema(example = "1") @NotNull(message = "El cliente es obligatorio.") Integer clienteId,
        @Schema(example = "Monitor sin señal") @NotBlank(message = "El asunto es obligatorio.")
        @Size(max = 150, message = "El asunto admite máximo 150 caracteres.") String asunto,
        @Schema(example = "Uno de los monitores no muestra imagen por USB-C.")
        @Size(max = 5000, message = "La descripción admite máximo 5000 caracteres.") String descripcion,
        @Schema(example = "MEDIA") @NotNull(message = "La prioridad es obligatoria.") PrioridadTicket prioridad,
        @Schema(description = "Responsable (opcional; null = sin asignar). Un VENDEDOR solo puede asignarse a sí mismo.",
                example = "3") Integer usuarioId) {
}
