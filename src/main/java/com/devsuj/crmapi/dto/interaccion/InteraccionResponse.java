package com.devsuj.crmapi.dto.interaccion;

import com.devsuj.crmapi.model.EstadoInteraccion;
import com.devsuj.crmapi.model.TipoInteraccion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Interacción o actividad")
public record InteraccionResponse(
        @Schema(example = "24") Integer id,
        @Schema(example = "14") Integer clienteId,
        @Schema(example = "Elena Rangel") String clienteNombre,
        @Schema(example = "5") Integer usuarioId,
        @Schema(example = "Sofía Herrera") String usuarioNombre,
        @Schema(example = "REUNION") TipoInteraccion tipo,
        @Schema(example = "PROGRAMADA") EstadoInteraccion estado,
        LocalDateTime fecha,
        @Schema(example = "Visita a la notaría") String asunto,
        String descripcion,
        String resultado,
        @Schema(description = "PROGRAMADA cuya fecha ya pasó", example = "false") boolean vencida) {
}
