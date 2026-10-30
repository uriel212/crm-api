package com.devsuj.crmapi.dto.catalogo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoProductoRequest(
        @Schema(example = "false") @NotNull(message = "Indica si el producto queda activo.") Boolean activo) {
}
