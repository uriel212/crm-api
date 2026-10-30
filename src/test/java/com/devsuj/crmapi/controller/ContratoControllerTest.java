package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Seed: 1 (cliente 1, VIGENTE hasta 2028-02-29), 2 (cliente 2, VIGENTE hasta 2027-03-14),
 * 3 (cliente 9, VENCIDO), 4 (cliente 4 de Jorge, BORRADOR). Hoy = 2026-10-20.
 */
class ContratoControllerTest extends PruebaIntegracion {

    @Test
    void cadaQuienVeLosContratosDeSusClientes() throws Exception {
        mvc.perform(como(CARLA, get("/api/contratos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(como(JORGE, get("/api/contratos")))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(4))
                .andExpect(jsonPath("$.contenido[0].diasParaVencer").doesNotExist());
        mvc.perform(como(GERENTE, get("/api/contratos")))
                .andExpect(jsonPath("$.totalElementos").value(4));
        mvc.perform(como(CARLA, get("/api/contratos/2")))
                .andExpect(jsonPath("$.ventaId").value(6))
                .andExpect(jsonPath("$.diasParaVencer").value(145));
        mvc.perform(como(ADMIN, get("/api/contratos").param("porVencerDias", "150")))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(2));
        mvc.perform(como(CARLA, get("/api/contratos/4")))
                .andExpect(status().isForbidden());
        mvc.perform(como(ADMIN, get("/api/clientes/1/contratos")))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void borradorSeEditaYPasaAVigente() throws Exception {
        mvc.perform(json(JORGE, put("/api/contratos/4"), """
                        {"clienteId":4,"titulo":"Arrendamiento de laboratorio","fechaInicio":"2026-12-01",
                         "fechaFin":"2029-11-30","monto":210000}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto").value(210000));
        mvc.perform(json(JORGE, patch("/api/contratos/4/estado"), "{\"estado\":\"VIGENTE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VIGENTE"));
        mvc.perform(json(JORGE, patch("/api/contratos/4/estado"), "{\"estado\":\"VENCIDO\"}"))
                .andExpect(status().isConflict());
        mvc.perform(json(JORGE, put("/api/contratos/4"), """
                        {"clienteId":4,"titulo":"X","fechaInicio":"2026-12-01","fechaFin":"2029-11-30","monto":1}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(ADMIN, patch("/api/contratos/3/estado"), "{\"estado\":\"VIGENTE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void extensionYRenovacion() throws Exception {
        mvc.perform(json(CARLA, patch("/api/contratos/2/extension"), "{\"fechaFin\":\"2027-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaFin").value("2027-12-31"));
        mvc.perform(json(CARLA, patch("/api/contratos/2/extension"), "{\"fechaFin\":\"2027-01-01\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(json(CARLA, post("/api/contratos/2/renovacion"), """
                        {"fechaInicio":"2028-01-01","fechaFin":"2028-12-31","monto":36000}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.contratoAnteriorId").value(2))
                .andExpect(jsonPath("$.titulo").value("Licenciamiento anual de Microsoft 365"));
        mvc.perform(json(CARLA, post("/api/contratos/2/renovacion"), """
                        {"fechaInicio":"2028-01-01","fechaFin":"2028-12-31","monto":36000}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(JORGE, post("/api/contratos/4/renovacion"), """
                        {"fechaInicio":"2030-01-01","fechaFin":"2030-12-31","monto":1}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void crearYVencerEnLote() throws Exception {
        String location = mvc.perform(json(CARLA, post("/api/contratos"), """
                        {"clienteId":1,"ventaId":13,"titulo":"Garantía de diademas","fechaInicio":"2026-01-01",
                         "fechaFin":"2026-06-30","monto":5000}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andReturn().getResponse().getHeader("Location");
        String ruta = location.substring(location.indexOf("/api/contratos"));
        mvc.perform(json(CARLA, patch(ruta + "/estado"), "{\"estado\":\"VIGENTE\"}"))
                .andExpect(jsonPath("$.diasParaVencer").value(-112));
        mvc.perform(como(CARLA, post("/api/contratos/vencimientos")))
                .andExpect(status().isForbidden());
        mvc.perform(como(GERENTE, post("/api/contratos/vencimientos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vencidos").value(1));
        mvc.perform(como(CARLA, get(ruta)))
                .andExpect(jsonPath("$.estado").value("VENCIDO"));
    }

    @Test
    void validacionesYReglasAlCrear() throws Exception {
        mvc.perform(json(CARLA, post("/api/contratos"), """
                        {"clienteId":1,"ventaId":1,"titulo":"X","fechaInicio":"2026-01-01","fechaFin":"2026-12-31","monto":1}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(CARLA, post("/api/contratos"), """
                        {"clienteId":15,"titulo":"X","fechaInicio":"2026-01-01","fechaFin":"2026-12-31","monto":1}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(JORGE, post("/api/contratos"), """
                        {"clienteId":1,"titulo":"X","fechaInicio":"2026-01-01","fechaFin":"2026-12-31","monto":1}"""))
                .andExpect(status().isForbidden());
        mvc.perform(json(CARLA, post("/api/contratos"), """
                        {"clienteId":1,"titulo":"","fechaInicio":"2026-12-31","fechaFin":"2026-01-01","monto":-1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.titulo").exists())
                .andExpect(jsonPath("$.errores.fechaFin").exists())
                .andExpect(jsonPath("$.errores.monto").exists());
    }
}
