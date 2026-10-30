package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.dto.auth.LoginRequest;
import com.devsuj.crmapi.dto.auth.LoginResponse;
import com.devsuj.crmapi.dto.usuario.UsuarioResponse;
import com.devsuj.crmapi.service.AuthService;
import com.devsuj.crmapi.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación", description = "Inicio de sesión con JWT y datos del usuario actual")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    @Operation(summary = "Iniciar sesión", description = "Valida email y contraseña y devuelve un token JWT.")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Credenciales correctas")
    @ApiResponse(responseCode = "400", description = "Faltan email o contraseña")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o usuario desactivado")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Usuario actual", description = "Datos del usuario dueño del token.")
    @ApiResponse(responseCode = "200", description = "Usuario del token")
    @GetMapping("/me")
    public UsuarioResponse me() {
        return usuarioService.obtenerActual();
    }
}
