package com.devsuj.crmapi.support;

import com.devsuj.crmapi.repository.UsuarioRepository;
import com.devsuj.crmapi.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base de las pruebas de integración: app completa + MySQL en Testcontainers + MockMvc.
 * Cada prueba corre en una transacción que se revierte al final, así los datos del seed no cambian.
 * Usuarios del seed: 1 ADMIN (Ana), 2 GERENTE (Luis), 3/4/5 VENDEDOR (Carla, Jorge, Sofía), 6 VENDEDOR inactivo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MySqlTestcontainersConfig.class)
@Transactional
public abstract class PruebaIntegracion {

    protected static final int ADMIN = 1;
    protected static final int GERENTE = 2;
    protected static final int CARLA = 3;
    protected static final int JORGE = 4;
    protected static final int SOFIA = 5;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /** Agrega el token del usuario del seed indicado a la petición. */
    protected MockHttpServletRequestBuilder como(int usuarioId, MockHttpServletRequestBuilder peticion) {
        String token = tokenService.generar(usuarioRepository.findById(usuarioId).orElseThrow()).valor();
        return peticion.header("Authorization", "Bearer " + token);
    }

    protected MockHttpServletRequestBuilder json(int usuarioId, MockHttpServletRequestBuilder peticion, String cuerpo) {
        return como(usuarioId, peticion).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }
}
