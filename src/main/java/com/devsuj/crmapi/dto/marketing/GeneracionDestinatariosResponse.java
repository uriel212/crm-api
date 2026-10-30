package com.devsuj.crmapi.dto.marketing;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de generar los destinatarios del segmento (idempotente)")
public record GeneracionDestinatariosResponse(
        @Schema(description = "Clientes nuevos agregados en esta llamada", example = "1") int agregados,
        @Schema(example = "3") long totalDestinatarios) {
}
