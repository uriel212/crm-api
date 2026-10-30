package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.auth.LoginRequest;
import com.devsuj.crmapi.dto.auth.LoginResponse;
import com.devsuj.crmapi.mapper.UsuarioMapper;
import com.devsuj.crmapi.model.Usuario;
import com.devsuj.crmapi.repository.UsuarioRepository;
import com.devsuj.crmapi.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final UsuarioMapper mapper;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (DisabledException ex) {
            throw new DisabledException("Tu usuario está desactivado. Contacta a un administrador.");
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Email o contraseña incorrectos.");
        }
        Usuario usuario = usuarioRepository.findByEmail(request.email()).orElseThrow();
        TokenService.TokenGenerado token = tokenService.generar(usuario);
        return new LoginResponse(token.valor(), "Bearer", token.expiraEn(), mapper.toResponse(usuario));
    }
}
