package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.interaccion.CambioEstadoInteraccionRequest;
import com.devsuj.crmapi.dto.interaccion.InteraccionActualizarRequest;
import com.devsuj.crmapi.dto.interaccion.InteraccionRequest;
import com.devsuj.crmapi.dto.interaccion.InteraccionResponse;
import com.devsuj.crmapi.model.EstadoInteraccion;
import com.devsuj.crmapi.model.TipoInteraccion;
import com.devsuj.crmapi.service.InteraccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Interacciones", description = """
        Historial de contacto y agenda de actividades (PROGRAMADA → REALIZADA o CANCELADA). No se borran.
        VENDEDOR ve las suyas y las de sus clientes; GERENTE las de su equipo; ADMIN todas.""")
@RestController
@RequiredArgsConstructor
public class InteraccionController {

    private final InteraccionService service;

    @Operation(summary = "Historial de un cliente", description = "Más reciente primero, filtrado por alcance.")
    @ApiResponse(responseCode = "200", description = "Página de interacciones")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @GetMapping("/api/clientes/{clienteId}/interacciones")
    public PaginaResponse<InteraccionResponse> delCliente(@PathVariable Integer clienteId,
            @RequestParam(required = false) TipoInteraccion tipo,
            @RequestParam(required = false) EstadoInteraccion estado,
            @ParameterObject @PageableDefault(sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(clienteId, null, tipo, estado, null, null, pageable);
    }

    @Operation(summary = "Registrar interacción o agendar actividad", description = """
            REALIZADA (por defecto; fecha opcional, no futura) o PROGRAMADA (fecha futura obligatoria).
            Responsable del cliente, su gerente o ADMIN.""")
    @ApiResponse(responseCode = "201", description = "Interacción creada")
    @ApiResponse(responseCode = "400", description = "Datos o fecha inválidos")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @PostMapping("/api/clientes/{clienteId}/interacciones")
    public ResponseEntity<InteraccionResponse> crear(@PathVariable Integer clienteId,
            @Valid @RequestBody InteraccionRequest request) {
        InteraccionResponse creada = service.crear(clienteId, request);
        return Respuestas.creado("/api/interacciones/{id}", creada.id(), creada);
    }

    @Operation(summary = "Buscar interacciones", description = "Por usuario, cliente, tipo, estado y periodo (fechas inclusive).")
    @ApiResponse(responseCode = "200", description = "Página de interacciones")
    @GetMapping("/api/interacciones")
    public PaginaResponse<InteraccionResponse> listar(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) TipoInteraccion tipo,
            @RequestParam(required = false) EstadoInteraccion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @ParameterObject @PageableDefault(sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(clienteId, usuarioId, tipo, estado, desde, hasta, pageable);
    }

    @Operation(summary = "Próximas actividades", description = """
            Actividades PROGRAMADA del usuario (por defecto el actual) hasta dentro de 'dias', incluidas las vencidas
            (vencida = true). Orden cronológico.""")
    @ApiResponse(responseCode = "200", description = "Agenda")
    @ApiResponse(responseCode = "400", description = "dias fuera de rango")
    @GetMapping("/api/interacciones/proximas")
    public List<InteraccionResponse> proximas(
            @Parameter(description = "Usuario (GERENTE/ADMIN pueden ver la agenda de su equipo)")
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(defaultValue = "7") @Min(0) @Max(365) int dias) {
        return service.proximas(usuarioId, dias);
    }

    @Operation(summary = "Detalle de una interacción")
    @ApiResponse(responseCode = "200", description = "Interacción encontrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/api/interacciones/{id}")
    public InteraccionResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Corregir o reprogramar", description = "Autor, su gerente o ADMIN. Una CANCELADA no se edita.")
    @ApiResponse(responseCode = "200", description = "Interacción actualizada")
    @ApiResponse(responseCode = "400", description = "Datos o fecha inválidos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Está CANCELADA")
    @PutMapping("/api/interacciones/{id}")
    public InteraccionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody InteraccionActualizarRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Cerrar una actividad", description = """
            PROGRAMADA → REALIZADA (con resultado; si se adelanta, la fecha pasa a ahora) o CANCELADA. Autor, su gerente o ADMIN.""")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado destino inválido")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "No está PROGRAMADA")
    @PatchMapping("/api/interacciones/{id}/estado")
    public InteraccionResponse cambiarEstado(@PathVariable Integer id,
            @Valid @RequestBody CambioEstadoInteraccionRequest request) {
        return service.cambiarEstado(id, request);
    }
}
