package com.devsuj.crmapi.dto.reporte;

import com.devsuj.crmapi.dto.cliente.ClienteResponse;
import com.devsuj.crmapi.dto.venta.VentaResumenResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Vista 360 de un cliente: ventas, pipeline, actividad, puntos, contratos, soporte y marketing")
public record ClienteResumenResponse(
        ClienteResponse cliente,
        @Schema(example = "3") long numeroCompras,
        @Schema(example = "233279.00") BigDecimal totalComprado,
        @Schema(example = "77759.67") BigDecimal ticketPromedio,
        @Schema(description = "null si nunca compró") LocalDateTime ultimaCompra,
        @Schema(description = "Las 5 compras más recientes") List<VentaResumenResponse> ultimasVentas,
        @Schema(example = "1") long oportunidadesAbiertas,
        @Schema(example = "70000.00") BigDecimal montoPipeline,
        @Schema(description = "Última interacción REALIZADA; null si no hay") LocalDateTime ultimaInteraccion,
        @Schema(description = "Actividades PROGRAMADA pendientes", example = "1") long actividadesProgramadas,
        @Schema(example = "1330") long saldoPuntos,
        @Schema(example = "1") long contratosVigentes,
        @Schema(description = "Tickets sin cerrar", example = "0") long ticketsAbiertos,
        @Schema(description = "Campañas en las que es destinatario", example = "1") long campanas) {
}
