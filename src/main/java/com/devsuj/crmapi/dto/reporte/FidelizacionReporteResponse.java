package com.devsuj.crmapi.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Programa de puntos en el alcance del usuario (clientes por su vendedor asignado)")
public record FidelizacionReporteResponse(
        @Schema(example = "7543") long puntosEmitidos,
        @Schema(example = "1500") long puntosCanjeados,
        @Schema(description = "emitidos − canjeados", example = "6043") long saldoEnCirculacion,
        @Schema(description = "Clientes con saldo mayor que 0", example = "10") long clientesConSaldo,
        @Schema(description = "Los 5 clientes con más saldo") List<ClienteConSaldo> topClientes) {

    public record ClienteConSaldo(@Schema(example = "1") Integer clienteId,
            @Schema(example = "Roberto Garza") String clienteNombre, @Schema(example = "1330") long saldo) {
    }
}
