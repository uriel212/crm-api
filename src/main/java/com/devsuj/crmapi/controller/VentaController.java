package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.venta.CalculoVentaRequest;
import com.devsuj.crmapi.dto.venta.CalculoVentaResponse;
import com.devsuj.crmapi.dto.venta.VentaRequest;
import com.devsuj.crmapi.dto.venta.VentaResponse;
import com.devsuj.crmapi.dto.venta.VentaResumenResponse;
import com.devsuj.crmapi.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Ventas", description = """
        Ventas concretadas (pagadas). No tienen estados: no se editan ni se cancelan. El servidor calcula precios
        y descuentos: precio actual del producto (queda congelado), mejor promoción por producto y después la mejor
        promoción sobre el total si el subtotal alcanza su monto mínimo. Registrar descuenta stock, gana la
        oportunidad ligada, pasa al cliente PROSPECTO a ACTIVO y otorga 1 punto por cada Q100.""")
@RestController
@RequiredArgsConstructor
public class VentaController {

    private final VentaService service;

    @Operation(summary = "Listar ventas", description = """
            Sin renglones. VENDEDOR ve lo suyo y lo de sus clientes; GERENTE su equipo; ADMIN todo.
            desde/hasta son días completos (ambos incluidos).""")
    @ApiResponse(responseCode = "200", description = "Página de ventas")
    @ApiResponse(responseCode = "400", description = "Rango de fechas inválido")
    @GetMapping("/api/ventas")
    public PaginaResponse<VentaResumenResponse> listar(
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Integer usuarioId,
            @Parameter(example = "2026-10-01") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(example = "2026-10-31") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @ParameterObject @PageableDefault(sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(clienteId, usuarioId, desde, hasta, pageable);
    }

    @Operation(summary = "Historial de compras de un cliente")
    @ApiResponse(responseCode = "200", description = "Página de ventas del cliente")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @GetMapping("/api/clientes/{clienteId}/ventas")
    public PaginaResponse<VentaResumenResponse> delCliente(@PathVariable Integer clienteId,
            @ParameterObject @PageableDefault(sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.delCliente(clienteId, pageable);
    }

    @Operation(summary = "Detalle de una venta", description = "Incluye renglones, promociones y puntos ganados.")
    @ApiResponse(responseCode = "200", description = "Venta encontrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/api/ventas/{id}")
    public VentaResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Calcular una venta sin guardarla", description = """
            Cotiza con los precios y promociones de hoy. Útil para mostrar el total antes de confirmar.""")
    @ApiResponse(responseCode = "200", description = "Importes calculados")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o producto repetido")
    @ApiResponse(responseCode = "404", description = "No existe un producto")
    @ApiResponse(responseCode = "409", description = "Producto desactivado")
    @PostMapping("/api/ventas/calculo")
    public CalculoVentaResponse calcular(@Valid @RequestBody CalculoVentaRequest request) {
        return service.calcular(request.detalles());
    }

    @Operation(summary = "Registrar venta", description = """
            Responsable del cliente, su gerente o ADMIN. Cliente no INACTIVO, productos activos y sin repetir,
            stock suficiente. oportunidadId (opcional) debe ser una oportunidad abierta del mismo cliente: pasa a GANADA.""")
    @ApiResponse(responseCode = "201", description = "Venta registrada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o producto repetido")
    @ApiResponse(responseCode = "404", description = "No existe el cliente, un producto o la oportunidad")
    @ApiResponse(responseCode = "409", description = "Cliente inactivo, producto desactivado, stock insuficiente u oportunidad cerrada o de otro cliente")
    @PostMapping("/api/ventas")
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody VentaRequest request) {
        VentaResponse creada = service.registrar(request);
        return Respuestas.creado("/api/ventas/{id}", creada.id(), creada);
    }
}
