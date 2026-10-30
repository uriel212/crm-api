package com.devsuj.crmapi.dto.postventa;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Asigna un responsable; null lo deja sin asignar (no permitido EN_PROCESO)")
public record AsignarTicketRequest(@Schema(example = "4") Integer usuarioId) {
}
