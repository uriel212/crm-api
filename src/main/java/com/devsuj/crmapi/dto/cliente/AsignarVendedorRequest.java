package com.devsuj.crmapi.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nuevo responsable del cliente (VENDEDOR o GERENTE activo). null = sin responsable.")
public record AsignarVendedorRequest(@Schema(example = "4") Integer vendedorId) {
}
