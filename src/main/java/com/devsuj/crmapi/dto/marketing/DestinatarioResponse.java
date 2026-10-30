package com.devsuj.crmapi.dto.marketing;

import com.devsuj.crmapi.model.ResultadoCampana;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cliente destinatario de una campaña y su resultado")
public record DestinatarioResponse(
        @Schema(example = "2") Integer campanaId,
        @Schema(example = "11") Integer clienteId,
        @Schema(example = "Miguel Ángel Vega") String clienteNombre,
        @Schema(example = "mvega@elfogon.test") String clienteEmail,
        @Schema(example = "3") Integer vendedorId,
        @Schema(example = "Carla Mendoza") String vendedorNombre,
        @Schema(example = "ABIERTO") ResultadoCampana resultado) {
}
