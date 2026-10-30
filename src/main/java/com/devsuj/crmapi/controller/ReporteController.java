package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.reporte.ConversionResponse;
import com.devsuj.crmapi.dto.marketing.MetricasCampanaResponse;
import com.devsuj.crmapi.dto.reporte.DashboardResponse;
import com.devsuj.crmapi.dto.reporte.FidelizacionReporteResponse;
import com.devsuj.crmapi.dto.reporte.SoporteResponse;
import com.devsuj.crmapi.dto.reporte.PipelineEtapaResponse;
import com.devsuj.crmapi.dto.reporte.ProductoVendidoResponse;
import com.devsuj.crmapi.dto.reporte.RendimientoVendedorResponse;
import com.devsuj.crmapi.dto.reporte.VentasPorCategoriaResponse;
import com.devsuj.crmapi.dto.reporte.VentasPorMesResponse;
import com.devsuj.crmapi.dto.reporte.VentasPorVendedorResponse;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Reportes y KPIs", description = """
        Consultas de solo lectura para el tablero y los informes. Todo se filtra por el alcance de quien consulta:
        VENDEDOR lo suyo, GERENTE su equipo, ADMIN todo. Ventas y oportunidades cuentan por su responsable.
        desde/hasta son días completos (ambos incluidos). Por defecto: el tablero usa el mes en curso y los demás
        reportes, del 1 de enero del año en curso a hoy.""")
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService service;

    @Operation(summary = "Tablero (KPIs)", description = """
            Ventas, ticket promedio, avance de la meta del mes, clientes, pipeline abierto, conversión del periodo,
            tickets abiertos y productos con stock bajo.""")
    @ApiResponse(responseCode = "200", description = "KPIs del periodo")
    @ApiResponse(responseCode = "400", description = "Rango de fechas inválido")
    @GetMapping("/dashboard")
    public DashboardResponse dashboard(
            @Parameter(example = "2026-10-01") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(example = "2026-10-31") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.dashboard(desde, hasta);
    }

    @Operation(summary = "Ventas por mes", description = "Siempre 12 renglones (enero a diciembre), con 0 si no hubo ventas.")
    @ApiResponse(responseCode = "200", description = "Ventas de cada mes")
    @ApiResponse(responseCode = "400", description = "Año inválido")
    @GetMapping("/ventas/por-mes")
    public List<VentasPorMesResponse> ventasPorMes(
            @Parameter(description = "Por defecto, el año en curso", example = "2026") @RequestParam(required = false)
            @Min(value = 2000, message = "El año mínimo es 2000.") @Max(value = 2100, message = "El año máximo es 2100.")
            Integer anio) {
        return service.ventasPorMes(anio);
    }

    @Operation(summary = "Ventas por vendedor", description = "Ordenado de mayor a menor total.")
    @ApiResponse(responseCode = "200", description = "Ventas de cada vendedor")
    @ApiResponse(responseCode = "400", description = "Rango de fechas inválido")
    @GetMapping("/ventas/por-vendedor")
    public List<VentasPorVendedorResponse> ventasPorVendedor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.ventasPorVendedor(desde, hasta);
    }

    @Operation(summary = "Ventas por categoría", description = """
            Suma de los renglones (ya con el descuento por producto, antes del descuento sobre el total de la venta).""")
    @ApiResponse(responseCode = "200", description = "Ventas de cada categoría")
    @ApiResponse(responseCode = "400", description = "Rango de fechas inválido")
    @GetMapping("/ventas/por-categoria")
    public List<VentasPorCategoriaResponse> ventasPorCategoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.ventasPorCategoria(desde, hasta);
    }

    @Operation(summary = "Productos más vendidos", description = "Por unidades vendidas.")
    @ApiResponse(responseCode = "200", description = "Ranking de productos")
    @ApiResponse(responseCode = "400", description = "Rango de fechas o límite inválido")
    @GetMapping("/productos/mas-vendidos")
    public List<ProductoVendidoResponse> productosMasVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "El límite mínimo es 1.")
            @Max(value = 50, message = "El límite máximo es 50.") int limite) {
        return service.productosMasVendidos(desde, hasta, limite);
    }

    @Operation(summary = "Pipeline por etapa", description = "Situación actual: las 6 etapas en orden, con cantidad y monto estimado.")
    @ApiResponse(responseCode = "200", description = "Oportunidades por etapa")
    @GetMapping("/oportunidades/pipeline")
    public List<PipelineEtapaResponse> pipeline() {
        return service.pipeline();
    }

    @Operation(summary = "Conversión de oportunidades", description = """
            Ganadas y perdidas según la fecha de su cierre en el historial; tasa = ganadas / (ganadas + perdidas).""")
    @ApiResponse(responseCode = "200", description = "Conversión del periodo")
    @ApiResponse(responseCode = "400", description = "Rango de fechas inválido")
    @GetMapping("/oportunidades/conversion")
    public ConversionResponse conversion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.conversion(desde, hasta);
    }

    @Operation(summary = "Rendimiento de vendedores", description = """
            Por VENDEDOR del alcance en un mes (por defecto, el mes en curso): ventas, meta, avance y conversión.""")
    @ApiResponse(responseCode = "200", description = "Rendimiento de cada vendedor")
    @ApiResponse(responseCode = "400", description = "Año o mes inválido")
    @GetMapping("/rendimiento-vendedores")
    public List<RendimientoVendedorResponse> rendimientoVendedores(
            @RequestParam(required = false) @Min(value = 2000, message = "El año mínimo es 2000.")
            @Max(value = 2100, message = "El año máximo es 2100.") Integer anio,
            @RequestParam(required = false) @Min(value = 1, message = "El mes va de 1 a 12.")
            @Max(value = 12, message = "El mes va de 1 a 12.") Integer mes) {
        return service.rendimientoVendedores(anio, mes);
    }

    @Operation(summary = "Efectividad de campañas", description = "Métricas de todas las campañas, más reciente primero.")
    @ApiResponse(responseCode = "200", description = "Métricas por campaña")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @GetMapping("/campanas/efectividad")
    public List<MetricasCampanaResponse> efectividadCampanas() {
        return service.efectividadCampanas();
    }

    @Operation(summary = "Soporte al cliente", description = """
            Tickets por estado, sin asignar, pendientes por prioridad y horas promedio de cierre (alcance del usuario).""")
    @ApiResponse(responseCode = "200", description = "Situación del soporte")
    @GetMapping("/soporte")
    public SoporteResponse soporte() {
        return service.soporte();
    }

    @Operation(summary = "Programa de fidelización", description = """
            Puntos emitidos, canjeados, saldo en circulación y los 5 clientes con más saldo (clientes del alcance).""")
    @ApiResponse(responseCode = "200", description = "Resumen de puntos")
    @GetMapping("/fidelizacion")
    public FidelizacionReporteResponse fidelizacion() {
        return service.fidelizacion();
    }
}
