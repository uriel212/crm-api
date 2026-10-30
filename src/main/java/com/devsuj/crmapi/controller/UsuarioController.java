package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.usuario.CambioEstadoUsuarioRequest;
import com.devsuj.crmapi.dto.usuario.CambioPasswordRequest;
import com.devsuj.crmapi.dto.usuario.UsuarioActualizarRequest;
import com.devsuj.crmapi.dto.usuario.UsuarioCrearRequest;
import com.devsuj.crmapi.dto.usuario.UsuarioResponse;
import com.devsuj.crmapi.model.Rol;
import com.devsuj.crmapi.security.Permisos;
import com.devsuj.crmapi.service.UsuarioService;
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

@Tag(name = "Usuarios", description = "Vendedores, gerentes y administradores. Sin borrado: se desactivan.")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @Operation(summary = "Listar usuarios", description = "Lista paginada con filtros opcionales. Rol: GERENTE, ADMIN.")
    @ApiResponse(responseCode = "200", description = "Página de usuarios")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @GetMapping
    public PaginaResponse<UsuarioResponse> listar(
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Texto en nombre o email") @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(sort = "nombre") Pageable pageable) {
        return service.listar(rol, activo, q, pageable);
    }

    @Operation(summary = "Detalle de un usuario", description = "Rol: GERENTE, ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "No existe el usuario")
    @PreAuthorize(Permisos.GERENTE_O_ADMIN)
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear usuario", description = "La contraseña se guarda con BCrypt. Rol: ADMIN.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "Email duplicado o gerente inválido")
    @PreAuthorize(Permisos.SOLO_ADMIN)
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        UsuarioResponse creado = service.crear(request);
        return Respuestas.creado("/api/usuarios/{id}", creado.id(), creado);
    }

    @Operation(summary = "Actualizar usuario", description = "Nombre, email, rol y gerente. Rol: ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe el usuario")
    @ApiResponse(responseCode = "409", description = "Email duplicado o el gerente aún tiene equipo")
    @PreAuthorize(Permisos.SOLO_ADMIN)
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioActualizarRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Activar o desactivar",
            description = """
                    Si el usuario tiene clientes asignados responde 409 con 'clientesAsignados'; repite la petición con
                    reasignarClientes = true para que sus clientes pasen a su gerente. Rol: ADMIN.""")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "No existe el usuario")
    @ApiResponse(responseCode = "409", description = "Tiene clientes sin confirmar, tiene equipo o es el propio usuario")
    @PreAuthorize(Permisos.SOLO_ADMIN)
    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoUsuarioRequest request) {
        return service.cambiarEstado(id, request);
    }

    @Operation(summary = "Cambiar contraseña",
            description = "ADMIN cambia cualquiera; cada usuario la suya indicando passwordActual.")
    @ApiResponse(responseCode = "204", description = "Contraseña cambiada")
    @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o nueva inválida")
    @ApiResponse(responseCode = "404", description = "No existe el usuario")
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Integer id, @Valid @RequestBody CambioPasswordRequest request) {
        service.cambiarPassword(id, request);
        return ResponseEntity.noContent().build();
    }
}
