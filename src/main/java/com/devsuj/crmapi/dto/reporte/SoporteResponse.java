package com.devsuj.crmapi.dto.reporte;

import com.devsuj.crmapi.model.PrioridadTicket;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Situación del soporte al cliente en el alcance del usuario")
public record SoporteResponse(
        @Schema(example = "2") long abiertos,
        @Schema(example = "1") long enProceso,
        @Schema(example = "3") long cerrados,
        @Schema(description = "Sin cerrar y sin responsable", example = "1") long sinAsignar,
        @Schema(description = "Horas promedio entre la creación y el cierre; null si no hay cerrados", example = "60.00")
        BigDecimal horasPromedioCierre,
        @Schema(description = "Tickets sin cerrar por prioridad (siempre BAJA, MEDIA, ALTA)") List<PorPrioridad> pendientesPorPrioridad) {

    public record PorPrioridad(@Schema(example = "ALTA") PrioridadTicket prioridad, @Schema(example = "1") long cantidad) {
    }
}
