package com.devsuj.crmapi.dto.postventa;

import com.devsuj.crmapi.model.EstadoContrato;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Contrato con un cliente")
public record ContratoResponse(
        @Schema(example = "1") Integer id,
        @Schema(example = "1") Integer clienteId,
        @Schema(example = "Roberto Garza") String clienteNombre,
        @Schema(example = "4") Integer ventaId,
        @Schema(description = "Contrato del que este es renovación", example = "2") Integer contratoAnteriorId,
        @Schema(example = "Soporte y garantía extendida de 10 laptops") String titulo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        @Schema(example = "30000.00") BigDecimal monto,
        @Schema(example = "VIGENTE") EstadoContrato estado,
        @Schema(description = "Días que faltan para fechaFin (negativo si ya pasó); null si no está VIGENTE", example = "496")
        Long diasParaVencer,
        @Schema(description = "Vendedor asignado al cliente (null si no tiene); útil para saber quién lo gestiona",
                example = "3") Integer clienteVendedorId) {
}
