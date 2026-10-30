package com.devsuj.crmapi.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Venta concretada con sus renglones")
public record VentaResponse(
        @Schema(example = "27") Integer id,
        @Schema(example = "1") Integer clienteId,
        @Schema(example = "Roberto Garza") String clienteNombre,
        @Schema(example = "3") Integer usuarioId,
        @Schema(example = "Carla Mendoza") String usuarioNombre,
        @Schema(example = "12") Integer oportunidadId,
        LocalDateTime fecha,
        @Schema(example = "12995.00") BigDecimal subtotal,
        @Schema(example = "500.00") BigDecimal descuento,
        @Schema(example = "4") Integer promocionId,
        @Schema(example = "Q500 de descuento en compras desde Q10,000") String promocionNombre,
        @Schema(example = "12495.00") BigDecimal total,
        @Schema(description = "Puntos de fidelización que ganó el cliente con esta venta", example = "124")
        Integer puntosGanados,
        List<DetalleVentaResponse> detalles) {
}
