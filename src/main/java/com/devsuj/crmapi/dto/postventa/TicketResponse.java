package com.devsuj.crmapi.dto.postventa;

import com.devsuj.crmapi.model.EstadoTicket;
import com.devsuj.crmapi.model.PrioridadTicket;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Ticket de soporte de un cliente")
public record TicketResponse(
        @Schema(example = "4") Integer id,
        @Schema(example = "2") Integer clienteId,
        @Schema(example = "Mariana López") String clienteNombre,
        @Schema(description = "Responsable; null = sin asignar", example = "3") Integer usuarioId,
        @Schema(example = "Carla Mendoza") String usuarioNombre,
        @Schema(example = "Activación de licencias pendiente") String asunto,
        String descripcion,
        @Schema(example = "ALTA") PrioridadTicket prioridad,
        @Schema(example = "EN_PROCESO") EstadoTicket estado,
        LocalDateTime creadoEn,
        @Schema(description = "Solo si está CERRADO") LocalDateTime cerradoEn,
        @Schema(description = "Ticket CERRADO del que este es seguimiento", example = "1") Integer ticketRelacionadoId,
        @Schema(description = "Vendedor asignado al cliente (null si no tiene); útil para saber quién lo gestiona",
                example = "3") Integer clienteVendedorId) {
}
