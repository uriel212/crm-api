package com.devsuj.crmapi.security;

import com.devsuj.crmapi.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Genera el JWT (HS256) que el frontend envía en {@code Authorization: Bearer <token>}. */
@Service
@RequiredArgsConstructor
public class TokenService {

    static final String CLAIM_ID = "uid";
    static final String CLAIM_ROL = "rol";

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiracion-minutos:480}")
    private long expiracionMinutos;

    public TokenGenerado generar(Usuario usuario) {
        // Reloj real (no el Clock de negocio): el decoder valida la expiración con la hora del sistema.
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiracionMinutos, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("crm-api")
                .issuedAt(ahora)
                .expiresAt(expira)
                .subject(usuario.getEmail())
                .claim(CLAIM_ID, usuario.getId())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
        return new TokenGenerado(token, expira);
    }

    public record TokenGenerado(String valor, Instant expiraEn) {
    }
}
