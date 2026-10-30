package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Flujo de punta a punta: lead → oportunidad → venta con promociones → stock → puntos → meta → tablero → vista 360. */
class FlujoCompletoTest extends PruebaIntegracion {

    @Test
    void deLeadAClienteConVentaPuntosYMeta() throws Exception {
        String cliente = mvc.perform(json(CARLA, post("/api/clientes"), """
                        {"nombre":"Paula Reyes","empresa":"Café La Antigua","email":"preyes@lantigua.test"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROSPECTO"))
                .andReturn().getResponse().getContentAsString();
        int clienteId = JsonPath.read(cliente, "$.id");

        String oportunidad = mvc.perform(json(CARLA, post("/api/oportunidades"), """
                        {"clienteId":%d,"titulo":"Licencias para la cafetería","montoEstimado":15000}""".formatted(clienteId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int oportunidadId = JsonPath.read(oportunidad, "$.id");

        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":%d,"oportunidadId":%d,"detalles":[{"productoId":12,"cantidad":5}]}"""
                        .formatted(clienteId, oportunidadId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(12495.00))
                .andExpect(jsonPath("$.puntosGanados").value(124));

        mvc.perform(como(CARLA, get("/api/clientes/" + clienteId)))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
        mvc.perform(como(CARLA, get("/api/oportunidades/" + oportunidadId)))
                .andExpect(jsonPath("$.etapa").value("GANADA"));
        mvc.perform(como(CARLA, get("/api/productos/12")))
                .andExpect(jsonPath("$.stock").value(95));
        mvc.perform(como(CARLA, get("/api/clientes/" + clienteId + "/puntos")))
                .andExpect(jsonPath("$.saldo").value(124));
        mvc.perform(como(CARLA, get("/api/metas/4")))
                .andExpect(jsonPath("$.montoVendido").value(12495.00));
        mvc.perform(como(CARLA, get("/api/reportes/dashboard")))
                .andExpect(jsonPath("$.totalVendido").value(12495.00))
                .andExpect(jsonPath("$.oportunidadesGanadas").value(1));
        mvc.perform(como(CARLA, get("/api/clientes/" + clienteId + "/resumen")))
                .andExpect(jsonPath("$.numeroCompras").value(1))
                .andExpect(jsonPath("$.saldoPuntos").value(124))
                .andExpect(jsonPath("$.oportunidadesAbiertas").value(0));
    }
}
