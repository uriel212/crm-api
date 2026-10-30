package com.devsuj.crmapi.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Venta concretada. Nunca se envían precios ni descuentos: los calcula el servidor.")
public record VentaRequest(
        @Schema(example = "1") @NotNull(message = "El cliente es obligatorio.") Integer clienteId,
        @Schema(description = "Oportunidad abierta del mismo cliente que esta venta gana (opcional)", example = "12")
        Integer oportunidadId,
        @Schema(description = "Vendedor responsable (solo GERENTE/ADMIN; por defecto quien la registra)", example = "3")
        Integer usuarioId,
        @NotEmpty(message = "La venta debe tener al menos un renglón.")
        @Size(max = 50, message = "Máximo 50 renglones por venta.")
        List<@Valid @NotNull(message = "El renglón no puede ser nulo.") DetalleVentaRequest> detalles) {
}
