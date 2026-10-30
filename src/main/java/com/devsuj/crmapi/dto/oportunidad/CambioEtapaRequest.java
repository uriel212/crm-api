package com.devsuj.crmapi.dto.oportunidad;

import com.devsuj.crmapi.model.EtapaOportunidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Mueve la oportunidad a otra etapa abierta o la marca PERDIDA (GANADA la pone la venta)")
public record CambioEtapaRequest(
        @Schema(example = "PROPUESTA") @NotNull(message = "La etapa es obligatoria.") EtapaOportunidad etapa,
        @Schema(example = "Se envió la propuesta formal") @Size(max = 255, message = "Máximo 255 caracteres.")
        String comentario) {
}
