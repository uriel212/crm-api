package com.devsuj.crmapi.dto.fidelizacion;

import com.devsuj.crmapi.model.TipoMovimientoPuntos;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Renglón del estado de cuenta de puntos (los puntos siempre son positivos)")
public record MovimientoPuntosResponse(
        @Schema(example = "30") Integer id,
        @Schema(example = "CANJEADO") TipoMovimientoPuntos tipo,
        @Schema(example = "1900") int puntos,
        @Schema(example = "Canje: 1 × Mouse Logitech MX Master 3S") String motivo,
        LocalDateTime fecha,
        @Schema(description = "Venta que generó los puntos (solo GANADO)", example = "4") Integer ventaId,
        @Schema(description = "Producto entregado (solo CANJEADO)", example = "7") Integer productoId,
        @Schema(example = "Mouse Logitech MX Master 3S") String productoNombre,
        @Schema(example = "1") Integer cantidad,
        @Schema(example = "3") Integer usuarioId,
        @Schema(example = "Carla Mendoza") String usuarioNombre) {
}
