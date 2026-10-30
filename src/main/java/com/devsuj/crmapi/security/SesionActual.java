package com.devsuj.crmapi.security;

import com.devsuj.crmapi.model.Rol;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** Da acceso al usuario autenticado sin que los services conozcan los detalles de Spring Security. */
@Component
public class SesionActual {

    public UsuarioActual obtener() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesión.");
        }
        Number id = jwt.getClaim(TokenService.CLAIM_ID);
        return new UsuarioActual(id.intValue(), jwt.getSubject(),
                Rol.valueOf(jwt.getClaimAsString(TokenService.CLAIM_ROL)));
    }
}
