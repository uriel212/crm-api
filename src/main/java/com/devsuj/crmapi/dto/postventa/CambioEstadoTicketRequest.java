package com.devsuj.crmapi.dto.postventa;

import com.devsuj.crmapi.model.EstadoTicket;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoTicketRequest(
        @Schema(example = "EN_PROCESO") @NotNull(message = "El estado es obligatorio.") EstadoTicket estado) {
}
