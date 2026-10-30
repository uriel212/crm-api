package com.devsuj.crmapi.dto.oportunidad;

import com.devsuj.crmapi.model.EtapaOportunidad;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Un paso de la línea de tiempo de la oportunidad")
public record HistorialEtapaResponse(
        @Schema(example = "41") Integer id,
        @Schema(description = "Etapa de la que venía (null = creación)", example = "PROPUESTA") EtapaOportunidad etapaAnterior,
        @Schema(example = "CALIFICADA") EtapaOportunidad etapa,
        @Schema(description = "Cuándo entró a la etapa") LocalDateTime fechaInicio,
        @Schema(description = "Cuándo salió (null si es la etapa actual)") LocalDateTime fechaFin,
        @Schema(description = "Días completos en la etapa (hasta hoy si es la actual abierta; null si es el cierre)",
                example = "7") Long diasEnEtapa,
        @Schema(example = "3") Integer usuarioId,
        @Schema(example = "Carla Mendoza") String usuarioNombre,
        @Schema(example = "El cliente cambió el alcance") String comentario,
        @Schema(description = "Es la etapa actual", example = "false") boolean actual) {
}
