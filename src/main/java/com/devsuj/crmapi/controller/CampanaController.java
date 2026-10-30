package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.marketing.CambioEstadoCampanaRequest;
import com.devsuj.crmapi.dto.marketing.CambioResultadoRequest;
import com.devsuj.crmapi.dto.marketing.CampanaRequest;
import com.devsuj.crmapi.dto.marketing.CampanaResponse;
import com.devsuj.crmapi.dto.marketing.DestinatarioRequest;
import com.devsuj.crmapi.dto.marketing.DestinatarioResponse;
import com.devsuj.crmapi.dto.marketing.GeneracionDestinatariosResponse;
import com.devsuj.crmapi.dto.marketing.MetricasCampanaResponse;
import com.devsuj.crmapi.model.CanalCampana;
import com.devsuj.crmapi.model.EstadoCampana;
import com.devsuj.crmapi.model.ResultadoCampana;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.CampanaService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Campañas", description = """
        Campañas de marketing dirigidas a un segmento de clientes. No se envían mensajes reales: se registran los
        destinatarios y su resultado (ENVIADO → ABIERTO → RESPONDIO). No se borran: se cancelan.
        Las administran GERENTE y ADMIN; el resultado de un destinatario lo registra quien gestiona al cliente.""")
@RestController
@RequestMapping("/api/campanas")
@RequiredArgsConstructor
public class CampanaController {

    private final CampanaService service;

    @Operation(summary = "Listar campañas", description = "Con el total de destinatarios. q busca en nombre y asunto.")
    @ApiResponse(responseCode = "200", description = "Página de campañas")
    @GetMapping
    public PaginaResponse<CampanaResponse> listar(
            @RequestParam(required = false) EstadoCampana estado,
            @RequestParam(required = false) CanalCampana canal,
            @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(estado, canal, q, pageable);
    }

    @Operation(summary = "Detalle de una campaña")
    @ApiResponse(responseCode = "200", description = "Campaña encontrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{id}")
    public CampanaResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear campaña", description = "Nace PLANEADA.")
    @ApiResponse(responseCode = "201", description = "Campaña creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o fechas al revés")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PostMapping
    public ResponseEntity<CampanaResponse> crear(@Valid @RequestBody CampanaRequest request) {
        CampanaResponse creada = service.crear(request);
        return Respuestas.creado("/api/campanas/{id}", creada.id(), creada);
    }

    @Operation(summary = "Editar campaña", description = "No se edita FINALIZADA ni CANCELADA.")
    @ApiResponse(responseCode = "200", description = "Campaña actualizada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Está cerrada")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PutMapping("/{id}")
    public CampanaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody CampanaRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Cambiar estado", description = "Manual entre PLANEADA, ACTIVA, FINALIZADA y CANCELADA (sirve para corregir errores).")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado inválido")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Ya está en ese estado")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PatchMapping("/{id}/estado")
    public CampanaResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoCampanaRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    @Operation(summary = "Destinatarios de la campaña", description = "Filtro opcional por resultado.")
    @ApiResponse(responseCode = "200", description = "Página de destinatarios")
    @ApiResponse(responseCode = "404", description = "No existe la campaña")
    @GetMapping("/{id}/destinatarios")
    public PaginaResponse<DestinatarioResponse> destinatarios(@PathVariable Integer id,
            @RequestParam(required = false) ResultadoCampana resultado,
            @ParameterObject @PageableDefault(sort = "cliente.nombre") Pageable pageable) {
        return service.destinatarios(id, resultado, pageable);
    }

    @Operation(summary = "Generar destinatarios del segmento", description = """
            Agrega los clientes del segmento que aún no están (idempotente). TODOS = clientes no INACTIVO;
            SIN_COMPRA_90_DIAS = ya compraron y su última compra fue hace más de 90 días. Solo PLANEADA o ACTIVA.""")
    @ApiResponse(responseCode = "200", description = "Cuántos se agregaron y el total")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "La campaña está cerrada")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @PostMapping("/{id}/destinatarios/generar")
    public GeneracionDestinatariosResponse generar(@PathVariable Integer id) {
        return service.generarDestinatarios(id);
    }

    @Operation(summary = "Agregar un destinatario a mano")
    @ApiResponse(responseCode = "201", description = "Destinatario agregado (ENVIADO)")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe la campaña o el cliente")
    @ApiResponse(responseCode = "409", description = "Ya es destinatario o la campaña está cerrada")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/destinatarios")
    public DestinatarioResponse agregar(@PathVariable Integer id, @Valid @RequestBody DestinatarioRequest request) {
        return service.agregarDestinatario(id, request.clienteId());
    }

    @Operation(summary = "Quitar un destinatario", description = "Solo mientras la campaña está PLANEADA.")
    @ApiResponse(responseCode = "204", description = "Destinatario quitado")
    @ApiResponse(responseCode = "404", description = "No existe la campaña o el destinatario")
    @ApiResponse(responseCode = "409", description = "La campaña ya no está PLANEADA")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @DeleteMapping("/{id}/destinatarios/{clienteId}")
    public ResponseEntity<Void> quitar(@PathVariable Integer id, @PathVariable Integer clienteId) {
        service.quitarDestinatario(id, clienteId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Registrar resultado de un destinatario", description = """
            Solo avanza ENVIADO → ABIERTO → RESPONDIO, con la campaña ACTIVA o FINALIZADA.
            Lo registra el vendedor del cliente, su gerente o ADMIN.""")
    @ApiResponse(responseCode = "200", description = "Resultado actualizado")
    @ApiResponse(responseCode = "400", description = "Resultado inválido")
    @ApiResponse(responseCode = "404", description = "No existe la campaña o el destinatario")
    @ApiResponse(responseCode = "409", description = "El resultado retrocede o la campaña no está ACTIVA/FINALIZADA")
    @PatchMapping("/{id}/destinatarios/{clienteId}/resultado")
    public DestinatarioResponse cambiarResultado(@PathVariable Integer id, @PathVariable Integer clienteId,
            @Valid @RequestBody CambioResultadoRequest request) {
        return service.cambiarResultado(id, clienteId, request.resultado());
    }

    @Operation(summary = "Métricas de la campaña", description = "Destinatarios, abiertos, respuestas, tasas y costo por respuesta.")
    @ApiResponse(responseCode = "200", description = "Métricas")
    @ApiResponse(responseCode = "404", description = "No existe")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @GetMapping("/{id}/metricas")
    public MetricasCampanaResponse metricas(@PathVariable Integer id) {
        return service.metricas(id);
    }
}
