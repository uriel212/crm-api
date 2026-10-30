package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.meta.MetaVentaRequest;
import com.devsuj.crmapi.dto.meta.MetaVentaResponse;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.MetaVentaService;
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

@Tag(name = "Metas de venta", description = """
        Metas mensuales de los VENDEDOR (una por vendedor y mes) con su avance contra las ventas reales del mes.
        VENDEDOR ve las suyas, GERENTE las de su equipo, ADMIN todas. Solo GERENTE y ADMIN las administran.""")
@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
public class MetaVentaController {

    private final MetaVentaService service;

    @Operation(summary = "Listar metas con su avance", description = "Filtros opcionales por vendedor, año y mes.")
    @ApiResponse(responseCode = "200", description = "Página de metas")
    @GetMapping
    public PaginaResponse<MetaVentaResponse> listar(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            @ParameterObject @PageableDefault(sort = {"anio", "mes"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(usuarioId, anio, mes, pageable);
    }

    @Operation(summary = "Detalle de una meta con su avance")
    @ApiResponse(responseCode = "200", description = "Meta encontrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{id}")
    public MetaVentaResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear meta", description = "Solo para un VENDEDOR activo; un GERENTE solo para su equipo.")
    @ApiResponse(responseCode = "201", description = "Meta creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe el usuario")
    @ApiResponse(responseCode = "409", description = "Ya hay meta para ese vendedor y mes, o el usuario no es VENDEDOR activo")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PostMapping
    public ResponseEntity<MetaVentaResponse> crear(@Valid @RequestBody MetaVentaRequest request) {
        MetaVentaResponse creada = service.crear(request);
        return Respuestas.creado("/api/metas/{id}", creada.id(), creada);
    }

    @Operation(summary = "Editar meta")
    @ApiResponse(responseCode = "200", description = "Meta actualizada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Duplicada o el usuario no es VENDEDOR activo")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PutMapping("/{id}")
    public MetaVentaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody MetaVentaRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Eliminar meta")
    @ApiResponse(responseCode = "204", description = "Meta eliminada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
