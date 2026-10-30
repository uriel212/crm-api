package com.devsuj.crmapi.dto.catalogo;

import com.devsuj.crmapi.model.TipoMovimientoInventario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Ajuste manual: ENTRADA (llegó mercancía) o SALIDA (merma, conteo físico)")
public record MovimientoInventarioRequest(
        @Schema(example = "ENTRADA") @NotNull(message = "El tipo es obligatorio.") TipoMovimientoInventario tipo,
        @Schema(example = "10") @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que 0.") Integer cantidad,
        @Schema(example = "Factura de proveedor F-1234") @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 150) String motivo) {
}
