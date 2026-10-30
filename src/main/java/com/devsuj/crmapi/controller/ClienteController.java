package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.cliente.AsignarVendedorRequest;
import com.devsuj.crmapi.dto.cliente.CambioEstadoClienteRequest;
import com.devsuj.crmapi.dto.cliente.ClienteRequest;
import com.devsuj.crmapi.dto.cliente.ClienteResponse;
import com.devsuj.crmapi.dto.cliente.ReasignacionLoteRequest;
import com.devsuj.crmapi.dto.cliente.ReasignacionResponse;
import com.devsuj.crmapi.model.EstadoCliente;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Clientes", description = """
        Clientes y leads (PROSPECTO). Todos los usuarios ven todos los clientes; solo el vendedor asignado,
        su gerente o un ADMIN pueden modificarlos. No se borran: se pasan a INACTIVO.""")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @Operation(summary = "Buscar clientes", description = "Lista paginada; q busca en nombre, empresa y email. Todos los roles.")
    @ApiResponse(responseCode = "200", description = "Página de clientes")
    @ApiResponse(responseCode = "400", description = "Parámetro inválido")
    @GetMapping
    public PaginaResponse<ClienteResponse> listar(
            @RequestParam(required = false) EstadoCliente estado,
            @RequestParam(required = false) Integer vendedorId,
            @Parameter(description = "Texto a buscar") @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(sort = "nombre") Pageable pageable) {
        return service.listar(estado, vendedorId, q, pageable);
    }

    @Operation(summary = "Detalle de un cliente", description = "Todos los roles.")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Registrar cliente o lead", description = """
            Se crea como PROSPECTO. Un VENDEDOR queda como responsable; GERENTE/ADMIN pueden indicar vendedorId
            (de su equipo) o dejarlo sin responsable.""")
    @ApiResponse(responseCode = "201", description = "Cliente creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "Email duplicado o responsable inválido")
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse creado = service.crear(request);
        return Respuestas.creado("/api/clientes/{id}", creado.id(), creado);
    }

    @Operation(summary = "Actualizar datos de contacto", description = """
            Vendedor asignado, su gerente o ADMIN. vendedorId null no cambia el responsable; un valor distinto
            lo reasigna (solo GERENTE/ADMIN).""")
    @ApiResponse(responseCode = "200", description = "Cliente actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @ApiResponse(responseCode = "409", description = "Email duplicado")
    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ClienteRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Cambiar estado", description = """
            PROSPECTO, ACTIVO o INACTIVO (desactivar = INACTIVO). Un PROSPECTO pasa solo a ACTIVO con su primera
            venta. Un cliente con compras no vuelve a PROSPECTO.""")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @ApiResponse(responseCode = "409", description = "Cambio no permitido")
    @PatchMapping("/{id}/estado")
    public ClienteResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoClienteRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    @Operation(summary = "Asignar o quitar responsable", description = "GERENTE (dentro de su equipo) o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Responsable actualizado")
    @ApiResponse(responseCode = "404", description = "No existe el cliente o el usuario")
    @ApiResponse(responseCode = "409", description = "Usuario inactivo o con rol ADMIN")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PatchMapping("/{id}/vendedor")
    public ClienteResponse asignarVendedor(@PathVariable Integer id, @RequestBody AsignarVendedorRequest request) {
        return service.asignarVendedor(id, request.vendedorId());
    }

    @Operation(summary = "Reasignar clientes en lote", description = "Pasa varios clientes a un responsable. GERENTE o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Clientes reasignados")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "Algún cliente o el usuario no existe")
    @ApiResponse(responseCode = "409", description = "Usuario inactivo o con rol ADMIN")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PostMapping("/reasignaciones")
    public ReasignacionResponse reasignarLote(@Valid @RequestBody ReasignacionLoteRequest request) {
        return service.reasignarLote(request);
    }
}
