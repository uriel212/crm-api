package com.devsuj.crmapi.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Activa o desactiva un usuario")
public record CambioEstadoUsuarioRequest(
        @Schema(example = "false") @NotNull(message = "Indica si el usuario queda activo.") Boolean activo,
        @Schema(description = "Confirma que los clientes del vendedor pasen a su gerente al desactivarlo",
                example = "true") Boolean reasignarClientes) {

    public boolean confirmaReasignacion() {
        return Boolean.TRUE.equals(reasignarClientes);
    }
}
