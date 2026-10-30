package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Seed: 1 Bienvenida (PROSPECTOS, ACTIVA, clientes 11–14), 2 Regreso a clases (TODOS, FINALIZADA, 9 destinatarios),
 * 3 Buen Fin (ACTIVOS, PLANEADA, sin destinatarios), 4 Reactivación (SIN_COMPRA_90_DIAS, ACTIVA, clientes 9 y 15).
 */
class CampanaControllerTest extends PruebaIntegracion {

    private static final String NUEVA = """
            {"nombre":"Fin de año","canal":"CORREO","asunto":"Cierra el año","mensaje":"Ofertas","segmento":"TODOS",
             "fechaInicio":"2026-12-01","fechaFin":"2026-12-31","presupuesto":3000}""";

    @Test
    void listadoDetalleYMetricas() throws Exception {
        mvc.perform(como(CARLA, get("/api/campanas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.contenido[0].id").value(3))
                .andExpect(jsonPath("$.contenido[2].id").value(2))
                .andExpect(jsonPath("$.contenido[2].totalDestinatarios").value(9));
        mvc.perform(como(GERENTE, get("/api/campanas/2/metricas")))
                .andExpect(jsonPath("$.destinatarios").value(9))
                .andExpect(jsonPath("$.abiertos").value(6))
                .andExpect(jsonPath("$.respondieron").value(3))
                .andExpect(jsonPath("$.tasaApertura").value(66.67))
                .andExpect(jsonPath("$.tasaRespuesta").value(33.33))
                .andExpect(jsonPath("$.costoPorRespuesta").value(1666.67));
        mvc.perform(como(CARLA, get("/api/campanas/2/metricas")))
                .andExpect(status().isForbidden());
        mvc.perform(como(CARLA, get("/api/campanas/2/destinatarios").param("resultado", "RESPONDIO")))
                .andExpect(jsonPath("$.totalElementos").value(3));
        mvc.perform(como(CARLA, get("/api/campanas/99")))
                .andExpect(status().isNotFound());
    }

    @Test
    void generarDestinatariosDelSegmentoEsIdempotente() throws Exception {
        mvc.perform(como(GERENTE, post("/api/campanas/4/destinatarios/generar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agregados").value(1))
                .andExpect(jsonPath("$.totalDestinatarios").value(3));
        mvc.perform(como(GERENTE, post("/api/campanas/4/destinatarios/generar")))
                .andExpect(jsonPath("$.agregados").value(0));
        mvc.perform(como(GERENTE, get("/api/campanas/4/destinatarios")))
                .andExpect(jsonPath("$.contenido[*].clienteId", hasItem(8)));
        mvc.perform(como(GERENTE, post("/api/campanas/1/destinatarios/generar")))
                .andExpect(jsonPath("$.agregados").value(0))
                .andExpect(jsonPath("$.totalDestinatarios").value(4));
        mvc.perform(como(GERENTE, post("/api/campanas/2/destinatarios/generar")))
                .andExpect(status().isConflict());
    }

    @Test
    void cicloDeVidaDeUnaCampanaNueva() throws Exception {
        String location = mvc.perform(json(GERENTE, post("/api/campanas"), NUEVA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PLANEADA"))
                .andReturn().getResponse().getHeader("Location");
        String ruta = location.substring(location.indexOf("/api/campanas"));
        mvc.perform(como(GERENTE, post(ruta + "/destinatarios/generar")))
                .andExpect(jsonPath("$.agregados").value(13));
        mvc.perform(como(GERENTE, delete(ruta + "/destinatarios/1")))
                .andExpect(status().isNoContent());
        mvc.perform(json(GERENTE, post(ruta + "/destinatarios"), "{\"clienteId\":2}"))
                .andExpect(status().isConflict());
        mvc.perform(json(GERENTE, post(ruta + "/destinatarios"), "{\"clienteId\":15}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultado").value("ENVIADO"));
        mvc.perform(json(GERENTE, patch(ruta + "/estado"), "{\"estado\":\"ACTIVA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDestinatarios").value(13));
        mvc.perform(json(GERENTE, patch(ruta + "/estado"), "{\"estado\":\"ACTIVA\"}"))
                .andExpect(status().isConflict());
        mvc.perform(como(GERENTE, delete(ruta + "/destinatarios/2")))
                .andExpect(status().isConflict());
        mvc.perform(json(GERENTE, patch(ruta + "/estado"), "{\"estado\":\"CANCELADA\"}"))
                .andExpect(status().isOk());
        mvc.perform(json(GERENTE, put(ruta), NUEVA))
                .andExpect(status().isConflict());
    }

    @Test
    void elResultadoSoloAvanzaYLoRegistraQuienGestionaAlCliente() throws Exception {
        mvc.perform(json(SOFIA, patch("/api/campanas/1/destinatarios/14/resultado"), "{\"resultado\":\"RESPONDIO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("RESPONDIO"));
        mvc.perform(json(SOFIA, patch("/api/campanas/1/destinatarios/14/resultado"), "{\"resultado\":\"ABIERTO\"}"))
                .andExpect(status().isConflict());
        mvc.perform(json(CARLA, patch("/api/campanas/1/destinatarios/13/resultado"), "{\"resultado\":\"RESPONDIO\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(json(SOFIA, patch("/api/campanas/1/destinatarios/1/resultado"), "{\"resultado\":\"ABIERTO\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(json(GERENTE, post("/api/campanas/3/destinatarios"), "{\"clienteId\":1}"))
                .andExpect(status().isCreated());
        mvc.perform(json(CARLA, patch("/api/campanas/3/destinatarios/1/resultado"), "{\"resultado\":\"ABIERTO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void validacionesYPermisos() throws Exception {
        mvc.perform(json(CARLA, post("/api/campanas"), NUEVA))
                .andExpect(status().isForbidden());
        mvc.perform(json(GERENTE, post("/api/campanas"), NUEVA.replace("2026-12-31", "2026-11-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaFin").exists());
        mvc.perform(json(GERENTE, post("/api/campanas"), "{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.segmento").exists());
        mvc.perform(json(GERENTE, put("/api/campanas/2"), NUEVA))
                .andExpect(status().isConflict());
    }
}
