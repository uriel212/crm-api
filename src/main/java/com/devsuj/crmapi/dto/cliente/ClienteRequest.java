package com.devsuj.crmapi.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos de contacto de un cliente o lead. El estado se cambia con PATCH /estado.")
public record ClienteRequest(
        @Schema(example = "Roberto Garza") @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres.") String nombre,
        @Schema(example = "Grupo Industrial del Norte") @Size(max = 100, message = "La empresa admite máximo 100 caracteres.") String empresa,
        @Schema(example = "rgarza@gin.test") @NotBlank(message = "El email es obligatorio.")
        @Email(message = "El email no es válido.") @Size(max = 150) String email,
        @Schema(example = "55123456") @Size(max = 20, message = "El teléfono admite máximo 20 caracteres.") String telefono,
        @Schema(example = "6a. Avenida 10-50, Zona 1, Guatemala") @Size(max = 255) String direccion,
        @Schema(description = """
                Responsable. Al crear: un VENDEDOR siempre queda como responsable; GERENTE/ADMIN pueden indicarlo.
                Al actualizar: null = no cambia; un valor distinto reasigna (solo GERENTE/ADMIN).""", example = "3")
        Integer vendedorId) {
}
