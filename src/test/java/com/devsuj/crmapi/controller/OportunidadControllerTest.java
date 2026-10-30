package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OportunidadControllerTest extends PruebaIntegracion {

    @Test
    void historialEnOrdenCronologicoConDiasPorEtapa() throws Exception {
        mvc.perform(como(CARLA, get("/api/oportunidades/12/historial")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].etapaAnterior").doesNotExist())
                .andExpect(jsonPath("$[0].etapa").value("PROSPECTO"))
                .andExpect(jsonPath("$[0].diasEnEtapa").value(7))
                .andExpect(jsonPath("$[3].etapaAnterior").value("PROPUESTA"))
                .andExpect(jsonPath("$[3].etapa").value("CALIFICADA"))
                .andExpect(jsonPath("$[4].actual").value(true))
                .andExpect(jsonPath("$[4].fechaFin").doesNotExist())
                .andExpect(jsonPath("$[4].diasEnEtapa").value(11));
    }

    @Test
    void elCierreNoCuentaDias() throws Exception {
        mvc.perform(como(CARLA, get("/api/oportunidades/1/historial")))
                .andExpect(jsonPath("$[4].etapa").value("GANADA"))
                .andExpect(jsonPath("$[4].diasEnEtapa").doesNotExist())
                .andExpect(jsonPath("$[3].diasEnEtapa").value(8));
    }

    @Test
    void cadaCambioDeEtapaQuedaEnElHistorial() throws Exception {
        mvc.perform(json(CARLA, patch("/api/oportunidades/9/etapa"), """
                        {"etapa":"CALIFICADA","comentario":"Tiene presupuesto"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etapa").value("CALIFICADA"));
        mvc.perform(como(CARLA, get("/api/oportunidades/9/historial")))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].diasEnEtapa").value(55))
                .andExpect(jsonPath("$[1].comentario").value("Tiene presupuesto"))
                .andExpect(jsonPath("$[1].usuarioNombre").value("Carla Mendoza"));
    }

    @Test
    void ganadaNoSePoneAManoYLasCerradasNoSeReabren() throws Exception {
        mvc.perform(json(CARLA, patch("/api/oportunidades/12/etapa"), "{\"etapa\":\"GANADA\"}"))
                .andExpect(status().isConflict());
        mvc.perform(json(CARLA, patch("/api/oportunidades/6/etapa"), "{\"etapa\":\"PROSPECTO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("oportunidadAnteriorId")));
    }

    @Test
    void unaPerdidaSeRetomaConUnaOportunidadNuevaLigada() throws Exception {
        mvc.perform(json(CARLA, post("/api/oportunidades"), """
                        {"clienteId":7,"titulo":"Monitores, segundo intento","montoEstimado":45000,"oportunidadAnteriorId":6}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etapa").value("PROSPECTO"))
                .andExpect(jsonPath("$.oportunidadAnteriorId").value(6))
                .andExpect(jsonPath("$.usuarioId").value(CARLA));
        mvc.perform(json(JORGE, post("/api/oportunidades"), """
                        {"clienteId":4,"titulo":"Otro","montoEstimado":1,"oportunidadAnteriorId":4}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void noSeRegistranOportunidadesAClientesInactivosNiAjenos() throws Exception {
        mvc.perform(json(CARLA, post("/api/oportunidades"), """
                        {"clienteId":15,"titulo":"X","montoEstimado":1}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(CARLA, post("/api/oportunidades"), """
                        {"clienteId":3,"titulo":"X","montoEstimado":1}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void visibilidadPorRol() throws Exception {
        mvc.perform(como(JORGE, get("/api/oportunidades")))
                .andExpect(jsonPath("$.totalElementos").value(5));
        mvc.perform(como(JORGE, get("/api/oportunidades/1"))).andExpect(status().isForbidden());
        mvc.perform(como(JORGE, get("/api/oportunidades/1/historial"))).andExpect(status().isForbidden());
        mvc.perform(como(GERENTE, get("/api/oportunidades/1"))).andExpect(status().isOk());
        mvc.perform(como(ADMIN, get("/api/oportunidades").param("abiertas", "true")))
                .andExpect(jsonPath("$.totalElementos").value(9));
    }

    @Test
    void montoNegativoResponde400() throws Exception {
        mvc.perform(json(CARLA, post("/api/oportunidades"), """
                        {"clienteId":1,"titulo":"X","montoEstimado":-5}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.montoEstimado").exists());
    }
}
