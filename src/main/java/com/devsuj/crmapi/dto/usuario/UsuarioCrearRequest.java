package com.devsuj.crmapi.dto.usuario;

import com.devsuj.crmapi.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear un usuario")
public record UsuarioCrearRequest(
        @Schema(example = "Pedro Gómez") @NotBlank(message = "El nombre es obligatorio.") @Size(max = 100) String nombre,
        @Schema(example = "pedro.gomez@crmventas.test") @NotBlank(message = "El email es obligatorio.")
        @Email(message = "El email no es válido.") @Size(max = 150) String email,
        @Schema(description = "Contraseña inicial (8 a 72 caracteres)", example = "Password123")
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.") String password,
        @Schema(example = "VENDEDOR") @NotNull(message = "El rol es obligatorio.") Rol rol,
        @Schema(description = "Gerente del equipo (solo para VENDEDOR)", example = "2") Integer gerenteId) {
}
