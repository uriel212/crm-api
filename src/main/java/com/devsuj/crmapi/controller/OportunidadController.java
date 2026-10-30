package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.oportunidad.CambioEtapaRequest;
import com.devsuj.crmapi.dto.oportunidad.HistorialEtapaResponse;
import com.devsuj.crmapi.dto.oportunidad.OportunidadActualizarRequest;
import com.devsuj.crmapi.dto.oportunidad.OportunidadRequest;
import com.devsuj.crmapi.dto.oportunidad.OportunidadResponse;
import com.devsuj.crmapi.model.EtapaOportunidad;
import com.devsuj.crmapi.service.OportunidadService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Oportunidades", description = """
        Pipeline PROSPECTO → CALIFICADA → PROPUESTA → NEGOCIACION (se avanza o retrocede), cierre en PERDIDA (manual)
        o GANADA (al registrar la venta ligada). Cada cambio queda en el historial. Las cerradas no se reabren.""")
@RestController
@RequiredArgsConstructor
public class OportunidadController {

    private final OportunidadService service;

    @Operation(summary = "Listar oportunidades", description = "Filtrado por alcance (VENDEDOR lo suyo, GERENTE su equipo).")
    @ApiResponse(responseCode = "200", description = "Página de oportunidades")
    @GetMapping("/api/oportunidades")
    public PaginaResponse<OportunidadResponse> listar(
            @RequestParam(required = false) EtapaOportunidad etapa,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Boolean abiertas,
            @ParameterObject @PageableDefault(sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(etapa, usuarioId, clienteId, abiertas, pageable);
    }

    @Operation(summary = "Oportunidades de un cliente")
    @ApiResponse(responseCode = "200", description = "Página de oportunidades")
    @GetMapping("/api/clientes/{clienteId}/oportunidades")
    public PaginaResponse<OportunidadResponse> delCliente(@PathVariable Integer clienteId,
            @ParameterObject @PageableDefault(sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(null, null, clienteId, null, pageable);
    }

    @Operation(summary = "Detalle de una oportunidad")
    @ApiResponse(responseCode = "200", description = "Oportunidad encontrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/api/oportunidades/{id}")
    public OportunidadResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Historial de etapas", description = """
            Línea de tiempo en orden cronológico: etapa, fecha de entrada y salida, días en la etapa, usuario y comentario.""")
    @ApiResponse(responseCode = "200", description = "Pasos del pipeline")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/api/oportunidades/{id}/historial")
    public List<HistorialEtapaResponse> historial(@PathVariable Integer id) {
        return service.historial(id);
    }

    @Operation(summary = "Crear oportunidad", description = """
            Empieza en PROSPECTO. Responsable del cliente, su gerente o ADMIN. Cliente no INACTIVO.
            oportunidadAnteriorId liga un nuevo intento con una PERDIDA del mismo cliente.""")
    @ApiResponse(responseCode = "201", description = "Oportunidad creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe el cliente")
    @ApiResponse(responseCode = "409", description = "Cliente inactivo u oportunidad anterior inválida")
    @PostMapping("/api/oportunidades")
    public ResponseEntity<OportunidadResponse> crear(@Valid @RequestBody OportunidadRequest request) {
        OportunidadResponse creada = service.crear(request);
        return Respuestas.creado("/api/oportunidades/{id}", creada.id(), creada);
    }

    @Operation(summary = "Editar oportunidad abierta", description = "Título, monto y fecha estimada. Dueño, su gerente o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Oportunidad actualizada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Está cerrada")
    @PutMapping("/api/oportunidades/{id}")
    public OportunidadResponse actualizar(@PathVariable Integer id, @Valid @RequestBody OportunidadActualizarRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Cambiar etapa", description = "Avanza/retrocede entre abiertas o marca PERDIDA. GANADA → 409.")
    @ApiResponse(responseCode = "200", description = "Etapa actualizada y registrada en el historial")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Cerrada, misma etapa o GANADA manual")
    @PatchMapping("/api/oportunidades/{id}/etapa")
    public OportunidadResponse cambiarEtapa(@PathVariable Integer id, @Valid @RequestBody CambioEtapaRequest request) {
        return service.cambiarEtapa(id, request);
    }
}
