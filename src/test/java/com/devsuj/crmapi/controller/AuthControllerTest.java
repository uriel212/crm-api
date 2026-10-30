package com.devsuj.crmapi.controller;

import org.springframework.http.MediaType;
import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends PruebaIntegracion {

    private static final String LOGIN = "/api/auth/login";

    @Test
    void loginCorrectoDevuelveTokenYUsuario() throws Exception {
        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana.torres@crmventas.test","password":"Password123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"))
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist());
    }

    @Test
    void passwordIncorrectaResponde401() throws Exception {
        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana.torres@crmventas.test","password":"otra"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email o contraseña incorrectos."));
    }

    @Test
    void usuarioInactivoNoPuedeEntrar() throws Exception {
        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"mario.ruiz@crmventas.test","password":"Password123"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", containsString("desactivado")));
    }

    @Test
    void sinTokenResponde401ConProblemDetail() throws Exception {
        mvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail", containsString("iniciar sesión")));
    }

    @Test
    void tokenInvalidoResponde401() throws Exception {
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meDevuelveElUsuarioDelToken() throws Exception {
        mvc.perform(como(CARLA, get("/api/auth/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CARLA))
                .andExpect(jsonPath("$.gerenteId").value(GERENTE));
    }
}
