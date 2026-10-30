package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuarioControllerTest extends PruebaIntegracion {

    @Test
    void vendedorNoPuedeListarUsuarios() throws Exception {
        mvc.perform(como(CARLA, get("/api/usuarios"))).andExpect(status().isForbidden());
    }

    @Test
    void gerenteListaUsuariosFiltrandoPorRol() throws Exception {
        mvc.perform(como(GERENTE, get("/api/usuarios").param("rol", "VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.contenido[0].passwordHash").doesNotExist());
    }

    @Test
    void adminCreaUsuarioYPuedeIniciarSesion() throws Exception {
        mvc.perform(json(ADMIN, post("/api/usuarios"), """
                        {"nombre":"Pedro Gómez","email":"pedro@crmventas.test","password":"ClaveSegura1",
                         "rol":"VENDEDOR","gerenteId":2}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/usuarios/")))
                .andExpect(jsonPath("$.gerenteNombre").value("Luis Ramírez"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("""
                                {"email":"pedro@crmventas.test","password":"ClaveSegura1"}"""))
                .andExpect(status().isOk());
    }

    @Test
    void emailDuplicadoResponde409() throws Exception {
        mvc.perform(json(ADMIN, post("/api/usuarios"), """
                        {"nombre":"Otra Ana","email":"ana.torres@crmventas.test","password":"ClaveSegura1","rol":"ADMIN"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void validacionesResponden400ConErroresPorCampo() throws Exception {
        mvc.perform(json(ADMIN, post("/api/usuarios"), """
                        {"nombre":"","email":"no-es-email","password":"corta"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.email").exists())
                .andExpect(jsonPath("$.errores.password").exists())
                .andExpect(jsonPath("$.errores.rol").exists());
    }

    @Test
    void gerenteSoloParaVendedores() throws Exception {
        mvc.perform(json(ADMIN, post("/api/usuarios"), """
                        {"nombre":"X","email":"x@crmventas.test","password":"ClaveSegura1","rol":"GERENTE","gerenteId":2}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void desactivarVendedorConClientesPideConfirmacionYLuegoLosPasaAlGerente() throws Exception {
        mvc.perform(json(ADMIN, patch("/api/usuarios/3/estado"), """
                        {"activo":false}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.clientesAsignados").value(5));

        mvc.perform(json(ADMIN, patch("/api/usuarios/3/estado"), """
                        {"activo":false,"reasignarClientes":true}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(como(ADMIN, get("/api/clientes/1")))
                .andExpect(jsonPath("$.vendedorId").value(GERENTE));
        mvc.perform(como(ADMIN, get("/api/usuarios/3")))
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void noSeDesactivaUnGerenteConEquipo() throws Exception {
        mvc.perform(json(ADMIN, patch("/api/usuarios/2/estado"), """
                        {"activo":false,"reasignarClientes":true}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.vendedoresEnEquipo").value(3));
    }

    @Test
    void cambiarPasswordPropiaExigeLaActual() throws Exception {
        mvc.perform(json(CARLA, patch("/api/usuarios/3/password"), """
                        {"passwordActual":"mala","passwordNueva":"NuevaClave2026"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(json(CARLA, patch("/api/usuarios/3/password"), """
                        {"passwordActual":"Password123","passwordNueva":"NuevaClave2026"}"""))
                .andExpect(status().isNoContent());
    }

    @Test
    void unVendedorNoCambiaLaPasswordDeOtro() throws Exception {
        mvc.perform(json(CARLA, patch("/api/usuarios/4/password"), """
                        {"passwordNueva":"NuevaClave2026"}"""))
                .andExpect(status().isForbidden());
    }
}
