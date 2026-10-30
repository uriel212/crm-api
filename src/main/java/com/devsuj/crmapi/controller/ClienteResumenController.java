package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.reporte.ClienteResumenResponse;
import com.devsuj.crmapi.service.ClienteResumenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reportes y KPIs")
@RestController
@RequiredArgsConstructor
public class ClienteResumenController {

    private final ClienteResumenService service;

    @Operation(summary = "Vista 360 del cliente", description = """
            Compras, últimas 5 ventas, pipeline abierto, actividad, puntos, contratos vigentes, tickets abiertos y
            campañas. La ve el vendedor del cliente, su gerente o ADMIN.""")
    @ApiResponse(responseCode = "200", description = "Resumen del cliente")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @GetMapping("/api/clientes/{id}/resumen")
    public ClienteResumenResponse resumen(@PathVariable Integer id) {
        return service.resumen(id);
    }
}
