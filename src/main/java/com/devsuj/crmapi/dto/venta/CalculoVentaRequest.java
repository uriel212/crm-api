package com.devsuj.crmapi.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Renglones a cotizar (no se guarda nada). Acepta el mismo cuerpo que POST /api/ventas.")
public record CalculoVentaRequest(
        @NotEmpty(message = "La venta debe tener al menos un renglón.")
        @Size(max = 50, message = "Máximo 50 renglones por venta.")
        List<@Valid @NotNull(message = "El renglón no puede ser nulo.") DetalleVentaRequest> detalles) {
}
