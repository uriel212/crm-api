package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.fidelizacion.CanjeRequest;
import com.devsuj.crmapi.dto.fidelizacion.CanjeResponse;
import com.devsuj.crmapi.dto.fidelizacion.MovimientoPuntosResponse;
import com.devsuj.crmapi.dto.fidelizacion.SaldoPuntosResponse;
import com.devsuj.crmapi.service.FidelizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Fidelización", description = """
        Puntos de los clientes: 1 punto por cada Q100 completos de cada venta (se otorgan solos al registrarla).
        Saldo = ganados − canjeados. Se canjean por productos con precio en puntos; el canje descuenta stock.""")
@RestController
@RequestMapping("/api/clientes/{clienteId}/puntos")
@RequiredArgsConstructor
public class FidelizacionController {

    private final FidelizacionService service;

    @Operation(summary = "Saldo de puntos del cliente")
    @ApiResponse(responseCode = "200", description = "Puntos ganados, canjeados y saldo")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @GetMapping
    public SaldoPuntosResponse saldo(@PathVariable Integer clienteId) {
        return service.saldo(clienteId);
    }

    @Operation(summary = "Movimientos de puntos", description = "Estado de cuenta paginado, más reciente primero.")
    @ApiResponse(responseCode = "200", description = "Página de movimientos")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @GetMapping("/movimientos")
    public PaginaResponse<MovimientoPuntosResponse> movimientos(@PathVariable Integer clienteId,
            @ParameterObject @PageableDefault(sort = {"fecha", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return service.movimientos(clienteId, pageable);
    }

    @Operation(summary = "Canjear puntos por un producto", description = """
            Responsable del cliente, su gerente o ADMIN. Cliente no INACTIVO, producto activo con precio en puntos,
            saldo y stock suficientes. Puntos usados = precioPuntos × cantidad.""")
    @ApiResponse(responseCode = "201", description = "Canje registrado; incluye el saldo restante")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe el cliente o el producto")
    @ApiResponse(responseCode = "409", description = "Cliente inactivo, producto no canjeable o desactivado, puntos o stock insuficientes (con saldo y puntosNecesarios)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/canjes")
    public CanjeResponse canjear(@PathVariable Integer clienteId, @Valid @RequestBody CanjeRequest request) {
        return service.canjear(clienteId, request);
    }
}
