package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Seed: 1 (cliente 1, Carla, CERRADO), 2 (cliente 3, Jorge, CERRADO), 3 (cliente 5, Sofía, CERRADO),
 * 4 (cliente 2, Carla, EN_PROCESO), 5 (cliente 8, Jorge, ABIERTO), 6 (cliente 10 de Sofía, sin asignar, ABIERTO).
 */
class TicketControllerTest extends PruebaIntegracion {

    @Test
    void visibilidadYFiltros() throws Exception {
        mvc.perform(como(CARLA, get("/api/tickets")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(como(SOFIA, get("/api/tickets")))
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(como(ADMIN, get("/api/tickets").param("sinAsignar", "true")))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(6));
        mvc.perform(como(ADMIN, get("/api/tickets").param("estado", "CERRADO")))
                .andExpect(jsonPath("$.totalElementos").value(3));
        mvc.perform(como(JORGE, get("/api/tickets/4")))
                .andExpect(status().isForbidden());
        mvc.perform(como(CARLA, get("/api/clientes/2/tickets")))
                .andExpect(jsonPath("$.contenido[0].id").value(4));
    }

    @Test
    void cicloDeVidaYSeguimiento() throws Exception {
        mvc.perform(json(SOFIA, patch("/api/tickets/6/estado"), "{\"estado\":\"EN_PROCESO\"}"))
                .andExpect(status().isConflict());
        mvc.perform(json(GERENTE, patch("/api/tickets/6/asignacion"), "{\"usuarioId\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioNombre").value("Sofía Herrera"));
        mvc.perform(json(SOFIA, patch("/api/tickets/6/estado"), "{\"estado\":\"EN_PROCESO\"}"))
                .andExpect(status().isOk());
        mvc.perform(json(GERENTE, patch("/api/tickets/6/asignacion"), "{\"usuarioId\":null}"))
                .andExpect(status().isConflict());
        mvc.perform(json(SOFIA, patch("/api/tickets/6/estado"), "{\"estado\":\"CERRADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cerradoEn").value("2026-10-20T10:00:00"));
        mvc.perform(json(SOFIA, patch("/api/tickets/6/estado"), "{\"estado\":\"ABIERTO\"}"))
                .andExpect(status().isConflict());
        mvc.perform(json(SOFIA, post("/api/tickets/6/seguimiento"), """
                        {"asunto":"Factura reenviada con error","prioridad":"MEDIA"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andExpect(jsonPath("$.ticketRelacionadoId").value(6))
                .andExpect(jsonPath("$.usuarioId").value(SOFIA));
        mvc.perform(json(JORGE, post("/api/tickets/5/seguimiento"), """
                        {"asunto":"X","prioridad":"BAJA"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void abrirYEditar() throws Exception {
        mvc.perform(json(CARLA, post("/api/tickets"), """
                        {"clienteId":1,"asunto":"Monitor sin señal","prioridad":"ALTA"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andExpect(jsonPath("$.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.creadoEn").value("2026-10-20T10:00:00"));
        mvc.perform(json(CARLA, post("/api/tickets"), """
                        {"clienteId":1,"asunto":"X","prioridad":"ALTA","usuarioId":4}"""))
                .andExpect(status().isForbidden());
        mvc.perform(json(CARLA, post("/api/tickets"), """
                        {"clienteId":3,"asunto":"X","prioridad":"ALTA"}"""))
                .andExpect(status().isForbidden());
        mvc.perform(json(CARLA, post("/api/tickets"), "{\"clienteId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.asunto").exists());
        mvc.perform(json(CARLA, put("/api/tickets/4"), """
                        {"asunto":"Activación de licencias (3 usuarios)","prioridad":"MEDIA"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prioridad").value("MEDIA"));
        mvc.perform(json(CARLA, put("/api/tickets/1"), """
                        {"asunto":"X","prioridad":"MEDIA"}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(CARLA, patch("/api/tickets/4/asignacion"), "{\"usuarioId\":3}"))
                .andExpect(status().isForbidden());
    }
}
