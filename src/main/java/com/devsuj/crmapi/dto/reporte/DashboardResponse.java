package com.devsuj.crmapi.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "KPIs del tablero para el periodo (por defecto, el mes en curso) y el alcance del usuario")
public record DashboardResponse(
        @Schema(example = "2026-10-01") LocalDate desde,
        @Schema(example = "2026-10-20") LocalDate hasta,
        @Schema(description = "Suma del total de las ventas del periodo", example = "30975.00") BigDecimal totalVendido,
        @Schema(example = "1") long numeroVentas,
        @Schema(description = "totalVendido / numeroVentas", example = "30975.00") BigDecimal ticketPromedio,
        @Schema(description = "Meta del mes en curso (suma de las metas del alcance)", example = "165000.00")
        BigDecimal metaMes,
        @Schema(description = "Vendido en el mes en curso", example = "30975.00") BigDecimal vendidoMes,
        @Schema(description = "vendidoMes / metaMes × 100", example = "18.77") BigDecimal avanceMetaMes,
        @Schema(description = "Clientes ACTIVO asignados al alcance", example = "9") long clientesActivos,
        @Schema(description = "Clientes PROSPECTO (leads) asignados al alcance", example = "4") long clientesProspecto,
        @Schema(description = "Clientes creados en el periodo", example = "1") long clientesNuevos,
        @Schema(example = "9") long oportunidadesAbiertas,
        @Schema(description = "Suma del monto estimado de las oportunidades abiertas", example = "699900.00")
        BigDecimal montoPipeline,
        @Schema(description = "Oportunidades ganadas en el periodo", example = "0") long oportunidadesGanadas,
        @Schema(description = "Oportunidades perdidas en el periodo", example = "0") long oportunidadesPerdidas,
        @Schema(description = "ganadas / (ganadas + perdidas) × 100 en el periodo", example = "0.00")
        BigDecimal tasaConversion,
        @Schema(description = "Tickets de soporte ABIERTO o EN_PROCESO", example = "3") long ticketsAbiertos,
        @Schema(description = "Productos activos con stock en o por debajo del mínimo", example = "2") long productosStockBajo) {
}
