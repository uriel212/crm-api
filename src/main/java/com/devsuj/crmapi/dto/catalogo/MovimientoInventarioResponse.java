package com.devsuj.crmapi.dto.catalogo;

import com.devsuj.crmapi.model.TipoMovimientoInventario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Renglón del kardex")
public record MovimientoInventarioResponse(
        @Schema(example = "15") Integer id,
        @Schema(example = "3") Integer productoId,
        @Schema(example = "MacBook Air 13 M3") String productoNombre,
        @Schema(example = "ENTRADA") TipoMovimientoInventario tipo,
        @Schema(example = "10") int cantidad,
        @Schema(example = "12") int stockResultante,
        @Schema(example = "Factura de proveedor F-1234") String motivo,
        @Schema(example = "2") Integer usuarioId,
        @Schema(example = "Luis Ramírez") String usuarioNombre,
        @Schema(description = "Venta que originó la salida (tipo VENTA)", example = "27") Integer ventaId,
        LocalDateTime fecha) {
}
