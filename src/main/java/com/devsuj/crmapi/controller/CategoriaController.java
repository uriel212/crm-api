package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.catalogo.CategoriaRequest;
import com.devsuj.crmapi.dto.catalogo.CategoriaResponse;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categorías", description = "Categorías del catálogo (lista corta, sin paginar)")
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService service;

    @Operation(summary = "Listar categorías", description = "Ordenadas por nombre. Todos los roles.")
    @ApiResponse(responseCode = "200", description = "Categorías")
    @GetMapping
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Detalle de una categoría")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{id}")
    public CategoriaResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear categoría", description = "GERENTE, ADMIN.")
    @ApiResponse(responseCode = "201", description = "Categoría creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse creada = service.crear(request);
        return Respuestas.creado("/api/categorias/{id}", creada.id(), creada);
    }

    @Operation(summary = "Actualizar categoría", description = "GERENTE, ADMIN.")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PutMapping("/{id}")
    public CategoriaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody CategoriaRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Borrar categoría", description = "Solo si no tiene productos. ADMIN.")
    @ApiResponse(responseCode = "204", description = "Categoría borrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Tiene productos")
    @PreAuthorize(Permisos.SOLO_ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
