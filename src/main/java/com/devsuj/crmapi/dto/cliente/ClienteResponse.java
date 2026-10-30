package com.devsuj.crmapi.dto.cliente;

import com.devsuj.crmapi.model.EstadoCliente;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Cliente o lead (PROSPECTO)")
public record ClienteResponse(
        @Schema(example = "1") Integer id,
        @Schema(example = "Roberto Garza") String nombre,
        @Schema(example = "Grupo Industrial del Norte") String empresa,
        @Schema(example = "rgarza@gin.test") String email,
        @Schema(example = "8112345678") String telefono,
        String direccion,
        @Schema(example = "ACTIVO") EstadoCliente estado,
        @Schema(example = "3") Integer vendedorId,
        @Schema(example = "Carla Mendoza") String vendedorNombre,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn) {
}
