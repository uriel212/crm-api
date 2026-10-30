package com.devsuj.crmapi.dto.auth;

import com.devsuj.crmapi.dto.usuario.UsuarioResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Token de acceso. Se envía en cada petición como 'Authorization: Bearer <token>'.")
public record LoginResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(example = "Bearer") String tipo,
        @Schema(description = "Momento en que expira el token (UTC)") Instant expiraEn,
        UsuarioResponse usuario) {
}
