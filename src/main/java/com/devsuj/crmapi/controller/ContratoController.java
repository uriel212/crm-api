package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.postventa.CambioEstadoContratoRequest;
import com.devsuj.crmapi.dto.postventa.ContratoRequest;
import com.devsuj.crmapi.dto.postventa.ContratoResponse;
import com.devsuj.crmapi.dto.postventa.ExtensionContratoRequest;
import com.devsuj.crmapi.dto.postventa.RenovacionContratoRequest;
import com.devsuj.crmapi.dto.postventa.VencimientoLoteResponse;
import com.devsuj.crmapi.model.EstadoContrato;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.ContratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Contratos", description = """
        Acuerdos con clientes. BORRADOR → VIGENTE | CANCELADO; VIGENTE → VENCIDO | CANCELADO. Solo se editan en
        BORRADOR; un VIGENTE se extiende; la renovación crea un contrato nuevo ligado. No se borran.
        Se ven según el vendedor asignado al cliente; los gestiona el vendedor del cliente, su gerente o ADMIN.""")
@RestController
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService service;

    @Operation(summary = "Listar contratos", description = "porVencerDias = VIGENTE que vencen entre hoy y hoy + N días.")
    @ApiResponse(responseCode = "200", description = "Página de contratos")
    @ApiResponse(responseCode = "400", description = "porVencerDias fuera de rango")
    @GetMapping("/api/contratos")
    public PaginaResponse<ContratoResponse> listar(
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) EstadoContrato estado,
            @RequestParam(required = false) @Min(value = 0, message = "porVencerDias mínimo es 0.")
            @Max(value = 365, message = "porVencerDias máximo es 365.") Integer porVencerDias,
            @ParameterObject @PageableDefault(sort = "fechaFin") Pageable pageable) {
        return service.listar(clienteId, estado, porVencerDias, pageable);
    }

    @Operation(summary = "Contratos de un cliente")
    @ApiResponse(responseCode = "200", description = "Página de contratos")
    @GetMapping("/api/clientes/{clienteId}/contratos")
    public PaginaResponse<ContratoResponse> delCliente(@PathVariable Integer clienteId,
            @ParameterObject @PageableDefault(sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(clienteId, null, null, pageable);
    }

    @Operation(summary = "Detalle de un contrato")
    @ApiResponse(responseCode = "200", description = "Contrato encontrado")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/api/contratos/{id}")
    public ContratoResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear contrato", description = "Nace en BORRADOR. Cliente no INACTIVO; ventaId debe ser del mismo cliente.")
    @ApiResponse(responseCode = "201", description = "Contrato creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o fechas al revés")
    @ApiResponse(responseCode = "404", description = "No existe el cliente o la venta")
    @ApiResponse(responseCode = "409", description = "Cliente inactivo o venta de otro cliente")
    @PostMapping("/api/contratos")
    public ResponseEntity<ContratoResponse> crear(@Valid @RequestBody ContratoRequest request) {
        ContratoResponse creado = service.crear(request);
        return Respuestas.creado("/api/contratos/{id}", creado.id(), creado);
    }

    @Operation(summary = "Editar contrato en BORRADOR", description = "El cliente no se cambia.")
    @ApiResponse(responseCode = "200", description = "Contrato actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o cambio de cliente")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "No está en BORRADOR o venta de otro cliente")
    @PutMapping("/api/contratos/{id}")
    public ContratoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ContratoRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Cambiar estado", description = "VENCIDO solo si la fecha de fin ya pasó.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado inválido")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Transición no permitida")
    @PatchMapping("/api/contratos/{id}/estado")
    public ContratoResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoContratoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    @Operation(summary = "Extender un contrato VIGENTE", description = "Nueva fecha de fin, posterior a la actual.")
    @ApiResponse(responseCode = "200", description = "Contrato extendido")
    @ApiResponse(responseCode = "400", description = "Fecha no posterior a la actual")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "No está VIGENTE")
    @PatchMapping("/api/contratos/{id}/extension")
    public ContratoResponse extender(@PathVariable Integer id, @Valid @RequestBody ExtensionContratoRequest request) {
        return service.extender(id, request.fechaFin());
    }

    @Operation(summary = "Renovar contrato", description = "Crea un contrato nuevo en BORRADOR ligado a este (una sola vez).")
    @ApiResponse(responseCode = "201", description = "Renovación creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "No está VIGENTE/VENCIDO, ya se renovó o cliente inactivo")
    @PostMapping("/api/contratos/{id}/renovacion")
    public ResponseEntity<ContratoResponse> renovar(@PathVariable Integer id,
            @Valid @RequestBody RenovacionContratoRequest request) {
        ContratoResponse nuevo = service.renovar(id, request);
        return Respuestas.creado("/api/contratos/{id}", nuevo.id(), nuevo);
    }

    @Operation(summary = "Vencer en lote", description = "Marca VENCIDO todos los VIGENTE con fecha de fin pasada (del alcance).")
    @ApiResponse(responseCode = "200", description = "Cuántos y cuáles se vencieron")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PostMapping("/api/contratos/vencimientos")
    public VencimientoLoteResponse vencerEnLote() {
        return service.vencerEnLote();
    }
}
