package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClienteControllerTest extends PruebaIntegracion {

    private static final String NUEVO = """
            {"nombre":"Lucía Pérez","empresa":"Café Antigua","email":"lucia@cafeantigua.test",
             "telefono":"55551234","direccion":"Antigua Guatemala"%s}""";

    @Test
    void unVendedorVeTodosLosClientes() throws Exception {
        mvc.perform(como(CARLA, get("/api/clientes").param("size", "50")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(15))
                .andExpect(jsonPath("$.tamano").value(50));
    }

    @Test
    void buscaPorTextoYEstado() throws Exception {
        mvc.perform(como(CARLA, get("/api/clientes").param("q", "notaría").param("estado", "PROSPECTO")))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].vendedorNombre").value("Sofía Herrera"));
    }

    @Test
    void ordenInvalidoResponde400() throws Exception {
        mvc.perform(como(CARLA, get("/api/clientes").param("sort", "noExiste")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void vendedorCreaClienteASuNombreComoProspecto() throws Exception {
        mvc.perform(json(CARLA, post("/api/clientes"), NUEVO.formatted("")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("PROSPECTO"))
                .andExpect(jsonPath("$.vendedorId").value(CARLA))
                .andExpect(jsonPath("$.creadoEn").exists());
    }

    @Test
    void vendedorNoRegistraClientesAOtroVendedor() throws Exception {
        mvc.perform(json(CARLA, post("/api/clientes"), NUEVO.formatted(",\"vendedorId\":4")))
                .andExpect(status().isForbidden());
    }

    @Test
    void emailDuplicadoResponde409() throws Exception {
        mvc.perform(json(CARLA, post("/api/clientes"), """
                        {"nombre":"Otro","email":"rgarza@gin.test"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("rgarza@gin.test")));
    }

    @Test
    void soloElVendedorAsignadoOSuGerenteModifican() throws Exception {
        String cambio = """
                {"nombre":"Roberto Garza M.","email":"rgarza@gin.test"}""";
        mvc.perform(json(JORGE, put("/api/clientes/1"), cambio)).andExpect(status().isForbidden());
        mvc.perform(json(CARLA, put("/api/clientes/1"), cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Roberto Garza M."))
                .andExpect(jsonPath("$.vendedorId").value(CARLA));
        mvc.perform(json(GERENTE, put("/api/clientes/1"), cambio)).andExpect(status().isOk());
    }

    @Test
    void vendedorNoReasignaConPut() throws Exception {
        mvc.perform(json(CARLA, put("/api/clientes/1"), """
                        {"nombre":"Roberto Garza","email":"rgarza@gin.test","vendedorId":4}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void gerenteReasignaIndividualYEnLote() throws Exception {
        mvc.perform(json(CARLA, patch("/api/clientes/1/vendedor"), "{\"vendedorId\":4}"))
                .andExpect(status().isForbidden());
        mvc.perform(json(GERENTE, patch("/api/clientes/1/vendedor"), "{\"vendedorId\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendedorNombre").value("Jorge Pérez"));
        mvc.perform(json(GERENTE, post("/api/clientes/reasignaciones"), """
                        {"clienteIds":[2,7],"vendedorId":5}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reasignados").value(2));
        mvc.perform(como(CARLA, get("/api/clientes/7"))).andExpect(jsonPath("$.vendedorId").value(SOFIA));
    }

    @Test
    void noSeAsignaAUnUsuarioInactivoNiAUnAdmin() throws Exception {
        mvc.perform(json(ADMIN, patch("/api/clientes/1/vendedor"), "{\"vendedorId\":6}"))
                .andExpect(status().isConflict());
        mvc.perform(json(ADMIN, patch("/api/clientes/1/vendedor"), "{\"vendedorId\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    void clienteConComprasNoVuelveAProspecto() throws Exception {
        mvc.perform(json(CARLA, patch("/api/clientes/1/estado"), "{\"estado\":\"PROSPECTO\"}"))
                .andExpect(status().isConflict());
        mvc.perform(json(CARLA, patch("/api/clientes/1/estado"), "{\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    void estadoInvalidoResponde400() throws Exception {
        mvc.perform(json(CARLA, patch("/api/clientes/1/estado"), "{\"estado\":\"BORRADO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clienteInexistenteResponde404() throws Exception {
        mvc.perform(como(CARLA, get("/api/clientes/999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un cliente con id 999."));
    }
}
