package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.catalogo.CambioEstadoProductoRequest;
import com.devsuj.crmapi.dto.catalogo.MovimientoInventarioRequest;
import com.devsuj.crmapi.dto.catalogo.MovimientoInventarioResponse;
import com.devsuj.crmapi.dto.catalogo.ProductoRequest;
import com.devsuj.crmapi.dto.catalogo.ProductoResponse;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.ProductoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Productos", description = """
        Catálogo e inventario. Los productos se desactivan (no se borran). El stock solo cambia con movimientos
        del kardex: ENTRADA/SALIDA manuales, VENTA y CANJE automáticos.""")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @Operation(summary = "Catálogo", description = "Paginado; canjeables=true filtra los que tienen precio en puntos. Todos los roles.")
    @ApiResponse(responseCode = "200", description = "Página de productos")
    @GetMapping
    public PaginaResponse<ProductoResponse> listar(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Boolean canjeables,
            @Parameter(description = "Texto en nombre o descripción") @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(sort = "nombre") Pageable pageable) {
        return service.listar(categoriaId, activo, canjeables, q, pageable);
    }

    @Operation(summary = "Productos con stock bajo", description = "Activos con stock <= stock mínimo. GERENTE, ADMIN.")
    @ApiResponse(responseCode = "200", description = "Productos por resurtir")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @GetMapping("/stock-bajo")
    public List<ProductoResponse> stockBajo() {
        return service.stockBajo();
    }

    @Operation(summary = "Detalle de un producto")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear producto", description = "stockInicial queda como ENTRADA en el kardex. GERENTE, ADMIN.")
    @ApiResponse(responseCode = "201", description = "Producto creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe la categoría")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = service.crear(request);
        return Respuestas.creado("/api/productos/{id}", creado.id(), creado);
    }

    @Operation(summary = "Actualizar producto", description = """
            Datos, precio y precio en puntos (stockInicial se ignora). Las ventas pasadas no cambian. GERENTE, ADMIN.""")
    @ApiResponse(responseCode = "200", description = "Producto actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe el producto o la categoría")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Activar o desactivar", description = "Un producto inactivo no se vende ni se canjea. GERENTE, ADMIN.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "No existe")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PatchMapping("/{id}/estado")
    public ProductoResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoProductoRequest request) {
        return service.cambiarEstado(id, request.activo());
    }

    @Operation(summary = "Ajustar inventario", description = "ENTRADA (llegó mercancía) o SALIDA (merma). GERENTE, ADMIN.")
    @ApiResponse(responseCode = "201", description = "Movimiento registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo no manual")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "La salida deja el stock negativo")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PostMapping("/{id}/movimientos")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoInventarioResponse ajustarInventario(@PathVariable Integer id,
            @Valid @RequestBody MovimientoInventarioRequest request) {
        return service.ajustarInventario(id, request);
    }

    @Operation(summary = "Kardex del producto", description = "Movimientos de inventario, más reciente primero. GERENTE, ADMIN.")
    @ApiResponse(responseCode = "200", description = "Página de movimientos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @GetMapping("/{id}/movimientos")
    public PaginaResponse<MovimientoInventarioResponse> movimientos(@PathVariable Integer id,
            @ParameterObject @PageableDefault(sort = {"fecha", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return service.movimientos(id, pageable);
    }
}
