package com.devsuj.crmapi.dto.interaccion;

import com.devsuj.crmapi.model.EstadoInteraccion;
import com.devsuj.crmapi.model.TipoInteraccion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Registra un contacto (REALIZADA) o agenda una actividad futura (PROGRAMADA)")
public record InteraccionRequest(
        @Schema(example = "REUNION") @NotNull(message = "El tipo es obligatorio.") TipoInteraccion tipo,
        @Schema(description = "REALIZADA (por defecto) o PROGRAMADA", example = "PROGRAMADA") EstadoInteraccion estado,
        @Schema(description = "Opcional en REALIZADA (ahora); obligatoria y futura en PROGRAMADA",
                example = "2026-10-28T10:00:00") LocalDateTime fecha,
        @Schema(example = "Demo de laptops") @NotBlank(message = "El asunto es obligatorio.")
        @Size(max = 150, message = "El asunto admite máximo 150 caracteres.") String asunto,
        @Schema(example = "Llevar 2 equipos de muestra") @Size(max = 5000) String descripcion) {
}
