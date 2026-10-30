package com.devsuj.crmapi.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Venta en un listado (sin renglones)")
public record VentaResumenResponse(
        @Schema(example = "27") Integer id,
        @Schema(example = "1") Integer clienteId,
        @Schema(example = "Roberto Garza") String clienteNombre,
        @Schema(example = "3") Integer usuarioId,
        @Schema(example = "Carla Mendoza") String usuarioNombre,
        @Schema(example = "12") Integer oportunidadId,
        LocalDateTime fecha,
        @Schema(example = "12995.00") BigDecimal subtotal,
        @Schema(example = "500.00") BigDecimal descuento,
        @Schema(example = "12495.00") BigDecimal total) {
}
