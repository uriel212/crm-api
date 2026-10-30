package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.postventa.AsignarTicketRequest;
import com.devsuj.crmapi.dto.postventa.CambioEstadoTicketRequest;
import com.devsuj.crmapi.dto.postventa.TicketActualizarRequest;
import com.devsuj.crmapi.dto.postventa.TicketRequest;
import com.devsuj.crmapi.dto.postventa.TicketResponse;
import com.devsuj.crmapi.model.EstadoTicket;
import com.devsuj.crmapi.model.PrioridadTicket;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tickets de soporte", description = """
        Atención al cliente integrada con ventas. ABIERTO ↔ EN_PROCESO → CERRADO (EN_PROCESO exige responsable).
        Un CERRADO no se reabre: se crea un seguimiento ligado. Se ven por responsable o por el vendedor del cliente.""")
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    @Operation(summary = "Listar tickets", description = "sinAsignar=true filtra los que no tienen responsable.")
    @ApiResponse(responseCode = "200", description = "Página de tickets")
    @GetMapping("/api/tickets")
    public PaginaResponse<TicketResponse> listar(
            @RequestParam(required = false) EstadoTicket estado,
            @RequestParam(required = false) PrioridadTicket prioridad,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Boolean sinAsignar,
            @ParameterObject @PageableDefault(sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(estado, prioridad, clienteId, usuarioId, sinAsignar, pageable);
    }

    @Operation(summary = "Tickets de un cliente")
    @ApiResponse(responseCode = "200", description = "Página de tickets")
    @GetMapping("/api/clientes/{clienteId}/tickets")
    public PaginaResponse<TicketResponse> delCliente(@PathVariable Integer clienteId,
            @ParameterObject @PageableDefault(sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(null, null, clienteId, null, null, pageable);
    }

    @Operation(summary = "Detalle de un ticket")
    @ApiResponse(responseCode = "200", description = "Ticket encontrado")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/api/tickets/{id}")
    public TicketResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Abrir ticket", description = """
            Lo abre quien gestiona al cliente. usuarioId opcional (null = sin asignar); un VENDEDOR solo se asigna a sí mismo.""")
    @ApiResponse(responseCode = "201", description = "Ticket creado (ABIERTO)")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe el cliente o el usuario")
    @ApiResponse(responseCode = "409", description = "Responsable desactivado")
    @PostMapping("/api/tickets")
    public ResponseEntity<TicketResponse> crear(@Valid @RequestBody TicketRequest request) {
        TicketResponse creado = service.crear(request);
        return Respuestas.creado("/api/tickets/{id}", creado.id(), creado);
    }

    @Operation(summary = "Editar ticket", description = "Asunto, descripción y prioridad. No CERRADO.")
    @ApiResponse(responseCode = "200", description = "Ticket actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Está CERRADO")
    @PutMapping("/api/tickets/{id}")
    public TicketResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TicketActualizarRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Asignar responsable", description = "usuarioId null lo deja sin asignar (no si está EN_PROCESO).")
    @ApiResponse(responseCode = "200", description = "Responsable actualizado")
    @ApiResponse(responseCode = "404", description = "No existe el ticket o el usuario")
    @ApiResponse(responseCode = "409", description = "CERRADO, EN_PROCESO sin responsable o usuario desactivado")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PatchMapping("/api/tickets/{id}/asignacion")
    public TicketResponse asignar(@PathVariable Integer id, @RequestBody AsignarTicketRequest request) {
        return service.asignar(id, request.usuarioId());
    }

    @Operation(summary = "Cambiar estado", description = "Al CERRADO se registra cerradoEn. EN_PROCESO exige responsable.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado inválido")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Transición no permitida o sin responsable")
    @PatchMapping("/api/tickets/{id}/estado")
    public TicketResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoTicketRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    @Operation(summary = "Seguimiento de un ticket CERRADO", description = "Crea un ticket nuevo ABIERTO ligado (ticketRelacionadoId).")
    @ApiResponse(responseCode = "201", description = "Ticket de seguimiento creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "El ticket no está CERRADO")
    @PostMapping("/api/tickets/{id}/seguimiento")
    public ResponseEntity<TicketResponse> seguimiento(@PathVariable Integer id,
            @Valid @RequestBody TicketActualizarRequest request) {
        TicketResponse creado = service.seguimiento(id, request);
        return Respuestas.creado("/api/tickets/{id}", creado.id(), creado);
    }
}
