package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Sugerencias del frontend: errores por campo en email duplicado y contraseña incorrecta; vendedor del cliente. */
class ErroresYCamposExtraTest extends PruebaIntegracion {

    @Test
    void emailDuplicadoTraeErrorPorCampo() throws Exception {
        mvc.perform(json(CARLA, post("/api/clientes"), """
                        {"nombre":"Otro","email":"rgarza@gin.test"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.email", containsString("rgarza@gin.test")));
        mvc.perform(json(ADMIN, post("/api/usuarios"), """
                        {"nombre":"Otra","email":"carla.mendoza@crmventas.test","password":"Password123",
                         "rol":"VENDEDOR","gerenteId":2}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.email").exists());
    }

    @Test
    void contrasenaActualIncorrectaTraeErrorPorCampo() throws Exception {
        mvc.perform(json(CARLA, patch("/api/usuarios/3/password"), """
                        {"passwordActual":"incorrecta","passwordNueva":"OtraClave123"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.passwordActual").value("La contraseña actual no es correcta."));
    }

    @Test
    void contratosYTicketsTraenElVendedorDelCliente() throws Exception {
        mvc.perform(como(CARLA, get("/api/contratos/2")))
                .andExpect(jsonPath("$.clienteVendedorId").value(CARLA));
        mvc.perform(como(SOFIA, get("/api/tickets/6")))
                .andExpect(jsonPath("$.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.clienteVendedorId").value(SOFIA));
        mvc.perform(como(ADMIN, get("/api/tickets").param("clienteId", "8")))
                .andExpect(jsonPath("$.contenido[0].clienteVendedorId").value(JORGE));
    }
}
