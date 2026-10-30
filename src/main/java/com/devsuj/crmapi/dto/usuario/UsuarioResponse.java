package com.devsuj.crmapi.dto.usuario;

import com.devsuj.crmapi.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Usuario del sistema (nunca incluye la contraseña)")
public record UsuarioResponse(
        @Schema(example = "3") Integer id,
        @Schema(example = "Carla Mendoza") String nombre,
        @Schema(example = "carla.mendoza@crmventas.test") String email,
        @Schema(example = "VENDEDOR") Rol rol,
        @Schema(example = "2") Integer gerenteId,
        @Schema(example = "Luis Ramírez") String gerenteNombre,
        @Schema(example = "true") boolean activo,
        LocalDateTime creadoEn) {
}
