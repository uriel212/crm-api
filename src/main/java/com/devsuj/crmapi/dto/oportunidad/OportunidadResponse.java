package com.devsuj.crmapi.dto.oportunidad;

import com.devsuj.crmapi.model.EtapaOportunidad;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Oportunidad del pipeline")
public record OportunidadResponse(
        @Schema(example = "9") Integer id,
        @Schema(example = "11") Integer clienteId,
        @Schema(example = "Miguel Ángel Vega") String clienteNombre,
        @Schema(example = "3") Integer usuarioId,
        @Schema(example = "Carla Mendoza") String usuarioNombre,
        @Schema(example = "Punto de venta y laptop") String titulo,
        @Schema(example = "25000.00") BigDecimal montoEstimado,
        @Schema(example = "PROSPECTO") EtapaOportunidad etapa,
        LocalDate fechaCierreEstimada,
        LocalDateTime creadoEn,
        @Schema(example = "6") Integer oportunidadAnteriorId) {
}
