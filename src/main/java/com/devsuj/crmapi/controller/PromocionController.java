package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.marketing.CambioEstadoPromocionRequest;
import com.devsuj.crmapi.dto.marketing.PromocionRequest;
import com.devsuj.crmapi.dto.marketing.PromocionResponse;
import com.devsuj.crmapi.model.AlcancePromocion;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.PromocionService;
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

import java.util.List;

@Tag(name = "Promociones", description = """
        Descuentos que el servidor aplica solo al vender. PORCENTAJE o MONTO_FIJO; alcance PRODUCTO (por renglón,
        monto fijo por unidad) o VENTA (sobre el total, con monto mínimo). Si la anuncia una campaña, solo aplica
        con la campaña ACTIVA. Si varias aplican, gana la de mayor descuento.""")
@RestController
@RequestMapping("/api/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService service;

    @Operation(summary = "Listar promociones", description = "Filtros opcionales; productoId = promociones que incluyen ese producto.")
    @ApiResponse(responseCode = "200", description = "Página de promociones")
    @GetMapping
    public PaginaResponse<PromocionResponse> listar(
            @RequestParam(required = false) Boolean activa,
            @RequestParam(required = false) AlcancePromocion alcance,
            @RequestParam(required = false) Integer campanaId,
            @RequestParam(required = false) Integer productoId,
            @ParameterObject @PageableDefault(sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(activa, alcance, campanaId, productoId, pageable);
    }

    @Operation(summary = "Promociones vigentes hoy", description = "Las que se aplicarían en una venta registrada ahora.")
    @ApiResponse(responseCode = "200", description = "Promociones vigentes")
    @GetMapping("/vigentes")
    public List<PromocionResponse> vigentes() {
        return service.vigentes();
    }

    @Operation(summary = "Detalle de una promoción")
    @ApiResponse(responseCode = "200", description = "Promoción encontrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{id}")
    public PromocionResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear promoción")
    @ApiResponse(responseCode = "201", description = "Promoción creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o incoherentes con el alcance")
    @ApiResponse(responseCode = "404", description = "No existe la campaña o un producto")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PostMapping
    public ResponseEntity<PromocionResponse> crear(@Valid @RequestBody PromocionRequest request) {
        PromocionResponse creada = service.crear(request);
        return Respuestas.creado("/api/promociones/{id}", creada.id(), creada);
    }

    @Operation(summary = "Editar promoción", description = "No cambia ventas pasadas (el descuento quedó congelado).")
    @ApiResponse(responseCode = "200", description = "Promoción actualizada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o incoherentes con el alcance")
    @ApiResponse(responseCode = "404", description = "No existe la promoción, la campaña o un producto")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PutMapping("/{id}")
    public PromocionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody PromocionRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Activar o desactivar")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "No existe")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PatchMapping("/{id}/estado")
    public PromocionResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoPromocionRequest request) {
        return service.cambiarEstado(id, request.activa());
    }

    @Operation(summary = "Borrar promoción", description = "Solo si nunca se aplicó en una venta; si no, desactívala.")
    @ApiResponse(responseCode = "204", description = "Promoción borrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Ya se usó en ventas")
    @PreAuthorize(Permisos.SOLO_ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
